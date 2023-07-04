package me.dahiorus.project.vending.web.security.filter;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.domain.exception.AppRuntimeException;
import me.dahiorus.project.vending.web.exception.UnparsableToken;
import me.dahiorus.project.vending.web.security.JwtService;
import me.dahiorus.project.vending.web.security.SecurityConstants;

@Log4j2
@RequiredArgsConstructor
public class JwtRequestFilter extends OncePerRequestFilter
{
  private static final String AUTHORIZATION_HEADER_PREFIX = "Bearer";

  private final JwtService jwtService;

  @Override
  protected void doFilterInternal(final HttpServletRequest request,
    final HttpServletResponse response, final FilterChain filterChain)
    throws ServletException, IOException
  {
    if (isAuthenticateRequest(request))
    {
      log.trace("Requesting the authentication or the refresh token path");
      filterChain.doFilter(request, response);
      return;
    }

    String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (!hasAuthorization(authorizationHeader))
    {
      log.debug("No header Authorization found with a Bearer token in the request");
      filterChain.doFilter(request, response);
      return;
    }

    handleAuthenticatedUser(authorizationHeader);
    filterChain.doFilter(request, response);
  }

  private void handleAuthenticatedUser(String authorizationHeader)
  {
    log.debug("Resolving the authenticated user from the request");
    
    String token = StringUtils.removeStart(authorizationHeader, AUTHORIZATION_HEADER_PREFIX);
    Authentication authentication;
    try
    {
      authentication = jwtService.parseToken(token);
    }
    catch (UnparsableToken e)
    {
      throw new AppRuntimeException("Unexpected error while authorizing a request user", e);
    }

    SecurityContextHolder.getContext()
      .setAuthentication(authentication);

    log.debug("User from the request: {}", authentication);
  }

  private static boolean hasAuthorization(String authorizationHeader)
  {
    return StringUtils.startsWith(authorizationHeader, AUTHORIZATION_HEADER_PREFIX);
  }

  private static boolean isAuthenticateRequest(final HttpServletRequest request)
  {
    return StringUtils.equalsAny(request.getServletPath(), SecurityConstants.AUTHENTICATE_ENDPOINT,
      SecurityConstants.REFRESH_TOKEN_ENDPOINT);
  }
}
