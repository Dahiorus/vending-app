package me.dahiorus.project.vending.web.api.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import me.dahiorus.project.vending.core.exception.AppException;
import me.dahiorus.project.vending.core.model.Report;
import me.dahiorus.project.vending.core.model.dto.ReportDTO;
import me.dahiorus.project.vending.web.api.impl.ReportRestService;

@Component
public class ReportDtoModelAssembler extends DtoModelAssembler<Report, ReportDTO>
{
  private static final Logger logger = LogManager.getLogger(ReportDtoModelAssembler.class);

  @Override
  public void addLinks(final CollectionModel<EntityModel<ReportDTO>> resources)
  {
    // empty method
  }

  @Override
  public Logger getLogger()
  {
    return logger;
  }

  @Override
  protected Link selfLink(final ReportDTO content) throws AppException
  {
    return linkTo(methodOn(ReportRestService.class).read(content.getId())).withSelfRel();
  }
}
