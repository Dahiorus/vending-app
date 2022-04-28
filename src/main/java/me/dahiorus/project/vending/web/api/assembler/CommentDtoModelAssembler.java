package me.dahiorus.project.vending.web.api.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import me.dahiorus.project.vending.core.exception.AppException;
import me.dahiorus.project.vending.core.model.dto.CommentDTO;
import me.dahiorus.project.vending.web.api.impl.VendingMachineRestController;

@Component
public class CommentDtoModelAssembler extends DtoModelAssembler<CommentDTO>
{
  private static final Logger logger = LogManager.getLogger(CommentDtoModelAssembler.class);

  @Override
  public Logger getLogger()
  {
    return logger;
  }

  @Override
  protected Iterable<Link> buildLinks(final CommentDTO content) throws AppException
  {
    return List
      .of(linkTo(methodOn(VendingMachineRestController.class).read(content.getVendingMachineId())).withRel("machine"));
  }
}
