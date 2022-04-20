package me.dahiorus.project.vending.web.api.assembler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import me.dahiorus.project.vending.core.model.Item;
import me.dahiorus.project.vending.core.model.dto.ItemDTO;
import me.dahiorus.project.vending.web.api.RestService;
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
  protected Class<? extends RestService<Item, ItemDTO>> getControllerClass()
  {
    return ItemRestService.class;
  }
}
