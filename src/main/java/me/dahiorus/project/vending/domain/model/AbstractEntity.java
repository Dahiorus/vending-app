package me.dahiorus.project.vending.domain.model;

import java.time.Instant;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;

import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.CacheStrategy;

@MappedSuperclass
@DynamicInsert
@DynamicUpdate
@EqualsAndHashCode(of = "id", cacheStrategy = CacheStrategy.LAZY)
public abstract class AbstractEntity
{
  private UUID id;

  private Instant createdAt;

  @PrePersist
  public void prePersist()
  {
    // the ID can be manually set
    if (id == null)
    {
      id = UUID.randomUUID();
    }

    createdAt = Instant.now();
  }

  @Id
  @Column(insertable = true, updatable = false)
  public UUID getId()
  {
    return id;
  }

  public void setId(final UUID id)
  {
    this.id = id;
  }

  @CreatedDate
  @Column(updatable = false, nullable = false)
  public Instant getCreatedAt()
  {
    return createdAt;
  }

  public void setCreatedAt(final Instant createdAt)
  {
    this.createdAt = createdAt;
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[id=" + id + ", createdAt=" + createdAt + "]";
  }
}
