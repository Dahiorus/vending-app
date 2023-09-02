package me.dahiorus.project.vending.web.api.impl;

import static me.dahiorus.project.vending.util.TestUtils.jsonValue;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.PagedModel.PageMetadata;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;

import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.ValidationException;
import me.dahiorus.project.vending.domain.model.Item;
import me.dahiorus.project.vending.domain.model.ItemType;
import me.dahiorus.project.vending.domain.model.dto.BinaryDataDto;
import me.dahiorus.project.vending.domain.model.dto.ItemDto;
import me.dahiorus.project.vending.domain.service.impl.ItemDtoServiceImpl;
import me.dahiorus.project.vending.domain.service.validation.CrudOperation;
import me.dahiorus.project.vending.domain.service.validation.ValidationResults;
import me.dahiorus.project.vending.util.ItemBuilder;
import me.dahiorus.project.vending.web.api.assembler.ItemDtoModelAssembler;

@WebMvcTest(ItemRestController.class)
class ItemRestControllerTest extends RestControllerTest
{
  @MockBean
  ItemDtoServiceImpl itemDtoService;

  @MockBean
  ItemDtoModelAssembler modelAssembler;

  @MockBean
  PagedResourcesAssembler<ItemDto> pageModelAssembler;

  static ItemDto buildItem()
  {
    return ItemBuilder.builder()
      .id(UUID.randomUUID())
      .name("Item")
      .type(ItemType.COLD_BAVERAGE)
      .price(BigDecimal.valueOf(1.2))
      .buildDto();
  }

  @Nested
  class CreateTests
  {
    @Test
    @WithMockUser(username = "admin", password = "secret", roles = "ADMIN")
    void adminCanCreateItem() throws Exception
    {
      ItemDto item = buildItem();
      when(itemDtoService.create(item)).then(invoc -> {
        item.setId(UUID.randomUUID());
        return item;
      });
      when(modelAssembler.toModel(item)).thenReturn(EntityModel.of(item));

      mockMvc.perform(post("/api/v1/items").contentType(MediaType.APPLICATION_JSON)
        .content(jsonValue(item)))
        .andExpect(status().isCreated())
        .andExpect(header().string(HttpHeaders.LOCATION, endsWith("/api/v1/items/" + item.getId())))
        .andExpect(content().contentType(MediaTypes.HAL_JSON))
        .andExpectAll(
          jsonPath("id").value(item.getId()
            .toString()),
          jsonPath("name").value(item.getName()),
          jsonPath("type").value(item.getType()
            .name()),
          jsonPath("price").value(item.getPrice()));
    }

    @Test
    @WithMockUser(username = "admin", password = "secret", roles = "ADMIN")
    void validationExceptionIsThrown() throws Exception
    {
      ItemDto item = ItemBuilder.builder()
        .buildDto();
      when(itemDtoService.create(item))
        .thenThrow(new ValidationException(CrudOperation.CREATE, item, new ValidationResults()));

      mockMvc.perform(post("/api/v1/items").contentType(MediaType.APPLICATION_JSON)
        .content(jsonValue(item)))
        .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    void nonAdminIsForbidden() throws Exception
    {
      mockMvc.perform(post("/api/v1/items").contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
        .andExpect(status().isForbidden());
      verify(itemDtoService, never()).create(any());
    }

    @Test
    @WithAnonymousUser
    void anonymousUserIsUnauthorized() throws Exception
    {
      mockMvc.perform(post("/api/v1/items").contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
        .andExpect(status().isUnauthorized());
      verify(itemDtoService, never()).create(any());
    }
  }

  @Nested
  class ReadTests
  {
    @Test
    void getItem() throws Exception
    {
      ItemDto item = buildItem();
      when(itemDtoService.read(item.getId())).thenReturn(item);
      when(modelAssembler.toModel(item)).thenReturn(EntityModel.of(item));

      mockMvc.perform(get("/api/v1/items/{id}", item.getId()))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaTypes.HAL_JSON))
        .andExpectAll(
          jsonPath("id").value(item.getId()
            .toString()),
          jsonPath("name").value(item.getName()),
          jsonPath("type").value(item.getType()
            .name()),
          jsonPath("price").value(item.getPrice()));
    }

    @Test
    void getNonExistingItem() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(itemDtoService.read(id)).thenThrow(new EntityNotFound(Item.class, id));

      mockMvc.perform(get("/api/v1/items/{id}", id))
        .andExpect(status().isNotFound());
    }
  }

