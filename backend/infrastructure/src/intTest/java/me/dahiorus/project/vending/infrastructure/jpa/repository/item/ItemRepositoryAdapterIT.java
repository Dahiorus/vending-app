package me.dahiorus.project.vending.infrastructure.jpa.repository.item;

import static me.dahiorus.project.vending.domain.file.entity.ContentType.JPG;
import static me.dahiorus.project.vending.domain.item.entity.ItemType.COLD_BEVERAGE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.UUID;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.file.entity.BinaryContent;
import me.dahiorus.project.vending.domain.file.entity.FileToUpload;
import me.dahiorus.project.vending.domain.file.entity.Filename;
import me.dahiorus.project.vending.domain.file.entity.UploadedFile;
import me.dahiorus.project.vending.domain.item.entity.Item;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.item.entity.ItemName;
import me.dahiorus.project.vending.domain.item.entity.ItemToCreate;
import me.dahiorus.project.vending.domain.item.entity.ItemToUpdate;
import me.dahiorus.project.vending.domain.pagination.entity.Filter;
import me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher;
import me.dahiorus.project.vending.domain.pagination.entity.Pagination;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaUploadedFile;
import me.dahiorus.project.vending.infrastructure.jpa.repository.H2DbContainer;
import me.dahiorus.project.vending.infrastructure.jpa.repository.item.ItemRepositoryAdapterIT.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestConfig.class)
class ItemRepositoryAdapterIT extends H2DbContainer {

  @Autowired ItemRepositoryAdapter repository;

  @Test
  void should_create_item() {
    var itemToCreate =
        new ItemToCreate(ItemName.of("Coca-Cola 33cL"), COLD_BEVERAGE, BigDecimal.valueOf(1.50));

    var result = repository.create(itemToCreate);

    assertThat(result)
        .usingRecursiveComparison()
        .ignoringFields("id")
        .isEqualTo(
            new Item(null, ItemName.of("Coca-Cola 33cL"), BigDecimal.valueOf(1.50), COLD_BEVERAGE));
  }

  @Nested
  class Find {
    @Test
    void should_find_item_by_id() {
      var itemToCreate =
          new ItemToCreate(ItemName.of("Coca-Cola 33cL"), COLD_BEVERAGE, BigDecimal.valueOf(1.50));
      var createdItem = repository.create(itemToCreate);
      entityManager.flush();

      var result = repository.find(createdItem.id());

      assertThat(result).contains(createdItem);
    }

    @Test
    void should_return_empty_when_item_not_found() {
      var result = repository.find(new ItemId(UUID.randomUUID()));

      assertThat(result).isEmpty();
    }
  }

  @Nested
  class Update {
    @Test
    void should_update_given_item_by_id() {
      var itemToCreate =
          new ItemToCreate(ItemName.of("Coca-Cola 33cL"), COLD_BEVERAGE, BigDecimal.valueOf(1.50));
      var createdItem = repository.create(itemToCreate);
      entityManager.flush();

      var itemToUpdate = new ItemToUpdate(createdItem.id(), BigDecimal.valueOf(2.00));
      var updatedItem = repository.update(itemToUpdate);

      assertThat(updatedItem)
          .isEqualTo(
              new Item(
                  createdItem.id(),
                  ItemName.of("Coca-Cola 33cL"),
                  BigDecimal.valueOf(2.00),
                  COLD_BEVERAGE));
    }

    @Test
    void should_throw_exception_when_update_non_existent_item() {
      var itemToUpdate = new ItemToUpdate(new ItemId(UUID.randomUUID()), BigDecimal.valueOf(2.00));

      assertThatThrownBy(() -> repository.update(itemToUpdate))
          .isInstanceOf(ResourceNotFound.class)
          .hasMessage("Resource not found with ID: " + itemToUpdate.id());
    }
  }

  @Test
  void should_delete_item_by_id() {
    var itemToCreate =
        new ItemToCreate(ItemName.of("Coca-Cola 33cL"), COLD_BEVERAGE, BigDecimal.valueOf(1.50));
    var createdItem = repository.create(itemToCreate);
    entityManager.flush();

    repository.delete(createdItem.id());

    assertThat(repository.find(createdItem.id())).isEmpty();
  }

  @Nested
  class SearchAndCount {
    Item item1, item2, item3;

    @BeforeEach
    void setUpItems() {
      item1 =
          repository.create(
              new ItemToCreate(
                  ItemName.of("Coca-Cola 33cL"), COLD_BEVERAGE, BigDecimal.valueOf(1.50)));
      item2 =
          repository.create(
              new ItemToCreate(ItemName.of("Pepsi 33cL"), COLD_BEVERAGE, BigDecimal.valueOf(1.50)));
      item3 =
          repository.create(
              new ItemToCreate(ItemName.of("Fanta 33cL"), COLD_BEVERAGE, BigDecimal.valueOf(1.50)));
      entityManager.flush();
    }

    @Nested
    class Search {
      @Test
      void should_return_all_items_given_empty_filter() {
        var result =
            repository.search(
                new Pagination(),
                new Filter<>(new Item(null, null, null, null), new FilterMatcher()));

        assertThat(result).containsExactly(item1, item2, item3);
      }

