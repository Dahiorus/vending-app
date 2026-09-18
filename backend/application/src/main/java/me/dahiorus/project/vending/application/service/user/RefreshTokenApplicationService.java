package me.dahiorus.project.vending.application.service.user;

import java.time.Instant;
import me.dahiorus.project.vending.domain.exception.InvalidRefreshToken;
import me.dahiorus.project.vending.domain.user.entity.RefreshToken;
import me.dahiorus.project.vending.domain.user.entity.RefreshTokenId;
import me.dahiorus.project.vending.domain.user.port.RefreshTokenApiPort;
import me.dahiorus.project.vending.domain.user.port.RefreshTokenRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class RefreshTokenApplicationService implements RefreshTokenApiPort {

  private final RefreshTokenRepositoryPort refreshTokenRepository;

  public RefreshTokenApplicationService(final RefreshTokenRepositoryPort refreshTokenRepository) {
    this.refreshTokenRepository = refreshTokenRepository;
  }

  public RefreshToken save(RefreshToken refreshToken) {
    return refreshTokenRepository.create(refreshToken);
  }

  @Override
  public RefreshToken rotate(final RefreshTokenId presentedId, final RefreshToken replacement)
      throws InvalidRefreshToken {
    var refreshToken =
        refreshTokenRepository
            .find(presentedId)
            .orElseThrow(() -> new InvalidRefreshToken("Unknown refresh token"));

    if (!refreshToken.isUsable(Instant.now())) {
      throw new InvalidRefreshToken("Expired or revoked refresh token");
    }

    refreshTokenRepository.revoke(presentedId);
    return refreshTokenRepository.create(replacement);
  }

  @Override
  public void revoke(final RefreshTokenId id) {
    refreshTokenRepository.revoke(id);
  }
}
