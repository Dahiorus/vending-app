package me.dahiorus.project.vending.web.config;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

import java.time.Instant;
import java.util.Map;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.BCryptVersion;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletResponse;
import me.dahiorus.project.vending.web.security.JwtService;
import me.dahiorus.project.vending.web.security.SecurityConstants;
import me.dahiorus.project.vending.web.security.filter.JwtAuthenticationFilter;
import me.dahiorus.project.vending.web.security.filter.JwtRequestFilter;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig
{
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final String DEFAULT_PWD_ENCODER_PREFIX = "bcrypt";

  @Bean
  SecurityFilterChain filterChain(final HttpSecurity http, final AuthenticationManager authenticationManager,
    final JwtService jwtService)
    throws Exception
  {
    // @formatter:off
    return http
      .csrf(CsrfConfigurer::disable)
      .httpBasic(HttpBasicConfigurer::disable)
      .logout(LogoutConfigurer::disable)
      .sessionManagement(customizer -> customizer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      // request permissions
      .authorizeHttpRequests(customizer -> customizer
        .requestMatchers(SecurityConstants.AUTHENTICATE_ENDPOINT, SecurityConstants.REFRESH_TOKEN_ENDPOINT).permitAll()
        .requestMatchers(antMatcher(HttpMethod.GET, "/api/v1/vending-machines/**"), antMatcher(HttpMethod.GET, "/api/v1/items/{.+}/**")).permitAll()
        .requestMatchers(antMatcher(HttpMethod.POST, "/api/v1/vending-machines/{.+}/purchase/**")).permitAll()
        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
        .requestMatchers(SecurityConstants.REGISTER_ENDPOINT).anonymous()
        .requestMatchers("/api/v1/me/**").authenticated()
        .anyRequest().hasRole("ADMIN"))
      // exception handling
      .exceptionHandling(customizer -> customizer.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
        .accessDeniedHandler(restAccessDeniedHandler()))
      // request filters
      .addFilter(new JwtAuthenticationFilter(authenticationManager, jwtService))
      .addFilterBefore(new JwtRequestFilter(jwtService), UsernamePasswordAuthenticationFilter.class)
      .build();
    // @formatter:on
  }

  private static AccessDeniedHandler restAccessDeniedHandler()
  {
    return (request, response, accessDeniedException) -> {
      response.setStatus(HttpServletResponse.SC_FORBIDDEN);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      MAPPER.writeValue(response.getOutputStream(), Map.of("timestamp", Instant.now()
        .toString(), "message", accessDeniedException.getMessage()));
      response.flushBuffer();
    };
  }

  @Bean
  AuthenticationManager authenticationManager(final HttpSecurity http, final UserDetailsService userDetailsService,
    final PasswordEncoder passwordEncoder) throws Exception
  {
    AuthenticationManagerBuilder builder = http.getSharedObject(AuthenticationManagerBuilder.class);
    builder.userDetailsService(userDetailsService)
      .passwordEncoder(passwordEncoder);

    return builder.build();
  }

  @Bean
  PasswordEncoder passwordEncoder()
  {
    return new DelegatingPasswordEncoder(DEFAULT_PWD_ENCODER_PREFIX,
      Map.of(DEFAULT_PWD_ENCODER_PREFIX, new BCryptPasswordEncoder(BCryptVersion.$2A, 13)));
  }
}
