package me.dahiorus.project.vending.domain.user.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
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
            false,
            null,
            null);

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
            true,
            now,
            new RefreshTokenId(UUID.randomUUID()));

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
            false,
            null,
            null);

    var result = token.isUsable(now);

    assertThat(result).isFalse();
  }

  @Test
  void should_be_replaced_within_grace_period_when_revoked_recently_with_a_successor() {
    var now = Instant.parse("2026-09-18T15:00:00Z");
    var token =
        new RefreshToken(
            new RefreshTokenId(UUID.randomUUID()),
            EmailAddress.of("user@test.org"),
            now.minusSeconds(3600),
            now.plusSeconds(3600),
            true,
            now.minusSeconds(2),
            new RefreshTokenId(UUID.randomUUID()));

    var result = token.isReplacedWithinGracePeriod(now, Duration.ofSeconds(10));

    assertThat(result).isTrue();
  }

  @Test
  void should_not_be_replaced_within_grace_period_once_it_has_elapsed() {
    var now = Instant.parse("2026-09-18T15:00:00Z");
    var token =
        new RefreshToken(
            new RefreshTokenId(UUID.randomUUID()),
            EmailAddress.of("user@test.org"),
            now.minusSeconds(3600),
            now.plusSeconds(3600),
            true,
            now.minusSeconds(20),
            new RefreshTokenId(UUID.randomUUID()));

    var result = token.isReplacedWithinGracePeriod(now, Duration.ofSeconds(10));

    assertThat(result).isFalse();
  }

  @Test
  void should_not_be_replaced_within_grace_period_when_not_revoked() {
    var now = Instant.parse("2026-09-18T15:00:00Z");
    var token =
        new RefreshToken(
            new RefreshTokenId(UUID.randomUUID()),
            EmailAddress.of("user@test.org"),
            now.minusSeconds(60),
            now.plusSeconds(60),
            false,
            null,
            null);

    var result = token.isReplacedWithinGracePeriod(now, Duration.ofSeconds(10));

    assertThat(result).isFalse();
  }

  @Test
  void should_not_be_replaced_within_grace_period_when_revoked_without_a_successor() {
    var now = Instant.parse("2026-09-18T15:00:00Z");
    var token =
        new RefreshToken(
            new RefreshTokenId(UUID.randomUUID()),
            EmailAddress.of("user@test.org"),
            now.minusSeconds(60),
            now.plusSeconds(60),
            true,
            now.minusSeconds(2),
            null);

    var result = token.isReplacedWithinGracePeriod(now, Duration.ofSeconds(10));

    assertThat(result).isFalse();
  }
}
