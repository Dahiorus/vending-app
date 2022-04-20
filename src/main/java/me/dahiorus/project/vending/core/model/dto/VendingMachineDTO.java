package me.dahiorus.project.vending.core.model.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.dahiorus.project.vending.core.model.CardSystemStatus;
import me.dahiorus.project.vending.core.model.ChangeSystemStatus;
import me.dahiorus.project.vending.core.model.ItemType;
import me.dahiorus.project.vending.core.model.PowerStatus;
import me.dahiorus.project.vending.core.model.VendingMachine;
import me.dahiorus.project.vending.core.model.WorkingStatus;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class VendingMachineDTO extends AbstractDTO<VendingMachine>
{
  @Getter
  @Setter
  private String serialNumber;

  @Getter
  @Setter
  private Double latitude;

  @Getter
  @Setter
  private Double longitude;

  @Getter
  @Setter
  private String place;

  @Getter
  @Setter
  private String address;

  @Schema(accessMode = AccessMode.READ_ONLY)
  @Getter
  @Setter
  private Instant lastIntervention;

  @Getter
  @Setter
  private Integer temperature;

  @Getter
  @Setter
  private ItemType type;

  @Getter
  @Setter
  private PowerStatus powerStatus;

  @Getter
  @Setter
  private WorkingStatus workingStatus;

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
  public Class<VendingMachine> getEntityClass()
  {
    return VendingMachine.class;
  }
}
