package me.dahiorus.project.vending.infrastructure.security.jwt;

import static java.time.Instant.now;

import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

/** Issues the access and refresh JWTs signed with the application's RSA key pair. */
@Component
public class JwtTokenIssuer {

  public static final String TOKEN_TYPE_CLAIM = "token_type";
  public static final String ACCESS_TOKEN_TYPE = "access";
  public static final String REFRESH_TOKEN_TYPE = "refresh";

  private static final String ROLES_CLAIM = "roles";

  private static final Logger logger = LoggerFactory.getLogger(JwtTokenIssuer.class);

  private final JwtEncoder jwtEncoder;
  private final JwtProperties jwtProperties;

  public JwtTokenIssuer(final JwtEncoder jwtEncoder, final JwtProperties jwtProperties) {
    this.jwtEncoder = jwtEncoder;
    this.jwtProperties = jwtProperties;
  }

  public String createAccessToken(
      final String username, final Collection<? extends GrantedAuthority> authorities) {
    var now = now();
    var claims =
        JwtClaimsSet.builder()
            .subject(username)
            .issuer(jwtProperties.getIssuerUri())
            .issuedAt(now)
            .expiresAt(now.plus(jwtProperties.getAccessTokenDuration()))
            .claim(TOKEN_TYPE_CLAIM, ACCESS_TOKEN_TYPE)
            .claim(ROLES_CLAIM, authorities.stream().map(GrantedAuthority::getAuthority).toList())
            .build();

    logger.debug("Creating a JWT access token for {}", username);

    return encode(claims);
  }

  public String createRefreshToken(final String username) {
    var now = now();
    var claims =
        JwtClaimsSet.builder()
            .subject(username)
            .issuer(jwtProperties.getIssuerUri())
            .issuedAt(now)
            .expiresAt(now.plus(jwtProperties.getRefreshTokenDuration()))
            .claim(TOKEN_TYPE_CLAIM, REFRESH_TOKEN_TYPE)
            .build();

    logger.debug("Creating a JWT refresh token for {}", username);

    return encode(claims);
  }

  private String encode(final JwtClaimsSet claims) {
    return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
  }
}
