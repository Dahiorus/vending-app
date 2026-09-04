package me.dahiorus.project.vending.domain.machine.usecase;

import static me.dahiorus.project.vending.domain.item.entity.ItemType.SNACK;
import static me.dahiorus.project.vending.fixture.AddressFixture.anAddress;
import static me.dahiorus.project.vending.fixture.VendingMachineFixture.aVendingMachine;
import static me.dahiorus.project.vending.fixture.VendingMachineStatusFixture.aVendingMachineStatus;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import me.dahiorus.project.vending.domain.exception.InvalidBusinessObject;
import me.dahiorus.project.vending.domain.machine.port.VendingMachineRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class VendingMachineValidatorTest {

  @Mock VendingMachineRepositoryPort vendingMachineRepository;
  @InjectMocks VendingMachineValidator validator;

  @Test
  void should_be_valid() {
    var vendingMachine =
        aVendingMachine()
            .serialNumber("VM-12345")
            .itemType(SNACK)
            .address(anAddress().build())
            .status(aVendingMachineStatus().build())
            .build();

    assertThatCode(() -> validator.validate(vendingMachine)).doesNotThrowAnyException();
  }

  @Test
  void should_be_invalid_given_duplicate_serial_number() {
    var vendingMachine =
        aVendingMachine()
            .serialNumber("VM-12345")
            .itemType(SNACK)
            .address(anAddress().build())
            .status(aVendingMachineStatus().build())
            .build();

    given(vendingMachineRepository.findDuplicateOf(vendingMachine))
        .willReturn(Optional.of(vendingMachine));

    assertThatThrownBy(() -> validator.validate(vendingMachine))
        .isInstanceOf(InvalidBusinessObject.class)
        .hasMessageContaining("1 error(s) found in invalid business object: " + vendingMachine);
  }
}
