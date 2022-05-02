package me.dahiorus.project.vending.web.security.filter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.dahiorus.project.vending.web.api.model.AuthenticateRequest;
import me.dahiorus.project.vending.web.api.model.AuthenticateResponse;
import me.dahiorus.project.vending.web.security.JwtService;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter
{
  private static final ObjectMapper MAPPER = new ObjectMapper();

  private final JwtService jwtService;

  public JwtAuthenticationFilter(final AuthenticationManager authenticationManager, final JwtService jwtService)
  {
    super(authenticationManager);
    setFilterProcessesUrl("/api/v1/authenticate");
    this.jwtService = jwtService;
  }

  @Override
  public Authentication attemptAuthentication(final HttpServletRequest request, final HttpServletResponse response)
      throws AuthenticationException
  {
    try
    {
      AuthenticateRequest authRequest = MAPPER.readValue(request.getInputStream(), AuthenticateRequest.class);
      Authentication authentication = new UsernamePasswordAuthenticationToken(authRequest.getUsername(),
          authRequest.getPassword());

      return getAuthenticationManager().authenticate(authentication);
    }
    catch (IOException e)
    {
      throw new InternalAuthenticationServiceException("Unable to authenticate a user", e);
    }
  }

  @Override
  protected void successfulAuthentication(final HttpServletRequest request, final HttpServletResponse response,
      final FilterChain chain,
      final Authentication authentication) throws IOException, ServletException
  {
    UserDetails user = (UserDetails) authentication.getPrincipal();
    String accessToken = jwtService.createAccessToken(user.getUsername(), user.getAuthorities());
    String refreshToken = jwtService.createRefreshToken(user.getUsername());

    AuthenticateResponse authResponse = new AuthenticateResponse(accessToken, refreshToken);

    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    MAPPER.writeValue(response.getOutputStream(), authResponse);
  }

  @Override
  protected void unsuccessfulAuthentication(final HttpServletRequest request, final HttpServletResponse response,
      final AuthenticationException failed) throws IOException, ServletException
  {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    MAPPER.writeValue(response.getOutputStream(), Map.of("message", failed.getMessage(), "timestamp", Instant.now()
      .toString()));
  }
}
