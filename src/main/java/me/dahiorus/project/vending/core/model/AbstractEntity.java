package me.dahiorus.project.vending.core.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.CreatedDate;

@MappedSuperclass
@DynamicInsert
@DynamicUpdate
public abstract class AbstractEntity
{
  private UUID id;

  private Instant createdAt;

  @PrePersist
  public void prePersist()
  {
    createdAt = Instant.now();
  }

  @Id
  @GeneratedValue
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
  @Column(updatable = false)
  public Instant getCreatedAt()
  {
    return createdAt;
  }

  public void setCreatedAt(final Instant createdAt)
  {
    this.createdAt = createdAt;
  }

  @Override
  public int hashCode()
  {
    return Objects.hash(id);
  }

  @Override
  public boolean equals(final Object obj)
  {
    if (this == obj)
    {
      return true;
    }
    if (!(obj instanceof AbstractEntity))
    {
      return false;
    }
    AbstractEntity other = (AbstractEntity) obj;
    return Objects.equals(id, other.id);
  }

  @Override
  public String toString()
  {
    return getClass().getSimpleName() + "[id=" + id + ", createdAt=" + createdAt + "]";
  }
}
