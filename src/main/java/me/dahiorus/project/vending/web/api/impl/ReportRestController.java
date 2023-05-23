package me.dahiorus.project.vending.web.api.impl;

import static me.dahiorus.project.vending.web.api.response.ResponseUtils.buildLocation;
import static org.springframework.http.ResponseEntity.created;
import static org.springframework.http.ResponseEntity.noContent;
import static org.springframework.http.ResponseEntity.ok;

import java.net.URI;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.springdoc.core.annotations.ParameterObject;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.common.HasLogger;
import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.model.dto.ReportDTO;
import me.dahiorus.project.vending.domain.service.ReportDtoService;
import me.dahiorus.project.vending.web.api.DeleteRestAPI;
import me.dahiorus.project.vending.web.api.ReadOnlyRestController;
import me.dahiorus.project.vending.web.api.request.ExampleMatcherAdapter;

@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Report", description = "Operations on reports")
@RestController
@RequestMapping(produces = MediaTypes.HAL_JSON_VALUE)
@RequiredArgsConstructor
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
  @PostMapping("/api/v1/vending-machines/{id}/report")
  public ResponseEntity<EntityModel<ReportDTO>> report(@PathVariable final UUID id) throws EntityNotFound
  {
    ReportDTO report = dtoService.report(id);
    URI location = buildLocation(report, ReportRestController.class);

    log.info("Created report of vending machine {}: {}", id, location);

    return created(location).body(modelAssembler.toModel(report));
  }

  @Operation(description = "Get a page of reports")
  @ApiResponse(responseCode = "200", description = "Reports found")
  @Override
  @GetMapping("/api/v1/reports")
  public ResponseEntity<PagedModel<EntityModel<ReportDTO>>> list(@ParameterObject final Pageable pageable,
    @ParameterObject final ReportDTO criteria, @ParameterObject final ExampleMatcherAdapter exampleMatcherAdapter)
  {
    Page<ReportDTO> page = dtoService.list(pageable, criteria, exampleMatcherAdapter.get());

    return ok(pageModelAssembler.toModel(page, modelAssembler));
  }

  @Operation(description = "Get a report by its ID")
  @ApiResponse(responseCode = "200", description = "Report found")
  @Override
  @GetMapping("/api/v1/reports/{id}")
  public ResponseEntity<EntityModel<ReportDTO>> read(@PathVariable final UUID id) throws EntityNotFound
  {
    ReportDTO entity = dtoService.read(id);

    return ok(modelAssembler.toModel(entity));
  }

  @Operation(description = "Delete an existing report targeted by its ID")
  @ApiResponse(responseCode = "200", description = "Report deleted")
  @Override
  @DeleteMapping("/api/v1/reports/{id}")
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
