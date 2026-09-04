package me.dahiorus.project.vending.infrastructure.jpa.entity;

import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.UUID.randomUUID;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;

@MappedSuperclass
@DynamicInsert
@DynamicUpdate
public abstract class JpaEntity {
  @Id
  @Column(updatable = false)
  private UUID id;

  @CreatedDate
  @Column(updatable = false, nullable = false)
  private LocalDateTime createdAt;

  @PrePersist
  public void prePersist() {
    if (id == null) {
      id = randomUUID();
    }
    if (createdAt == null) {
      createdAt = now().truncatedTo(MILLIS);
    }
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  protected void setCreatedAt(final LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "[id=" + id + ", createdAt=" + createdAt + "]";
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    JpaEntity jpaEntity = (JpaEntity) o;
    return Objects.equals(id, jpaEntity.id);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
