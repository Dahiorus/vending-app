package me.dahiorus.project.vending.infrastructure.rest.controller.item;

import static me.dahiorus.project.vending.infrastructure.rest.utils.ToPaginationConvertor.toPagination;
import static org.springframework.hateoas.IanaLinkRelations.SELF;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.item.port.ItemApiPort;
import me.dahiorus.project.vending.infrastructure.rest.entity.FilterMatcherDto;
import me.dahiorus.project.vending.infrastructure.rest.entity.item.ItemDto;
import me.dahiorus.project.vending.infrastructure.rest.entity.item.ItemToCreateDto;
import me.dahiorus.project.vending.infrastructure.rest.entity.item.ItemToUpdateDto;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Item", description = "Operations on Item")
@RestController
@RequestMapping(value = "/api/v1/items")
public class ItemCrudRestController {

  private final ItemApiPort service;
  private final PagedResourcesAssembler<ItemDto> pageModelAssembler;
  private final RepresentationModelAssembler<ItemDto, EntityModel<ItemDto>> modelAssembler;

  public ItemCrudRestController(
      final ItemApiPort service,
      final PagedResourcesAssembler<ItemDto> pageModelAssembler,
      final RepresentationModelAssembler<ItemDto, EntityModel<ItemDto>> modelAssembler) {
    this.service = service;
    this.pageModelAssembler = pageModelAssembler;
    this.modelAssembler = modelAssembler;
  }

  @Operation(description = "Create a new item")
  @ApiResponse(responseCode = "201", description = "Item created")
  @ApiResponse(responseCode = "400", description = "Bad request")
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<EntityModel<ItemDto>> create(
      @Valid @RequestBody ItemToCreateDto itemToCreate) {
    var createdItem = ItemDto.fromDomain(service.create(itemToCreate.toDomain()));
    var itemDtoModel = modelAssembler.toModel(createdItem);

    return created(itemDtoModel.getRequiredLink(SELF).toUri()).body(itemDtoModel);
  }

  @Operation(description = "Get a vending machine by its ID")
  @ApiResponse(responseCode = "200", description = "Entity found")
  @ApiResponse(responseCode = "404", description = "Entity not found")
  @GetMapping("/{id}")
  public ResponseEntity<EntityModel<ItemDto>> read(@PathVariable("id") UUID id) {
    var itemDto = ItemDto.fromDomain(service.read(new ItemId(id)));

    return ok(modelAssembler.toModel(itemDto));
  }

  @Operation(description = "Update an item targeted by its ID")
  @ApiResponse(responseCode = "200", description = "Entity created or updated")
  @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<EntityModel<ItemDto>> update(
      @PathVariable final UUID id, @RequestBody final ItemToUpdateDto item) {
    var updatedItem = service.update(item.toDomain(id));

    return ok(modelAssembler.toModel(ItemDto.fromDomain(updatedItem)));
  }

  @Operation(description = "Delete an existing vending machine targeted by its ID")
  @ApiResponse(responseCode = "204", description = "Entity deleted")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
    service.delete(new ItemId(id));

    return noContent().build();
  }

  @Operation(description = "Get a page of items")
  @ApiResponse(responseCode = "200", description = "Items found")
  @GetMapping
  public ResponseEntity<PagedModel<EntityModel<ItemDto>>> search(
      @ParameterObject Pageable pageable,
      @ParameterObject ItemDto example,
      @ParameterObject FilterMatcherDto filterMatcher) {
    var page =
        service
            .search(toPagination(pageable), example.toDomain(), filterMatcher.toDomain())
            .map(ItemDto::fromDomain);

    return ok(
        pageModelAssembler.toModel(new PageImpl<>(page.content(), pageable, page.totalElements())));
  }
}
