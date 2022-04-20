package me.dahiorus.project.vending.web.api.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.model.dto.CommentDTO;
import me.dahiorus.project.vending.web.api.impl.VendingMachineRestService;

@Component
public class CommentDtoModelAssembler implements SimpleRepresentationModelAssembler<CommentDTO>
{
  private static final Logger logger = LogManager.getLogger(CommentDtoModelAssembler.class);

  @Override
  public void addLinks(final EntityModel<CommentDTO> resource)
  {
    CommentDTO content = resource.getContent();

    if (content == null)
    {
      return;
    }

    try
    {
      resource.add(
          linkTo(methodOn(VendingMachineRestService.class).read(content.getVendingMachineId())).withRel("machine"));
    }
    catch (EntityNotFound e)
    {
      logger.error("Unable to build self link to {}: {}", content, e.getMessage());
    }
  }

  @Override
  public void addLinks(final CollectionModel<EntityModel<CommentDTO>> resources)
  {
    UriComponents requestUri = ServletUriComponentsBuilder.fromCurrentRequest()
      .build();

    resources.add(Link.of(requestUri.toString()));
  }
}
