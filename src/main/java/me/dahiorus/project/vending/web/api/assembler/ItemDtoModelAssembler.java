package me.dahiorus.project.vending.web.api.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

import org.apache.logging.log4j.Logger;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.core.exception.AppException;
import me.dahiorus.project.vending.core.model.dto.ItemDTO;
import me.dahiorus.project.vending.web.api.impl.ItemRestController;

@Log4j2
@Component
public class ItemDtoModelAssembler extends DtoModelAssembler<ItemDTO>
{
  @Override
  public Logger getLogger()
  {
    return log;
  }

  @Override
  protected Optional<Link> selfLink(final ItemDTO content) throws AppException
  {
    return Optional.of(linkTo(methodOn(ItemRestController.class).read(content.getId())).withSelfRel());
  }

  @Override
  protected Iterable<Link> buildLinks(final ItemDTO content) throws AppException
  {
    Collection<Link> links = new LinkedList<>();

    if (content.getPictureId() != null)
    {
      links.add(linkTo(methodOn(ItemRestController.class).getPicture(content.getId())).withRel("picture"));
    }

    return links;
  }
}
