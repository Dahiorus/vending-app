package me.dahiorus.project.vending.web.api.impl;

import static me.dahiorus.project.vending.util.TestUtils.jsonValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import me.dahiorus.project.vending.core.model.dto.UserDTO;
import me.dahiorus.project.vending.core.service.impl.UserDtoServiceImpl;
import me.dahiorus.project.vending.util.UserBuilder;
import me.dahiorus.project.vending.web.api.assembler.UserDtoModelAssembler;

@WebMvcTest(UserRestController.class)
class UserRestControllerTest extends RestControllerTest
{
  @MockBean
  UserDtoServiceImpl userDtoService;

  @MockBean
  UserDtoModelAssembler modelAssembler;

  @MockBean
  PagedResourcesAssembler<UserDTO> pageModelAssembler;

  @Nested
  class CreateTests
  {
    @Captor
    ArgumentCaptor<UserDTO> userArg;

    @Test
    @WithMockUser(username = "admin", password = "secret", roles = "ADMIN")
    void createdUserHasAdminRole() throws Exception
    {
      UserDTO user = UserBuilder.builder()
        .firstName("User")
        .lastName("Test")
        .password("secret")
        .email("admin")
        .buildDto();

      when(userDtoService.create(userArg.capture())).then(invoc -> {
        UserDTO arg = invoc.getArgument(0);
        arg.setId(UUID.randomUUID());
        return arg;
      });
      when(modelAssembler.toModel(any())).then(invoc -> EntityModel.of(invoc.getArgument(0)));

      mockMvc.perform(post("/api/v1/users").contentType(MediaType.APPLICATION_JSON)
        .content(jsonValue(user)))
        .andExpect(status().isCreated())
        .andExpect(header().string(HttpHeaders.LOCATION, endsWith("/api/v1/users/" + userArg.getValue()
          .getId())))
        .andExpect(content().contentType(MediaTypes.HAL_JSON))
        .andExpectAll(
            jsonPath("id").value(userArg.getValue()
              .getId()
              .toString()),
            jsonPath("firstName").value(user.getFirstName()),
            jsonPath("lastName").value(user.getLastName()),
            jsonPath("email").value(user.getEmail()));

      assertThat(userArg.getValue()
        .getRoles()).containsExactly("ROLE_ADMIN");
    }
  }
}
