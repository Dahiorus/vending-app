package me.dahiorus.project.vending.web.api.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import me.dahiorus.project.vending.core.exception.AppException;
import me.dahiorus.project.vending.core.model.dto.ReportDTO;
import me.dahiorus.project.vending.web.api.impl.ReportRestController;

@Component
public class ReportDtoModelAssembler extends DtoModelAssembler<ReportDTO>
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
  protected Optional<Link> selfLink(final ReportDTO content) throws AppException
  {
    return Optional.of(linkTo(methodOn(ReportRestController.class).read(content.getId())).withSelfRel());
  }
}
