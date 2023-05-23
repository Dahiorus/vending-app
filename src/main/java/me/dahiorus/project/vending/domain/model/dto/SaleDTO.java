package me.dahiorus.project.vending.domain.model.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.dahiorus.project.vending.domain.model.Sale;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SaleDTO extends AbstractDTO<Sale>
{
  @Getter
  @Setter
  private BigDecimal amount;

  @JsonIgnore
  @Getter
  @Setter
  private UUID machineId;

  @Override
  public Class<Sale> getEntityClass()
  {
    return Sale.class;
  }
}
