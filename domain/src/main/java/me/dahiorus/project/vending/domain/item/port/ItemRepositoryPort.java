package me.dahiorus.project.vending.domain.item.port;

import java.util.Optional;
import me.dahiorus.project.vending.domain.CreateSpi;
import me.dahiorus.project.vending.domain.DeleteSpi;
import me.dahiorus.project.vending.domain.FindSpi;
import me.dahiorus.project.vending.domain.SearchSpi;
import me.dahiorus.project.vending.domain.UpdateSpi;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.file.entity.FileToUpload;
import me.dahiorus.project.vending.domain.file.entity.UploadedFile;
import me.dahiorus.project.vending.domain.item.entity.Item;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.item.entity.ItemToCreate;
import me.dahiorus.project.vending.domain.item.entity.ItemToUpdate;
import me.dahiorus.project.vending.domain.item.entity.ItemWithImage;

public interface ItemRepositoryPort
    extends CreateSpi<ItemToCreate, Item>,
        FindSpi<ItemId, Item>,
        UpdateSpi<ItemToUpdate, Item>,
        DeleteSpi<ItemId>,
        SearchSpi<Item, Item> {
  ItemWithImage uploadImage(ItemId itemId, FileToUpload picture) throws ResourceNotFound;

  Optional<UploadedFile> findImage(ItemId itemId);
}
