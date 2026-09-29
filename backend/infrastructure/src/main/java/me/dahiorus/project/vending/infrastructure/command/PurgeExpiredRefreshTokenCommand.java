package me.dahiorus.project.vending.infrastructure.command;

import static java.time.Instant.now;

import java.time.Clock;
import me.dahiorus.project.vending.domain.user.port.RefreshTokenRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * CLI command purging expired refresh tokens on demand, in addition to {@link
 * me.dahiorus.project.vending.infrastructure.security.RefreshTokenPurgeJob}'s daily schedule. Runs
 * only when the {@code --purge-expired-refresh-tokens} option is passed on the command line; {@link
 * me.dahiorus.project.vending.VendingApplication} starts the application in non-web mode and exits
 * right after the runners complete in that case.
 */
@Component
public class PurgeExpiredRefreshTokenCommand implements ApplicationRunner {

  public static final String OPTION_NAME = "purge-expired-refresh-tokens";

  private static final Logger logger =
      LoggerFactory.getLogger(PurgeExpiredRefreshTokenCommand.class);

  private final RefreshTokenRepositoryPort refreshTokenRepository;
  private final Clock clock;

  public PurgeExpiredRefreshTokenCommand(
      final RefreshTokenRepositoryPort refreshTokenRepository, Clock clock) {
    this.refreshTokenRepository = refreshTokenRepository;
    this.clock = clock;
  }

  @Override
  public void run(final ApplicationArguments args) {
    if (!args.containsOption(OPTION_NAME)) {
      return;
    }

    logger.info("Purging expired refresh tokens...");
    refreshTokenRepository.deleteExpiredBefore(now(clock));
    logger.info("Expired refresh tokens purged.");
  }
}
