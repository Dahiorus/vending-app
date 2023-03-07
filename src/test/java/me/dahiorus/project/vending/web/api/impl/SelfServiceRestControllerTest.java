package me.dahiorus.project.vending.web.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Optional;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.UserNotAuthenticated;
import me.dahiorus.project.vending.domain.exception.ValidationException;
import me.dahiorus.project.vending.domain.model.dto.BinaryDataDTO;
import me.dahiorus.project.vending.domain.model.dto.EditPasswordDTO;
import me.dahiorus.project.vending.domain.model.dto.UserDTO;
import me.dahiorus.project.vending.domain.service.impl.UserDtoServiceImpl;
import me.dahiorus.project.vending.domain.service.validation.ValidationResults;
import me.dahiorus.project.vending.util.UserBuilder;
import me.dahiorus.project.vending.web.api.assembler.UserDtoModelAssembler;
import me.dahiorus.project.vending.web.security.impl.AuthenticationFacadeImpl;

@WebMvcTest(SelfServiceRestController.class)
class SelfServiceRestControllerTest extends RestControllerTest
{
  @MockBean
  AuthenticationFacadeImpl authenticationFacade;

  @MockBean
  UserDtoServiceImpl userDtoService;

  @MockBean
  UserDtoModelAssembler userModelAssembler;

  @Nested
  class GetTests
  {
    @Captor
    ArgumentCaptor<UserDTO> userArg;

    @Test
    @WithMockUser(username = "user.test", password = "Secret")
    void authenticatedUserIsAuthorized() throws Exception
    {
      mockGetAuthenticatedUser();
      when(userModelAssembler.toModel(userArg.capture())).then(invoc -> EntityModel.of(invoc.getArgument(0)));

      mockMvc.perform(get("/api/v1/me"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaTypes.HAL_JSON))
        .andExpect(result -> jsonPath("email").value("user.test"));
    }

