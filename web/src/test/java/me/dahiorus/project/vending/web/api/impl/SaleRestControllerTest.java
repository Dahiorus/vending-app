package me.dahiorus.project.vending.web.api.impl;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.MediaTypes;

import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.ItemMissing;
import me.dahiorus.project.vending.domain.exception.VendingMachineNotWorking;
import me.dahiorus.project.vending.domain.model.Item;
import me.dahiorus.project.vending.domain.model.ItemBuilder;
import me.dahiorus.project.vending.domain.model.VendingMachine;
import me.dahiorus.project.vending.domain.model.dto.ItemDto;
import me.dahiorus.project.vending.domain.model.dto.SaleDto;
import me.dahiorus.project.vending.domain.service.impl.ItemDtoServiceImpl;
import me.dahiorus.project.vending.domain.service.impl.SaleDtoServiceImpl;

@WebMvcTest(SaleRestController.class)
class SaleRestControllerTest extends RestControllerTest
{
  @MockBean
  SaleDtoServiceImpl saleDtoService;

  @MockBean
  ItemDtoServiceImpl itemDtoService;

  @Test
  void purchaseItem() throws Exception
  {
    UUID id = UUID.randomUUID(), itemId = UUID.randomUUID();
    ItemDto item = ItemBuilder.builder()
      .id(itemId)
      .price(BigDecimal.valueOf(1.5))
      .buildDto();
    when(itemDtoService.read(itemId)).thenReturn(item);
    when(saleDtoService.purchaseItem(id, item)).thenReturn(new SaleDto());

    mockMvc.perform(post("/api/v1/vending-machines/{id}/purchase/{itemId}", id, itemId))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaTypes.HAL_JSON));
  }

  @Test
  void purchaseFromNonExistingMachine() throws Exception
  {
    UUID id = UUID.randomUUID(), itemId = UUID.randomUUID();
    ItemDto item = ItemBuilder.builder()
      .id(itemId)
      .price(BigDecimal.valueOf(1.5))
      .buildDto();
    when(itemDtoService.read(itemId)).thenReturn(item);
    when(saleDtoService.purchaseItem(id, item))
      .thenThrow(new EntityNotFound(VendingMachine.class, id));

    mockMvc.perform(post("/api/v1/vending-machines/{id}/purchase/{itemId}", id, itemId))
      .andExpect(status().isNotFound());
  }

  @Test
  void purchaseNonExistingItem() throws Exception
  {
    UUID id = UUID.randomUUID(), itemId = UUID.randomUUID();
    when(itemDtoService.read(itemId)).thenThrow(new EntityNotFound(Item.class, itemId));

    mockMvc.perform(post("/api/v1/vending-machines/{id}/purchase/{itemId}", id, itemId))
      .andExpect(status().isNotFound());
  }

  @Test
  void itemMissingFromMachine() throws Exception
  {
    UUID id = UUID.randomUUID(), itemId = UUID.randomUUID();
    ItemDto item = ItemBuilder.builder()
      .id(itemId)
      .price(BigDecimal.valueOf(1.5))
      .buildDto();
    when(itemDtoService.read(itemId)).thenReturn(item);
    when(saleDtoService.purchaseItem(id, item)).thenThrow(new ItemMissing("Exception from test"));

    mockMvc.perform(post("/api/v1/vending-machines/{id}/purchase/{itemId}", id, itemId))
      .andExpect(status().isBadRequest());
  }

  @Test
  void purchaseFromNotWorkingMachine() throws Exception
  {
    UUID id = UUID.randomUUID(), itemId = UUID.randomUUID();
    ItemDto item = ItemBuilder.builder()
      .id(itemId)
      .price(BigDecimal.valueOf(1.5))
      .buildDto();
    when(itemDtoService.read(itemId)).thenReturn(item);
    when(saleDtoService.purchaseItem(id, item))
      .thenThrow(new VendingMachineNotWorking("Exception from test"));

    mockMvc.perform(post("/api/v1/vending-machines/{id}/purchase/{itemId}", id, itemId))
      .andExpect(status().isBadRequest());
  }
}
