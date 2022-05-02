package me.dahiorus.project.vending.web.security;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

public interface JwtService
{
  String createAccessToken(String username, Collection<? extends GrantedAuthority> authorities);

  String createRefreshToken(String username);

  Authentication parseToken(String token);
}
