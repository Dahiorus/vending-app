package me.dahiorus.project.vending.infrastructure.security.filter;

import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static me.dahiorus.project.vending.infrastructure.config.WebSecurityConfig.AUTHENTICATE_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import me.dahiorus.project.vending.infrastructure.rest.entity.user.AuthenticateRequestDto;
import me.dahiorus.project.vending.infrastructure.rest.entity.user.AuthenticateResponseDto;
import me.dahiorus.project.vending.infrastructure.security.TokenService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final TokenService tokenService;

  public JwtAuthenticationFilter(
      final AuthenticationManager authenticationManager, final TokenService tokenService) {
    super(authenticationManager);
    setFilterProcessesUrl(AUTHENTICATE_PATH);
    this.tokenService = tokenService;
  }

  @Override
  public Authentication attemptAuthentication(
      final HttpServletRequest request, final HttpServletResponse response)
      throws AuthenticationException {
    AuthenticateRequestDto authRequest;
    try {
      authRequest = MAPPER.readValue(request.getInputStream(), AuthenticateRequestDto.class);
    } catch (IOException e) {
      throw new InternalAuthenticationServiceException("Unable to authenticate a user", e);
    }

    Authentication authentication =
        new UsernamePasswordAuthenticationToken(authRequest.username(), authRequest.password());

    return getAuthenticationManager().authenticate(authentication);
  }

  @Override
  protected void successfulAuthentication(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final FilterChain chain,
      final Authentication authentication) {
    var user = (UserDetails) authentication.getPrincipal();

    var accessToken = tokenService.createAccessToken(user.getUsername(), user.getAuthorities());
    var refreshToken = tokenService.createRefreshToken(user.getUsername());

    AuthenticateResponseDto authResponse = new AuthenticateResponseDto(accessToken, refreshToken);

    response.setContentType(APPLICATION_JSON_VALUE);
    try {
      MAPPER.writeValue(response.getOutputStream(), authResponse);
    } catch (IOException e) {
      throw new InternalAuthenticationServiceException(
          "Unable to write authentication response", e);
    }
  }

  @Override
  protected void unsuccessfulAuthentication(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final AuthenticationException failed) {
    response.setStatus(SC_UNAUTHORIZED);
    response.setContentType(APPLICATION_JSON_VALUE);
    try {
      MAPPER.writeValue(
          response.getOutputStream(),
          Map.of("message", failed.getMessage(), "timestamp", Instant.now().toString()));
      response.flushBuffer();
    } catch (IOException e) {
      throw new InternalAuthenticationServiceException(e.getMessage(), e);
    }
  }
}
