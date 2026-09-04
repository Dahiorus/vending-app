package me.dahiorus.project.vending.infrastructure.rest.entity.machine;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import me.dahiorus.project.vending.domain.machine.entity.ClientOrder;

public record ClientOrderDto(
    @JsonIgnore UUID vendingMachineId,
    @JsonIgnore UUID itemId,
    BigDecimal amount,
    LocalDateTime createdAt) {
  public static ClientOrderDto fromDomain(ClientOrder clientOrder) {
    return new ClientOrderDto(
        clientOrder.vendingMachine().id().value(),
        clientOrder.orderedItemId().value(),
        clientOrder.orderedItemPrice(),
        clientOrder.orderAt());
  }
}
