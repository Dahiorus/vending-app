package me.dahiorus.project.vending.web.api.model;

import org.apache.commons.lang3.StringUtils;

public record RefreshTokenRequest(String token)
{
  @Override
  public String toString()
  {
    return "RefreshTokenRequest [token=" + StringUtils.abbreviate(token, 10) + "]";
  }
}
