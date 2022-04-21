package me.dahiorus.project.vending.web.api.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import me.dahiorus.project.vending.core.exception.AppException;
import me.dahiorus.project.vending.core.model.Item;
import me.dahiorus.project.vending.core.model.dto.ItemDTO;
import me.dahiorus.project.vending.web.api.impl.ItemRestService;

@Component
public class ItemDtoModelAssembler extends DtoModelAssembler<Item, ItemDTO>
{
  private static final Logger logger = LogManager.getLogger(ItemDtoModelAssembler.class);

  @Override
  public Logger getLogger()
  {
    return logger;
  }

  @Override
  protected Link selfLink(final ItemDTO content) throws AppException
  {
    return linkTo(methodOn(ItemRestService.class).read(content.getId())).withSelfRel();
  }
}
