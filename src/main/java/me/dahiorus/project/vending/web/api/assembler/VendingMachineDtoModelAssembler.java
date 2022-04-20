package me.dahiorus.project.vending.web.api.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import me.dahiorus.project.vending.core.exception.AppException;
import me.dahiorus.project.vending.core.model.VendingMachine;
import me.dahiorus.project.vending.core.model.dto.VendingMachineDTO;
import me.dahiorus.project.vending.web.api.RestService;
import me.dahiorus.project.vending.web.api.impl.CommentRestService;
import me.dahiorus.project.vending.web.api.impl.VendingMachineRestService;

@Component
public class VendingMachineDtoModelAssembler extends DtoModelAssembler<VendingMachine, VendingMachineDTO>
{
  private static final Logger logger = LogManager.getLogger(VendingMachineDtoModelAssembler.class);

  @Override
  public Logger getLogger()
  {
    return logger;
  }

  @Override
  protected Iterable<Link> buildLinks(final VendingMachineDTO content) throws AppException
  {
    return List.of(linkTo(methodOn(VendingMachineRestService.class).getStocks(content.getId())).withRel("stocks"),
        linkTo(methodOn(CommentRestService.class).getComments(content.getId())).withRel("comments"));
  }

  @Override
  protected Class<? extends RestService<VendingMachine, VendingMachineDTO>> getControllerClass()
  {
    return VendingMachineRestService.class;
  }
}
