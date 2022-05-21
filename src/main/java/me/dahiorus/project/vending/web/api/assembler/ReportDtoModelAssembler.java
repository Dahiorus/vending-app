package me.dahiorus.project.vending.web.api.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Optional;

import org.apache.logging.log4j.Logger;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.domain.exception.AppException;
import me.dahiorus.project.vending.domain.model.dto.ReportDTO;
import me.dahiorus.project.vending.web.api.impl.ReportRestController;

@Log4j2
@Component
public class ReportDtoModelAssembler extends DtoModelAssembler<ReportDTO>
{
  @Override
  public Logger getLogger()
  {
    return log;
  }

  @Override
  protected Optional<Link> selfLink(final ReportDTO content) throws AppException
  {
    return Optional.of(linkTo(methodOn(ReportRestController.class).read(content.getId())).withSelfRel());
  }
}