  @Nested
  class UpdateTests
  {
    @Test
    @WithMockUser(username = "admin", password = "secret", roles = "ADMIN")
    void adminCanUpdateItem() throws Exception
    {
      ItemDto item = buildItem();
      when(itemDtoService.update(item.getId(), item)).thenReturn(item);
      when(modelAssembler.toModel(item)).thenReturn(EntityModel.of(item));

      mockMvc.perform(put("/api/v1/items/{id}", item.getId()).contentType(MediaType.APPLICATION_JSON)
        .content(jsonValue(item)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaTypes.HAL_JSON))
        .andExpectAll(
          jsonPath("id").value(item.getId()
            .toString()),
          jsonPath("name").value(item.getName()),
          jsonPath("type").value(item.getType()
            .name()),
          jsonPath("price").value(item.getPrice()));
    }

    @Test
    @WithMockUser(username = "admin", password = "secret", roles = "ADMIN")
    void validationExceptionIsThrown() throws Exception
    {
      ItemDto item = ItemBuilder.builder()
        .id(UUID.randomUUID())
        .buildDto();
      when(itemDtoService.update(item.getId(), item))
        .thenThrow(new ValidationException(CrudOperation.UPDATE, item, new ValidationResults()));

      mockMvc.perform(put("/api/v1/items/{id}", item.getId()).contentType(MediaType.APPLICATION_JSON)
        .content(jsonValue(item)))
        .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", password = "secret", roles = "ADMIN")
    void createItemWithId() throws Exception
    {
      ItemDto item = buildItem();
      when(itemDtoService.update(item.getId(), item))
        .thenThrow(new EntityNotFound(Item.class, item.getId()));
      when(itemDtoService.create(item)).thenReturn(item);
      when(modelAssembler.toModel(item)).thenReturn(EntityModel.of(item));

      mockMvc.perform(put("/api/v1/items/{id}", item.getId()).contentType(MediaType.APPLICATION_JSON)
        .content(jsonValue(item)))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaTypes.HAL_JSON))
        .andExpectAll(
          jsonPath("id").value(item.getId()
            .toString()),
          jsonPath("name").value(item.getName()),
          jsonPath("type").value(item.getType()
            .name()),
          jsonPath("price").value(item.getPrice()));
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    void nonAdminIsForbidden() throws Exception
    {
      mockMvc.perform(put("/api/v1/items/{id}", UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
        .andExpect(status().isForbidden());
      verify(itemDtoService, never()).update(any(), any());
    }

    @Test
    @WithAnonymousUser
    void anonymousUserIsUnauthorized() throws Exception
    {
      mockMvc.perform(put("/api/v1/items/{id}", UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
        .andExpect(status().isUnauthorized());
      verify(itemDtoService, never()).update(any(), any());
    }
  }

  @Nested
  class DeleteTests
  {
    @Test
    @WithMockUser(username = "admin", password = "secret", roles = "ADMIN")
    void adminCanDeleteItem() throws Exception
    {
      UUID id = UUID.randomUUID();

      mockMvc.perform(delete("/api/v1/items/{id}", id))
        .andExpect(status().isNoContent());
      verify(itemDtoService).delete(id);
    }

    @Test
    @WithMockUser(username = "admin", password = "secret", roles = "ADMIN")
    void deleteNonExistingItem() throws Exception
    {
      UUID id = UUID.randomUUID();
      doThrow(new EntityNotFound(Item.class, id)).when(itemDtoService)
        .delete(id);

      mockMvc.perform(delete("/api/v1/items/{id}", id))
        .andExpect(status().isNoContent());
      verify(itemDtoService).delete(id);
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    void nonAdminIsForbidden() throws Exception
    {
      mockMvc.perform(delete("/api/v1/items/{id}", UUID.randomUUID()))
        .andExpect(status().isForbidden());
      verify(itemDtoService, never()).delete(any());
    }

    @Test
    @WithAnonymousUser
    void anonymousUserIsUnauthorized() throws Exception
    {
      mockMvc.perform(delete("/api/v1/items/{id}", UUID.randomUUID()))
        .andExpect(status().isUnauthorized());
      verify(itemDtoService, never()).delete(any());
    }
  }

  @Nested
  class PatchTests
  {
    @Test
    @WithMockUser(username = "admin", password = "secret", roles = "ADMIN")
    void adminCanUpdateItem() throws Exception
    {
      ItemDto item = buildItem();
      when(itemDtoService.read(item.getId())).thenReturn(item);
      when(itemDtoService.update(eq(item.getId()), any())).then(invoc -> invoc.getArgument(1));
      when(modelAssembler.toModel(any())).then(invoc -> EntityModel.of(invoc.getArgument(0)));

      mockMvc.perform(patch("/api/v1/items/{id}", item.getId()).contentType("application/json-patch+json")
        .content("[ { \"path\": \"/name\", \"op\": \"replace\", \"value\": \"Other\" } ]"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaTypes.HAL_JSON))
        .andExpectAll(
          jsonPath("id").value(item.getId()
            .toString()),
          jsonPath("name").value("Other"),
          jsonPath("type").value(item.getType()
            .name()),
          jsonPath("price").value(item.getPrice()));
    }

    @Test
    @WithMockUser(username = "admin", password = "secret", roles = "ADMIN")
    void validationExceptionIsThrown() throws Exception
    {
      ItemDto item = ItemBuilder.builder()
        .id(UUID.randomUUID())
        .buildDto();
      when(itemDtoService.read(item.getId())).thenReturn(item);
      when(itemDtoService.update(eq(item.getId()), any()))
        .thenThrow(new ValidationException(CrudOperation.UPDATE, item, new ValidationResults()));

      mockMvc.perform(patch("/api/v1/items/{id}", item.getId()).contentType("application/json-patch+json")
        .content("[]"))
        .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", password = "secret", roles = "ADMIN")
    void patchNonExistingItem() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(itemDtoService.read(id)).thenThrow(new EntityNotFound(Item.class, id));

      mockMvc.perform(patch("/api/v1/items/{id}", id).contentType("application/json-patch+json")
        .content("[]"))
        .andExpect(status().isNotFound());
      verify(itemDtoService, never()).update(eq(id), any());
    }

    @Test
    @WithMockUser(username = "admin", password = "secret", roles = "ADMIN")
    void badRequestOnPatch() throws Exception
    {
      UUID id = UUID.randomUUID();

      mockMvc.perform(patch("/api/v1/items/{id}", id).contentType("application/json-patch+json")
        .content("{ \"name\": \"test\" }"))
        .andExpect(status().isBadRequest());
      verifyNoInteractions(itemDtoService);
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    void nonAdminIsForbidden() throws Exception
    {
      mockMvc.perform(patch("/api/v1/items/{id}", UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON)
        .content("[]"))
        .andExpect(status().isForbidden());
      verifyNoInteractions(itemDtoService);
    }

    @Test
    @WithAnonymousUser
    void anonymousUserIsUnauthorized() throws Exception
    {
      mockMvc.perform(put("/api/v1/items/{id}", UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON)
        .content("{}"))
        .andExpect(status().isUnauthorized());
      verifyNoInteractions(itemDtoService);
    }
  }

  @Nested
  class ListTests
  {
    @Test
    @WithMockUser(username = "admin", password = "secret", roles = "ADMIN")
    void getItems() throws Exception
    {
      Pageable pageable = PageRequest.of(0, 20, Direction.DESC, "name");
      List<ItemDto> content = List.of(buildItem());
      PageImpl<ItemDto> page = new PageImpl<>(content, pageable, 50);
      when(itemDtoService.list(eq(pageable), any(), any())).thenReturn(page);
      when(pageModelAssembler.toModel(page, modelAssembler)).thenReturn(PagedModel.wrap(content,
        new PageMetadata(pageable.getPageSize(), pageable.getPageNumber(), page.getTotalElements())));

      mockMvc.perform(get("/api/v1/items").queryParam("page", "1")
        .queryParam("size", "20")
        .queryParam("sort", "name,desc"))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaTypes.HAL_JSON))
        .andExpect(jsonPath("_embedded.itemDTOList").isArray());
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    void nonAdminIsForbidden() throws Exception
    {
      mockMvc.perform(get("/api/v1/items"))
        .andExpect(status().isForbidden());
      verifyNoInteractions(itemDtoService);
    }

    @Test
    @WithAnonymousUser
    void anonymousUserIsUnauthorized() throws Exception
    {
      mockMvc.perform(get("/api/v1/items"))
        .andExpect(status().isUnauthorized());
      verifyNoInteractions(itemDtoService);
    }
  }

  @Nested
  class GetPictureTests
  {
    @Test
    void getItemPicture() throws Exception
    {
      UUID id = UUID.randomUUID();
      BinaryDataDto dto = new BinaryDataDto();
      dto.setName("picture.jpg");
      dto.setContentType(MediaType.IMAGE_JPEG_VALUE);
      dto.setContent(new byte[32]);
      dto.setCreatedAt(Instant.now());
      when(itemDtoService.getImage(id)).thenReturn(Optional.of(dto));

      mockMvc.perform(get("/api/v1/items/{id}/picture", id))
        .andExpect(status().isOk())
        .andExpect(content().contentType(dto.getContentType()))
        .andExpect(content().bytes(dto.getContent()))
        .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"picture.jpg\""))
        .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "max-age=3600, public"))
        .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, dto.getSize()));
    }

