package me.dahiorus.project.vending.web.api.impl;

import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

import java.net.URI;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.common.HasLogger;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.model.dto.ReportDTO;
import me.dahiorus.project.vending.core.service.ReportDtoService;
import me.dahiorus.project.vending.web.api.DeleteRestAPI;
import me.dahiorus.project.vending.web.api.ReadOnlyRestController;
import me.dahiorus.project.vending.web.api.request.ExampleMatcherAdapter;

@Tag(name = "Report", description = "Operations on reports")
@RestController
@RequestMapping(produces = MediaTypes.HAL_JSON_VALUE)
@AllArgsConstructor
@Log4j2
public class ReportRestController
    implements ReadOnlyRestController<ReportDTO>, DeleteRestAPI, HasLogger
{
  protected final ReportDtoService dtoService;

  protected final RepresentationModelAssembler<ReportDTO, EntityModel<ReportDTO>> modelAssembler;

  protected final PagedResourcesAssembler<ReportDTO> pageModelAssembler;

  @Override
  public Logger getLogger()
  {
    return log;
  }

  @Operation(description = "Create a report of a vending machine at this instant")
  @ApiResponse(responseCode = "201", description = "Report created")
  @PostMapping("/api/v1/vending-machines/{id:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}/report")
  public ResponseEntity<EntityModel<ReportDTO>> report(@PathVariable final UUID id) throws EntityNotFound
  {
    ReportDTO report = dtoService.report(id);

    URI location = MvcUriComponentsBuilder.fromController(ReportRestController.class)
      .path("/{id}")
      .buildAndExpand(report.getId())
      .toUri();

    log.info("Created report of vending machine {}: {}", id, location);

    return ResponseEntity.created(location)
      .body(modelAssembler.toModel(report));
  }

  @Override
  @GetMapping("/api/v1/reports")
  public ResponseEntity<PagedModel<EntityModel<ReportDTO>>> list(@ParameterObject final Pageable pageable,
      @ParameterObject final ReportDTO criteria, @ParameterObject final ExampleMatcherAdapter exampleMatcherAdapter)
  {
    Page<ReportDTO> page = dtoService.list(pageable, criteria, exampleMatcherAdapter.toExampleMatcher());

    return ok(pageModelAssembler.toModel(page, modelAssembler));
  }

  @Override
  @GetMapping("/api/v1/reports/{id:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}")
  public ResponseEntity<EntityModel<ReportDTO>> read(@PathVariable final UUID id) throws EntityNotFound
  {
    ReportDTO entity = dtoService.read(id);

    return ok(modelAssembler.toModel(entity));
  }

  @Override
  @DeleteMapping("/api/v1/reports/{id:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}")
  public ResponseEntity<Void> delete(@PathVariable final UUID id)
  {
    log.debug("Deleting report with ID {}", id);

    try
    {
      dtoService.delete(id);
      log.info("Deleted report: {}", id);
    }
    catch (EntityNotFound e)
    {
      log.warn("Unable to delete report with ID {}", id);
    }

    return noContent().build();
  }
}
