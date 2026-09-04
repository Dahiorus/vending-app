package me.dahiorus.project.vending.infrastructure.security.config;

import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static java.time.LocalDateTime.now;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;
import static org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.BCryptVersion.$2A;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

  public static final String AUTHENTICATE_PATH = "/api/v1/authenticate";
  public static final String REFRESH_TOKEN_PATH = "/api/v1/authenticate/refresh";
  public static final String JWKS_PATH = "/oauth2/jwks";

  private static final String DEFAULT_PWD_ENCODER_PREFIX = "bcrypt";

  @Bean
  SecurityFilterChain filterChain(
      final HttpSecurity http,
      final ObjectMapper objectMapper,
      final JwtAuthenticationConverter jwtAuthenticationConverter, final Clock clock)
      throws Exception {
    return http.csrf(CsrfConfigurer::disable)
        .httpBasic(HttpBasicConfigurer::disable)
        .logout(LogoutConfigurer::disable)
        .sessionManagement(customizer -> customizer.sessionCreationPolicy(STATELESS))
        // request permissions
        .authorizeHttpRequests(
            customizer ->
                customizer
                    .requestMatchers(AUTHENTICATE_PATH, REFRESH_TOKEN_PATH, JWKS_PATH, "/api/v1/")
                    .permitAll()
                    .requestMatchers(
                        antMatcher(GET, "/api/v1/vending-machines/**"),
                        antMatcher(GET, "/api/v1/items/{itemId}/**"))
                    .permitAll()
                    .requestMatchers(
                        antMatcher(POST, "/api/v1/vending-machines/{vendingMachineId}/order/**"))
                    .permitAll()
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**")
                    .permitAll()
                    .requestMatchers("/api/v1/register")
                    .anonymous()
                    .requestMatchers("/api/v1/me/**")
                    .authenticated()
                    .anyRequest()
                    .hasRole("ADMIN"))
        // exception handling
        .exceptionHandling(
            customizer ->
                customizer
                    .authenticationEntryPoint(restAuthenticationEntryPoint(objectMapper, clock))
                    .accessDeniedHandler(restAccessDeniedHandler(objectMapper, clock)))
        // resource server: incoming requests are authenticated via a Bearer JWT
        .oauth2ResourceServer(
            customizer ->
                customizer.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
        .build();
  }

  private static AuthenticationEntryPoint restAuthenticationEntryPoint(
      final ObjectMapper objectMapper, final Clock clock) {
    return (request, response, authException) -> {
      response.setStatus(SC_UNAUTHORIZED);
      response.setContentType(APPLICATION_JSON_VALUE);
      objectMapper.writeValue(
          response.getOutputStream(),
          Map.of("timestamp", now(clock), "message", authException.getMessage()));
      response.flushBuffer();
    };
  }

  private static AccessDeniedHandler restAccessDeniedHandler(final ObjectMapper objectMapper, final Clock clock) {
    return (request, response, accessDeniedException) -> {
      response.setStatus(SC_FORBIDDEN);
      response.setContentType(APPLICATION_JSON_VALUE);
      objectMapper.writeValue(
          response.getOutputStream(),
          Map.of("timestamp", now(clock), "message", accessDeniedException.getMessage()));
      response.flushBuffer();
    };
  }

  @Bean
  AuthenticationManager authenticationManager(
      final HttpSecurity http,
      final UserDetailsService userDetailsService,
      final PasswordEncoder passwordEncoder)
      throws Exception {
    AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
    builder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder);

    return builder.build();
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new DelegatingPasswordEncoder(
        DEFAULT_PWD_ENCODER_PREFIX,
        Map.of(DEFAULT_PWD_ENCODER_PREFIX, new BCryptPasswordEncoder($2A, 13)));
  }
}

