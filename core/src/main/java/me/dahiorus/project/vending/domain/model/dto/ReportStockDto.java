package me.dahiorus.project.vending.domain.model.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.dahiorus.project.vending.domain.model.ReportStock;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ReportStockDto extends AbstractDto<ReportStock>
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
