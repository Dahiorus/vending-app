package me.dahiorus.project.vending.core.model.dto;

import java.util.ArrayList;
import java.util.List;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.dahiorus.project.vending.core.model.CardSystemStatus;
import me.dahiorus.project.vending.core.model.ChangeSystemStatus;
import me.dahiorus.project.vending.core.model.PowerStatus;
import me.dahiorus.project.vending.core.model.Report;
import me.dahiorus.project.vending.core.model.WorkingStatus;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true, exclude = "reportStocks")
public class ReportDTO extends AbstractDTO<Report>
{
  @Getter
  @Setter
  private String machineSerialNumber;

  @Getter
  @Setter
  private Integer mesuredTemperature;

  @Getter
  @Setter
  private Double totalSaleAmount;

  @Getter
  @Setter
  private PowerStatus powerStatus;

  @Getter
  @Setter
  private WorkingStatus workingStatus;

  @Parameter(hidden = true)
  @Getter
  @Setter
  private List<ReportStockDTO> reportStocks = new ArrayList<>();

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
