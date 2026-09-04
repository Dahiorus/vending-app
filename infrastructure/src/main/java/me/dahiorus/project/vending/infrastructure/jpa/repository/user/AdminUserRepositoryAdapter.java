package me.dahiorus.project.vending.infrastructure.jpa.repository.user;

import static me.dahiorus.project.vending.infrastructure.jpa.entity.JpaUser.ROLE_ADMIN;

import java.util.Optional;
import java.util.Set;
import me.dahiorus.project.vending.domain.user.entity.AdminUser;
import me.dahiorus.project.vending.domain.user.entity.AdminUserToCreate;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.UserId;
import me.dahiorus.project.vending.domain.user.port.AdminUserRepositoryPort;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaUser;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

@CacheConfig(cacheNames = "admins")
@Repository
public class AdminUserRepositoryAdapter implements AdminUserRepositoryPort {

  private final UserJpaRepository jpaUserRepository;
  private final PasswordEncoder passwordEncoder;

  public AdminUserRepositoryAdapter(
      final UserJpaRepository jpaUserRepository, final PasswordEncoder passwordEncoder) {
    this.jpaUserRepository = jpaUserRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @CachePut(key = "#result.id.value")
  @Override
  public AdminUser create(final AdminUserToCreate toCreate) {
    var jpaUserToCreate = JpaUser.toCreateFrom(toCreate);
    jpaUserToCreate.setEncodedPassword(passwordEncoder.encode(toCreate.password().value()));

    return jpaUserRepository.save(jpaUserToCreate).toAdminUser();
  }

  @Cacheable(key = "#id.value")
  @Override
  public Optional<AdminUser> find(final UserId id) {
    return jpaUserRepository
        .findByIdAndRoles(id.value(), Set.of(ROLE_ADMIN))
        .map(JpaUser::toAdminUser);
  }

  @Override
  public boolean existsByEmail(final EmailAddress email) {
    return jpaUserRepository.existsByEmail(email.value());
  }
}
