package me.dahiorus.project.vending.domain.user.port;

import java.util.Optional;
import me.dahiorus.project.vending.domain.Creatable;
import me.dahiorus.project.vending.domain.Deletable;
import me.dahiorus.project.vending.domain.Findable;
import me.dahiorus.project.vending.domain.Updatable;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.user.entity.AppUser;
import me.dahiorus.project.vending.domain.user.entity.AppUserToCreate;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.Password;
import me.dahiorus.project.vending.domain.user.entity.UserId;

public interface AppUserRepositoryPort
    extends Creatable<AppUserToCreate, AppUser>,
        Findable<UserId, AppUser>,
        Updatable<AppUser, AppUser>,
        Deletable<UserId> {
  Optional<AppUser> findByUsername(EmailAddress emailAddress);

  void updatePassword(UserId userId, Password password) throws ResourceNotFound;
}
