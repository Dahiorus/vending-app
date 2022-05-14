package me.dahiorus.project.vending.web.api.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.model.CardSystemStatus;
import me.dahiorus.project.vending.core.model.ChangeSystemStatus;
import me.dahiorus.project.vending.core.model.PowerStatus;
import me.dahiorus.project.vending.core.model.VendingMachine;
import me.dahiorus.project.vending.core.model.WorkingStatus;
import me.dahiorus.project.vending.core.model.dto.VendingMachineDTO;
import me.dahiorus.project.vending.core.service.impl.VendingMachineDtoServiceImpl;
import me.dahiorus.project.vending.util.VendingMachineBuilder;
import me.dahiorus.project.vending.web.api.assembler.VendingMachineDtoModelAssembler;

@WebMvcTest(VendingMachineRestController.class)
class VendingMachineRestControllerTest extends RestControllerTest
{
  @MockBean
  VendingMachineDtoServiceImpl vendingMachineDtoService;

  @MockBean
  VendingMachineDtoModelAssembler modelAssembler;

  @MockBean
  PagedResourcesAssembler<VendingMachineDTO> pageModelAssembler;

  @Test
  @WithMockUser(username = "admin", password = "password", roles = "ADMIN")
  void resetStatus() throws Exception
  {
    UUID id = UUID.randomUUID();
    when(vendingMachineDtoService.resetStatus(id)).thenReturn(VendingMachineBuilder.builder()
      .id(id)
      .powerStatus(PowerStatus.ON)
      .workingStatus(WorkingStatus.OK)
      .changeMoneyStatus(ChangeSystemStatus.NORMAL)
      .rfidStatus(CardSystemStatus.NORMAL)
      .smartCardStatus(CardSystemStatus.NORMAL)
      .buildDto());
    when(modelAssembler.toModel(any())).then(invoc -> EntityModel.of(invoc.getArgument(0)));

    mockMvc.perform(post("/api/v1/vending-machines/{id}/reset", id))
      .andExpect(status().isOk())
      .andExpect(content().contentType(MediaTypes.HAL_JSON));
  }

  @Test
  @WithMockUser(username = "admin", password = "password", roles = "ADMIN")
  void resetNonExistingMachine() throws Exception
  {
    UUID id = UUID.randomUUID();
    when(vendingMachineDtoService.resetStatus(id)).thenThrow(new EntityNotFound(VendingMachine.class, id));

    mockMvc.perform(post("/api/v1/vending-machines/{id}/reset", id))
      .andExpect(status().isNotFound());
  }

  @Test
  @WithMockUser(username = "user", password = "secret")
  void nonAdminIsForbidden() throws Exception
  {
    mockMvc
      .perform(post("/api/v1/vending-machines/{id}/reset", UUID.randomUUID()))
      .andExpect(status().isForbidden());
    verifyNoInteractions(vendingMachineDtoService);
  }

  @Test
  @WithAnonymousUser
  void anonymousUserIsUnauthorized() throws Exception
  {
    mockMvc
      .perform(post("/api/v1/vending-machines/{id}/reset", UUID.randomUUID()))
      .andExpect(status().isUnauthorized());
    verifyNoInteractions(vendingMachineDtoService);
  }
}
