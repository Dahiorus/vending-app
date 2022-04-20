package me.dahiorus.project.vending.core.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import me.dahiorus.project.vending.core.manager.impl.ItemManager;
import me.dahiorus.project.vending.core.model.Item;
import me.dahiorus.project.vending.core.model.dto.ItemDTO;
import me.dahiorus.project.vending.core.service.DtoMapper;
import me.dahiorus.project.vending.core.service.ItemDtoService;

@Service
public class ItemDtoServiceImpl extends DtoServiceImpl<Item, ItemDTO, ItemManager> implements ItemDtoService
{
  private static final Logger logger = LogManager.getLogger(ItemDtoServiceImpl.class);

  public ItemDtoServiceImpl(final ItemManager manager, final DtoMapper dtoMapper,
      final DtoValidator<Item, ItemDTO> dtoValidator)
  {
    super(manager, dtoMapper, dtoValidator);
  }

  @Override
  public Logger getLogger()
  {
    return logger;
  }

  @Override
  protected Class<ItemDTO> getDomainClass()
  {
    return ItemDTO.class;
  }
}
