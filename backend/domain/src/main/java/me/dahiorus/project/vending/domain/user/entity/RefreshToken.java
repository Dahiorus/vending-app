package me.dahiorus.project.vending.domain.user.entity;

import java.time.Instant;

public record RefreshToken(
    RefreshTokenId id,
    EmailAddress username,
    Instant issuedAt,
    Instant expiresAt,
    boolean revoked) {

  public boolean isUsable(Instant now) {
    return !revoked && now.isBefore(expiresAt);
  }
}
