package me.dahiorus.project.vending.web.api.impl;

import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

import java.util.List;
import java.util.UUID;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.dto.CommentDTO;
import me.dahiorus.project.vending.core.service.CommentDtoService;
import me.dahiorus.project.vending.web.api.AppWebService;

@Tag(name = "Comment", description = "Operation on a vending machine comments")
@RestController
@RequestMapping(
    value = "/api/v1/vending-machines/{id:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}/comments",
    produces = MediaTypes.HAL_JSON_VALUE)
@AllArgsConstructor
public class CommentRestController implements AppWebService
{
  private final CommentDtoService dtoService;

  private final RepresentationModelAssembler<CommentDTO, EntityModel<CommentDTO>> commentModelAssembler;

  @Operation(description = "Get the comments of a vending machine")
  @ApiResponse(responseCode = "200", description = "Comments found")
  @GetMapping
  public ResponseEntity<CollectionModel<EntityModel<CommentDTO>>> getComments(@PathVariable final UUID id)
      throws EntityNotFound
  {
    List<CommentDTO> comments = dtoService.getComments(id);

    return ok(commentModelAssembler.toCollectionModel(comments));
  }

  @Operation(description = "Comment a vending machine")
  @ApiResponse(responseCode = "204", description = "Comment created")
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<Void> comment(@PathVariable final UUID id, @RequestBody final CommentDTO comment)
      throws EntityNotFound, ValidationException
  {
    dtoService.comment(id, comment);

    return noContent().build();
  }
}
