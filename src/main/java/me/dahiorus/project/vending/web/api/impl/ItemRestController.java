package me.dahiorus.project.vending.web.api.impl;

import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.core.exception.AppRuntimeException;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.dto.BinaryDataDTO;
import me.dahiorus.project.vending.core.model.dto.ItemDTO;
import me.dahiorus.project.vending.core.service.ItemDtoService;
import me.dahiorus.project.vending.core.service.validation.ValidationResults;
import me.dahiorus.project.vending.web.api.request.MultiPartFileUtils;

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Item", description = "Operations on Item")
@RestController
@RequestMapping(value = "/api/v1/items")
@Log4j2
public class ItemRestController extends RestControllerImpl<ItemDTO, ItemDtoService>
{
  public ItemRestController(final ItemDtoService dtoService,
      final RepresentationModelAssembler<ItemDTO, EntityModel<ItemDTO>> modelAssembler,
      final PagedResourcesAssembler<ItemDTO> pageModelAssembler)
  {
    super(dtoService, modelAssembler, pageModelAssembler);
  }

  @Override
  public Logger getLogger()
  {
    return log;
  }

  @Operation(description = "Get the picture of an item")
  @ApiResponse(responseCode = "200", description = "Item picture found")
  @ApiResponse(responseCode = "404", description = "No item or picture found")
  @GetMapping("/{id}/picture")
  public ResponseEntity<ByteArrayResource> getPicture(@PathVariable final UUID id) throws EntityNotFound
  {
    Optional<BinaryDataDTO> picture = dtoService.getImage(id);

    return picture.map(p -> {
      log.info("Picture found for item {}", id);

      return ok().contentType(MediaType.parseMediaType(p.getContentType()))
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + p.getName() + "\"")
        .contentLength(p.getSize())
        .body(new ByteArrayResource(p.getContent()));
    })
      .orElseGet(() -> {
        log.info("No picture found for the item {}", id);

        return notFound().build();
      });
  }

  @PostMapping(value = "/{id}/picture", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<EntityModel<ItemDTO>> uploadPicture(@PathVariable final UUID id,
      @RequestParam("file") final MultipartFile file) throws EntityNotFound, ValidationException
  {
    ValidationResults validationResults = MultiPartFileUtils.validateImage("file", file);
    validationResults.throwIfError("Unable to upload the given file");

    try
    {
      BinaryDataDTO picture = MultiPartFileUtils.convert(file);
      ItemDTO updatedItem = dtoService.uploadImage(id, picture);

      return ResponseEntity.ok(modelAssembler.toModel(updatedItem));
    }
    catch (IOException e)
    {
      throw new AppRuntimeException("Unable to get the file to upload", e);
    }
  }
}
