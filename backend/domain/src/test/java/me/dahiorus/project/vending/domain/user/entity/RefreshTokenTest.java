package me.dahiorus.project.vending.domain.user.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class RefreshTokenTest {

  @Test
  void should_be_usable_when_not_revoked_and_not_expired() {
    var now = Instant.parse("2026-09-18T15:00:00Z");
    var token =
        new RefreshToken(
            new RefreshTokenId(UUID.randomUUID()),
            EmailAddress.of("user@test.org"),
            now.minusSeconds(60),
            now.plusSeconds(60),
            false);

    var result = token.isUsable(now);

    assertThat(result).isTrue();
  }

  @Test
  void should_not_be_usable_when_revoked() {
    var now = Instant.parse("2026-09-18T15:00:00Z");
    var token =
        new RefreshToken(
            new RefreshTokenId(UUID.randomUUID()),
            EmailAddress.of("user@test.org"),
            now.minusSeconds(60),
            now.plusSeconds(60),
            true);

    var result = token.isUsable(now);

    assertThat(result).isFalse();
  }

  @Test
  void should_not_be_usable_when_expired() {
    var now = Instant.parse("2026-09-18T15:00:00Z");
    var token =
        new RefreshToken(
            new RefreshTokenId(UUID.randomUUID()),
            EmailAddress.of("user@test.org"),
            now.minusSeconds(120),
            now.minusSeconds(1),
            false);

    var result = token.isUsable(now);

    assertThat(result).isFalse();
  }
}
