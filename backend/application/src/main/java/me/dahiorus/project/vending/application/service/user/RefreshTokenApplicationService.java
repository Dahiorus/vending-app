package me.dahiorus.project.vending.application.service.user;

import static java.time.Instant.now;

import java.time.Clock;
import java.time.Duration;
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

  private final Clock clock;

  private final Duration reuseGracePeriod;

  public RefreshTokenApplicationService(
      final RefreshTokenRepositoryPort refreshTokenRepository,
      final Clock clock,
      final Duration refreshTokenReuseGracePeriod) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.clock = clock;
    this.reuseGracePeriod = refreshTokenReuseGracePeriod;
  }

  public RefreshToken save(RefreshToken refreshToken) {
    return refreshTokenRepository.create(refreshToken);
  }

  @Override
  public RefreshToken rotate(final RefreshTokenId presentedId, final RefreshToken replacement)
      throws InvalidRefreshToken {
    var currentInstant = now(clock);
    var presentedToken =
        refreshTokenRepository
            .find(presentedId)
            .orElseThrow(() -> new InvalidRefreshToken("Unknown refresh token"));

    if (presentedToken.isUsable(currentInstant)) {
      var created = refreshTokenRepository.create(replacement);
      refreshTokenRepository.markReplaced(presentedId, created.id(), currentInstant);
      return created;
    }

    // Tolerate near-simultaneous refresh requests (e.g. several browser tabs reloading at once)
    // that present the same, already-rotated refresh token: return its still-valid successor
    // instead of rejecting it, as long as the rotation happened very recently.
    if (presentedToken.isReplacedWithinGracePeriod(currentInstant, reuseGracePeriod)) {
      return refreshTokenRepository
          .find(presentedToken.replacedBy())
          .filter(successor -> successor.isUsable(currentInstant))
          .orElseThrow(() -> new InvalidRefreshToken("Expired or revoked refresh token"));
    }

    throw new InvalidRefreshToken("Expired or revoked refresh token");
  }

  @Override
  public void revoke(final RefreshTokenId id) {
    refreshTokenRepository.revoke(id);
  }
}
