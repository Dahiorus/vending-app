package me.dahiorus.project.vending.domain.service.impl;

import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.domain.dao.Dao;
import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.model.BinaryData;
import me.dahiorus.project.vending.domain.model.Item;
import me.dahiorus.project.vending.domain.model.dto.BinaryDataDto;
import me.dahiorus.project.vending.domain.model.dto.ItemDto;
import me.dahiorus.project.vending.domain.service.DtoMapper;
import me.dahiorus.project.vending.domain.service.ItemDtoService;
import me.dahiorus.project.vending.domain.service.validation.DtoValidator;

@Log4j2
@Service
public class ItemDtoServiceImpl extends DtoServiceImpl<Item, ItemDto, Dao<Item>>
  implements ItemDtoService
{
  private final Dao<BinaryData> binaryDataDao;

  public ItemDtoServiceImpl(final Dao<Item> dao, final DtoMapper dtoMapper,
    final DtoValidator<ItemDto> dtoValidator, final Dao<BinaryData> binaryDataDao)
  {
    super(dao, dtoMapper, dtoValidator);
    this.binaryDataDao = binaryDataDao;
  }

  @Override
  public Logger getLogger()
  {
    return log;
  }

  @Override
  protected Class<ItemDto> getDomainClass()
  {
    return ItemDto.class;
  }

  @Transactional
  @Override
  public ItemDto uploadImage(final UUID id, final BinaryDataDto picture) throws EntityNotFound
  {
    log.debug("Uploading a picture for the item {}", id);

    Item entity = dao.read(id);

    BinaryData pictureToUpload = dtoMapper.toEntity(picture, BinaryData.class);
    BinaryData savedPicture = binaryDataDao.save(pictureToUpload);
    entity.setPicture(savedPicture);

    Item updatedEntity = dao.save(entity);

    log.info("New picture uploaded for the item {}", id);

    return dtoMapper.toDto(updatedEntity, ItemDto.class);
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<BinaryDataDto> getImage(final UUID id) throws EntityNotFound
  {
    log.debug("Getting the picture of the item {}", id);

    Item entity = dao.read(id);

    return Optional.ofNullable(dtoMapper.toDto(entity.getPicture(), BinaryDataDto.class));
  }
}
