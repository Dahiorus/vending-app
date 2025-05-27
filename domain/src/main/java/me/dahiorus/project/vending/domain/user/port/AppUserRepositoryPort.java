package me.dahiorus.project.vending.domain.user.port;

import java.util.Optional;
import me.dahiorus.project.vending.domain.CreateSpi;
import me.dahiorus.project.vending.domain.DeleteSpi;
import me.dahiorus.project.vending.domain.FindSpi;
import me.dahiorus.project.vending.domain.UpdateSpi;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.user.entity.AppUser;
import me.dahiorus.project.vending.domain.user.entity.AppUserToCreate;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.Password;
import me.dahiorus.project.vending.domain.user.entity.UserId;

public interface AppUserRepositoryPort
    extends CreateSpi<AppUserToCreate, AppUser>,
        FindSpi<UserId, AppUser>,
        UpdateSpi<AppUser, AppUser>,
        DeleteSpi<UserId> {
  Optional<AppUser> findByUsername(EmailAddress emailAddress);

  void updatePassword(UserId userId, Password password) throws ResourceNotFound;
}
