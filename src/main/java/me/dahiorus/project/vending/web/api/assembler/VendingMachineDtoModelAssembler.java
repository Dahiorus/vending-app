package me.dahiorus.project.vending.web.api.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.hateoas.Affordance;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import me.dahiorus.project.vending.core.exception.AppException;
import me.dahiorus.project.vending.core.model.dto.VendingMachineDTO;
import me.dahiorus.project.vending.web.api.impl.CommentRestController;
import me.dahiorus.project.vending.web.api.impl.VendingMachineRestController;

@Component
public class VendingMachineDtoModelAssembler extends DtoModelAssembler<VendingMachineDTO>
{
  private static final Logger logger = LogManager.getLogger(VendingMachineDtoModelAssembler.class);

  @Override
  public Logger getLogger()
  {
    return logger;
  }

  @Override
  protected Optional<Link> selfLink(final VendingMachineDTO content) throws AppException
  {
    Link selfLink = linkTo(methodOn(VendingMachineRestController.class).read(content.getId())).withSelfRel();
    Affordance updateLink = afford(methodOn(VendingMachineRestController.class).update(content.getId(), null));
    Affordance deleteLink = afford(methodOn(VendingMachineRestController.class).delete(content.getId()));

    return Optional.of(selfLink.andAffordance(updateLink)
      .andAffordance(deleteLink));
  }

  @Override
  protected Iterable<Link> buildLinks(final VendingMachineDTO content) throws AppException
  {
    return List.of(linkTo(methodOn(VendingMachineRestController.class).getStocks(content.getId())).withRel("stocks"),
        linkTo(methodOn(CommentRestController.class).getComments(content.getId())).withRel("comments"));
  }
}
