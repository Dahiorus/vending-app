package me.dahiorus.project.vending.application.service.item;

import java.util.Optional;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.file.entity.FileToUpload;
import me.dahiorus.project.vending.domain.file.entity.UploadedFile;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.item.entity.ItemWithImage;
import me.dahiorus.project.vending.domain.item.port.ItemImageApiPort;
import me.dahiorus.project.vending.domain.item.port.ItemRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class ItemImageApplicationService implements ItemImageApiPort {

  private final ItemRepositoryPort itemRepository;

  public ItemImageApplicationService(final ItemRepositoryPort itemRepository) {
    this.itemRepository = itemRepository;
  }

  @Override
  public ItemWithImage uploadImage(final ItemId itemId, final FileToUpload image)
      throws ResourceNotFound {
    return itemRepository.uploadImage(itemId, image);
  }

  @Override
  public Optional<UploadedFile> findImage(final ItemId itemId) throws ResourceNotFound {
    return itemRepository.findImage(itemId);
  }
}
