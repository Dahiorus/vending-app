package me.dahiorus.project.vending.web.config;

import java.time.Instant;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.BCryptVersion;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import me.dahiorus.project.vending.web.security.JwtService;
import me.dahiorus.project.vending.web.security.SecurityConstants;
import me.dahiorus.project.vending.web.security.filter.JwtAuthenticationFilter;
import me.dahiorus.project.vending.web.security.filter.JwtRequestFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig extends WebSecurityConfigurerAdapter
{
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final UserDetailsService userDetailsService;

  private final JwtService jwtService;

  @Override
  protected void configure(final AuthenticationManagerBuilder auth) throws Exception
  {
    auth.userDetailsService(userDetailsService)
      .passwordEncoder(passwordEncoder());
  }

  @Override
  protected void configure(final HttpSecurity http) throws Exception
  {
    // @formatter:off
    http
      .csrf().disable()
      .httpBasic().disable()
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
        .logout().disable()
        // request permissions
        .authorizeRequests()
          .mvcMatchers(SecurityConstants.AUTHENTICATE_ENDPOINT, SecurityConstants.REFRESH_TOKEN_ENDPOINT).permitAll()
          .antMatchers(HttpMethod.GET, "/api/v1/vending-machines/**", "/api/v1/items/{.+}/**").permitAll()
          .antMatchers(HttpMethod.POST, "/api/v1/vending-machines/{.+}/purchase/**").permitAll()
          .antMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
          .mvcMatchers(SecurityConstants.REGISTER_ENDPOINT).anonymous()
          .antMatchers("/api/v1/me/**").authenticated()
          .anyRequest().hasAuthority("ROLE_ADMIN")
      .and()
        // exception handling
        .exceptionHandling()
          .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
          .accessDeniedHandler(restAccessDeniedHandler())
      .and()
        // request filters
        .addFilter(new JwtAuthenticationFilter(authenticationManagerBean(), jwtService))
        .addFilterBefore(new JwtRequestFilter(jwtService), UsernamePasswordAuthenticationFilter.class);
    // @formatter:on
  }

  @Bean
  public AccessDeniedHandler restAccessDeniedHandler()
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
  public PasswordEncoder passwordEncoder()
  {
    return new BCryptPasswordEncoder(BCryptVersion.$2A, 13);
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception
  {
    return super.authenticationManagerBean();
  }
}
