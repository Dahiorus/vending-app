package me.dahiorus.project.vending.web.api.model;

import org.apache.commons.lang3.StringUtils;

public record AuthenticateResponse(String accessToken, String refreshToken)
{
  @Override
  public String toString()
  {
    return "AuthenticateResponse [accessToken=" + StringUtils.abbreviate(accessToken, 10) + ", refreshToken="
      + StringUtils.abbreviate(refreshToken, 10) + "]";
  }
}
