package me.dahiorus.project.vending.infrastructure.security;

import static org.hamcrest.Matchers.not;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.oauth2.jose.jws.SignatureAlgorithm.RS256;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import me.dahiorus.project.vending.domain.user.entity.AdminUserToCreate;
import me.dahiorus.project.vending.domain.user.entity.AppUserToCreate;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.Firstname;
import me.dahiorus.project.vending.domain.user.entity.Lastname;
import me.dahiorus.project.vending.domain.user.entity.Password;
import me.dahiorus.project.vending.domain.user.port.AdminUserRepositoryPort;
import me.dahiorus.project.vending.domain.user.port.AppUserRepositoryPort;
import me.dahiorus.project.vending.infrastructure.rest.entity.user.AuthenticateRequestDto;
import me.dahiorus.project.vending.infrastructure.rest.entity.user.RefreshTokenRequestDto;
import me.dahiorus.project.vending.infrastructure.security.jwt.JwtTokenIssuer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Exercises the whole OAuth2 resource server chain: login, refresh, public/protected/admin-only
 * endpoints, and rejection of expired/tampered/foreign-key-signed tokens. Each test runs in a
 * rolled-back transaction so the users created in {@link #createUsers()} never leak between
 * tests.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("int-test")
@Transactional
class SecurityChainIT {

  private static final String PASSWORD = "S3cr3t!Password";
  private static final String ISSUER_URI = "https://vending-app.dahiorus.me";
  private static final String TEST_ORIGIN = "https://spa.example.test";
  private static final String TOKEN_TYPE_CLAIM = "token_type";
  private static final String ROLES_CLAIM = "roles";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private AppUserRepositoryPort appUserRepository;
  @Autowired private AdminUserRepositoryPort adminUserRepository;
  @Autowired private JwtTokenIssuer tokenIssuer;
  @Autowired private JwtEncoder jwtEncoder;

  private String userEmail;
  private String adminEmail;

  @BeforeEach
  void createUsers() {
    userEmail = "user-" + UUID.randomUUID() + "@test.org";
    adminEmail = "admin-" + UUID.randomUUID() + "@test.org";

    appUserRepository.create(
        new AppUserToCreate(
            EmailAddress.of(userEmail),
            Password.of(PASSWORD),
            Firstname.of("John"),
            Lastname.of("Doe")));
    adminUserRepository.create(
        new AdminUserToCreate(
            EmailAddress.of(adminEmail),
            Password.of(PASSWORD),
            Firstname.of("Admin"),
            Lastname.of("Root")));
  }

  @Test
  void should_authenticate_and_return_usable_tokens() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/authenticate")
                .contentType(APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new AuthenticateRequestDto(userEmail, PASSWORD))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").isNotEmpty())
        .andExpect(jsonPath("$.refreshToken").isNotEmpty());
  }

  @Test
  void should_reject_bad_credentials_with_a_json_body() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/authenticate")
                .contentType(APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new AuthenticateRequestDto(userEmail, "wrong-password"))))
        .andExpect(status().isUnauthorized())
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  void should_reject_a_protected_resource_without_a_token() throws Exception {
    mockMvc.perform(get("/api/v1/me")).andExpect(status().isUnauthorized());
  }

  @Test
  void should_allow_a_protected_resource_with_a_valid_token() throws Exception {
    String accessToken = accessTokenFor(userEmail, List.of());

    mockMvc
        .perform(get("/api/v1/me").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isOk());
  }

  @Test
  void should_reject_an_admin_only_resource_with_a_non_admin_token_but_return_a_json_body()
      throws Exception {
    // regression test for the ObjectMapper bug fixed in commit 5198642: a 403 must be
    // serializable JSON, not crash the response
    String accessToken = accessTokenFor(userEmail, List.of());

    mockMvc
        .perform(get("/api/v1/items").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.timestamp").exists())
        .andExpect(jsonPath("$.message").exists());
  }

  @Test
  void should_allow_an_admin_only_resource_with_an_admin_token() throws Exception {
    String accessToken = accessTokenFor(adminEmail, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

    mockMvc
        .perform(get("/api/v1/items").header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
        .andExpect(status().isOk());
  }

  @Test
  void should_reject_a_malformed_token() throws Exception {
    mockMvc
        .perform(get("/api/v1/items").header(HttpHeaders.AUTHORIZATION, "Bearer not-a-jwt"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void should_reject_a_tampered_token() throws Exception {
    String accessToken =
        accessTokenFor(adminEmail, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    String tampered = accessToken.substring(0, accessToken.length() - 4) + "abcd";

    mockMvc
        .perform(get("/api/v1/items").header(HttpHeaders.AUTHORIZATION, "Bearer " + tampered))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void should_reject_a_token_signed_with_a_foreign_key() throws Exception {
    String foreignToken = tokenSignedWithAForeignKey(adminEmail);

    mockMvc
        .perform(get("/api/v1/items").header(HttpHeaders.AUTHORIZATION, "Bearer " + foreignToken))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void should_reject_an_expired_token() throws Exception {
    var now = Instant.now();
    var expiredClaims =
        JwtClaimsSet.builder()
            .subject(adminEmail)
            .issuer(ISSUER_URI)
            .issuedAt(now.minusSeconds(7200))
            .expiresAt(now.minusSeconds(3600))
            .claim(TOKEN_TYPE_CLAIM, "access")
            .claim(ROLES_CLAIM, List.of("ROLE_ADMIN"))
            .build();

    String expiredToken = encode(jwtEncoder, expiredClaims);

    mockMvc
        .perform(get("/api/v1/items").header(HttpHeaders.AUTHORIZATION, "Bearer " + expiredToken))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void should_refresh_an_access_token_from_a_refresh_token() throws Exception {
    String refreshToken = tokenIssuer.createRefreshToken(adminEmail);

    mockMvc
        .perform(
            post("/api/v1/authenticate/refresh")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RefreshTokenRequestDto(refreshToken))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.accessToken").isNotEmpty())
        .andExpect(jsonPath("$.accessToken").value(not(refreshToken)));
  }

  @Test
  void should_reject_refresh_with_an_access_token() throws Exception {
    String accessToken =
        accessTokenFor(adminEmail, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

    mockMvc
        .perform(
            post("/api/v1/authenticate/refresh")
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new RefreshTokenRequestDto(accessToken))))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void should_expose_the_jwks_endpoint_without_authentication() throws Exception {
    mockMvc
        .perform(get("/oauth2/jwks"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.keys").isArray())
        .andExpect(jsonPath("$.keys[0].kty").value("RSA"))
        .andExpect(jsonPath("$.keys[0].d").doesNotExist());
  }

  @Test
  void should_allow_public_endpoints_without_authentication() throws Exception {
    mockMvc.perform(get("/api/v1/vending-machines")).andExpect(status().isOk());
  }

  @Test
  void should_handle_a_cors_preflight_request_for_an_allowed_origin() throws Exception {
    mockMvc
        .perform(
            options("/api/v1/me")
                .header(HttpHeaders.ORIGIN, TEST_ORIGIN)
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization,Content-Type"))
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, TEST_ORIGIN))
        .andExpect(
            header().string(
                HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,DELETE"))
        .andExpect(
            header()
                .string(
                    HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                    "Authorization, Content-Type"));
  }

  private String accessTokenFor(final String username, final List<SimpleGrantedAuthority> roles) {
    return tokenIssuer.createAccessToken(username, roles);
  }

  private static String tokenSignedWithAForeignKey(final String username) throws Exception {
    KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
    generator.initialize(2048);
    KeyPair keyPair = generator.generateKeyPair();
    var foreignJwk =
        new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
            .privateKey((RSAPrivateKey) keyPair.getPrivate())
            .keyID(UUID.randomUUID().toString())
            .build();
    var foreignEncoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(foreignJwk)));

    var now = Instant.now();
    var claims =
        JwtClaimsSet.builder()
            .subject(username)
            .issuer(ISSUER_URI)
            .issuedAt(now)
            .expiresAt(now.plusSeconds(3600))
            .claim(TOKEN_TYPE_CLAIM, "access")
            .claim(ROLES_CLAIM, List.of("ROLE_ADMIN"))
            .build();

    return encode(foreignEncoder, claims);
  }

  private static String encode(final JwtEncoder encoder, final JwtClaimsSet claims) {
    var parameters = JwtEncoderParameters.from(JwsHeader.with(RS256).build(), claims);

    return encoder.encode(parameters).getTokenValue();
  }
}
