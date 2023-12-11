package me.dahiorus.project.vending.domain.model.dto;

import java.time.Instant;

import org.springframework.hateoas.server.core.Relation;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.dahiorus.project.vending.domain.model.Address;
import me.dahiorus.project.vending.domain.model.CardSystemStatus;
import me.dahiorus.project.vending.domain.model.ChangeSystemStatus;
import me.dahiorus.project.vending.domain.model.ItemType;
import me.dahiorus.project.vending.domain.model.PowerStatus;
import me.dahiorus.project.vending.domain.model.VendingMachine;
import me.dahiorus.project.vending.domain.model.WorkingStatus;

@Relation(collectionRelation = "elements")
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class VendingMachineDto extends AbstractDto<VendingMachine>
{
  @Getter
  @Setter
  private String serialNumber;

  @Getter
  @Setter
  private Address address = new Address();

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
