package me.dahiorus.project.vending.infrastructure.command;

import static java.time.Instant.now;
import static java.time.ZoneId.systemDefault;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mock.Strictness.LENIENT;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import me.dahiorus.project.vending.domain.user.port.RefreshTokenRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
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

  @Mock(strictness = LENIENT)
  Clock clock;

  @InjectMocks PurgeExpiredRefreshTokenCommand command;

  @BeforeEach
  void setUpClock() {
    var fixedClock = Clock.fixed(now(), systemDefault());
    doReturn(fixedClock.instant()).when(clock).instant();
    doReturn(fixedClock.getZone()).when(clock).getZone();
  }

  @Test
  void should_purge_expired_refresh_tokens_when_option_is_present() {
    when(args.containsOption(PurgeExpiredRefreshTokenCommand.OPTION_NAME)).thenReturn(true);

    command.run(args);

    verify(refreshTokenRepository).deleteExpiredBefore(now(clock));
  }

  @Test
  void should_do_nothing_when_option_is_absent() {
    when(args.containsOption(PurgeExpiredRefreshTokenCommand.OPTION_NAME)).thenReturn(false);

    command.run(args);

    verify(refreshTokenRepository, never()).deleteExpiredBefore(any());
  }
}
