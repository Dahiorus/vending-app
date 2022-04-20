package me.dahiorus.project.vending.core.model.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.dahiorus.project.vending.core.model.Sale;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SaleDTO extends AbstractDTO<Sale>
{
  @Getter
  @Setter
  private Double amount;

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
