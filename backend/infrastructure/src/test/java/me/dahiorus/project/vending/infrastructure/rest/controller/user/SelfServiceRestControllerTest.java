package me.dahiorus.project.vending.infrastructure.rest.controller.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import me.dahiorus.project.vending.domain.file.entity.FileToUpload;
import me.dahiorus.project.vending.domain.user.entity.AppUser;
import me.dahiorus.project.vending.domain.user.entity.AppUserWithPicture;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.Firstname;
import me.dahiorus.project.vending.domain.user.entity.Lastname;
import me.dahiorus.project.vending.domain.user.entity.UserId;
import me.dahiorus.project.vending.domain.user.port.AppUserApiPort;
import me.dahiorus.project.vending.domain.user.port.RefreshTokenApiPort;
import me.dahiorus.project.vending.infrastructure.rest.assembler.UserDtoModelAssembler;
import me.dahiorus.project.vending.infrastructure.rest.entity.user.EditPasswordRequestDto;
import me.dahiorus.project.vending.infrastructure.rest.entity.user.UserToUpdateDto;
import me.dahiorus.project.vending.infrastructure.rest.exception.RestResponseExceptionHandler;
import me.dahiorus.project.vending.infrastructure.security.config.CorsProperties;
import me.dahiorus.project.vending.infrastructure.security.config.WebSecurityConfig;
import me.dahiorus.project.vending.infrastructure.security.cookie.RefreshTokenCookieFactory;
import me.dahiorus.project.vending.infrastructure.security.cookie.RefreshTokenCookieProperties;
import me.dahiorus.project.vending.infrastructure.security.jwt.JwtProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SelfServiceRestController.class)
@Import({
  RestResponseExceptionHandler.class,
  WebSecurityConfig.class,
  UserDtoModelAssembler.class,
  SelfServiceRestControllerTest.TestConfig.class
})
@TestPropertySource(properties = "app.cors.allowed-origins=https://spa.example.test")
class SelfServiceRestControllerTest {

  private static final String USERNAME = "user@vending.me";
  private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockitoBean private AppUserApiPort appUserApiPort;
  @MockitoBean private UserDetailsService userDetailsService;
  @MockitoBean private JwtDecoder jwtDecoder;
  @MockitoBean private JwtAuthenticationConverter jwtAuthenticationConverter;
  @MockitoBean private RefreshTokenApiPort refreshTokenApiPort;

