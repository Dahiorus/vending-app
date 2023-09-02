package me.dahiorus.project.vending.domain.model.dto;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.CacheStrategy;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.dahiorus.project.vending.domain.model.AbstractEntity;

@EqualsAndHashCode(of = "id", cacheStrategy = CacheStrategy.LAZY)
@ToString(of = { "id", "createdAt" })
public abstract class AbstractDto<E extends AbstractEntity>
{
  @Parameter(hidden = true)
  @Schema(accessMode = AccessMode.READ_ONLY)
  @Getter
  @Setter
  private UUID id;

  @JsonIgnore
  @Getter
  @Setter
  private Instant createdAt;

  @JsonIgnore
  public abstract Class<E> getEntityClass();
}
