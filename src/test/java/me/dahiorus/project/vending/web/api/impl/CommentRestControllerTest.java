package me.dahiorus.project.vending.web.api.impl;

import static me.dahiorus.project.vending.util.TestUtils.jsonValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.ValidationException;
import me.dahiorus.project.vending.domain.model.VendingMachine;
import me.dahiorus.project.vending.domain.model.dto.CommentDTO;
import me.dahiorus.project.vending.domain.service.impl.CommentDtoServiceImpl;
import me.dahiorus.project.vending.domain.service.validation.ValidationResults;
import me.dahiorus.project.vending.web.api.assembler.CommentDtoModelAssembler;

@WebMvcTest(CommentRestController.class)
class CommentRestControllerTest extends RestControllerTest
{
  @MockBean
  CommentDtoServiceImpl commentDtoService;

  @MockBean
  CommentDtoModelAssembler modelAssembler;

  @Nested
  class GetCommentsTests
  {
    @Test
    void getMachineComments() throws Exception
    {
      UUID id = UUID.randomUUID();
      List<CommentDTO> comments = List.of(buildComment("Comment 1", 2), buildComment("Comment 2", 3));
      when(commentDtoService.getComments(id)).thenReturn(comments);
      when(modelAssembler.toCollectionModel(comments)).thenReturn(CollectionModel.wrap(comments));

      mockMvc.perform(get("/api/v1/vending-machines/{id}/comments", id))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaTypes.HAL_JSON))
        .andExpect(result -> {
          jsonPath("_embedded.commentDTOes[0].content").value("Comment 1");
          jsonPath("_embedded.commentDTOes[1].content").value("Comment 2");
        });
    }

    @Test
    void getNonExistingMachineComments() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(commentDtoService.getComments(id)).thenThrow(new EntityNotFound(VendingMachine.class, id));

      mockMvc.perform(get("/api/v1/vending-machines/{id}/comments", id))
        .andExpect(status().isNotFound());
    }
  }

  @Nested
  class CommentTests
  {
    @Test
    @WithMockUser(username = "user", password = "secret", roles = "ADMIN")
    void commentMachine() throws Exception
    {
      UUID id = UUID.randomUUID();
      CommentDTO comment = buildComment("Comment", 5);

      when(commentDtoService.comment(id, comment)).then(invoc -> {
        comment.setId(UUID.randomUUID());
        return comment;
      });
      when(modelAssembler.toModel(comment)).thenReturn(EntityModel.of(comment));

      mockMvc.perform(post("/api/v1/vending-machines/{id}/comments", id).contentType(MediaType.APPLICATION_JSON)
        .content(jsonValue(comment)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaTypes.HAL_JSON))
        .andExpect(result -> {
          jsonPath("content").value("Comment");
          jsonPath("rate").value(5);
        });
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    void nonAdminCannotComment() throws Exception
    {
      mockMvc
        .perform(
          post("/api/v1/vending-machines/{id}/comments", UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON)
            .content(jsonValue(buildComment("Comment", 0))))
        .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user", password = "secret", roles = "ADMIN")
    void commentNonExistingMachine() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(commentDtoService.comment(eq(id), any())).thenThrow(new EntityNotFound(VendingMachine.class, id));

      mockMvc
        .perform(
          post("/api/v1/vending-machines/{id}/comments", id).contentType(MediaType.APPLICATION_JSON)
            .content(jsonValue(buildComment("Comment", 0))))
        .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user", password = "secret", roles = "ADMIN")
    void commentWithValidationErrors() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(commentDtoService.comment(eq(id), any()))
        .thenThrow(new ValidationException("Validation errors from test", new ValidationResults()));

      mockMvc
        .perform(
          post("/api/v1/vending-machines/{id}/comments", id).contentType(MediaType.APPLICATION_JSON)
            .content(jsonValue(buildComment("Comment", 0))))
        .andExpect(status().isBadRequest());
    }
  }

  CommentDTO buildComment(final String content, final int rate)
  {
    CommentDTO comment = new CommentDTO();
    comment.setContent(content);
    comment.setRate(rate);

    return comment;
  }
}
