package me.dahiorus.project.vending.infrastructure.jpa.repository.user;

import static me.dahiorus.project.vending.infrastructure.jpa.entity.JpaUser.ROLE_USER;

import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.Set;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.user.entity.AppUser;
import me.dahiorus.project.vending.domain.user.entity.AppUserToCreate;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.Password;
import me.dahiorus.project.vending.domain.user.entity.UserId;
import me.dahiorus.project.vending.domain.user.port.AppUserRepositoryPort;
import me.dahiorus.project.vending.domain.user.port.PasswordMatcherPort;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaUser;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

@CacheConfig(cacheNames = "users")
@Repository
public class AppUserRepositoryAdapter implements AppUserRepositoryPort, PasswordMatcherPort {

  private final UserJpaRepository jpaRepository;
  private final PasswordEncoder passwordEncoder;

  public AppUserRepositoryAdapter(
      final UserJpaRepository jpaRepository, final PasswordEncoder passwordEncoder) {
    this.jpaRepository = jpaRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @CachePut(key = "#result.id.value")
  @Override
  public AppUser create(final AppUserToCreate toCreate) {
    var jpaUser = JpaUser.toCreateFrom(toCreate);
    jpaUser.setEncodedPassword(passwordEncoder.encode(toCreate.password().value()));

    return jpaRepository.save(jpaUser).toUser();
  }

  @Cacheable(key = "#id.value", unless = "#result == null")
  @Override
  public Optional<AppUser> find(final UserId id) {
    return jpaRepository.findByIdAndRoles(id.value(), Set.of(ROLE_USER)).map(JpaUser::toUser);
  }

  @CachePut(key = "#result.id.value")
  @Override
  public AppUser update(final AppUser toUpdate) {
    try {
      var userToUpdate = jpaRepository.getReferenceById(toUpdate.id().value());
      userToUpdate.updateFrom(toUpdate);
      var userUpdated = jpaRepository.save(userToUpdate);

      return userUpdated.toUser();
    } catch (EntityNotFoundException _) {
      throw new ResourceNotFound(toUpdate.id());
    }
  }

  @CacheEvict(
      cacheNames = {"users", "userPictures"},
      key = "#id.value")
  @Override
  public void delete(final UserId id) {
    jpaRepository.deleteById(id.value());
  }

  @Override
  public Optional<AppUser> findByUsername(final EmailAddress emailAddress) {
    return jpaRepository
        .findByEmailAndRoles(emailAddress.value(), Set.of(ROLE_USER))
        .map(JpaUser::toUser);
  }

  @Override
  public void updatePassword(final UserId userId, final Password password) throws ResourceNotFound {
    try {
      var jpaUser = jpaRepository.getReferenceById(userId.value());
      jpaUser.setEncodedPassword(passwordEncoder.encode(password.value()));
      jpaRepository.save(jpaUser);
    } catch (EntityNotFoundException _) {
      throw new ResourceNotFound(userId);
    }
  }

  @Override
  public boolean matches(final UserId userId, final Password password) {
    return jpaRepository
        .findById(userId.value())
        .map(jpaUser -> passwordEncoder.matches(password.value(), jpaUser.getEncodedPassword()))
        .orElseThrow(() -> new ResourceNotFound(userId));
  }
}
