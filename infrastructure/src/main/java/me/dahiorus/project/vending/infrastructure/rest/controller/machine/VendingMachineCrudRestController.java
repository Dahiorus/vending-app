package me.dahiorus.project.vending.infrastructure.rest.controller.machine;

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
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.machine.port.VendingMachineApiPort;
import me.dahiorus.project.vending.infrastructure.rest.entity.FilterMatcherDto;
import me.dahiorus.project.vending.infrastructure.rest.entity.machine.VendingMachineDto;
import me.dahiorus.project.vending.infrastructure.rest.entity.machine.VendingMachineToCreateDto;
import me.dahiorus.project.vending.infrastructure.rest.entity.machine.VendingMachineToUpdateDto;
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
@Tag(name = "VendingMachine", description = "Operations on Vending machine")
@RestController
@RequestMapping(value = "/api/v1/vending-machines")
public class VendingMachineCrudRestController {
  private final VendingMachineApiPort service;
  private final PagedResourcesAssembler<VendingMachineDto> pageModelAssembler;
  private final RepresentationModelAssembler<VendingMachineDto, EntityModel<VendingMachineDto>>
      modelAssembler;

  public VendingMachineCrudRestController(
      final VendingMachineApiPort service,
      final PagedResourcesAssembler<VendingMachineDto> pageModelAssembler,
      final RepresentationModelAssembler<VendingMachineDto, EntityModel<VendingMachineDto>>
          modelAssembler) {
    this.service = service;
    this.pageModelAssembler = pageModelAssembler;
    this.modelAssembler = modelAssembler;
  }

  @Operation(description = "Create a new vending machine")
  @ApiResponse(responseCode = "201", description = "Vending machine created")
  @ApiResponse(responseCode = "400", description = "Bad request")
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<EntityModel<VendingMachineDto>> create(
      @Valid @RequestBody final VendingMachineToCreateDto vendingMachine) {
    var createdMachine = VendingMachineDto.fromDomain(service.create(vendingMachine.toDomain()));

    var vendingMachineDtoModel = modelAssembler.toModel(createdMachine);

    return created(vendingMachineDtoModel.getRequiredLink(SELF).toUri())
        .body(vendingMachineDtoModel);
  }

  @Operation(description = "Get a vending machine by its ID")
  @ApiResponse(responseCode = "200", description = "Entity found")
  @ApiResponse(responseCode = "404", description = "Entity not found")
  @GetMapping("/{id}")
  public ResponseEntity<EntityModel<VendingMachineDto>> read(@PathVariable("id") UUID id) {
    var vendingMachineDto = VendingMachineDto.fromDomain(service.read(new VendingMachineId(id)));

    return ok(modelAssembler.toModel(vendingMachineDto));
  }

  @Operation(description = "Update a vending machine targeted by its ID")
  @ApiResponse(responseCode = "200", description = "Entity created or updated")
  @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<EntityModel<VendingMachineDto>> update(
      @PathVariable final UUID id, @RequestBody final VendingMachineToUpdateDto vendingMachine) {
    var updatedMachine = service.update(vendingMachine.toDomain(id));

    return ok(modelAssembler.toModel(VendingMachineDto.fromDomain(updatedMachine)));
  }

  @Operation(description = "Delete an existing vending machine targeted by its ID")
  @ApiResponse(responseCode = "204", description = "Entity deleted")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
    service.delete(new VendingMachineId(id));

    return noContent().build();
  }

  @Operation(description = "Get a page of vending machines")
  @ApiResponse(responseCode = "200", description = "Vending machines found")
  @GetMapping
  public ResponseEntity<PagedModel<EntityModel<VendingMachineDto>>> search(
      @ParameterObject Pageable pageable,
      @ParameterObject VendingMachineDto example,
      @ParameterObject FilterMatcherDto filterMatcher) {
    var page =
        service
            .search(toPagination(pageable), example.toDomain(), filterMatcher.toDomain())
            .map(VendingMachineDto::fromDomain);

    return ok(
        pageModelAssembler.toModel(new PageImpl<>(page.content(), pageable, page.totalElements())));
  }
}
