package me.dahiorus.project.vending.core.service.impl;

import static me.dahiorus.project.vending.core.service.validation.FieldValidationError.fieldError;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ItemMissing;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.manager.impl.VendingMachineManager;
import me.dahiorus.project.vending.core.model.Item;
import me.dahiorus.project.vending.core.model.ItemType;
import me.dahiorus.project.vending.core.model.Stock;
import me.dahiorus.project.vending.core.model.VendingMachine;
import me.dahiorus.project.vending.core.model.dto.CommentDTO;
import me.dahiorus.project.vending.core.model.dto.ItemDTO;
import me.dahiorus.project.vending.core.model.dto.SaleDTO;
import me.dahiorus.project.vending.core.service.validation.ValidationResults;
import me.dahiorus.project.vending.core.service.validation.impl.CommentDtoValidator;
import me.dahiorus.project.vending.core.service.validation.impl.VendingMachineDtoValidator;

@ExtendWith(MockitoExtension.class)
class VendingMachineDtoServiceImplTest
{
  @Mock
  VendingMachineManager manager;

  @Mock
  VendingMachineDtoValidator dtoValidator;

  @Mock
  CommentDtoValidator commentDtoValidator;

  VendingMachineDtoServiceImpl controller;

  VendingMachine buildMachine(final UUID id, final ItemType itemType)
  {
    VendingMachine machine = new VendingMachine();
    machine.setId(id);
    machine.setType(itemType);

    return machine;
  }

  void addStock(final VendingMachine machine, final Item item, final long quantity)
  {
    machine.getStocks()
      .clear();

    Stock stock = new Stock();
    stock.setId(UUID.randomUUID());
    stock.setItem(item);
    stock.setQuantity(quantity);
    machine.addStock(stock);
  }

  @BeforeEach
  void setUp()
  {
    controller = new VendingMachineDtoServiceImpl(manager, new DtoMapperImpl(), dtoValidator, commentDtoValidator);
  }

  @Nested
  class ProvisionStockTests
  {
    @Test
    void provisionNewItem() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID(), ItemType.COLD_BAVERAGE);
      ItemDTO itemDto = new ItemDTO();
      itemDto.setName("CocaCola");
      itemDto.setType(machine.getType());

      when(manager.read(machine.getId())).thenReturn(machine);
      when(manager.update(machine)).thenReturn(machine);

      assertThatNoException().isThrownBy(() -> controller.provisionStock(machine.getId(), itemDto, 10L));

