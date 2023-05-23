package me.dahiorus.project.vending.domain.service.impl;

import static me.dahiorus.project.vending.domain.service.validation.FieldValidationError.emptyOrNullValue;
import static me.dahiorus.project.vending.util.ValidationTestUtils.successResults;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import me.dahiorus.project.vending.domain.dao.BinaryDataDAO;
import me.dahiorus.project.vending.domain.dao.ItemDAO;
import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.ValidationException;
import me.dahiorus.project.vending.domain.model.AppUser;
import me.dahiorus.project.vending.domain.model.BinaryData;
import me.dahiorus.project.vending.domain.model.Item;
import me.dahiorus.project.vending.domain.model.ItemType;
import me.dahiorus.project.vending.domain.model.dto.BinaryDataDTO;
import me.dahiorus.project.vending.domain.model.dto.ItemDTO;
import me.dahiorus.project.vending.domain.service.validation.ValidationResults;
import me.dahiorus.project.vending.domain.service.validation.impl.ItemDtoValidator;
import me.dahiorus.project.vending.util.ItemBuilder;

@ExtendWith(MockitoExtension.class)
class ItemDtoServiceImplTest
{
  @Mock
  ItemDAO dao;

  @Mock
  ItemDtoValidator dtoValidator;

  @Mock
  BinaryDataDAO binaryDataDao;

  ItemDtoServiceImpl dtoService;

  @BeforeEach
  void setUp()
  {
    when(dao.getDomainClass()).thenReturn(Item.class);
    dtoService = new ItemDtoServiceImpl(dao, new DtoMapperImpl(), dtoValidator, binaryDataDao);
  }

  @Nested
  class CreateTests
  {
    @Test
    void createValidDto() throws Exception
    {
      ItemDTO dto = buildDto("Item", ItemType.FOOD, BigDecimal.valueOf(1.5));

      when(dtoValidator.validate(dto)).thenReturn(successResults());
      when(dao.save(any())).then(invocation -> {
        Item item = invocation.getArgument(0);
        item.setId(UUID.randomUUID());
        return item;
      });

      ItemDTO createdDto = dtoService.create(dto);

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

      assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> dtoService.create(dto));
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

      ItemDTO dto = dtoService.read(entity.getId());

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

