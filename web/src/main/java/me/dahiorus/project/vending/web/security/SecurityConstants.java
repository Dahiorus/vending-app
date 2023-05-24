package me.dahiorus.project.vending.web.security;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SecurityConstants
{
  public static final String AUTHENTICATE_ENDPOINT = "/api/v1/authenticate";

  public static final String REFRESH_TOKEN_ENDPOINT = "/api/v1/authenticate/refresh";

  public static final String REGISTER_ENDPOINT = "/api/v1/register";
}
