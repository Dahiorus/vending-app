package me.dahiorus.project.vending.domain.user.port;

import java.time.Instant;
import me.dahiorus.project.vending.domain.Creatable;
import me.dahiorus.project.vending.domain.Findable;
import me.dahiorus.project.vending.domain.user.entity.RefreshToken;
import me.dahiorus.project.vending.domain.user.entity.RefreshTokenId;

public interface RefreshTokenRepositoryPort
    extends Creatable<RefreshToken, RefreshToken>, Findable<RefreshTokenId, RefreshToken> {
  void revoke(RefreshTokenId id);

  /** Revokes {@code id} and records {@code replacedBy} as its successor, for grace-period reuse. */
  void markReplaced(RefreshTokenId id, RefreshTokenId replacedBy, Instant revokedAt);

  void deleteExpiredBefore(Instant instant);
}
