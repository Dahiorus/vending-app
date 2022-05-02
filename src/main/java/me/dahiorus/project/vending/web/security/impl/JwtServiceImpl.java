package me.dahiorus.project.vending.web.security.impl;

import java.text.ParseException;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.core.exception.AppRuntimeException;
import me.dahiorus.project.vending.web.config.JwtProperties;
import me.dahiorus.project.vending.web.security.JwtService;

@Service
@RequiredArgsConstructor
@Log4j2
public class JwtServiceImpl implements JwtService
{
  private static final JWSAlgorithm ALGO = JWSAlgorithm.HS256;

  private final JwtProperties jwtProperties;

  @Override
  public String createAccessToken(final String username, final Collection<? extends GrantedAuthority> authorities)
  {
    Instant now = Instant.now();
    JWTClaimsSet claims = new JWTClaimsSet.Builder()
      .subject(username)
      .issueTime(Date.from(now))
      .issuer(jwtProperties.getIssuerUri())
      .claim("roles", authorities
        .stream()
        .map(GrantedAuthority::getAuthority)
        .toArray(String[]::new))
      .expirationTime(Date.from(now.plus(jwtProperties.getAccessTokenDuration())))
      .build();

    log.debug("Creating a JWT access token for {}", username);

    return createToken(claims);
  }

  @Override
  public String createRefreshToken(final String username)
  {
    Instant now = Instant.now();
    JWTClaimsSet claims = new JWTClaimsSet.Builder()
      .subject(username)
      .issueTime(Date.from(now))
      .expirationTime(Date.from(now.plus(jwtProperties.getRefreshTokenDuration())))
      .build();

    log.debug("Creating a JWT refresh token for {}", username);

    return createToken(claims);
  }

  private String createToken(final JWTClaimsSet claims)
  {
    Payload payload = new Payload(claims.toJSONObject());
    JWSObject jwsObject = new JWSObject(new JWSHeader(ALGO), payload);

    try
    {
      jwsObject.sign(new MACSigner(jwtProperties.getSecret()));
    }
    catch (JOSEException e)
    {
      throw new AppRuntimeException(e.getMessage(), e);
    }

    return jwsObject.serialize();
  }

  @Override
  public Authentication parseToken(final String token)
  {
    try
    {
      log.debug("Parsing a JWT token");

      // parse the string to a JWT object
      String secret = jwtProperties.getSecret();
      SignedJWT signedJwt = SignedJWT.parse(token);
      signedJwt.verify(new MACVerifier(secret.getBytes()));

      // process the JWT to get the claims, verify the signature
      ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
      JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(ALGO,
          new ImmutableSecret<>(secret.getBytes()));
      jwtProcessor.setJWSKeySelector(keySelector);
      jwtProcessor.process(signedJwt, null);

      JWTClaimsSet claims = signedJwt.getJWTClaimsSet();
      String username = claims.getSubject();
      List<String> roles = claims.getStringListClaim("roles");

      List<SimpleGrantedAuthority> authorities = roles == null ? Collections.emptyList()
          : roles.stream()
            .map(SimpleGrantedAuthority::new)
            .toList();

      log.debug("Token parsed for username '{}'", username);

      return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }
    catch (BadJOSEException | JOSEException | ParseException e)
    {
      throw new AppRuntimeException(e.getMessage(), e);
    }
  }
}
