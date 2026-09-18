package me.dahiorus.project.vending.domain.user.port;

import java.time.Instant;
import java.util.Optional;
import me.dahiorus.project.vending.domain.user.entity.RefreshToken;
import me.dahiorus.project.vending.domain.user.entity.RefreshTokenId;

public interface RefreshTokenRepositoryPort {
  RefreshToken save(RefreshToken token);

  Optional<RefreshToken> findById(RefreshTokenId id);

  void revoke(RefreshTokenId id);

  void deleteExpiredBefore(Instant instant);
}
