package me.dahiorus.project.vending.application.service.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import me.dahiorus.project.vending.domain.exception.InvalidRefreshToken;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.RefreshToken;
import me.dahiorus.project.vending.domain.user.entity.RefreshTokenId;
import me.dahiorus.project.vending.domain.user.port.RefreshTokenRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenApplicationServiceTest {

  @Mock RefreshTokenRepositoryPort refreshTokenRepository;

  @InjectMocks RefreshTokenApplicationService refreshTokenApplicationService;

  @Test
  void should_rotate_refresh_token_when_presented_token_is_usable() {
    var presentedId = new RefreshTokenId(UUID.randomUUID());
    var presentedToken = refreshToken(presentedId, Instant.now().plusSeconds(60), false);
    var replacement = refreshToken(new RefreshTokenId(UUID.randomUUID()), Instant.now().plusSeconds(120), false);
    given(refreshTokenRepository.findById(presentedId)).willReturn(Optional.of(presentedToken));
    given(refreshTokenRepository.save(replacement)).willReturn(replacement);

    var result = refreshTokenApplicationService.rotate(presentedId, replacement);

    assertThat(result).isEqualTo(replacement);
    then(refreshTokenRepository).should().findById(presentedId);
    then(refreshTokenRepository).should().revoke(presentedId);
    then(refreshTokenRepository).should().save(replacement);
  }

  @Test
  void should_throw_exception_when_presented_token_is_unknown() {
    var presentedId = new RefreshTokenId(UUID.randomUUID());
    var replacement = refreshToken(new RefreshTokenId(UUID.randomUUID()), Instant.now().plusSeconds(120), false);
    given(refreshTokenRepository.findById(presentedId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> refreshTokenApplicationService.rotate(presentedId, replacement))
        .isInstanceOf(InvalidRefreshToken.class);
    then(refreshTokenRepository).should().findById(presentedId);
    then(refreshTokenRepository).should(never()).revoke(presentedId);
    then(refreshTokenRepository).should(never()).save(replacement);
  }

  @Test
  void should_throw_exception_when_presented_token_is_expired() {
    var presentedId = new RefreshTokenId(UUID.randomUUID());
    var presentedToken = refreshToken(presentedId, Instant.now().minusSeconds(1), false);
    var replacement = refreshToken(new RefreshTokenId(UUID.randomUUID()), Instant.now().plusSeconds(120), false);
    given(refreshTokenRepository.findById(presentedId)).willReturn(Optional.of(presentedToken));

    assertThatThrownBy(() -> refreshTokenApplicationService.rotate(presentedId, replacement))
        .isInstanceOf(InvalidRefreshToken.class);
    then(refreshTokenRepository).should().findById(presentedId);
    then(refreshTokenRepository).should(never()).revoke(presentedId);
    then(refreshTokenRepository).should(never()).save(replacement);
  }

  @Test
  void should_throw_exception_when_presented_token_is_already_revoked() {
    var presentedId = new RefreshTokenId(UUID.randomUUID());
    var presentedToken = refreshToken(presentedId, Instant.now().plusSeconds(60), true);
    var replacement = refreshToken(new RefreshTokenId(UUID.randomUUID()), Instant.now().plusSeconds(120), false);
    given(refreshTokenRepository.findById(presentedId)).willReturn(Optional.of(presentedToken));

    assertThatThrownBy(() -> refreshTokenApplicationService.rotate(presentedId, replacement))
        .isInstanceOf(InvalidRefreshToken.class);
    then(refreshTokenRepository).should().findById(presentedId);
    then(refreshTokenRepository).should(never()).revoke(presentedId);
    then(refreshTokenRepository).should(never()).save(replacement);
  }

  @Test
  void should_delegate_refresh_token_revocation() {
    var refreshTokenId = new RefreshTokenId(UUID.randomUUID());

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
        revoked);
  }
}
