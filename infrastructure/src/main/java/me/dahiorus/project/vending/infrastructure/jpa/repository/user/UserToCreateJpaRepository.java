package me.dahiorus.project.vending.infrastructure.jpa.repository.user;

import java.util.Optional;
import me.dahiorus.project.vending.domain.user.entity.UserId;
import me.dahiorus.project.vending.domain.user.entity.UserToCreate;
import me.dahiorus.project.vending.domain.user.port.UserToCreateRepositoryPort;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaUser;
import org.springframework.stereotype.Repository;

@Repository
public class UserToCreateJpaRepository implements UserToCreateRepositoryPort {

  private final JpaUserDao jpaRepository;

  public UserToCreateJpaRepository(final JpaUserDao jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Override
  public Optional<UserId> findDuplicateOf(final UserToCreate userToCreate) {
    return jpaRepository
        .findByEmail(userToCreate.emailAddress().value())
        .map(JpaUser::getId)
        .map(UserId::new);
  }
}
