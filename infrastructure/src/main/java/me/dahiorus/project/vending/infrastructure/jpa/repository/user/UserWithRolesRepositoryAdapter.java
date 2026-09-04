package me.dahiorus.project.vending.infrastructure.jpa.repository.user;

import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.UserWithRoles;
import me.dahiorus.project.vending.domain.user.port.UserWithRolesRepositoryPort;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaUser;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

@CacheConfig(cacheNames = "usersWithRoles")
@Repository
public class UserWithRolesRepositoryAdapter implements UserWithRolesRepositoryPort {

  private final UserJpaRepository jpaRepository;

  public UserWithRolesRepositoryAdapter(final UserJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @Cacheable(key = "#emailAddress.value()")
  @Override
  public UserWithRoles getByUsername(final EmailAddress emailAddress) throws ResourceNotFound {
    return jpaRepository
        .findByEmail(emailAddress.value())
        .map(JpaUser::toUserWithRoles)
        .orElseThrow(
            () ->
                new ResourceNotFound(
                    String.format("No user found with username [%s]", emailAddress.value())));
  }
}
