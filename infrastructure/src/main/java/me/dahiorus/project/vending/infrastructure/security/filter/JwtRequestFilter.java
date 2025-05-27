package me.dahiorus.project.vending.infrastructure.security.filter;

import static me.dahiorus.project.vending.infrastructure.config.WebSecurityConfig.AUTHENTICATE_PATH;
import static me.dahiorus.project.vending.infrastructure.config.WebSecurityConfig.REFRESH_TOKEN_PATH;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.security.core.context.SecurityContextHolder.getContext;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import me.dahiorus.project.vending.infrastructure.security.TokenService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtRequestFilter extends OncePerRequestFilter {

  private static final String AUTHORIZATION_HEADER_PREFIX = "Bearer";

  private final TokenService tokenService;

  public JwtRequestFilter(final TokenService tokenService) {
    this.tokenService = tokenService;
  }

  @Override
  protected void doFilterInternal(
      final HttpServletRequest request,
      final HttpServletResponse response,
      final FilterChain filterChain)
      throws ServletException, IOException {
    if (isAuthenticateRequest(request)) {
      logger.trace("Requesting the authentication or the refresh token path");
      filterChain.doFilter(request, response);
      return;
    }

    String authorizationHeader = request.getHeader(AUTHORIZATION);

    if (!hasAuthorization(authorizationHeader)) {
      logger.debug("No header Authorization found with a Bearer token in the request");
      filterChain.doFilter(request, response);
      return;
    }

    handleAuthenticatedUser(authorizationHeader);
    filterChain.doFilter(request, response);
  }

  private void handleAuthenticatedUser(String authorizationHeader) {
    logger.debug("Resolving the authenticated user from the request");

    var token = StringUtils.removeStart(authorizationHeader, AUTHORIZATION_HEADER_PREFIX);
    var authentication = tokenService.parseToken(token);

    getContext().setAuthentication(authentication);
  }

  private static boolean hasAuthorization(String authorizationHeader) {
    return StringUtils.startsWith(authorizationHeader, AUTHORIZATION_HEADER_PREFIX);
  }

  private static boolean isAuthenticateRequest(final HttpServletRequest request) {
    return StringUtils.equalsAny(request.getServletPath(), AUTHENTICATE_PATH, REFRESH_TOKEN_PATH);
  }
}
