package me.dahiorus.project.vending.application.service.user;

import static java.time.Instant.now;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

import java.time.Instant;
import java.util.Optional;
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
  void should_save_refresh_token() {
    var refreshTokenId = new RefreshTokenId(randomUUID());
    var refreshToken = refreshToken(refreshTokenId, now().plusSeconds(60), false);
    given(refreshTokenRepository.create(refreshToken)).willReturn(refreshToken);

    var result = refreshTokenApplicationService.save(refreshToken);

    assertThat(result).isEqualTo(refreshToken);
    then(refreshTokenRepository).should().create(refreshToken);
  }

  @Test
  void should_rotate_refresh_token_when_presented_token_is_usable() {
    var presentedId = new RefreshTokenId(randomUUID());
    var presentedToken = refreshToken(presentedId, now().plusSeconds(60), false);
    var replacement = refreshToken(new RefreshTokenId(randomUUID()), now().plusSeconds(120), false);
    given(refreshTokenRepository.find(presentedId)).willReturn(Optional.of(presentedToken));
    given(refreshTokenRepository.create(replacement)).willReturn(replacement);

    var result = refreshTokenApplicationService.rotate(presentedId, replacement);

    assertThat(result).isEqualTo(replacement);
    then(refreshTokenRepository).should().find(presentedId);
    then(refreshTokenRepository).should().revoke(presentedId);
    then(refreshTokenRepository).should().create(replacement);
  }

  @Test
  void should_throw_exception_when_presented_token_is_unknown() {
    var presentedId = new RefreshTokenId(randomUUID());
    var replacement = refreshToken(new RefreshTokenId(randomUUID()), now().plusSeconds(120), false);
    given(refreshTokenRepository.find(presentedId)).willReturn(Optional.empty());

    assertThatThrownBy(() -> refreshTokenApplicationService.rotate(presentedId, replacement))
        .isInstanceOf(InvalidRefreshToken.class);
    then(refreshTokenRepository).should().find(presentedId);
    then(refreshTokenRepository).should(never()).revoke(presentedId);
    then(refreshTokenRepository).should(never()).create(replacement);
  }

  @Test
  void should_throw_exception_when_presented_token_is_expired() {
    var presentedId = new RefreshTokenId(randomUUID());
    var presentedToken = refreshToken(presentedId, now().minusSeconds(1), false);
    var replacement = refreshToken(new RefreshTokenId(randomUUID()), now().plusSeconds(120), false);
    given(refreshTokenRepository.find(presentedId)).willReturn(Optional.of(presentedToken));

    assertThatThrownBy(() -> refreshTokenApplicationService.rotate(presentedId, replacement))
        .isInstanceOf(InvalidRefreshToken.class);
    then(refreshTokenRepository).should().find(presentedId);
    then(refreshTokenRepository).should(never()).revoke(presentedId);
    then(refreshTokenRepository).should(never()).create(replacement);
  }

  @Test
  void should_throw_exception_when_presented_token_is_already_revoked() {
    var presentedId = new RefreshTokenId(randomUUID());
    var presentedToken = refreshToken(presentedId, now().plusSeconds(60), true);
    var replacement = refreshToken(new RefreshTokenId(randomUUID()), now().plusSeconds(120), false);
    given(refreshTokenRepository.find(presentedId)).willReturn(Optional.of(presentedToken));

    assertThatThrownBy(() -> refreshTokenApplicationService.rotate(presentedId, replacement))
        .isInstanceOf(InvalidRefreshToken.class);
    then(refreshTokenRepository).should().find(presentedId);
    then(refreshTokenRepository).should(never()).revoke(presentedId);
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
        id, EmailAddress.of("user@test.org"), expiresAt.minusSeconds(60), expiresAt, revoked);
  }
}
