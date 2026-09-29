package me.dahiorus.project.vending.infrastructure.command;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import me.dahiorus.project.vending.domain.user.port.RefreshTokenRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

@ExtendWith(MockitoExtension.class)
class PurgeExpiredRefreshTokenCommandTest {

  @Mock RefreshTokenRepositoryPort refreshTokenRepository;

  @Mock ApplicationArguments args;

  @InjectMocks PurgeExpiredRefreshTokenCommand command;

  @Test
  void should_purge_expired_refresh_tokens_when_option_is_present() {
    when(args.containsOption(PurgeExpiredRefreshTokenCommand.OPTION_NAME)).thenReturn(true);

    command.run(args);

    verify(refreshTokenRepository).deleteExpiredBefore(any());
  }

  @Test
  void should_do_nothing_when_option_is_absent() {
    when(args.containsOption(PurgeExpiredRefreshTokenCommand.OPTION_NAME)).thenReturn(false);

    command.run(args);

    verify(refreshTokenRepository, never()).deleteExpiredBefore(any());
  }
}