      @Test
      void should_return_filtered_items() {
        var result =
            repository.search(
                new Pagination(),
                new Filter<>(
                    new Item(null, ItemName.of("Coca-Cola 33cL"), null, null),
                    new FilterMatcher()));

        assertThat(result).containsExactly(item1);
      }
    }

    @Nested
    class Count {
      @Test
      void should_count_all_items_given_empty_filter() {
        var count =
            repository.count(new Filter<>(new Item(null, null, null, null), new FilterMatcher()));

        assertThat(count).isEqualTo(3);
      }

      @Test
      void should_count_filtered_items() {
        var count =
            repository.count(
                new Filter<>(
                    new Item(null, ItemName.of("Coca-Cola 33cL"), null, null),
                    new FilterMatcher()));

        assertThat(count).isEqualTo(1);
      }
    }
  }

  @Nested
  class PictureTests {
    Item item;

    @BeforeEach
    void setUpItem() {
      item =
          repository.create(
              new ItemToCreate(
                  ItemName.of("Coca-Cola 33cL"), COLD_BEVERAGE, BigDecimal.valueOf(1.50)));
      entityManager.flush();
    }

    @Nested
    class UploadPicture {
      @Test
      void should_upload_picture_for_item() {
        var picture =
            new FileToUpload(
                new Filename("coca-cola.jpg"), new BinaryContent(new byte[] {1, 2, 3}), JPG);

        var result = repository.uploadImage(item.id(), picture);

        assertThat(result)
            .satisfies(
                itemWithImage -> {
                  assertThat(itemWithImage.item()).isEqualTo(item);
                  assertThat(itemWithImage.image())
                      .usingRecursiveComparison()
                      .ignoringFields("id", "uploadedAt")
                      .isEqualTo(
                          new UploadedFile(
                              null,
                              new Filename("coca-cola.jpg"),
                              new BinaryContent(new byte[] {1, 2, 3}),
                              JPG,
                              null));
                  assertThat(itemWithImage.image().id()).isNotNull();
                });
      }

      @Test
      void should_throw_exception_when_upload_picture_for_non_existent_item() {
        var itemId = new ItemId(UUID.randomUUID());
        var picture =
            new FileToUpload(
                new Filename("coca-cola.jpg"), new BinaryContent(new byte[] {1, 2, 3}), JPG);

        assertThatThrownBy(() -> repository.uploadImage(itemId, picture))
            .isInstanceOf(ResourceNotFound.class)
            .hasMessageContaining("Resource not found with ID: " + itemId);
      }

      @Test
      void should_upload_and_replace_old_picture() {
        // Given
        var oldPicture =
            new FileToUpload(
                new Filename("old-coca-cola.jpg"), new BinaryContent(new byte[] {1, 2, 3}), JPG);
        var itemWithPictureToReplace = repository.uploadImage(item.id(), oldPicture);
        entityManager.flush();

        // When
        var newPicture =
            new FileToUpload(
                new Filename("new-coca-cola.jpg"), new BinaryContent(new byte[] {4, 5, 6}), JPG);
        var result = repository.uploadImage(item.id(), newPicture);
        entityManager.flush();

        assertThat(result.image())
            .usingRecursiveComparison()
            .ignoringFields("id", "uploadedAt")
            .isEqualTo(
                new UploadedFile(
                    null,
                    new Filename("new-coca-cola.jpg"),
                    new BinaryContent(new byte[] {4, 5, 6}),
                    JPG,
                    null));
        assertThat(
                entityManager.find(
                    JpaUploadedFile.class, itemWithPictureToReplace.image().id().value()))
            .isNull();
      }
    }

    @Nested
    class FindPicture {
      @Test
      void should_find_empty_picture_for_given_item() {
        var result = repository.findImage(item.id());

        assertThat(result).isEmpty();
      }

      @Test
      void should_find_picture_for_given_item() {
        // Given
        var picture =
            new FileToUpload(
                new Filename("coca-cola.jpg"), new BinaryContent(new byte[] {1, 2, 3}), JPG);
        repository.uploadImage(item.id(), picture);
        entityManager.flush();

        // When
        var result = repository.findImage(item.id());

        // Then
        assertThat(result)
            .get()
            .usingRecursiveComparison()
            .ignoringFields("id", "uploadedAt")
            .isEqualTo(
                new UploadedFile(
                    null,
                    new Filename("coca-cola.jpg"),
                    new BinaryContent(new byte[] {1, 2, 3}),
                    JPG,
                    null));
      }

      @Test
      void should_throw_exception_when_find_picture_for_non_existent_item() {
        var itemId = new ItemId(UUID.randomUUID());

        assertThatThrownBy(() -> repository.findImage(itemId))
            .isInstanceOf(ResourceNotFound.class)
            .hasMessageContaining("Resource not found with ID: " + itemId);
      }
    }
  }

  static class TestConfig {
    @Bean
    ItemRepositoryAdapter itemJpaRepository(EntityManager entityManager) {
      return new ItemRepositoryAdapter(entityManager);
    }
  }
}
