package me.dahiorus.project.vending.infrastructure.jpa.repository.item;

import static me.dahiorus.project.vending.infrastructure.jpa.repository.ExampleMatcherAdapter.toExample;
import static me.dahiorus.project.vending.infrastructure.jpa.repository.ToPageableConverter.toPageable;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.file.entity.FileToUpload;
import me.dahiorus.project.vending.domain.file.entity.UploadedFile;
import me.dahiorus.project.vending.domain.item.entity.Item;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.item.entity.ItemToCreate;
import me.dahiorus.project.vending.domain.item.entity.ItemToUpdate;
import me.dahiorus.project.vending.domain.item.entity.ItemWithImage;
import me.dahiorus.project.vending.domain.item.port.ItemRepositoryPort;
import me.dahiorus.project.vending.domain.pagination.entity.Filter;
import me.dahiorus.project.vending.domain.pagination.entity.Pagination;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaItem;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaUploadedFile;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;

@CacheConfig(cacheNames = "items")
@Repository
public class ItemRepositoryAdapter implements ItemRepositoryPort {
  private final JpaRepository<JpaItem, UUID> jpaRepository;
  private final JpaRepository<JpaUploadedFile, UUID> jpaUploadedFileRepository;

  public ItemRepositoryAdapter(EntityManager entityManager) {
    this.jpaRepository = new SimpleJpaRepository<>(JpaItem.class, entityManager);
    this.jpaUploadedFileRepository =
        new SimpleJpaRepository<>(JpaUploadedFile.class, entityManager);
  }

  @Cacheable(key = "#id.value")
  @Override
  public Optional<Item> find(ItemId id) {
    return jpaRepository.findById(id.value()).map(JpaItem::toDomain);
  }

  @CachePut(key = "#result.id.value")
  @Override
  public Item create(ItemToCreate itemToCreate) {
    return jpaRepository.save(JpaItem.createFrom(itemToCreate)).toDomain();
  }

  @CachePut(key = "#result.id")
  @Override
  public Item update(ItemToUpdate toUpdate) {
    return find(toUpdate.id())
        .map(item -> item.updateFrom(toUpdate))
        .map(JpaItem::fromDomain)
        .map(jpaRepository::save)
        .map(JpaItem::toDomain)
        .orElseThrow(() -> new ResourceNotFound(toUpdate.id()));
  }

  @CacheEvict(key = "#itemId.value")
  @Override
  public void delete(ItemId itemId) {
    jpaRepository.deleteById(itemId.value());
  }

  @Override
  public List<Item> search(Pagination pagination, Filter<Item> filter) {
    return jpaRepository
        .findAll(toExample(filter, JpaItem::fromDomain), toPageable(pagination))
        .map(JpaItem::toDomain)
        .toList();
  }

  @Override
  public long count(Filter<Item> filter) {
    return jpaRepository.count(toExample(filter, JpaItem::fromDomain));
  }

  @CachePut(cacheNames = "itemImages", key = "#result.item.id.value")
  @Override
  public ItemWithImage uploadImage(final ItemId itemId, final FileToUpload image)
      throws ResourceNotFound {
    return jpaRepository
        .findById(itemId.value())
        .map(
            jpaItem -> {
              var uploadedImage = jpaUploadedFileRepository.save(JpaUploadedFile.toCreate(image));
              jpaItem.setImage(uploadedImage);
              jpaRepository.save(jpaItem);

              return new ItemWithImage(jpaItem.toDomain(), uploadedImage.toDomain());
            })
        .orElseThrow(() -> new ResourceNotFound(itemId));
  }

  @Cacheable(value = "itemImages", key = "#itemId.value")
  @Override
  public Optional<UploadedFile> findImage(final ItemId itemId) {
    var jpaItem =
        jpaRepository.findById(itemId.value()).orElseThrow(() -> new ResourceNotFound(itemId));

    return jpaItem.maybeImage().map(JpaUploadedFile::toDomain);
  }
}
