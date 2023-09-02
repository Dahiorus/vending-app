package me.dahiorus.project.vending.domain.model.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.dahiorus.project.vending.domain.model.CardSystemStatus;
import me.dahiorus.project.vending.domain.model.ChangeSystemStatus;
import me.dahiorus.project.vending.domain.model.PowerStatus;
import me.dahiorus.project.vending.domain.model.Report;
import me.dahiorus.project.vending.domain.model.WorkingStatus;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = "reportStocks")
public class ReportDto extends AbstractDto<Report>
{
  @Getter
  @Setter
  private String machineSerialNumber;

  @Getter
  @Setter
  private Integer mesuredTemperature;

  @Getter
  @Setter
  private BigDecimal totalSaleAmount;

  @Getter
  @Setter
  private PowerStatus powerStatus;

  @Getter
  @Setter
  private WorkingStatus workingStatus;

  @Parameter(hidden = true)
  @Getter
  @Setter
  private List<ReportStockDto> reportStocks = new ArrayList<>();

  @Getter
  @Setter
  private CardSystemStatus rfidStatus;

  @Getter
  @Setter
  private CardSystemStatus smartCardStatus;

  @Getter
  @Setter
  private ChangeSystemStatus changeMoneyStatus;

  @Override
  public Class<Report> getEntityClass()
  {
    return Report.class;
  }
}
