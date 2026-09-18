package me.dahiorus.project.vending.infrastructure.jpa.repository.user;

import java.time.Instant;
import java.util.Optional;
import me.dahiorus.project.vending.domain.user.entity.RefreshToken;
import me.dahiorus.project.vending.domain.user.entity.RefreshTokenId;
import me.dahiorus.project.vending.domain.user.port.RefreshTokenRepositoryPort;
import org.springframework.stereotype.Repository;

@Repository
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {

  private final RefreshTokenJpaRepository jpaRepository;

  public RefreshTokenRepositoryAdapter(final RefreshTokenJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public RefreshToken save(final RefreshToken token) {
    return jpaRepository.save(JpaRefreshToken.fromDomain(token)).toDomain();
  }

  @Override
  public Optional<RefreshToken> findById(final RefreshTokenId id) {
    return jpaRepository.findById(id.value()).map(JpaRefreshToken::toDomain);
  }

  @Override
  public void revoke(final RefreshTokenId id) {
    jpaRepository
        .findById(id.value())
        .ifPresent(
            token -> {
              token.setRevoked(true);
              jpaRepository.save(token);
            });
  }

  @Override
  public void deleteExpiredBefore(final Instant instant) {
    jpaRepository.deleteAllByExpiresAtBefore(instant);
  }
}
