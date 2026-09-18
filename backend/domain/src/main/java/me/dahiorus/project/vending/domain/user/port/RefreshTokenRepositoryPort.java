package me.dahiorus.project.vending.domain.user.port;

import java.time.Instant;
import me.dahiorus.project.vending.domain.Creatable;
import me.dahiorus.project.vending.domain.Findable;
import me.dahiorus.project.vending.domain.user.entity.RefreshToken;
import me.dahiorus.project.vending.domain.user.entity.RefreshTokenId;

public interface RefreshTokenRepositoryPort
    extends Creatable<RefreshToken, RefreshToken>, Findable<RefreshTokenId, RefreshToken> {
  void revoke(RefreshTokenId id);

  void deleteExpiredBefore(Instant instant);
}
