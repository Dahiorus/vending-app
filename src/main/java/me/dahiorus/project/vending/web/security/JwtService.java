package me.dahiorus.project.vending.web.security;

import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import me.dahiorus.project.vending.web.exception.UncreatableToken;
import me.dahiorus.project.vending.web.exception.UnparsableToken;

public interface JwtService
{
  String createAccessToken(String username, Collection<? extends GrantedAuthority> authorities) throws UncreatableToken;

  String createRefreshToken(String username) throws UncreatableToken;

  Authentication parseToken(String token) throws UnparsableToken;
}
