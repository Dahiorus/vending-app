package me.dahiorus.project.vending.web.api.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;

import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.model.dto.ReportDTO;
import me.dahiorus.project.vending.web.api.impl.ReportRestService;

@Component
public class ReportDtoModelAssembler implements SimpleRepresentationModelAssembler<ReportDTO>
{
  private static final Logger logger = LogManager.getLogger(ReportDtoModelAssembler.class);

  @Override
  public void addLinks(final EntityModel<ReportDTO> resource)
  {
    ReportDTO content = resource.getContent();

    if (content == null)
    {
      return;
    }

    try
    {
      resource.add(
          linkTo(methodOn(ReportRestService.class).read(content.getId())).withSelfRel());
    }
    catch (EntityNotFound e)
    {
      logger.error("Unable to build self link to {}: {}", content, e.getMessage());
    }
  }

  @Override
  public void addLinks(final CollectionModel<EntityModel<ReportDTO>> resources)
  {
    // empty method
  }
}
