package me.dahiorus.project.vending.infrastructure.rest.entity.user;

import static org.apache.commons.lang3.StringUtils.abbreviate;

public record RefreshTokenRequestDto(String token) {
  @Override
  public String toString() {
    return "RefreshTokenRequest [token=" + abbreviate(token, 10) + "]";
  }
}
