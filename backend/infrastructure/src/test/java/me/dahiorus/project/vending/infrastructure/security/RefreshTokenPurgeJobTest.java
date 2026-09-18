package me.dahiorus.project.vending.infrastructure.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import me.dahiorus.project.vending.domain.user.port.RefreshTokenRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RefreshTokenPurgeJobTest {

  @Mock RefreshTokenRepositoryPort repository;

  @InjectMocks RefreshTokenPurgeJob job;

  @Test
  void should_delete_expired_refresh_tokens() {
    job.purgeExpiredRefreshTokens();

    verify(repository).deleteExpiredBefore(any());
  }
}
