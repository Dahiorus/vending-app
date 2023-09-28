package me.dahiorus.project.vending.web.api.impl;

import static me.dahiorus.project.vending.domain.service.validation.FieldValidationError.emptyOrNullValue;
import static me.dahiorus.project.vending.web.utils.WebTestUtils.jsonValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.model.UserBuilder;
import me.dahiorus.project.vending.domain.model.dto.UserDto;
import me.dahiorus.project.vending.domain.service.impl.UserDtoServiceImpl;
import me.dahiorus.project.vending.domain.service.validation.ValidationResults;
import me.dahiorus.project.vending.web.api.assembler.UserDtoModelAssembler;
import me.dahiorus.project.vending.web.api.model.AuthenticateResponse;
import me.dahiorus.project.vending.web.api.model.RefreshTokenRequest;
import me.dahiorus.project.vending.web.exception.InvalidTokenCreation;
import me.dahiorus.project.vending.web.exception.UnparsableToken;

@WebMvcTest(PublicRestController.class)
class PublicRestControllerTest extends RestControllerTest
{
  @MockBean
  UserDtoServiceImpl userDtoService;

  @MockBean
  UserDtoModelAssembler modelAssembler;

  @Nested
  class RegisterTests
  {
    @Captor
    ArgumentCaptor<UserDto> userArg;

    @Test
    @WithAnonymousUser
    void registerUser() throws Exception
    {
      UUID id = UUID.randomUUID();
      UserDto dto = UserBuilder.builder()
        .email("user.test@yopmail.com")
        .firstName("User")
        .lastName("Test")
        .buildDto();

      when(userDtoService.create(userArg.capture())).then(invoc -> {
        UserDto user = invoc.getArgument(0);
        user.setId(id);
        return user;
      });

      when(modelAssembler.toModel(any())).then(invoc -> EntityModel.of(invoc.getArgument(0)));

      mockMvc.perform(post("/api/v1/register").contentType(MediaType.APPLICATION_JSON)
        .content(jsonValue(dto)))
        .andExpect(status().isCreated())
        .andExpect(content().contentType(MediaTypes.HAL_JSON))
        .andExpect(result -> {
          jsonPath("id").value(id);
          jsonPath("email").value(dto.getEmail());
          jsonPath("firstName").value(dto.getFirstName());
          jsonPath("lastName").value(dto.getLastName());
        })
        .andExpect(header().string(HttpHeaders.LOCATION, endsWith("/api/v1/users/" + id)));

      assertThat(userArg.getValue()
        .getRoles()).isEmpty();
    }

    @Test
    @WithAnonymousUser
    void registerInvalidUser() throws Exception
    {
      UserDto dto = UserBuilder.builder()
        .firstName("User")
        .lastName("Test")
        .roles(List.of())
        .buildDto();

      when(userDtoService.create(dto)).then(invoc -> {
        ValidationResults validations = new ValidationResults();
        validations.addError(emptyOrNullValue("email"));
        validations.throwIfError("Error from test");
        return null;
      });

      mockMvc.perform(post("/api/v1/register").contentType(MediaType.APPLICATION_JSON)
        .content(jsonValue(dto)))
        .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user", password = "Secret123")
    void nonAnonymousUserCannotRegister() throws Exception
    {
      mockMvc.perform(post("/api/v1/register").contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
        .andExpect(status().isForbidden());
    }
  }

  @Nested
  class RefreshTokenTests
  {
    @Test
    void refreshAccessToken() throws Exception
    {
      String refreshToken = "123456789";
      Authentication authentication = mockAuthentication("user.test");

      when(jwtService.parseToken(refreshToken)).thenReturn(authentication);
      when(userDtoService.getByUsername("user.test")).thenReturn(UserBuilder.builder()
        .email("user.test")
        .roles(List.of("ROLE_USER"))
        .buildDto());
      when(jwtService.createAccessToken("user.test", List.of(new SimpleGrantedAuthority("ROLE_USER"))))
        .thenReturn("9874563210");

      RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);

      mockMvc.perform(post("/api/v1/authenticate/refresh").contentType(MediaType.APPLICATION_JSON)
        .content(jsonValue(request)))
        .andExpect(status().isOk())
        .andExpect(content().json(jsonValue(new AuthenticateResponse("9874563210", refreshToken))));
    }

    @Test
    void unparsableToken() throws Exception
    {
      String refreshToken = "123456789";
      when(jwtService.parseToken(refreshToken)).thenThrow(new UnparsableToken("Token unparsable from test"));

      RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);

      mockMvc.perform(post("/api/v1/authenticate/refresh").contentType(MediaType.APPLICATION_JSON)
        .content(jsonValue(request)))
        .andExpect(status().is5xxServerError());
    }

    @Test
    void unableToCreateToken() throws Exception
    {
      String refreshToken = "123456789";
      Authentication authentication = mockAuthentication("user.test");

      when(jwtService.parseToken(refreshToken)).thenReturn(authentication);
      when(userDtoService.getByUsername("user.test")).thenReturn(UserBuilder.builder()
        .email("user.test")
        .roles(List.of())
        .buildDto());
      when(jwtService.createAccessToken("user.test", List.of()))
        .thenThrow(new InvalidTokenCreation("Invalid token creation from test"));

      RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);

      mockMvc.perform(post("/api/v1/authenticate/refresh").contentType(MediaType.APPLICATION_JSON)
        .content(jsonValue(request)))
        .andExpect(status().is5xxServerError());
    }

    @Test
    void userNotFound() throws Exception
    {
      String refreshToken = "123456789";
      Authentication authentication = mockAuthentication("user.test");

      when(jwtService.parseToken(refreshToken)).thenReturn(authentication);
      when(userDtoService.getByUsername("user.test")).thenThrow(new EntityNotFound("User not found from test"));

      RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);

      mockMvc.perform(post("/api/v1/authenticate/refresh").contentType(MediaType.APPLICATION_JSON)
        .content(jsonValue(request)))
        .andExpect(status().isNotFound());
    }

    Authentication mockAuthentication(final String username)
    {
      Authentication authentication = mock(Authentication.class);
      when(authentication.getPrincipal()).thenReturn("user.test");

      return authentication;
    }
  }
}
