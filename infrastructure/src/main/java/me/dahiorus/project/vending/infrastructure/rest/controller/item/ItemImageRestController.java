package me.dahiorus.project.vending.infrastructure.rest.controller.item;

import static me.dahiorus.project.vending.infrastructure.rest.controller.MultipartFileValidator.validator;
import static me.dahiorus.project.vending.infrastructure.rest.utils.ToFileToUploadConvertor.toFileToUpload;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.item.port.ItemImageApiPort;
import me.dahiorus.project.vending.infrastructure.rest.entity.item.ItemDto;
import me.dahiorus.project.vending.infrastructure.rest.utils.ToByteArrayResponseConvertor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Item")
@RestController
@RequestMapping(value = "/api/v1/items/{id}/image")
public class ItemImageRestController {

  private final ItemImageApiPort itemImageService;
  private final RepresentationModelAssembler<ItemDto, EntityModel<ItemDto>> modelAssembler;

  public ItemImageRestController(
      final ItemImageApiPort itemImageService,
      final RepresentationModelAssembler<ItemDto, EntityModel<ItemDto>> modelAssembler) {
    this.itemImageService = itemImageService;
    this.modelAssembler = modelAssembler;
  }

  @Operation(description = "Get the image of an item")
  @ApiResponse(responseCode = "200", description = "Item image found")
  @ApiResponse(responseCode = "404", description = "No item or image found")
  @GetMapping(produces = {IMAGE_JPEG_VALUE, IMAGE_PNG_VALUE, APPLICATION_OCTET_STREAM_VALUE})
  public ResponseEntity<ByteArrayResource> getImage(@PathVariable("id") UUID id) {
    var maybeItemImage = itemImageService.findImage(new ItemId(id));

    return maybeItemImage
        .map(ToByteArrayResponseConvertor::toResponseEntity)
        .orElse(notFound().build());
  }

  @Operation(description = "Upload an image to an item")
  @ApiResponse(responseCode = "200", description = "Image uploaded")
  @PostMapping(consumes = MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<EntityModel<ItemDto>> uploadImage(
      @PathVariable("id") UUID id, @RequestParam("file") MultipartFile multipartFile) {
    validator(multipartFile).validate();

    var itemWithImage = itemImageService.uploadImage(new ItemId(id), toFileToUpload(multipartFile));

    return ok(modelAssembler.toModel(ItemDto.fromDomain(itemWithImage.item())));
  }
}
