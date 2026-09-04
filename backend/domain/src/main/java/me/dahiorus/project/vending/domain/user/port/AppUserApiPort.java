package me.dahiorus.project.vending.domain.user.port;

import java.util.Optional;
import me.dahiorus.project.vending.domain.exception.InvalidBusinessObject;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.file.entity.FileToUpload;
import me.dahiorus.project.vending.domain.file.entity.UploadedFile;
import me.dahiorus.project.vending.domain.user.entity.AppUser;
import me.dahiorus.project.vending.domain.user.entity.AppUserToCreate;
import me.dahiorus.project.vending.domain.user.entity.AppUserWithPicture;
import me.dahiorus.project.vending.domain.user.entity.EditPassword;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.UserId;

public interface AppUserApiPort {
  AppUser create(AppUserToCreate userToCreate) throws InvalidBusinessObject;

  AppUser read(UserId id) throws ResourceNotFound;

  AppUser update(AppUser userToUpdate) throws InvalidBusinessObject;

  void delete(UserId id);

  AppUser getByUsername(EmailAddress emailAddress) throws ResourceNotFound;

  AppUserWithPicture uploadProfilePicture(UserId userId, FileToUpload picture)
      throws ResourceNotFound;

  Optional<UploadedFile> getProfilePicture(UserId userId) throws ResourceNotFound;

  void updateUserPassword(UserId userId, EditPassword editPassword)
      throws InvalidBusinessObject, ResourceNotFound;
}
