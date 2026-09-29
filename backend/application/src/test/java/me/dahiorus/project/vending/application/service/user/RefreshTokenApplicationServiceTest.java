package me.dahiorus.project.vending.application.service.user;

import static java.time.Instant.now;
import static java.time.ZoneId.systemDefault;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mock.Strictness.LENIENT;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import me.dahiorus.project.vending.domain.exception.InvalidRefreshToken;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.RefreshToken;
import me.dahiorus.project.vending.domain.user.entity.RefreshTokenId;
import me.dahiorus.project.vending.domain.user.port.RefreshTokenRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenApplicationServiceTest {

  @Mock RefreshTokenRepositoryPort refreshTokenRepository;

  @Mock(strictness = LENIENT)
  Clock clock;

  Instant fixedNow;

  RefreshTokenApplicationService refreshTokenApplicationService;

  @BeforeEach
  void setUpClock() {
    var fixedClock = Clock.fixed(now(), systemDefault());
    fixedNow = fixedClock.instant();
    doReturn(fixedClock.instant()).when(clock).instant();
    doReturn(fixedClock.getZone()).when(clock).getZone();
    refreshTokenApplicationService =
        new RefreshTokenApplicationService(refreshTokenRepository, clock, Duration.ofSeconds(10));
  }

  @Test
  void should_save_refresh_token() {
    var refreshTokenId = new RefreshTokenId(randomUUID());
    var refreshToken = refreshToken(refreshTokenId, fixedNow.plusSeconds(60), false);
    given(refreshTokenRepository.create(refreshToken)).willReturn(refreshToken);

    var result = refreshTokenApplicationService.save(refreshToken);

    assertThat(result).isEqualTo(refreshToken);
    then(refreshTokenRepository).should().create(refreshToken);
  }

  @Test
  void should_rotate_refresh_token_when_presented_token_is_usable() {
    var presentedId = new RefreshTokenId(randomUUID());
    var presentedToken = refreshToken(presentedId, fixedNow.plusSeconds(60), false);
    var replacementId = new RefreshTokenId(randomUUID());
    var replacement = refreshToken(replacementId, fixedNow.plusSeconds(120), false);
    given(refreshTokenRepository.find(presentedId)).willReturn(Optional.of(presentedToken));
    given(refreshTokenRepository.create(replacement)).willReturn(replacement);

    var result = refreshTokenApplicationService.rotate(presentedId, replacement);

    assertThat(result).isEqualTo(replacement);
    then(refreshTokenRepository).should().find(presentedId);
    then(refreshTokenRepository).should().create(replacement);
    then(refreshTokenRepository).should().markReplaced(presentedId, replacementId, fixedNow);
  }

  @Test
  void should_throw_exception_when_presented_token_is_unknown() {
    var presentedId = new RefreshTokenId(randomUUID());
    var replacement =
        refreshToken(new RefreshTokenId(randomUUID()), fixedNow.plusSeconds(120), false);
    given(refreshTokenRepository.find(presentedId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> refreshTokenApplicationService.rotate(presentedId, replacement))
        .isInstanceOf(InvalidRefreshToken.class);
    then(refreshTokenRepository).should().find(presentedId);
    then(refreshTokenRepository).should(never()).markReplaced(any(), any(), any());
    then(refreshTokenRepository).should(never()).create(replacement);
  }

  @Test
  void should_throw_exception_when_presented_token_is_expired() {
    var presentedId = new RefreshTokenId(randomUUID());
    var presentedToken = refreshToken(presentedId, fixedNow.minusSeconds(1), false);
    var replacement =
        refreshToken(new RefreshTokenId(randomUUID()), fixedNow.plusSeconds(120), false);
    given(refreshTokenRepository.find(presentedId)).willReturn(Optional.of(presentedToken));

    assertThatThrownBy(() -> refreshTokenApplicationService.rotate(presentedId, replacement))
        .isInstanceOf(InvalidRefreshToken.class);
    then(refreshTokenRepository).should().find(presentedId);
    then(refreshTokenRepository).should(never()).markReplaced(any(), any(), any());
    then(refreshTokenRepository).should(never()).create(replacement);
  }

  @Test
  void should_throw_exception_when_presented_token_was_revoked_without_a_successor() {
    var presentedId = new RefreshTokenId(randomUUID());
    var presentedToken =
        new RefreshToken(
            presentedId,
            EmailAddress.of("user@test.org"),
            fixedNow.minusSeconds(60),
            fixedNow.plusSeconds(60),
            true,
            null,
            null);
    var replacement =
        refreshToken(new RefreshTokenId(randomUUID()), fixedNow.plusSeconds(120), false);
    given(refreshTokenRepository.find(presentedId)).willReturn(Optional.of(presentedToken));

    assertThatThrownBy(() -> refreshTokenApplicationService.rotate(presentedId, replacement))
        .isInstanceOf(InvalidRefreshToken.class);
    then(refreshTokenRepository).should().find(presentedId);
    then(refreshTokenRepository).should(never()).markReplaced(any(), any(), any());
    then(refreshTokenRepository).should(never()).create(replacement);
  }

  @Test
  void
      should_throw_exception_when_presented_token_was_revoked_and_its_reuse_grace_period_has_elapsed() {
    var presentedId = new RefreshTokenId(randomUUID());
    var successorId = new RefreshTokenId(randomUUID());
    var presentedToken =
        new RefreshToken(
            presentedId,
            EmailAddress.of("user@test.org"),
            fixedNow.minusSeconds(60),
            fixedNow.plusSeconds(60),
            true,
            fixedNow.minusSeconds(20),
            successorId);
    var replacement =
        refreshToken(new RefreshTokenId(randomUUID()), fixedNow.plusSeconds(120), false);
    given(refreshTokenRepository.find(presentedId)).willReturn(Optional.of(presentedToken));

    assertThatThrownBy(() -> refreshTokenApplicationService.rotate(presentedId, replacement))
        .isInstanceOf(InvalidRefreshToken.class);
    then(refreshTokenRepository).should().find(presentedId);
    then(refreshTokenRepository).should(never()).markReplaced(any(), any(), any());
    then(refreshTokenRepository).should(never()).create(replacement);
  }

  @Test
  void
      should_return_the_successor_when_the_presented_token_was_replaced_within_the_reuse_grace_period() {
    var presentedId = new RefreshTokenId(randomUUID());
    var successorId = new RefreshTokenId(randomUUID());
    var presentedToken =
        new RefreshToken(
            presentedId,
            EmailAddress.of("user@test.org"),
            fixedNow.minusSeconds(60),
            fixedNow.plusSeconds(60),
            true,
            fixedNow.minusSeconds(2),
            successorId);
    var successor = refreshToken(successorId, fixedNow.plusSeconds(120), false);
    var replacement =
        refreshToken(new RefreshTokenId(randomUUID()), fixedNow.plusSeconds(120), false);
    given(refreshTokenRepository.find(presentedId)).willReturn(Optional.of(presentedToken));
    given(refreshTokenRepository.find(successorId)).willReturn(Optional.of(successor));

    var result = refreshTokenApplicationService.rotate(presentedId, replacement);

    assertThat(result).isEqualTo(successor);
    then(refreshTokenRepository).should(never()).markReplaced(any(), any(), any());
    then(refreshTokenRepository).should(never()).create(replacement);
  }

  @Test
  void should_delegate_refresh_token_revocation() {
    var refreshTokenId = new RefreshTokenId(randomUUID());

    refreshTokenApplicationService.revoke(refreshTokenId);

    then(refreshTokenRepository).should().revoke(refreshTokenId);
  }

  private static RefreshToken refreshToken(
      final RefreshTokenId id, final Instant expiresAt, final boolean revoked) {
    return new RefreshToken(
        id,
        EmailAddress.of("user@test.org"),
        expiresAt.minusSeconds(60),
        expiresAt,
        revoked,
        null,
        null);
  }
}
