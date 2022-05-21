package me.dahiorus.project.vending.web.api.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.Logger;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.domain.exception.AppException;
import me.dahiorus.project.vending.domain.model.dto.VendingMachineDTO;
import me.dahiorus.project.vending.web.api.impl.CommentRestController;
import me.dahiorus.project.vending.web.api.impl.ReportRestController;
import me.dahiorus.project.vending.web.api.impl.StockRestController;
import me.dahiorus.project.vending.web.api.impl.VendingMachineRestController;

@Log4j2
@Component
public class VendingMachineDtoModelAssembler extends DtoModelAssembler<VendingMachineDTO>
{
  @Override
  public Logger getLogger()
  {
    return log;
  }

  @Override
  protected Optional<Link> selfLink(final VendingMachineDTO content) throws AppException
  {
    Link selfLink = linkTo(methodOn(VendingMachineRestController.class).read(content.getId())).withSelfRel();
    return Optional.of(selfLink);
  }

  @Override
  protected Iterable<Link> buildLinks(final VendingMachineDTO content) throws AppException
  {
    return List.of(linkTo(methodOn(StockRestController.class).getStocks(content.getId())).withRel("stocks"),
        linkTo(methodOn(CommentRestController.class).getComments(content.getId())).withRel("comments"),
        linkTo(methodOn(ReportRestController.class).report(content.getId())).withRel("report"),
        linkTo(methodOn(VendingMachineRestController.class).resetStatus(content.getId())).withRel("reset"));
  }
}
