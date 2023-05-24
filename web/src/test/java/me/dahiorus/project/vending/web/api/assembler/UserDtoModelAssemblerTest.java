package me.dahiorus.project.vending.web.api.assembler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.hateoas.EntityModel;

import me.dahiorus.project.vending.domain.model.dto.UserDTO;
import me.dahiorus.project.vending.util.UserBuilder;
import me.dahiorus.project.vending.web.security.impl.AuthenticationFacadeImpl;

@ExtendWith(MockitoExtension.class)
class UserDtoModelAssemblerTest
{
  UserDtoModelAssembler modelAssembler;

  @Mock
  AuthenticationFacadeImpl authenticationFacade;

  @BeforeEach
  void setUp() throws Exception
  {
    modelAssembler = new UserDtoModelAssembler(authenticationFacade);
  }

  @Test
  void dtoToEntityModel()
  {
    UserDTO dto = UserBuilder.builder()
      .id(UUID.randomUUID())
      .email("user.test@yopmail.com")
      .buildDto();

    EntityModel<UserDTO> model = modelAssembler.toModel(dto);

    Assertions.assertEntityModel(model, dto);
  }

  @Test
  void authenticatedUserToEntityModel() throws Exception
  {
    UserDTO dto = UserBuilder.builder()
      .id(UUID.randomUUID())
      .email("user.test@yopmail.com")
      .buildDto();
    when(authenticationFacade.getAuthenticatedUser(any())).thenReturn(dto);

    EntityModel<UserDTO> model = modelAssembler.toModel(dto);

    Assertions.assertEntityModel(model, dto, "me:get", "me:update-password", "me:picture");
  }
}