    @Test
    @WithAnonymousUser
    void anonymousUserIsUnauthorized() throws Exception
    {
      mockMvc.perform(get("/api/v1/me"))
        .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user.test", password = "Secret")
    void userNotAuthenticated() throws Exception
    {
      when(authenticationFacade.getAuthenticatedUser(any())).thenThrow(new UserNotAuthenticated("Exception from test"));

      mockMvc.perform(get("/api/v1/me"))
        .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  class UpdatePasswordTests
  {
    @Captor
    ArgumentCaptor<EditPasswordDTO> editPasswordArg;

    @Test
    @WithMockUser(username = "user.test", password = "secret")
    void updatePassword() throws Exception
    {
      mockGetAuthenticatedUser();
      doNothing().when(userDtoService)
        .updatePassword(any(), editPasswordArg.capture());

      mockMvc.perform(post("/api/v1/me/password").contentType(MediaType.APPLICATION_JSON)
        .content("{ \"oldPassword\": \"secret\", \"password\": \"secret123\" }"))
        .andExpect(status().isNoContent());

      verify(userDtoService).updatePassword(any(), any());
      assertThat(editPasswordArg.getValue()).hasFieldOrPropertyWithValue("oldPassword", "secret")
        .hasFieldOrPropertyWithValue("password", "secret123");
    }

    @Test
    @WithAnonymousUser
    void anonymousIsUnauthorized() throws Exception
    {
      mockMvc.perform(post("/api/v1/me/password").contentType(MediaType.APPLICATION_JSON)
        .content("{ \"oldPassword\": \"secret\", \"password\": \"secret123\" }"))
        .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user.test", password = "secret")
    void validationExceptionIsThrown() throws Exception
    {
      mockGetAuthenticatedUser();
      doThrow(new ValidationException("Validation exception from test", new ValidationResults())).when(userDtoService)
        .updatePassword(any(), any());

      mockMvc.perform(post("/api/v1/me/password").contentType(MediaType.APPLICATION_JSON)
        .content("{ \"oldPassword\": \"secret\", \"password\": \"secret123\" }"))
        .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user.test", password = "secret")
    void userNotAuthenticated() throws Exception
    {
      when(authenticationFacade.getAuthenticatedUser(any())).thenThrow(new UserNotAuthenticated("Exception from test"));

      mockMvc.perform(post("/api/v1/me/password").contentType(MediaType.APPLICATION_JSON)
        .content("{ \"oldPassword\": \"secret\", \"password\": \"secret123\" }"))
        .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user.test", password = "secret")
    void entityNotFoundIsThrown() throws Exception
    {
      mockGetAuthenticatedUser();
      doThrow(new EntityNotFound("Exception from test")).when(userDtoService)
        .updatePassword(any(), editPasswordArg.capture());

      mockMvc.perform(post("/api/v1/me/password").contentType(MediaType.APPLICATION_JSON)
        .content("{ \"oldPassword\": \"secret\", \"password\": \"secret123\" }"))
        .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  class GetPictureTests
  {
    @Test
    @WithMockUser(username = "user.test", password = "secret")
    void userHasPicture() throws Exception
    {
      BinaryDataDTO dto = new BinaryDataDTO();
      dto.setName("picture.jpg");
      dto.setContentType(MediaType.IMAGE_JPEG_VALUE);
      dto.setContent(new byte[32]);
      dto.setCreatedAt(Instant.now());

      mockGetAuthenticatedUser();
      when(userDtoService.getImage(any())).thenReturn(Optional.of(dto));

      mockMvc.perform(get("/api/v1/me/picture"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(dto.getContentType()))
        .andExpect(content().bytes(dto.getContent()))
        .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"picture.jpg\""))
        .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "max-age=3600, public"))
        .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, dto.getSize()));
    }

    @Test
    @WithMockUser(username = "user.test", password = "secret")
    void userHasNoPicture() throws Exception
    {
      mockGetAuthenticatedUser();
      when(userDtoService.getImage(any())).thenReturn(Optional.empty());

      mockMvc.perform(get("/api/v1/me/picture"))
        .andExpect(status().isNotFound());
    }

    @Test
    @WithAnonymousUser
    void annonymousUserIsUnauthorized() throws Exception
    {
      mockMvc.perform(get("/api/v1/me/picture"))
        .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user.test", password = "secret")
    void entityNotFoundIsThrown() throws Exception
    {
      mockGetAuthenticatedUser();
      when(userDtoService.getImage(any())).thenThrow(new EntityNotFound("Exception from test"));

      mockMvc.perform(get("/api/v1/me/picture"))
        .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user.test", password = "secret")
    void userNotAuthenticated() throws Exception
    {
      when(authenticationFacade.getAuthenticatedUser(any())).thenThrow(new UserNotAuthenticated("Exception from test"));

      mockMvc.perform(get("/api/v1/me/picture"))
        .andExpect(status().isUnauthorized());
    }
  }

  @Nested
  class UploadPictureTests
  {
    @Test
    @WithMockUser(username = "user.test", password = "secret")
    void uploadPicture() throws Exception
    {
      mockGetAuthenticatedUser();
      when(userDtoService.uploadImage(any(), any())).then(invoc -> UserBuilder.builder()
        .id(invoc.getArgument(0, UUID.class))
        .email("user.test")
        .buildDto());

      mockMvc
        .perform(multipart("/api/v1/me/picture")
          .file(new MockMultipartFile("file", "picture.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[32])))
        .andExpect(status().isOk());
    }

    @Test
    @WithAnonymousUser
    void anonymousUserIsUnauthorized() throws Exception
    {
      mockMvc
        .perform(multipart("/api/v1/me/picture")
          .file(new MockMultipartFile("file", "picture.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[32])))
        .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user.test", password = "secret")
    void userNotAuthenticated() throws Exception
    {
      when(authenticationFacade.getAuthenticatedUser(any())).thenThrow(new UserNotAuthenticated("Exception from test"));

      mockMvc
        .perform(multipart("/api/v1/me/picture")
          .file(new MockMultipartFile("file", "picture.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[32])))
        .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "user.test", password = "secret")
    void uploadNonImageThrowsValidationException() throws Exception
    {
      mockGetAuthenticatedUser();

      mockMvc
        .perform(multipart("/api/v1/me/picture")
          .file(new MockMultipartFile("file", "test.md", MediaType.TEXT_MARKDOWN_VALUE, new byte[32])))
        .andExpect(status().isBadRequest());
      verify(userDtoService, never()).uploadImage(any(), any());
    }

    @Test
    @WithMockUser(username = "user.test", password = "secret")
    void uploadImageThrowsEntityNotFound() throws Exception
    {
      mockGetAuthenticatedUser();
      when(userDtoService.uploadImage(any(), any())).thenThrow(new EntityNotFound("Exception from test"));

      mockMvc
        .perform(multipart("/api/v1/me/picture")
          .file(new MockMultipartFile("file", "picture.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[32])))
        .andExpect(status().isUnauthorized());
    }
  }

  void mockGetAuthenticatedUser()
  {
    when(authenticationFacade.getAuthenticatedUser(any())).then(invoc -> {
      Authentication auth = invoc.getArgument(0, Authentication.class);
      return UserBuilder.builder()
        .id(UUID.randomUUID())
        .email(auth.getName())
        .password((String) auth.getCredentials())
        .buildDto();
    });
  }
}
