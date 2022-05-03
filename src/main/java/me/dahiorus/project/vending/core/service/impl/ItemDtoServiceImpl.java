package me.dahiorus.project.vending.core.service.impl;

import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.core.dao.DAO;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.model.BinaryData;
import me.dahiorus.project.vending.core.model.Item;
import me.dahiorus.project.vending.core.model.dto.BinaryDataDTO;
import me.dahiorus.project.vending.core.model.dto.ItemDTO;
import me.dahiorus.project.vending.core.service.DtoMapper;
import me.dahiorus.project.vending.core.service.ItemDtoService;
import me.dahiorus.project.vending.core.service.validation.DtoValidator;

@Log4j2
@Service
public class ItemDtoServiceImpl extends DtoServiceImpl<Item, ItemDTO, DAO<Item>> implements ItemDtoService
{
  private final DAO<BinaryData> binaryDataDao;

  public ItemDtoServiceImpl(final DAO<Item> manager, final DtoMapper dtoMapper,
      final DtoValidator<ItemDTO> dtoValidator, final DAO<BinaryData> binaryDataDao)
  {
    super(manager, dtoMapper, dtoValidator);
    this.binaryDataDao = binaryDataDao;
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

  @Transactional(rollbackFor = { EntityNotFound.class })
  @Override
  public ItemDTO uploadImage(final UUID id, final BinaryDataDTO picture) throws EntityNotFound
  {
    log.debug("Uploading a picture for the item {}", id);

    Item entity = dao.read(id);

    BinaryData pictureToUpload = dtoMapper.toEntity(picture, BinaryData.class);
    BinaryData savedPicture = binaryDataDao.save(pictureToUpload);
    entity.setPicture(savedPicture);

    Item updatedEntity = dao.save(entity);

    log.info("New picture uploaded for the item {}", id);

    return dtoMapper.toDto(updatedEntity, ItemDTO.class);
  }

  @Transactional(readOnly = true)
  @Override
  public BinaryDataDTO getImage(final UUID id) throws EntityNotFound
  {
    log.debug("Getting the picture of the item {}", id);

    Item entity = dao.read(id);

    return dtoMapper.toDto(entity.getPicture(), BinaryDataDTO.class);
  }
}
