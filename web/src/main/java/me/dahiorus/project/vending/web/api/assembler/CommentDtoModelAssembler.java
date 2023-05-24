package me.dahiorus.project.vending.web.api.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.domain.exception.AppException;
import me.dahiorus.project.vending.domain.model.dto.CommentDTO;
import me.dahiorus.project.vending.web.api.impl.VendingMachineRestController;

@Log4j2
@Component
public class CommentDtoModelAssembler extends DtoModelAssembler<CommentDTO>
{
  @Override
  public Logger getLogger()
  {
    return log;
  }

  @Override
  protected Iterable<Link> buildLinks(final CommentDTO content) throws AppException
  {
    return List
      .of(linkTo(methodOn(VendingMachineRestController.class).read(content.getVendingMachineId())).withRel("machine"));
  }
}
