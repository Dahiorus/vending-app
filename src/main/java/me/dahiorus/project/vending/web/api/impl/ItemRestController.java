package me.dahiorus.project.vending.web.api.impl;

import java.io.IOException;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
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

  @GetMapping("/{id}/picture")
  public ResponseEntity<Resource> getPicture(@PathVariable final UUID id) throws EntityNotFound
  {
    BinaryDataDTO picture = dtoService.getImage(id);

    if (picture == null)
    {
      throw new EntityNotFound("No file found for the item " + id);
    }

    log.info("Picture found for item {}", id);

    return ResponseEntity.ok()
      .contentType(MediaType.parseMediaType(picture.getContentType()))
      .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + picture.getName() + "\"")
      .contentLength(picture.getSize())
      .body(new ByteArrayResource(picture.getContent()));
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
