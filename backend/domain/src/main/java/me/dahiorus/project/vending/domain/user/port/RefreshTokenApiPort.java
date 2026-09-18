package me.dahiorus.project.vending.domain.user.port;

import me.dahiorus.project.vending.domain.exception.InvalidRefreshToken;
import me.dahiorus.project.vending.domain.user.entity.RefreshToken;
import me.dahiorus.project.vending.domain.user.entity.RefreshTokenId;

public interface RefreshTokenApiPort {
  RefreshToken rotate(RefreshTokenId presentedId, RefreshToken replacement)
      throws InvalidRefreshToken;

  void revoke(RefreshTokenId id);
}
