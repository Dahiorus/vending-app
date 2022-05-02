package me.dahiorus.project.vending.web.security.filter;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.web.security.JwtService;

@Log4j2
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter
{
  private static final String AUTHORIZATION_HEADER_PREFIX = "Bearer ";

  private final JwtService jwtService;

  @Override
  protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
      final FilterChain filterChain)
      throws ServletException, IOException
  {
    if (StringUtils.equalsAny(request.getServletPath(), "/api/v1/authenticate"))
    {
      log.trace("Requesting the authentication path");
      filterChain.doFilter(request, response);
      return;
    }

    // get the header Authorization to get the JWT token
    String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (!StringUtils.startsWith(authorizationHeader, AUTHORIZATION_HEADER_PREFIX))
    {
      log.debug("No header Authorization found with a Bearer token in the request");
      filterChain.doFilter(request, response);
      return;
    }

    log.debug("Get the user from the request");

    // parse the JWT token
    String token = StringUtils.substringAfter(authorizationHeader, AUTHORIZATION_HEADER_PREFIX);
    Authentication authentication = jwtService.parseToken(token);
    SecurityContextHolder.getContext()
      .setAuthentication(authentication);

    log.debug("User from the request: {}", authentication);

    filterChain.doFilter(request, response);
  }
}
