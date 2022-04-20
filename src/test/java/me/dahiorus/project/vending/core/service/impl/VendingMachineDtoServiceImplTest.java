package me.dahiorus.project.vending.core.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.validation.Errors;

import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.InvalidData;
import me.dahiorus.project.vending.core.exception.ItemMissing;
import me.dahiorus.project.vending.core.manager.impl.VendingMachineManager;
import me.dahiorus.project.vending.core.model.Item;
import me.dahiorus.project.vending.core.model.ItemType;
import me.dahiorus.project.vending.core.model.Stock;
import me.dahiorus.project.vending.core.model.VendingMachine;
import me.dahiorus.project.vending.core.model.dto.CommentDTO;
import me.dahiorus.project.vending.core.model.dto.ItemDTO;
import me.dahiorus.project.vending.core.model.dto.SaleDTO;
import me.dahiorus.project.vending.core.model.dto.StockDTO;

@ExtendWith(MockitoExtension.class)
class VendingMachineDtoServiceImplTest
{
  @Mock
  VendingMachineManager manager;

  @Mock
  VendingMachineDtoValidator dtoValidator;

  @Mock
  StockDtoValidator stockDtoValidator;

  @Mock
  CommentDtoValidator commentDtoValidator;

  VendingMachineDtoServiceImpl controller;

  @Captor
  ArgumentCaptor<StockDTO> stockArg;

  @Captor
  ArgumentCaptor<Errors> errorsArg;

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
    when(manager.getDomainClass()).thenCallRealMethod();
    controller = new VendingMachineDtoServiceImpl(manager, new DtoMapperImpl(), dtoValidator, stockDtoValidator,
        commentDtoValidator);
  }

  @Nested
  class ProvisionStockTests
  {
    @BeforeEach
    void setUp()
    {
      lenient().when(stockDtoValidator.supports(StockDTO.class))
        .thenReturn(true);
    }

    @Test
    void provisionNewItem() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID(), ItemType.COLD_BAVERAGE);
      ItemDTO itemDto = new ItemDTO();
      itemDto.setName("CocaCola");
      itemDto.setType(machine.getType());

      when(manager.read(machine.getId())).thenReturn(machine);
      doNothing().when(stockDtoValidator)
        .validate(stockArg.capture(), any());
      when(manager.update(machine)).thenReturn(machine);

      assertThatNoException().isThrownBy(() -> controller.provisionStock(machine.getId(), itemDto, 10L));
      assertAll(() -> assertThat(machine.getLastIntervention()).isNotNull(),
          () -> assertThat(stockArg.getValue()).hasFieldOrPropertyWithValue("itemName", itemDto.getName())
            .hasFieldOrPropertyWithValue("quantity", 10L));
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
      doNothing().when(stockDtoValidator)
        .validate(any(), any());
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

      assertThatExceptionOfType(InvalidData.class)
        .isThrownBy(() -> controller.provisionStock(machine.getId(), itemDto, 10L));
      verify(manager, never()).update(machine);
    }

    @Test
    void cannotProvisionUnknownItem() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID(), ItemType.COLD_BAVERAGE);
      ItemDTO itemDto = new ItemDTO();
      itemDto.setName("CocaCola");
      itemDto.setType(machine.getType());

      when(manager.read(machine.getId())).thenReturn(machine);
      doAnswer(invocation -> {
        invocation.getArgument(1, Errors.class)
          .rejectValue("itemId", "validation.constraints.stock.item_not_found",
              "Error from test");
        return null;
      }).when(stockDtoValidator)
        .validate(any(), errorsArg.capture());

      assertThatExceptionOfType(InvalidData.class)
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
      Item item = new Item();
      item.setId(UUID.randomUUID());
      item.setName("Chips");
      item.setType(machine.getType());
      item.setPrice(1.5);
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
      Item item = new Item();
      item.setId(UUID.randomUUID());
      item.setName("Chips");
      item.setType(machine.getType());
      item.setPrice(1.5);
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
  }

  @Nested
  class CommentTests
  {
    @BeforeEach
    void setUp()
    {
      lenient().when(commentDtoValidator.supports(CommentDTO.class))
        .thenReturn(true);
    }

    @Test
    void commentMachine() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID(), ItemType.FOOD);
      when(manager.read(machine.getId())).thenReturn(machine);
      when(manager.update(machine)).thenReturn(machine);

      CommentDTO comment = new CommentDTO();
      comment.setRate(5);
      comment.setContent("This is a comment");

      assertThatNoException().isThrownBy(() -> controller.comment(machine.getId(), comment));
      assertThat(machine.getComments()).isNotEmpty();
    }

    @Test
    void commentMustHaveRate() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID(), ItemType.FOOD);
      when(manager.read(machine.getId())).thenReturn(machine);

      CommentDTO comment = new CommentDTO();
      comment.setContent("This is a comment");

      doAnswer(invocation -> {
        invocation.getArgument(1, Errors.class)
          .rejectValue("rate", "validation.constraints.comment.rate_interval",
              "Error from test");
        return null;
      }).when(commentDtoValidator)
        .validate(eq(comment), errorsArg.capture());

      assertThatExceptionOfType(InvalidData.class).isThrownBy(() -> controller.comment(machine.getId(), comment));
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
