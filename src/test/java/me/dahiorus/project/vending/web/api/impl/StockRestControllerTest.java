package me.dahiorus.project.vending.web.api.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.Item;
import me.dahiorus.project.vending.core.model.VendingMachine;
import me.dahiorus.project.vending.core.model.dto.ItemDTO;
import me.dahiorus.project.vending.core.model.dto.StockDTO;
import me.dahiorus.project.vending.core.service.impl.ItemDtoServiceImpl;
import me.dahiorus.project.vending.core.service.impl.StockDtoServiceImpl;
import me.dahiorus.project.vending.core.service.validation.ValidationResults;
import me.dahiorus.project.vending.util.ItemBuilder;
import me.dahiorus.project.vending.web.api.assembler.StockDtoModelAssembler;

@WebMvcTest(StockRestController.class)
class StockRestControllerTest extends RestControllerTest
{
  @MockBean
  StockDtoServiceImpl stockDtoService;

  @MockBean
  ItemDtoServiceImpl itemDtoService;

  @MockBean
  StockDtoModelAssembler modelAssembler;

  @Nested
  class GetStocksTests
  {
    @Test
    void getStocks() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(stockDtoService.getStocks(id)).thenReturn(List.of(buildStock(UUID.randomUUID(), 5)));
      when(modelAssembler.toCollectionModel(anyIterable()))
        .then(invoc -> CollectionModel.of(invoc.getArgument(0)));

      mockMvc.perform(get("/api/v1/vending-machines/{id}/stocks", id))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaTypes.HAL_JSON))
        .andExpect(jsonPath("_embedded.stockDTOes[0]").isNotEmpty());
    }

    @Test
    void getStocksFromNonExistingMachine() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(stockDtoService.getStocks(id)).thenThrow(new EntityNotFound(VendingMachine.class, id));

      mockMvc.perform(get("/api/v1/vending-machines/{id}/stocks", id))
        .andExpect(status().isNotFound());
    }
  }

  @Nested
  class ProvisionTests
  {
    @Test
    @WithMockUser(username = "admin", password = "secret", roles = "ADMIN")
    void provisionItem() throws Exception
    {
      UUID id = UUID.randomUUID(), itemId = UUID.randomUUID();
      ItemDTO item = ItemBuilder.builder()
        .id(itemId)
        .buildDto();
      when(itemDtoService.read(itemId)).thenReturn(item);
      when(stockDtoService.getStocks(itemId))
        .thenReturn(List.of(buildStock(itemId, 1), buildStock(UUID.randomUUID(), 5)));
      when(modelAssembler.toCollectionModel(anyIterable())).then(invoc -> CollectionModel.wrap(invoc.getArgument(0)));

      mockMvc
        .perform(
            post("/api/v1/vending-machines/{id}/provision/{itemId}", id, itemId).contentType(MediaType.APPLICATION_JSON)
              .content("{ \"quantity\": 10 }"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaTypes.HAL_JSON))
        .andExpect(result -> {
          jsonPath("_embedded.stockDtoes[0]").isNotEmpty();
          jsonPath("_embedded.stockDtoes[1]").isNotEmpty();
        });
      verify(stockDtoService).provisionStock(id, item, 10);
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    void nonAdminIsForbidden() throws Exception
    {
      UUID id = UUID.randomUUID(), itemId = UUID.randomUUID();

      mockMvc
        .perform(
            post("/api/v1/vending-machines/{id}/provision/{itemId}", id, itemId).contentType(MediaType.APPLICATION_JSON)
              .content("{ \"quantity\": 10 }"))
        .andExpect(status().isForbidden());
    }

    @Test
    @WithAnonymousUser
    void anonymousUserIsUnauthorized() throws Exception
    {
      UUID id = UUID.randomUUID(), itemId = UUID.randomUUID();

      mockMvc
        .perform(
            post("/api/v1/vending-machines/{id}/provision/{itemId}", id, itemId).contentType(MediaType.APPLICATION_JSON)
              .content("{ \"quantity\": 10 }"))
        .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", password = "secret", roles = "ADMIN")
    void provisionNonExistingMachine() throws Exception
    {
      UUID id = UUID.randomUUID(), itemId = UUID.randomUUID();
      ItemDTO item = ItemBuilder.builder()
        .id(itemId)
        .buildDto();
      when(itemDtoService.read(itemId)).thenReturn(item);
      doThrow(new EntityNotFound(VendingMachine.class, id)).when(stockDtoService)
        .provisionStock(id, item, 10);

      mockMvc
        .perform(
            post("/api/v1/vending-machines/{id}/provision/{itemId}", id, itemId).contentType(MediaType.APPLICATION_JSON)
              .content("{ \"quantity\": 10 }"))
        .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", password = "secret", roles = "ADMIN")
    void provisionNonExistingItem() throws Exception
    {
      UUID id = UUID.randomUUID(), itemId = UUID.randomUUID();
      when(itemDtoService.read(itemId)).thenThrow(new EntityNotFound(Item.class, id));

      mockMvc
        .perform(
            post("/api/v1/vending-machines/{id}/provision/{itemId}", id, itemId).contentType(MediaType.APPLICATION_JSON)
              .content("{ \"quantity\": 10 }"))
        .andExpect(status().isNotFound());
      verify(stockDtoService, never()).provisionStock(eq(id), any(), eq(10));
    }

    @Test
    @WithMockUser(username = "admin", password = "secret", roles = "ADMIN")
    void provisionWrongStock() throws Exception
    {
      UUID id = UUID.randomUUID(), itemId = UUID.randomUUID();
      ItemDTO item = ItemBuilder.builder()
        .id(itemId)
        .buildDto();
      when(itemDtoService.read(itemId)).thenReturn(item);
      doThrow(new ValidationException("Exception from test", new ValidationResults())).when(stockDtoService)
        .provisionStock(id, item, -1);

      mockMvc
        .perform(
            post("/api/v1/vending-machines/{id}/provision/{itemId}", id, itemId).contentType(MediaType.APPLICATION_JSON)
              .content("{ \"quantity\": -1 }"))
        .andExpect(status().isBadRequest());
    }
  }

  static StockDTO buildStock(final UUID itemId, final int quantity)
  {
    StockDTO stock = new StockDTO();
    stock.setId(UUID.randomUUID());
    stock.setItemId(itemId);
    stock.setQuantity(quantity);

    return stock;
  }
}
