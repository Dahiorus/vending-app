package me.dahiorus.project.vending.web.api.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import me.dahiorus.project.vending.common.HasLogger;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.model.dto.StockDTO;
import me.dahiorus.project.vending.web.api.impl.VendingMachineRestService;

@Component
public class StockDtoModelAssembler implements SimpleRepresentationModelAssembler<StockDTO>, HasLogger
{
  private static final Logger logger = LogManager.getLogger(StockDtoModelAssembler.class);

  @Override
  public void addLinks(final EntityModel<StockDTO> resource)
  {
    StockDTO content = resource.getContent();

    if (content == null)
    {
      return;
    }

    try
    {
      UUID vendingMachineId = content.getVendingMachineId();
      resource
        .add(linkTo(methodOn(VendingMachineRestService.class).read(vendingMachineId)).withRel("machine"));
    }
    catch (EntityNotFound e)
    {
      logger.warn("Unable to build links for {}: {}", content, e.getMessage());
    }
  }

  @Override
  public void addLinks(final CollectionModel<EntityModel<StockDTO>> resources)
  {
    UriComponents requestUri = ServletUriComponentsBuilder.fromCurrentRequest()
      .build();

    resources.add(Link.of(requestUri.toString()));
  }

  @Override
  public Logger getLogger()
  {
    return logger;
  }
}