    @Test
    void getItemWithoutPicture() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(itemDtoService.getImage(id)).thenReturn(Optional.empty());

      mockMvc.perform(get("/api/v1/items/{id}/picture", id))
        .andExpect(status().isNotFound());
    }

    @Test
    void getNonExistingItemPicture() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(itemDtoService.getImage(id)).thenThrow(new EntityNotFound(Item.class, id));

      mockMvc.perform(get("/api/v1/items/{id}/picture", id))
        .andExpect(status().isNotFound());
    }
  }

  @Nested
  class UploadPictureTests
  {
    @Test
    @WithMockUser(username = "admin", password = "secret", roles = "ADMIN")
    void uploadPicture() throws Exception
    {
      ItemDto item = buildItem();
      when(itemDtoService.uploadImage(eq(item.getId()), any())).thenReturn(item);

      mockMvc
        .perform(multipart("/api/v1/items/{id}/picture", item.getId())
          .file(new MockMultipartFile("file", "picture.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[32])))
        .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", password = "secret", roles = "ADMIN")
    void uploadNonImageThrowsValidationException() throws Exception
    {
      UUID id = UUID.randomUUID();

      mockMvc
        .perform(multipart("/api/v1/items/{id}/picture", id)
          .file(new MockMultipartFile("file", "test.md", MediaType.TEXT_MARKDOWN_VALUE, new byte[32])))
        .andExpect(status().isBadRequest());
      verifyNoInteractions(itemDtoService);
    }

    @Test
    @WithMockUser(username = "admin", password = "secret", roles = "ADMIN")
    void uploadImageThrowsEntityNotFound() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(itemDtoService.uploadImage(eq(id), any())).thenThrow(new EntityNotFound(Item.class, id));

      mockMvc
        .perform(multipart("/api/v1/items/{id}/picture", id)
          .file(new MockMultipartFile("file", "picture.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[32])))
        .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user", password = "secret")
    void nonAdminIsForbidden() throws Exception
    {
      mockMvc
        .perform(multipart("/api/v1/items/{id}/picture", UUID.randomUUID())
          .file(new MockMultipartFile("file", "picture.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[32])))
        .andExpect(status().isForbidden());
      verifyNoInteractions(itemDtoService);
    }

    @Test
    @WithAnonymousUser
    void anonymousUserIsUnauthorized() throws Exception
    {
      UUID id = UUID.randomUUID();
      mockMvc
        .perform(multipart("/api/v1/items/{id}/picture", id)
          .file(new MockMultipartFile("file", "picture.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[32])))
        .andExpect(status().isUnauthorized());
      verifyNoInteractions(itemDtoService);
    }
  }
}
