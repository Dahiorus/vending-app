package me.dahiorus.project.vending.infrastructure.jpa.repository.user;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.RefreshToken;
import me.dahiorus.project.vending.domain.user.entity.RefreshTokenId;
import me.dahiorus.project.vending.domain.user.port.RefreshTokenRepositoryPort;
import me.dahiorus.project.vending.infrastructure.jpa.repository.H2DbContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = RefreshTokenRepositoryAdapterIT.TestConfig.class)
class RefreshTokenRepositoryAdapterIT extends H2DbContainer {

  @Autowired private RefreshTokenRepositoryAdapter repository;

  @Test
  void should_save_and_find_by_id() {
    var token =
        token(UUID.randomUUID(), "user@test.org", false, Instant.parse("2026-09-18T12:00:00Z"));

    var saved = repository.save(token);
    entityManager.flush();
    entityManager.clear();

    assertThat(repository.findById(saved.id())).contains(saved);
  }

  @Test
  void should_revoke_token() {
    var token =
        repository.save(
            token(randomUUID(), "user@test.org", false, Instant.parse("2026-09-18T12:00:00Z")));
    entityManager.flush();
    entityManager.clear();

    repository.revoke(token.id());
    entityManager.flush();
    entityManager.clear();

    assertThat(repository.findById(token.id()))
        .get()
        .extracting(RefreshToken::revoked)
        .isEqualTo(true);
  }

  @Test
  void should_delete_only_expired_tokens() {
    var threshold = Instant.parse("2026-09-18T12:00:00Z");
    var expired =
        repository.save(token(randomUUID(), "expired@test.org", false, threshold.minusSeconds(1)));
    var valid =
        repository.save(token(randomUUID(), "valid@test.org", false, threshold.plusSeconds(1)));
    entityManager.flush();
    entityManager.clear();

    repository.deleteExpiredBefore(threshold);
    entityManager.flush();
    entityManager.clear();

    assertThat(repository.findById(expired.id())).isEmpty();
    assertThat(repository.findById(valid.id())).contains(valid);
  }

  private static RefreshToken token(UUID id, String username, boolean revoked, Instant expiresAt) {
    return new RefreshToken(
        new RefreshTokenId(id),
        EmailAddress.of(username),
        expiresAt.minusSeconds(3600),
        expiresAt,
        revoked);
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    RefreshTokenRepositoryPort refreshTokenRepository(RefreshTokenJpaRepository jpaRepository) {
      return new RefreshTokenRepositoryAdapter(jpaRepository);
    }
  }
}
