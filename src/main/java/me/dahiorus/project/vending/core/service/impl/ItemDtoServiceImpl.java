package me.dahiorus.project.vending.core.service.impl;

import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.core.dao.DAO;
import me.dahiorus.project.vending.core.model.Item;
import me.dahiorus.project.vending.core.model.dto.ItemDTO;
import me.dahiorus.project.vending.core.service.DtoMapper;
import me.dahiorus.project.vending.core.service.ItemDtoService;
import me.dahiorus.project.vending.core.service.validation.DtoValidator;

@Log4j2
@Service
public class ItemDtoServiceImpl extends DtoServiceImpl<Item, ItemDTO, DAO<Item>> implements ItemDtoService
{
  public ItemDtoServiceImpl(final DAO<Item> manager, final DtoMapper dtoMapper,
      final DtoValidator<ItemDTO> dtoValidator)
  {
    super(manager, dtoMapper, dtoValidator);
  }

  @Override
  public Logger getLogger()
  {
    return log;
  }

  @Override
  protected Class<ItemDTO> getDomainClass()
  {
    return ItemDTO.class;
  }
}
