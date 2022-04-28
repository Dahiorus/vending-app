package me.dahiorus.project.vending.web.api.impl;

import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

import java.net.URI;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
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
import me.dahiorus.project.vending.common.HasLogger;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.model.dto.ReportDTO;
import me.dahiorus.project.vending.core.service.ReportDtoService;
import me.dahiorus.project.vending.web.api.AppWebService;
import me.dahiorus.project.vending.web.api.model.ExampleMatcherAdapter;

@Tag(name = "Report", description = "Operations on reports")
@RestController
@RequestMapping(produces = MediaTypes.HAL_JSON_VALUE)
public class ReportRestController implements HasLogger, AppWebService
{
  private static final Logger logger = LogManager.getLogger(ReportRestController.class);

  protected final ReportDtoService dtoService;

  protected final RepresentationModelAssembler<ReportDTO, EntityModel<ReportDTO>> modelAssembler;

  protected final PagedResourcesAssembler<ReportDTO> pageModelAssembler;

  public ReportRestController(final ReportDtoService dtoService,
      final RepresentationModelAssembler<ReportDTO, EntityModel<ReportDTO>> modelAssembler,
      final PagedResourcesAssembler<ReportDTO> pageModelAssembler)
  {
    this.dtoService = dtoService;
    this.modelAssembler = modelAssembler;
    this.pageModelAssembler = pageModelAssembler;
  }

  @Override
  public Logger getLogger()
  {
    return logger;
  }

  @Operation(description = "Create a report of a vending machine at this instant")
  @ApiResponse(responseCode = "201", description = "Report created")
  @PostMapping("/v1/vending-machines/{id:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}/report")
  public ResponseEntity<EntityModel<ReportDTO>> report(@PathVariable final UUID id) throws EntityNotFound
  {
    ReportDTO report = dtoService.report(id);

    URI location = MvcUriComponentsBuilder.fromController(ReportRestController.class)
      .path("/{id}")
      .buildAndExpand(report.getId())
      .toUri();

    return ResponseEntity.created(location)
      .body(modelAssembler.toModel(report));
  }

  @Operation(description = "Get a page of reports")
  @GetMapping("/v1/reports")
  public ResponseEntity<PagedModel<EntityModel<ReportDTO>>> list(@ParameterObject final Pageable pageable,
      @ParameterObject final ReportDTO criteria, @ParameterObject final ExampleMatcherAdapter exampleMatcherAdapter)
  {
    Page<ReportDTO> page = dtoService.list(pageable, criteria, exampleMatcherAdapter.toExampleMatcher());

    return ok(pageModelAssembler.toModel(page, modelAssembler));
  }

  @Operation(description = "Get a report by its ID")
  @GetMapping("/v1/reports/{id:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}")
  public ResponseEntity<EntityModel<ReportDTO>> read(@PathVariable final UUID id) throws EntityNotFound
  {
    ReportDTO entity = dtoService.read(id);

    return ok(modelAssembler.toModel(entity));
  }

  @Operation(description = "Delete an existing report targeted by its ID")
  @ApiResponse(responseCode = "204", description = "Report deleted")
  @DeleteMapping("/v1/reports/{id:^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$}")
  public ResponseEntity<Void> delete(@PathVariable final UUID id)
  {
    try
    {
      dtoService.delete(id);
    }
    catch (EntityNotFound e)
    {
      getLogger().warn("Unable to delete report with ID {}", id);
    }

    return noContent().build();
  }
}
