package me.dahiorus.project.vending.infrastructure.rest.assembler;

import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.VENDING_MACHINE;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.CardSystemStatus;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.ChangeSystemStatus;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.PowerStatus;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineStatus.WorkingStatus;
import me.dahiorus.project.vending.infrastructure.rest.entity.machine.VendingMachineStatusReportDto;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;

class VendingMachineStatusReportDtoModelAssemblerTest {
  private final VendingMachineStatusReportDtoModelAssembler assembler =
      new VendingMachineStatusReportDtoModelAssembler();

  @Test
  void should_add_vending_machine_link() {
    // Given
    var vendingMachineId = UUID.randomUUID();
    var statusReport =
        new VendingMachineStatusReportDto(
            vendingMachineId,
            "SN-001",
            LocalDateTime.now(),
            5,
            PowerStatus.POWER_ON,
            WorkingStatus.WORKING,
            CardSystemStatus.OK,
            CardSystemStatus.OK,
            ChangeSystemStatus.NORMAL);
    var resource = EntityModel.of(statusReport);

    // When
    assembler.addLinks(resource);

    // Then
    assertThat(resource.getRequiredLink(VENDING_MACHINE).getHref())
        .endsWith("/api/v1/vending-machines/" + vendingMachineId);
  }

  @Test
  void should_do_nothing_on_collection_model() {
    // Given
    var resources = CollectionModel.of(List.<EntityModel<VendingMachineStatusReportDto>>of());

    // When
    assembler.addLinks(resources);

    // Then
    assertThat(resources.getLinks()).isEmpty();
  }
}
