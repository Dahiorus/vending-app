package me.dahiorus.project.vending.application.service.item;

import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.item.entity.Item;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.item.entity.ItemToCreate;
import me.dahiorus.project.vending.domain.item.entity.ItemToUpdate;
import me.dahiorus.project.vending.domain.item.port.ItemApiPort;
import me.dahiorus.project.vending.domain.item.port.ItemRepositoryPort;
import me.dahiorus.project.vending.domain.pagination.entity.Filter;
import me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher;
import me.dahiorus.project.vending.domain.pagination.entity.PageResult;
import me.dahiorus.project.vending.domain.pagination.entity.Pagination;
import me.dahiorus.project.vending.domain.pagination.entity.Total;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class ItemApplicationService implements ItemApiPort {

  private final ItemRepositoryPort itemRepository;

  public ItemApplicationService(final ItemRepositoryPort itemRepository) {
    this.itemRepository = itemRepository;
  }

  @Override
  public Item create(final ItemToCreate itemToCreate) {
    return itemRepository.create(itemToCreate);
  }

  @Override
  public Item read(final ItemId id) throws ResourceNotFound {
    return itemRepository.find(id).orElseThrow(() -> new ResourceNotFound(id));
  }

  @Override
  public Item update(final ItemToUpdate itemToUpdate) {
    return itemRepository.update(itemToUpdate);
  }

  @Override
  public void delete(final ItemId id) {
    itemRepository.delete(id);
  }

  @Override
  public PageResult<Item> search(
      final Pagination pagination, final Item example, final FilterMatcher filterMatcher) {
    var filter = new Filter<>(example, filterMatcher);
    var items = itemRepository.search(pagination, filter);
    var count = itemRepository.count(filter);

    return new PageResult<>(items, pagination, new Total(count));
  }
}