      Stock stock = machine.getStocks()
        .get(0);
      assertAll(() -> assertThat(machine.getLastIntervention()).isNotNull(),
          () -> assertThat(stock.getQuantity()).isEqualTo(10L),
          () -> assertThat(stock).extracting(Stock::getItem)
            .hasFieldOrPropertyWithValue("name", itemDto.getName()));
    }

    @Test
    void provisionExistingItem() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID(), ItemType.COLD_BAVERAGE);
      Item item = new Item();
      item.setId(UUID.randomUUID());
      item.setName("CocaCola");
      addStock(machine, item, 5L);

      when(manager.read(machine.getId())).thenReturn(machine);
      when(manager.update(machine)).thenReturn(machine);

      ItemDTO itemDto = new ItemDTO();
      itemDto.setId(item.getId());
      itemDto.setType(machine.getType());

      assertThatNoException().isThrownBy(() -> controller.provisionStock(machine.getId(), itemDto, 10L));
      assertAll(() -> assertThat(machine.getLastIntervention()).isNotNull(),
          () -> assertThat(machine.getQuantityInStock(item)).isEqualTo(15L));
    }

    @Test
    void cannotProvisionOtherItemType() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID(), ItemType.COLD_BAVERAGE);
      ItemDTO itemDto = new ItemDTO();
      itemDto.setName("Lays");
      itemDto.setType(ItemType.FOOD);

      when(manager.read(machine.getId())).thenReturn(machine);

      assertThatExceptionOfType(ValidationException.class)
        .isThrownBy(() -> controller.provisionStock(machine.getId(), itemDto, 10L));
      verify(manager, never()).update(machine);
    }

    @Test
    void cannotProvisionUnknownMachine() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(manager.read(id)).thenThrow(new EntityNotFound(VendingMachine.class, id));

      assertThatExceptionOfType(EntityNotFound.class)
        .isThrownBy(() -> controller.provisionStock(id, new ItemDTO(), 15L));
      verify(manager, never()).update(any());
    }
  }

  @Nested
  class PurchaseItemTests
  {
    @Test
    void purchaseItem() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID(), ItemType.FOOD);
      Item item = buildItem("Chips", machine.getType(), 1.5);
      addStock(machine, item, 10L);

      when(manager.read(machine.getId())).thenReturn(machine);
      when(manager.update(machine)).thenReturn(machine);

      ItemDTO itemToPurchase = new ItemDTO();
      itemToPurchase.setId(item.getId());
      itemToPurchase.setPrice(item.getPrice());

      SaleDTO sale = controller.purchaseItem(machine.getId(), itemToPurchase);

      assertAll(
          () -> assertThat(sale.getAmount()).isEqualTo(itemToPurchase.getPrice()),
          () -> assertThat(machine.getSales()).isNotEmpty());
    }

    @Test
    void purchaseItemFromEmptyMachine() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID(), ItemType.FOOD);
      Item item = buildItem("Chips", machine.getType(), 1.5);
      addStock(machine, item, 0L);

      when(manager.read(machine.getId())).thenReturn(machine);

      ItemDTO itemToPurchase = new ItemDTO();
      itemToPurchase.setId(item.getId());

      assertThatExceptionOfType(ItemMissing.class)
        .isThrownBy(() -> controller.purchaseItem(machine.getId(), itemToPurchase));
      verify(manager, never()).update(machine);
    }

    @Test
    void purchaseUnknownItem() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID(), ItemType.FOOD);
      when(manager.read(machine.getId())).thenReturn(machine);

      ItemDTO itemToPurchase = new ItemDTO();
      itemToPurchase.setId(UUID.randomUUID());

      assertThatExceptionOfType(ItemMissing.class)
        .isThrownBy(() -> controller.purchaseItem(machine.getId(), itemToPurchase));
      verify(manager, never()).update(machine);
    }

    @Test
    void purchaseFromUnknownMachine() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(manager.read(id)).thenThrow(new EntityNotFound(VendingMachine.class, id));

      ItemDTO itemToPurchase = new ItemDTO();
      itemToPurchase.setId(UUID.randomUUID());

      assertThatExceptionOfType(EntityNotFound.class)
        .isThrownBy(() -> controller.purchaseItem(id, itemToPurchase));
      verify(manager, never()).update(any());
    }

    Item buildItem(final String name, final ItemType type, final Double price)
    {
      Item item = new Item();
      item.setId(UUID.randomUUID());
      item.setName(name);
      item.setType(type);
      item.setPrice(price);

      return item;
    }
  }

  @Nested
  class CommentTests
  {
    @Test
    void commentMachine() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID(), ItemType.FOOD);
      when(manager.read(machine.getId())).thenReturn(machine);
      when(manager.update(machine)).thenReturn(machine);

      CommentDTO comment = new CommentDTO();
      comment.setRate(5);
      comment.setContent("This is a comment");
      when(commentDtoValidator.validate(comment)).thenReturn(new ValidationResults());

      assertThatNoException().isThrownBy(() -> controller.comment(machine.getId(), comment));
      assertThat(machine.getComments()).isNotEmpty();
    }

    @Test
    void commentHasInvalidValue() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID(), ItemType.FOOD);
      when(manager.read(machine.getId())).thenReturn(machine);

      CommentDTO comment = new CommentDTO();
      comment.setContent("This is a comment");

      when(commentDtoValidator.validate(comment)).then(invoc -> {
        ValidationResults results = new ValidationResults();
        results.addError(fieldError("rate", "validation.constraints.comment.rate_interval",
            "Error from test"));
        return results;
      });

      assertThatExceptionOfType(ValidationException.class)
        .isThrownBy(() -> controller.comment(machine.getId(), comment));
      verify(manager, never()).update(machine);
    }

    @Test
    void commentUnknownMachine() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(manager.read(id)).thenThrow(new EntityNotFound(VendingMachine.class, id));

      assertThatExceptionOfType(EntityNotFound.class)
        .isThrownBy(() -> controller.comment(id, new CommentDTO()));
      verify(manager, never()).update(any());
    }
  }
}
