package me.dahiorus.project.vending.domain.item.port;

import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.item.entity.Item;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.item.entity.ItemToCreate;
import me.dahiorus.project.vending.domain.item.entity.ItemToUpdate;
import me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher;
import me.dahiorus.project.vending.domain.pagination.entity.PageResult;
import me.dahiorus.project.vending.domain.pagination.entity.Pagination;

public interface ItemApiPort {
  Item create(ItemToCreate itemToCreate);

  Item read(ItemId id) throws ResourceNotFound;

  Item update(ItemToUpdate itemToUpdate);

  void delete(ItemId id);

  PageResult<Item> search(Pagination pagination, Item example, FilterMatcher filterMatcher);
}
