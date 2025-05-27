package me.dahiorus.project.vending.infrastructure.security.jwt;

import static com.nimbusds.jose.JWSAlgorithm.HS256;
import static java.time.Instant.now;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import me.dahiorus.project.vending.infrastructure.rest.exception.InvalidTokenCreation;
import me.dahiorus.project.vending.infrastructure.rest.exception.UnparsableToken;
import me.dahiorus.project.vending.infrastructure.security.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class JwtService implements TokenService {
  private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
  private static final JWSAlgorithm ALGO = HS256;

  private final JwtProperties jwtProperties;

  public JwtService(final JwtProperties jwtProperties) {
    this.jwtProperties = jwtProperties;
  }

  @Override
  public String createAccessToken(
      final String username, final Collection<? extends GrantedAuthority> authorities)
      throws InvalidTokenCreation {
    var now = now();
    var jwtClaims =
        new JWTClaimsSet.Builder()
            .subject(username)
            .issueTime(Date.from(now))
            .issuer(jwtProperties.getIssuerUri())
            .claim(
                "roles",
                authorities.stream().map(GrantedAuthority::getAuthority).toArray(String[]::new))
            .expirationTime(Date.from(now.plus(jwtProperties.getAccessTokenDuration())))
            .build();

    logger.debug("Creating a JWT access token for {}", username);

    return createToken(jwtClaims);
  }

  @Override
  public String createRefreshToken(final String username) throws InvalidTokenCreation {
    var now = now();
    var jwtClaims =
        new JWTClaimsSet.Builder()
            .subject(username)
            .issueTime(Date.from(now))
            .expirationTime(Date.from(now.plus(jwtProperties.getRefreshTokenDuration())))
            .build();

    logger.debug("Creating a JWT refresh token for {}", username);

    return createToken(jwtClaims);
  }

  private String createToken(final JWTClaimsSet claims) throws InvalidTokenCreation {
    var payload = new Payload(claims.toJSONObject());
    var jwsObject = new JWSObject(new JWSHeader(ALGO), payload);

    try {
      jwsObject.sign(new MACSigner(jwtProperties.getSecret()));
    } catch (JOSEException e) {
      throw new InvalidTokenCreation(e.getMessage(), e);
    }

    return jwsObject.serialize();
  }

  @Override
  public Authentication parseToken(final String token) throws UnparsableToken {
    try {
      logger.debug("Parsing a JWT token");

      // parse the string to a JWT object
      var secret = jwtProperties.getSecret();
      var signedJwt = SignedJWT.parse(token);
      signedJwt.verify(new MACVerifier(secret.getBytes()));

      // process the JWT to get the claims, verify the signature
      var securityContextJwtProcessor = new DefaultJWTProcessor<>();
      var securityContextJwsKeySelector =
          new JWSVerificationKeySelector<>(ALGO, new ImmutableSecret<>(secret.getBytes()));
      securityContextJwtProcessor.setJWSKeySelector(securityContextJwsKeySelector);
      securityContextJwtProcessor.process(signedJwt, null);

      var jwtClaims = signedJwt.getJWTClaimsSet();
      var username = jwtClaims.getSubject();
      var roles = jwtClaims.getStringListClaim("roles");
      var grantedAuthorities =
          roles == null
              ? List.<GrantedAuthority>of()
              : roles.stream().map(SimpleGrantedAuthority::new).toList();

      logger.debug("Token parsed for username '{}'", username);

      return new UsernamePasswordAuthenticationToken(username, null, grantedAuthorities);
    } catch (BadJOSEException e) {
      throw new BadCredentialsException(e.getMessage(), e);
    } catch (JOSEException | ParseException e) {
      throw new UnparsableToken(e.getMessage(), e);
    }
  }
}
