package me.dahiorus.project.vending.core.service.impl;

import static me.dahiorus.project.vending.core.service.validation.FieldValidationError.emptyOrNullValue;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import me.dahiorus.project.vending.core.dao.ItemDAO;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.Item;
import me.dahiorus.project.vending.core.model.ItemType;
import me.dahiorus.project.vending.core.model.dto.ItemDTO;
import me.dahiorus.project.vending.core.service.validation.ValidationResults;
import me.dahiorus.project.vending.core.service.validation.impl.ItemDtoValidator;

@ExtendWith(MockitoExtension.class)
class ItemDtoServiceImplTest
{
  @Mock
  ItemDAO dao;

  @Mock
  ItemDtoValidator dtoValidator;

  ItemDtoServiceImpl controller;

  @BeforeEach
  void setUp()
  {
    when(dao.getDomainClass()).thenReturn(Item.class);
    controller = new ItemDtoServiceImpl(dao, new DtoMapperImpl(), dtoValidator);
  }

  @Nested
  class CreateTests
  {
    @Test
    void createValidDto() throws Exception
    {
      ItemDTO dto = buildDto("Item", ItemType.FOOD, 2.0);

      when(dtoValidator.validate(dto)).thenReturn(new ValidationResults());
      when(dao.save(any())).then(invocation -> {
        Item item = invocation.getArgument(0);
        item.setId(UUID.randomUUID());
        return item;
      });

      ItemDTO createdDto = controller.create(dto);

      assertAll(() -> assertThat(createdDto.getId()).isNotNull(),
          () -> assertThat(createdDto.getName()).isEqualTo(dto.getName()),
          () -> assertThat(createdDto.getType()).isEqualTo(dto.getType()),
          () -> assertThat(createdDto.getPrice()).isEqualTo(dto.getPrice()));
    }

