package me.dahiorus.project.vending.infrastructure.security;

import java.util.Collection;
import me.dahiorus.project.vending.infrastructure.rest.exception.InvalidTokenCreation;
import me.dahiorus.project.vending.infrastructure.rest.exception.UnparsableToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public interface TokenService {
  String createAccessToken(String username, Collection<? extends GrantedAuthority> authorities)
      throws InvalidTokenCreation;

  String createRefreshToken(String username) throws InvalidTokenCreation;

  Authentication parseToken(String token) throws UnparsableToken;
}
