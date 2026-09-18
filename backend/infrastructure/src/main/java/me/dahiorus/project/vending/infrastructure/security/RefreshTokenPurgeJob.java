package me.dahiorus.project.vending.infrastructure.security;

import java.time.Instant;
import me.dahiorus.project.vending.domain.user.port.RefreshTokenRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenPurgeJob {

  private static final Logger logger = LoggerFactory.getLogger(RefreshTokenPurgeJob.class);

  private final RefreshTokenRepositoryPort repository;

  public RefreshTokenPurgeJob(final RefreshTokenRepositoryPort repository) {
    this.repository = repository;
  }

  @Scheduled(cron = "0 0 3 * * *")
  public void purgeExpiredRefreshTokens() {
    logger.info("Purging expired refresh tokens");
    repository.deleteExpiredBefore(Instant.now());
  }
}
