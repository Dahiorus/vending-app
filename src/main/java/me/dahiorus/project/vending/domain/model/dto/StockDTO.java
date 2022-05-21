package me.dahiorus.project.vending.domain.model.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.dahiorus.project.vending.domain.model.Stock;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StockDTO extends AbstractDTO<Stock>
{
  @JsonIgnore
  @Getter
  @Setter
  private UUID vendingMachineId;

  @JsonIgnore
  @Getter
  @Setter
  private UUID itemId;

  @Getter
  @Setter
  private String itemName;

  @Getter
  @Setter
  private Integer quantity;

  @Override
  public Class<Stock> getEntityClass()
  {
    return Stock.class;
  }
}
