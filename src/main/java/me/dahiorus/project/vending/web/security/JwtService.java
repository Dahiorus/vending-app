package me.dahiorus.project.vending.web.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService
{
  String createAccessToken(UserDetails userDetails);

  String createRefreshToken(UserDetails userDetails);

  Authentication parseToken(String token);
}
