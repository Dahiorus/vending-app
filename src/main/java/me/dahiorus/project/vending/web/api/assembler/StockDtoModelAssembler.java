package me.dahiorus.project.vending.web.api.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.hateoas.Link;
import org.springframework.stereotype.Component;

import me.dahiorus.project.vending.core.exception.AppException;
import me.dahiorus.project.vending.core.model.Stock;
import me.dahiorus.project.vending.core.model.dto.StockDTO;
import me.dahiorus.project.vending.web.api.impl.VendingMachineRestService;

@Component
public class StockDtoModelAssembler extends DtoModelAssembler<Stock, StockDTO>
{
  private static final Logger logger = LogManager.getLogger(StockDtoModelAssembler.class);

  @Override
  public Logger getLogger()
  {
    return logger;
  }

  @Override
  protected Iterable<Link> buildLinks(final StockDTO content) throws AppException
  {
    UUID vendingMachineId = content.getVendingMachineId();

    return List.of(linkTo(methodOn(VendingMachineRestService.class).read(vendingMachineId)).withRel("machine"));
  }
}
