package me.dahiorus.project.vending.infrastructure.jpa.repository.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.util.UUID;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.RefreshToken;
import me.dahiorus.project.vending.domain.user.entity.RefreshTokenId;
import org.springframework.data.domain.Persistable;

@Entity
@Table(
    name = "refresh_token",
    indexes = {
      @Index(columnList = "username", name = "IDX_REFRESH_TOKEN_USERNAME"),
      @Index(columnList = "expires_at", name = "IDX_REFRESH_TOKEN_EXPIRES_AT")
    })
public class JpaRefreshToken implements Persistable<UUID> {

  @Id
  @Column(updatable = false, nullable = false)
  private UUID id;

  @Transient private boolean isNew = true;

  @Column(nullable = false)
  private String username;

  @Column(nullable = false)
  private Instant issuedAt;

  @Column(nullable = false)
  private Instant expiresAt;

  @Column(nullable = false)
  private boolean revoked;

  public UUID getId() {
    return id;
  }

  public void setId(final UUID id) {
    this.id = id;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public void setRevoked(final boolean revoked) {
    this.revoked = revoked;
  }

  @Override
  public boolean isNew() {
    return isNew;
  }

  @PostLoad
  @PostPersist
  void markNotNew() {
    isNew = false;
  }

  public RefreshToken toDomain() {
    return new RefreshToken(
        new RefreshTokenId(id), EmailAddress.of(username), issuedAt, expiresAt, revoked);
  }

  public static JpaRefreshToken fromDomain(final RefreshToken refreshToken) {
    var jpaRefreshToken = new JpaRefreshToken();
    jpaRefreshToken.id = refreshToken.id().value();
    jpaRefreshToken.username = refreshToken.username().value();
    jpaRefreshToken.issuedAt = refreshToken.issuedAt();
    jpaRefreshToken.expiresAt = refreshToken.expiresAt();
    jpaRefreshToken.revoked = refreshToken.revoked();

    return jpaRefreshToken;
  }
}
