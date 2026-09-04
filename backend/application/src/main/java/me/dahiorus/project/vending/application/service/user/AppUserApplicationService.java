package me.dahiorus.project.vending.application.service.user;

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
import me.dahiorus.project.vending.domain.user.entity.Password;
import me.dahiorus.project.vending.domain.user.entity.UserId;
import me.dahiorus.project.vending.domain.user.entity.UserToCreate;
import me.dahiorus.project.vending.domain.user.port.AppUserApiPort;
import me.dahiorus.project.vending.domain.user.port.AppUserProfilePictureRepositoryPort;
import me.dahiorus.project.vending.domain.user.port.AppUserRepositoryPort;
import me.dahiorus.project.vending.domain.user.usecase.UpdateUserPassword;
import me.dahiorus.project.vending.domain.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
public class AppUserApplicationService implements AppUserApiPort {

  private final AppUserRepositoryPort appUserRepository;
  private final AppUserProfilePictureRepositoryPort profilePictureRepository;
  private final UpdateUserPassword updateUserPassword;
  private final Validator<UserToCreate> userToCreateValidator;
  private final Validator<Password> userPasswordValidator;

  public AppUserApplicationService(
      final AppUserRepositoryPort appUserRepository,
      final AppUserProfilePictureRepositoryPort profilePictureRepository,
      final UpdateUserPassword updateUserPassword,
      final Validator<UserToCreate> userToCreateValidator,
      final Validator<Password> userPasswordValidator) {
    this.appUserRepository = appUserRepository;
    this.profilePictureRepository = profilePictureRepository;
    this.updateUserPassword = updateUserPassword;
    this.userToCreateValidator = userToCreateValidator;
    this.userPasswordValidator = userPasswordValidator;
  }

  @Override
  public AppUser create(final AppUserToCreate userToCreate) throws InvalidBusinessObject {
    userToCreateValidator.validate(userToCreate);
    userPasswordValidator.validate(userToCreate.password());

    return appUserRepository.create(userToCreate);
  }

  @Override
  public AppUser read(final UserId id) throws ResourceNotFound {
    return appUserRepository.find(id).orElseThrow(() -> new ResourceNotFound(id));
  }

  @Override
  public AppUser update(final AppUser userToUpdate) throws InvalidBusinessObject {
    return appUserRepository.update(userToUpdate);
  }

  @Override
  public void delete(final UserId id) {
    appUserRepository.delete(id);
  }

  @Override
  public AppUser getByUsername(final EmailAddress emailAddress) throws ResourceNotFound {
    return appUserRepository
        .findByUsername(emailAddress)
        .orElseThrow(
            () ->
                new ResourceNotFound(
                    String.format("No user found with username %s", emailAddress)));
  }

  @Override
  public AppUserWithPicture uploadProfilePicture(final UserId userId, final FileToUpload picture)
      throws ResourceNotFound {
    return profilePictureRepository.uploadPicture(userId, picture);
  }

  @Override
  public Optional<UploadedFile> getProfilePicture(final UserId userId) throws ResourceNotFound {
    return profilePictureRepository.findPicture(userId);
  }

  @Override
  public void updateUserPassword(final UserId userId, final EditPassword editPassword)
      throws InvalidBusinessObject, ResourceNotFound {
    updateUserPassword.execute(userId, editPassword);
  }
}
