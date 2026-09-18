package me.dahiorus.project.vending.infrastructure.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

@ExtendWith(MockitoExtension.class)
class JwtTokenIssuerTest {

  @Mock JwtEncoder jwtEncoder;

  @InjectMocks JwtTokenIssuer jwtTokenIssuer;

  @Test
  void should_issue_refresh_token_with_jti_and_expires_at_metadata() {
    var jwtProperties = new JwtProperties();
    jwtProperties.setIssuerUri("https://issuer.test");
    jwtProperties.setAccessTokenDuration(Duration.ofHours(1));
    jwtProperties.setRefreshTokenDuration(Period.ofDays(30));
    jwtTokenIssuer = new JwtTokenIssuer(jwtEncoder, jwtProperties);
    when(jwtEncoder.encode(any()))
        .thenReturn(
            new Jwt(
                "refresh.jwt",
                Instant.EPOCH,
                Instant.EPOCH.plusSeconds(1),
                java.util.Map.of("alg", "RS256"),
                java.util.Map.of("sub", "user@test.org")));

    var issuedRefreshToken = jwtTokenIssuer.createRefreshToken("user@test.org");

    var parametersCaptor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
    verify(jwtEncoder).encode(parametersCaptor.capture());
    JwtClaimsSet claims = parametersCaptor.getValue().getClaims();

    assertThat(issuedRefreshToken.value()).isEqualTo("refresh.jwt");
    assertThat(issuedRefreshToken.jti()).isEqualTo(claims.getId());
    assertThat(issuedRefreshToken.jti()).isNotBlank();
    assertThat(issuedRefreshToken.expiresAt()).isEqualTo(claims.getExpiresAt());
    assertThat(claims.getSubject()).isEqualTo("user@test.org");
    assertThat(claims.getIssuer().toString()).isEqualTo("https://issuer.test");
    assertThat(claims.getClaimAsString(JwtTokenIssuer.TOKEN_TYPE_CLAIM))
        .isEqualTo(JwtTokenIssuer.REFRESH_TOKEN_TYPE);
  }

  @Test
  void should_issue_access_token_without_jti_claim() {
    var jwtProperties = new JwtProperties();
    jwtProperties.setIssuerUri("https://issuer.test");
    jwtProperties.setAccessTokenDuration(Duration.ofHours(1));
    jwtProperties.setRefreshTokenDuration(Period.ofDays(30));
    jwtTokenIssuer = new JwtTokenIssuer(jwtEncoder, jwtProperties);
    when(jwtEncoder.encode(any()))
        .thenReturn(
            new Jwt(
                "access.jwt",
                Instant.EPOCH,
                Instant.EPOCH.plusSeconds(1),
                java.util.Map.of("alg", "RS256"),
                java.util.Map.of("sub", "user@test.org")));

    var accessToken =
        jwtTokenIssuer.createAccessToken(
            "user@test.org", List.of(new SimpleGrantedAuthority("ROLE_USER")));

    var parametersCaptor = ArgumentCaptor.forClass(JwtEncoderParameters.class);
    verify(jwtEncoder).encode(parametersCaptor.capture());
    JwtClaimsSet claims = parametersCaptor.getValue().getClaims();

    assertThat(accessToken).isEqualTo("access.jwt");
    assertThat(claims.getId()).isNull();
    assertThat(claims.getClaimAsString(JwtTokenIssuer.TOKEN_TYPE_CLAIM))
        .isEqualTo(JwtTokenIssuer.ACCESS_TOKEN_TYPE);
    assertThat(claims.getClaimAsStringList("roles")).containsExactly("ROLE_USER");
  }
}