      assertThatExceptionOfType(EntityNotFound.class).isThrownBy(() -> dtoService.read(id));
    }
  }

  @Nested
  class UpdateTests
  {
    @Test
    void updateValidDto() throws Exception
    {
      Item entity = mockRead(UUID.randomUUID());

      ItemDTO dto = buildDto("Item", ItemType.FOOD, BigDecimal.valueOf(2.5));
      when(dtoValidator.validate(dto)).thenReturn(successResults());

      when(dao.save(any())).then(invocation -> invocation.getArgument(0));

      ItemDTO updatedDto = dtoService.update(entity.getId(), dto);

      assertAll(() -> assertThat(updatedDto.getId()).isEqualTo(entity.getId()),
        () -> assertThat(updatedDto.getName()).isEqualTo(dto.getName()),
        () -> assertThat(updatedDto.getType()).isEqualTo(dto.getType()),
        () -> assertThat(updatedDto.getPrice()).isEqualTo(dto.getPrice()));
    }

    @Test
    void updateInvalidDto() throws Exception
    {
      Item entity = mockRead(UUID.randomUUID());

      ItemDTO dto = buildDto("Item", ItemType.FOOD, BigDecimal.valueOf(1.5));
      when(dtoValidator.validate(dto)).then(invocation -> {
        ValidationResults results = new ValidationResults();
        results.addError(emptyOrNullValue("name"));
        results.addError(emptyOrNullValue("price"));
        return results;
      });

      assertThatExceptionOfType(ValidationException.class).isThrownBy(() -> dtoService.update(entity.getId(), dto));
      verify(dao, never()).save(any());
    }

    @Test
    void updateNonExistingDto() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(dao.read(id)).thenThrow(new EntityNotFound(Item.class, id));

      assertThatExceptionOfType(EntityNotFound.class).isThrownBy(() -> dtoService.update(id, new ItemDTO()));
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

      assertThatNoException().isThrownBy(() -> dtoService.delete(entity.getId()));
      verify(dao).delete(entity);
    }

    @Test
    void deleteNonExistingDto() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(dao.read(id)).thenThrow(new EntityNotFound(Item.class, id));

      assertThatExceptionOfType(EntityNotFound.class).isThrownBy(() -> dtoService.delete(id));
      verify(dao, never()).delete(any(Item.class));
    }
  }

  @Nested
  class ListTests
  {
    List<Item> entities;

    Pageable pageable;

    @Captor
    ArgumentCaptor<Example<Item>> exampleArg;

    @BeforeEach
    void setUp()
    {
      entities = IntStream.range(0, 20)
        .mapToObj(i -> buildEntity("Item-" + i, ItemType.FOOD, BigDecimal.valueOf(1.5)))
        .toList();
      entities.forEach(entity -> entity.setId(UUID.randomUUID()));

      pageable = PageRequest.of(0, entities.size());
    }

    @Test
    void listDtos()
    {
      when(dao.findAll(pageable)).thenReturn(new PageImpl<>(entities, pageable, 50));

      Page<ItemDTO> page = dtoService.list(pageable, null, null);

      assertAll(
          () -> assertThat(page.getContent()).hasSameSizeAs(entities),
          () -> assertThat(page.getPageable()).isEqualTo(pageable));
    }

    @Test
    void listByExample()
    {
      when(dao.findAll(exampleArg.capture(), eq(pageable)))
        .thenReturn(new PageImpl<>(entities, pageable, 50));

      ItemDTO criteria = buildDto("item", ItemType.FOOD, null);
      dtoService.list(pageable, criteria, null);

      Example<Item> example = exampleArg.getValue();
      assertAll(() -> assertThat(example.getMatcher()).isEqualTo(ExampleMatcher.matching()),
          () -> assertThat(example.getProbe()).hasFieldOrPropertyWithValue("name", criteria.getName())
            .hasFieldOrPropertyWithValue("type", criteria.getType())
            .hasFieldOrPropertyWithValue("price", criteria.getPrice()));
    }

    @Test
    void listByExampleAndMatcher()
    {
      when(dao.findAll(exampleArg.capture(), eq(pageable)))
        .thenReturn(new PageImpl<>(entities, pageable, 50));

      ItemDTO criteria = buildDto(null, ItemType.FOOD, null);
      ExampleMatcher exampleMatcher = ExampleMatcher.matchingAny()
        .withIgnoreCase()
        .withIgnoreNullValues();
      dtoService.list(pageable, criteria, exampleMatcher);

      Example<Item> example = exampleArg.getValue();
      assertAll(() -> assertThat(example.getMatcher()).isEqualTo(exampleMatcher),
          () -> assertThat(example.getProbe()).hasFieldOrPropertyWithValue("name", criteria.getName())
            .hasFieldOrPropertyWithValue("type", criteria.getType())
            .hasFieldOrPropertyWithValue("price", criteria.getPrice()));
    }
  }

  @Nested
  class FindByIdTests
  {
    @Test
    void findDtoById() throws Exception
    {
      Item entity = buildEntity("Item", ItemType.FOOD, BigDecimal.valueOf(2));
      entity.setId(UUID.randomUUID());
      when(dao.findById(entity.getId())).thenReturn(Optional.of(entity));

      Optional<ItemDTO> dtoOpt = dtoService.findById(entity.getId());

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

      Optional<ItemDTO> dtoOpt = dtoService.findById(id);

      assertThat(dtoOpt).isEmpty();
    }
  }

  @Nested
  class GetPictureTests
  {
    @Test
    void getPicture() throws Exception
    {
      Item entity = buildEntity("Item", ItemType.FOOD, BigDecimal.valueOf(2));
      entity.setId(UUID.randomUUID());
      entity.setPicture(new BinaryData());
      when(dao.read(entity.getId())).thenReturn(entity);

      Optional<BinaryDataDTO> image = dtoService.getImage(entity.getId());

      assertThat(image).isNotEmpty();
    }

    @Test
    void getEmptyPicture() throws Exception
    {
      Item item = mockRead(UUID.randomUUID());

      Optional<BinaryDataDTO> image = dtoService.getImage(item.getId());

      assertThat(image).isEmpty();
    }

    @Test
    void getFromNonExistingUser() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(dao.read(id)).thenThrow(new EntityNotFound(Item.class, id));

      assertThatExceptionOfType(EntityNotFound.class).isThrownBy(() -> dtoService.getImage(id));
    }
  }

  @Nested
  class UploadImageTests
  {
    @Test
    void uploadImage() throws Exception
    {
      Item item = mockRead(UUID.randomUUID());

      when(binaryDataDao.save(any())).then(invoc -> {
        BinaryData binary = invoc.getArgument(0);
        binary.setId(UUID.randomUUID());
        return binary;
      });
      when(dao.save(item)).thenReturn(item);

      BinaryDataDTO dto = buildBinary("picture.jpg", "image/jpg");
      ItemDTO updatedItem = dtoService.uploadImage(item.getId(), dto);

      assertThat(updatedItem.getPictureId()).isEqualTo(item.getPicture()
        .getId());
    }

    @Test
    void uploadImageToNonExistingUser() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(dao.read(id)).thenThrow(new EntityNotFound(AppUser.class, id));

      assertThatExceptionOfType(EntityNotFound.class).isThrownBy(() -> dtoService.uploadImage(id, new BinaryDataDTO()));
      verify(binaryDataDao, never()).save(any());
      verify(dao, never()).save(any());
    }

    BinaryDataDTO buildBinary(final String name, final String contentType)
    {
      BinaryDataDTO dto = new BinaryDataDTO();
      dto.setName(name);
      dto.setContentType(contentType);
      dto.setContent(new byte[0]);

      return dto;
    }
  }

  Item buildEntity(final String name, final ItemType type, final BigDecimal price)
  {
    return ItemBuilder.builder()
      .name(name)
      .type(type)
      .price(price)
      .build();
  }

  ItemDTO buildDto(final String name, final ItemType type, final BigDecimal price)
  {
    return ItemBuilder.builder()
      .name(name)
      .type(type)
      .price(price)
      .buildDto();
  }

  Item mockRead(final UUID id) throws Exception
  {
    Item entity = buildEntity("Item", ItemType.FOOD, BigDecimal.valueOf(2.0));
    entity.setId(id);
    when(dao.read(entity.getId())).thenReturn(entity);

    return entity;
  }
}
