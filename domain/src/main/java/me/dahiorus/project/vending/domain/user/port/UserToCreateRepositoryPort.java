package me.dahiorus.project.vending.domain.user.port;

import java.util.Optional;
import me.dahiorus.project.vending.domain.user.entity.UserId;
import me.dahiorus.project.vending.domain.user.entity.UserToCreate;

public interface UserToCreateRepositoryPort {
  Optional<UserId> findDuplicateOf(UserToCreate userToCreate);
}
