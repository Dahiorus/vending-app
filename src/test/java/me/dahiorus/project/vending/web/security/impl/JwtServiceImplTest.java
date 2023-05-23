package me.dahiorus.project.vending.web.security.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.Base64;
import java.util.Collection;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;

import me.dahiorus.project.vending.web.config.JwtProperties;
import me.dahiorus.project.vending.web.exception.InvalidTokenCreation;
import me.dahiorus.project.vending.web.exception.UnparsableToken;

@ExtendWith(MockitoExtension.class)
class JwtServiceImplTest
{
  JwtServiceImpl jwtService;

  JwtProperties jwtProperties;

  @BeforeEach
  void setUp()
  {
    byte[] secretBytes = new byte[32];
    new SecureRandom().nextBytes(secretBytes);

    jwtProperties = new JwtProperties();
    jwtProperties.setIssuerUri("http://test.com/");
    jwtProperties.setSecret(Base64.getEncoder()
      .encodeToString(secretBytes));
    jwtProperties.setAccessTokenDuration(Duration.ofMinutes(3));
    jwtProperties.setRefreshTokenDuration(Period.ofDays(1));

    jwtService = new JwtServiceImpl(jwtProperties);
  }

  @Nested
  class CreateAccessTokenTests
  {
    String username;

    Collection<? extends GrantedAuthority> grantedAuthorities;

    @BeforeEach
    void setUp()
    {
      username = "user.test";
      grantedAuthorities = Stream.of("ROLE_USER")
        .map(SimpleGrantedAuthority::new)
        .toList();
    }

    @Test
    void createToken() throws Exception
    {
      String accessToken = jwtService.createAccessToken(username, grantedAuthorities);
      JWT parsedJwt = JWTParser.parse(accessToken);

      JWTClaimsSet jwtClaimsSet = parsedJwt.getJWTClaimsSet();
      assertAll(() -> assertThat(jwtClaimsSet.getSubject()).isEqualTo(username),
        () -> assertThat(jwtClaimsSet.getStringListClaim("roles")).containsExactly("ROLE_USER"),
        () -> assertThat(jwtClaimsSet.getIssuer()).isEqualTo(jwtProperties.getIssuerUri()),
        () -> assertThat(jwtClaimsSet.getExpirationTime()).isBeforeOrEqualTo(Instant.now()
          .plus(jwtProperties.getAccessTokenDuration())));
    }

    @Test
    void createWithInvalidSecret()
    {
      jwtProperties.setSecret("secret");

      assertThatExceptionOfType(InvalidTokenCreation.class)
        .isThrownBy(() -> jwtService.createAccessToken(username, grantedAuthorities));
    }
  }

  @Nested
  class CreateRefreshTokenTests
  {
    String username;

    @BeforeEach
    void setUp()
    {
      username = "user.test";
    }

    @Test
    void createToken() throws Exception
    {
      String refreshToken = jwtService.createRefreshToken(username);
      JWT parsedJwt = JWTParser.parse(refreshToken);

      JWTClaimsSet jwtClaimsSet = parsedJwt.getJWTClaimsSet();
      assertAll(() -> assertThat(jwtClaimsSet.getSubject()).isEqualTo(username),
        () -> assertThat(jwtClaimsSet.getExpirationTime()).isBeforeOrEqualTo(Instant.now()
          .plus(jwtProperties.getRefreshTokenDuration())));
    }

    @Test
    void createWithInvalidSecret()
    {
      jwtProperties.setSecret("secret");

      assertThatExceptionOfType(InvalidTokenCreation.class)
        .isThrownBy(() -> jwtService.createRefreshToken(username));
    }
  }

  @Nested
  class ParseTokenTests
  {
    String username;

    Collection<? extends GrantedAuthority> grantedAuthorities;

    @BeforeEach
    void setUp()
    {
      username = "user.test";
      grantedAuthorities = Stream.of("ROLE_USER")
        .map(SimpleGrantedAuthority::new)
        .toList();
    }

    @Test
    void parseAccessToken() throws Exception
    {
      String accessToken = jwtService.createAccessToken(username, grantedAuthorities);

      Authentication parsedToken = jwtService.parseToken(accessToken);

      assertAll(() -> assertThat(parsedToken.getPrincipal()).isEqualTo(username),
        () -> assertThat(parsedToken.getAuthorities()).map(GrantedAuthority::getAuthority)
          .containsExactly("ROLE_USER"));
    }

    @Test
    void parseRefreshToken() throws Exception
    {
      String refreshToken = jwtService.createRefreshToken(username);

      Authentication parsedToken = jwtService.parseToken(refreshToken);

      assertAll(() -> assertThat(parsedToken.getPrincipal()).isEqualTo(username),
        () -> assertThat(parsedToken.getAuthorities()).isEmpty());
    }

    @Test
    void parseExpiredToken() throws Exception
    {
      jwtProperties.setAccessTokenDuration(Duration.ofMinutes(-5));
      String accessToken = jwtService.createAccessToken(username, grantedAuthorities);

      assertThatExceptionOfType(BadCredentialsException.class)
        .isThrownBy(() -> jwtService.parseToken(accessToken));
    }

    @Test
    void unparsableToken() throws Exception
    {
      assertThatExceptionOfType(UnparsableToken.class)
        .isThrownBy(() -> jwtService.parseToken("not a token"));
    }
  }
}
