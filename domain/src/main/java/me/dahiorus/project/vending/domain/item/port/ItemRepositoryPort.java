package me.dahiorus.project.vending.domain.item.port;

import java.util.Optional;
import me.dahiorus.project.vending.domain.Creatable;
import me.dahiorus.project.vending.domain.Deletable;
import me.dahiorus.project.vending.domain.Findable;
import me.dahiorus.project.vending.domain.Searchable;
import me.dahiorus.project.vending.domain.Updatable;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.file.entity.FileToUpload;
import me.dahiorus.project.vending.domain.file.entity.UploadedFile;
import me.dahiorus.project.vending.domain.item.entity.Item;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.item.entity.ItemToCreate;
import me.dahiorus.project.vending.domain.item.entity.ItemToUpdate;
import me.dahiorus.project.vending.domain.item.entity.ItemWithImage;

public interface ItemRepositoryPort
    extends Creatable<ItemToCreate, Item>,
        Findable<ItemId, Item>,
        Updatable<ItemToUpdate, Item>,
        Deletable<ItemId>,
        Searchable<Item, Item> {
  ItemWithImage uploadImage(ItemId itemId, FileToUpload picture) throws ResourceNotFound;

  Optional<UploadedFile> findImage(ItemId itemId);
}
