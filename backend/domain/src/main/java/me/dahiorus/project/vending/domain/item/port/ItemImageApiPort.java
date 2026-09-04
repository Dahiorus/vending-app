package me.dahiorus.project.vending.domain.item.port;

import java.util.Optional;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.file.entity.FileToUpload;
import me.dahiorus.project.vending.domain.file.entity.UploadedFile;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.item.entity.ItemWithImage;

public interface ItemImageApiPort {
  ItemWithImage uploadImage(ItemId itemId, FileToUpload image) throws ResourceNotFound;

  Optional<UploadedFile> findImage(ItemId itemId) throws ResourceNotFound;
}
