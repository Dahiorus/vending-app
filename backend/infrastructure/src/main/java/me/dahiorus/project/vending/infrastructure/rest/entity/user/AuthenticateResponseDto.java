package me.dahiorus.project.vending.infrastructure.rest.entity.user;

import static org.apache.commons.lang3.StringUtils.abbreviate;

public record AuthenticateResponseDto(String accessToken, String refreshToken) {
  @Override
  public String toString() {
    return "AuthenticateResponse [accessToken="
        + abbreviate(accessToken, 10)
        + ", refreshToken="
        + abbreviate(refreshToken, 10)
        + "]";
  }
}
