package me.dahiorus.project.vending.domain.user.entity;

import java.time.Duration;
import java.time.Instant;

public record RefreshToken(
    RefreshTokenId id,
    EmailAddress username,
    Instant issuedAt,
    Instant expiresAt,
    boolean revoked,
    Instant revokedAt,
    RefreshTokenId replacedBy) {

  public boolean isUsable(Instant now) {
    return !revoked && now.isBefore(expiresAt);
  }

  /**
   * A revoked token can still be safely presented to retrieve its successor for a short grace
   * period right after it was rotated, to tolerate near-simultaneous refresh requests sharing the
   * same refresh-token cookie (e.g. several browser tabs reloading at once). Past that window,
   * presenting a revoked token is treated as a genuine reuse/theft signal instead.
   */
  public boolean isReplacedWithinGracePeriod(Instant now, Duration gracePeriod) {
    return revoked
        && replacedBy != null
        && revokedAt != null
        && revokedAt.plus(gracePeriod).isAfter(now);
  }
}