  @Test
  void should_return_the_authenticated_user_with_hal_links() throws Exception {
    // Given
    var user =
        new AppUser(
            new UserId(USER_ID),
            EmailAddress.of(USERNAME),
            Firstname.of("Ada"),
            Lastname.of("Lovelace"));
    when(appUserApiPort.getByUsername(EmailAddress.of(USERNAME))).thenReturn(user);

    // When / Then
    mockMvc
        .perform(get("/api/v1/me").with(jwt().jwt(builder -> builder.subject(USERNAME))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value(USERNAME))
        .andExpect(jsonPath("$.firstname").value("Ada"))
        .andExpect(jsonPath("$.lastname").value("Lovelace"))
        .andExpect(jsonPath("$._links.self.href").exists())
        .andExpect(jsonPath("$._links['me:picture'].href").exists())
        .andExpect(jsonPath("$._links['me:password'].href").exists());
  }

  @Test
  void should_update_the_authenticated_user_information() throws Exception {
    // Given
    var authenticatedUser =
        new AppUser(
            new UserId(USER_ID),
            EmailAddress.of(USERNAME),
            Firstname.of("Ada"),
            Lastname.of("Lovelace"));
    var updatedUser =
        new AppUser(
            new UserId(USER_ID),
            EmailAddress.of(USERNAME),
            Firstname.of("Grace"),
            Lastname.of("Hopper"));
    when(appUserApiPort.getByUsername(EmailAddress.of(USERNAME))).thenReturn(authenticatedUser);
    when(appUserApiPort.update(any())).thenReturn(updatedUser);

    // When / Then
    mockMvc
        .perform(
            put("/api/v1/me")
                .with(jwt().jwt(builder -> builder.subject(USERNAME)))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new UserToUpdateDto("Grace", "Hopper"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.firstname").value("Grace"))
        .andExpect(jsonPath("$.lastname").value("Hopper"));

    var updateCaptor = ArgumentCaptor.forClass(AppUser.class);
    verify(appUserApiPort).update(updateCaptor.capture());
    assertThat(updateCaptor.getValue().id()).isEqualTo(new UserId(USER_ID));
  }

  @Test
  void should_return_the_full_user_information_when_uploading_a_profile_picture() throws Exception {
    // Given: AppUserWithPicture only carries the id and the picture (see domain entity), so the
    // controller must reuse the authenticated user's email/firstname/lastname to build a
    // complete response -- regression test for a bug where the upload response was missing
    // those fields, making the frontend believe the user had no name/email after an upload.
    var authenticatedUser =
        new AppUser(
            new UserId(USER_ID),
            EmailAddress.of(USERNAME),
            Firstname.of("Ada"),
            Lastname.of("Lovelace"));
    when(appUserApiPort.getByUsername(EmailAddress.of(USERNAME))).thenReturn(authenticatedUser);
    when(appUserApiPort.uploadProfilePicture(eq(new UserId(USER_ID)), any(FileToUpload.class)))
        .thenReturn(new AppUserWithPicture(new UserId(USER_ID), null));

    var file =
        new MockMultipartFile("file", "picture.png", "image/png", "fake-image-bytes".getBytes());

    // When / Then
    mockMvc
        .perform(
            multipart("/api/v1/me/picture")
                .file(file)
                .with(jwt().jwt(builder -> builder.subject(USERNAME)))
                .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(USER_ID.toString()))
        .andExpect(jsonPath("$.email").value(USERNAME))
        .andExpect(jsonPath("$.firstname").value("Ada"))
        .andExpect(jsonPath("$.lastname").value("Lovelace"));
  }

  @Test
  void should_update_the_authenticated_user_password() throws Exception {
    // Given
    var authenticatedUser =
        new AppUser(
            new UserId(USER_ID),
            EmailAddress.of(USERNAME),
            Firstname.of("Ada"),
            Lastname.of("Lovelace"));
    when(appUserApiPort.getByUsername(EmailAddress.of(USERNAME))).thenReturn(authenticatedUser);

    // When / Then
    mockMvc
        .perform(
            post("/api/v1/me/password")
                .with(jwt().jwt(builder -> builder.subject(USERNAME)))
                .with(csrf())
                .contentType(APPLICATION_JSON)
                .content(
                    objectMapper.writeValueAsString(
                        new EditPasswordRequestDto("old-secret", "new-secret"))))
        .andExpect(status().isNoContent());

    verify(appUserApiPort).updateUserPassword(eq(new UserId(USER_ID)), any());
  }

  static class TestConfig {
    @Bean
    Clock clock() {
      return Clock.fixed(Instant.parse("2026-09-29T20:00:00Z"), ZoneOffset.UTC);
    }

    @Bean
    RefreshTokenCookieProperties refreshTokenCookieProperties() {
      return new RefreshTokenCookieProperties();
    }

    @Bean
    RefreshTokenCookieFactory refreshTokenCookieFactory(
        final RefreshTokenCookieProperties properties) {
      return new RefreshTokenCookieFactory(properties);
    }

    @Bean
    @Primary
    CorsProperties testCorsProperties() {
      CorsProperties properties = new CorsProperties();
      properties.setAllowedOrigins(java.util.List.of("https://spa.example.test"));
      return properties;
    }

    @Bean
    JwtProperties jwtProperties() {
      JwtProperties properties = new JwtProperties();
      properties.setIssuerUri("https://issuer.example.test");
      return properties;
    }
  }
}