    @Test
    void createInvalidDto()
    {
      ItemDTO dto = buildDto("", ItemType.FOOD, null);
      when(dtoValidator.validate(dto)).then(invocation -> {
        ValidationResults results = new ValidationResults();
        results.addError(emptyOrNullValue("name"));
        results.addError(emptyOrNullValue("price"));
        return results;
      });

      assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> controller.create(dto));
      verify(dao, never()).save(any());
    }
  }

  @Nested
  class ReadTests
  {
    @Test
    void readDto() throws Exception
    {
      Item entity = mockRead(UUID.randomUUID());

      ItemDTO dto = controller.read(entity.getId());

      assertAll(() -> assertThat(dto.getId()).isEqualTo(entity.getId()),
          () -> assertThat(dto.getName()).isEqualTo(entity.getName()),
          () -> assertThat(dto.getType()).isEqualTo(entity.getType()),
          () -> assertThat(dto.getPrice()).isEqualTo(entity.getPrice()));
    }

    @Test
    void readNonExistingDto() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(dao.read(id)).thenThrow(new EntityNotFound(Item.class, id));

      assertThatExceptionOfType(EntityNotFound.class).isThrownBy(() -> controller.read(id));
    }
  }

  @Nested
  class UpdateTests
  {
    @Test
    void updateValidDto() throws Exception
    {
      Item entity = mockRead(UUID.randomUUID());

      ItemDTO dto = buildDto("Item", ItemType.FOOD, 2.5);
      when(dtoValidator.validate(dto)).thenReturn(new ValidationResults());

      when(dao.save(any())).then(invocation -> invocation.getArgument(0));

      ItemDTO updatedDto = controller.update(entity.getId(), dto);

      assertAll(() -> assertThat(updatedDto.getId()).isEqualTo(entity.getId()),
          () -> assertThat(updatedDto.getName()).isEqualTo(dto.getName()),
          () -> assertThat(updatedDto.getType()).isEqualTo(dto.getType()),
          () -> assertThat(updatedDto.getPrice()).isEqualTo(dto.getPrice()));
    }

    @Test
    void updateInvalidDto() throws Exception
    {
      Item entity = mockRead(UUID.randomUUID());

      ItemDTO dto = buildDto("Item", ItemType.FOOD, 2.5);
      when(dtoValidator.validate(dto)).then(invocation -> {
        ValidationResults results = new ValidationResults();
        results.addError(emptyOrNullValue("name"));
        results.addError(emptyOrNullValue("price"));
        return results;
      });

      assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> controller.update(entity.getId(), dto));
      verify(dao, never()).save(any());
    }

    @Test
    void updateNonExistingDto() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(dao.read(id)).thenThrow(new EntityNotFound(Item.class, id));

      assertThatExceptionOfType(EntityNotFound.class).isThrownBy(() -> controller.update(id, new ItemDTO()));
      verify(dtoValidator, never()).validate(any());
      verify(dao, never()).save(any());
    }
  }

  @Nested
  class DeleteTests
  {
    @Test
    void deleteDto() throws Exception
    {
      Item entity = mockRead(UUID.randomUUID());

      assertThatNoException().isThrownBy(() -> controller.delete(entity.getId()));
      verify(dao).delete(entity);
    }

    @Test
    void deleteNonExistingDto() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(dao.read(id)).thenThrow(new EntityNotFound(Item.class, id));

      assertThatExceptionOfType(EntityNotFound.class).isThrownBy(() -> controller.delete(id));
      verify(dao, never()).delete(any());
    }
  }

  @Test
  void listDtos()
  {
    List<Item> entities = IntStream.range(0, 20)
      .mapToObj(i -> buildEntity("Item-" + i, ItemType.FOOD, 1.5))
      .toList();
    entities.forEach(entity -> entity.setId(UUID.randomUUID()));

    Pageable pageable = PageRequest.of(0, entities.size());
    when(dao.findAll(pageable)).thenReturn(new PageImpl<>(entities, pageable, 50));

    Page<ItemDTO> page = controller.list(pageable);

    assertAll(
        () -> assertThat(page.getContent()).hasSameSizeAs(entities),
        () -> assertThat(page.getPageable()).isEqualTo(pageable));
  }

  @Nested
  class FindByIdTests
  {
    @Test
    void findDtoById() throws Exception
    {
      Item entity = buildEntity("Item", ItemType.FOOD, 2.0);
      entity.setId(UUID.randomUUID());
      when(dao.findById(entity.getId())).thenReturn(Optional.of(entity));

      Optional<ItemDTO> dtoOpt = controller.findById(entity.getId());

      assertThat(dtoOpt).isNotEmpty()
        .hasValueSatisfying(dto -> {
          assertThat(dto.getId()).isEqualTo(entity.getId());
          assertThat(dto.getName()).isEqualTo(entity.getName());
          assertThat(dto.getType()).isEqualTo(entity.getType());
          assertThat(dto.getPrice()).isEqualTo(entity.getPrice());
        });
    }

    @Test
    void findNothingById()
    {
      UUID id = UUID.randomUUID();
      when(dao.findById(id)).thenReturn(Optional.empty());

      Optional<ItemDTO> dtoOpt = controller.findById(id);

      assertThat(dtoOpt).isEmpty();
    }
  }

  Item buildEntity(final String name, final ItemType type, final Double price)
  {
    Item item = new Item();
    item.setName(name);
    item.setType(type);
    item.setPrice(price);

    return item;
  }

  ItemDTO buildDto(final String name, final ItemType type, final Double price)
  {
    ItemDTO item = new ItemDTO();
    item.setName(name);
    item.setType(type);
    item.setPrice(price);

    return item;
  }

  Item mockRead(final UUID id) throws Exception
  {
    Item entity = buildEntity("Item", ItemType.FOOD, 2.0);
    entity.setId(id);
    when(dao.read(entity.getId())).thenReturn(entity);

    return entity;
  }
}
