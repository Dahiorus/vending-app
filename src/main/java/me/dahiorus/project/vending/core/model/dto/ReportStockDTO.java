package me.dahiorus.project.vending.core.model.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.dahiorus.project.vending.core.model.ReportStock;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ReportStockDTO extends AbstractDTO<ReportStock>
{
  @Getter
  @Setter
  private String itemName;

  @Getter
  @Setter
  private Integer quantity;

  @Override
  public Class<ReportStock> getEntityClass()
  {
    return ReportStock.class;
  }
}
