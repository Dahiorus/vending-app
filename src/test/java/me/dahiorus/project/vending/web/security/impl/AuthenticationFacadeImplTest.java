package me.dahiorus.project.vending.web.security.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.UserNotAuthenticated;
import me.dahiorus.project.vending.domain.model.dto.UserDTO;
import me.dahiorus.project.vending.domain.service.impl.UserDtoServiceImpl;
import me.dahiorus.project.vending.util.UserBuilder;

@ExtendWith(MockitoExtension.class)
class AuthenticationFacadeImplTest
{
  AuthenticationFacadeImpl authenticationFacade;

  @Mock
  UserDtoServiceImpl userDtoService;

  @BeforeEach
  void setUp()
  {
    authenticationFacade = new AuthenticationFacadeImpl(userDtoService);
  }

  @Test
  void userIsAuthenticated() throws Exception
  {
    String username = "user.test";
    when(userDtoService.getByUsername(username)).thenReturn(UserBuilder.builder()
      .email(username)
      .buildDto());

    UserDTO authenticatedUser = authenticationFacade.getAuthenticatedUser(mockAuthentication(username));

    assertThat(authenticatedUser).isNotNull()
      .hasFieldOrPropertyWithValue("email", username);
  }

  @Test
  void nullAuthentication()
  {
    assertThatExceptionOfType(UserNotAuthenticated.class)
      .isThrownBy(() -> authenticationFacade.getAuthenticatedUser(null));
  }

  @Test
  void noUserMatchAuthentication() throws Exception
  {
    String username = "user.test";
    when(userDtoService.getByUsername(username)).thenThrow(new EntityNotFound("No user found"));

    assertThatExceptionOfType(UserNotAuthenticated.class)
      .isThrownBy(() -> authenticationFacade.getAuthenticatedUser(mockAuthentication(username)));
  }

  Authentication mockAuthentication(final String username)
  {
    Authentication auth = mock(Authentication.class);
    when(auth.getName()).thenReturn(username);

    return auth;
  }
}
