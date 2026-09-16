package me.dahiorus.project.vending.domain.user.port;

import java.util.Optional;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.file.entity.FileToUpload;
import me.dahiorus.project.vending.domain.file.entity.UploadedFile;
import me.dahiorus.project.vending.domain.user.entity.UserId;

public interface AppUserProfilePictureRepositoryPort {
  UploadedFile uploadPicture(UserId userId, FileToUpload profilePicture) throws ResourceNotFound;

  Optional<UploadedFile> findPicture(UserId userId) throws ResourceNotFound;
}
