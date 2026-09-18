package me.dahiorus.project.vending.infrastructure.jpa.repository.user;

import java.time.Instant;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface RefreshTokenJpaRepository extends JpaRepository<JpaRefreshToken, UUID> {
  void deleteAllByExpiresAtBefore(Instant instant);
}
