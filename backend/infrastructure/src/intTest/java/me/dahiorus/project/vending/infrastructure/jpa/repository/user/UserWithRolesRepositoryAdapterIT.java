package me.dahiorus.project.vending.infrastructure.jpa.repository.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import java.util.Set;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.user.entity.AdminUserToCreate;
import me.dahiorus.project.vending.domain.user.entity.AppUserToCreate;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.Firstname;
import me.dahiorus.project.vending.domain.user.entity.Lastname;
import me.dahiorus.project.vending.domain.user.entity.Password;
import me.dahiorus.project.vending.domain.user.entity.Role;
import me.dahiorus.project.vending.domain.user.entity.UserWithRoles;
import me.dahiorus.project.vending.domain.user.port.UserWithRolesRepositoryPort;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaUser;
import me.dahiorus.project.vending.infrastructure.jpa.repository.H2DbContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.spring.sql.QuickPerfSqlConfig;
import org.quickperf.sql.annotation.ExpectSelect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;

@QuickPerfTest
@Import(QuickPerfSqlConfig.class)
@ContextConfiguration(classes = UserWithRolesRepositoryAdapterIT.TestConfig.class)
class UserWithRolesRepositoryAdapterIT extends H2DbContainer {

  @Autowired UserWithRolesRepositoryAdapter userWithRolesRepository;

  @BeforeEach
  void setUpUsers() {
    var admin =
        new AdminUserToCreate(
            EmailAddress.of("admin@test.org"),
            Password.of("secret"),
            Firstname.of("Admin"),
            Lastname.of("Test"));
    var user =
        new AppUserToCreate(
            EmailAddress.of("user@test.org"),
            Password.of("secret"),
            Firstname.of("User"),
            Lastname.of("Test"));

    entityManager.persist(JpaUser.toCreateFrom(admin));
    entityManager.persist(JpaUser.toCreateFrom(user));
    entityManager.flush();
  }

  @Test
  @ExpectSelect
  void should_get_admin_user_given_username() {
    var result = userWithRolesRepository.getByUsername(EmailAddress.of("admin@test.org"));

    assertThat(result)
        .usingRecursiveComparison()
        .isEqualTo(
            new UserWithRoles(
                result.id(),
                EmailAddress.of("admin@test.org"),
                Password.of(null),
                Set.of(new Role("ADMIN"))));
  }

  @Test
  @ExpectSelect
  void should_get_app_user_given_username() {
    var result = userWithRolesRepository.getByUsername(EmailAddress.of("user@test.org"));

    assertThat(result)
        .usingRecursiveComparison()
        .isEqualTo(
            new UserWithRoles(
                result.id(),
                EmailAddress.of("user@test.org"),
                Password.of(null),
                Set.of(new Role("USER"))));
  }

  @Test
  @ExpectSelect
  void should_throw_exception_given_unknown_username() {
    var throwable =
        catchThrowable(() -> userWithRolesRepository.getByUsername(EmailAddress.of("toto")));
    assertThat(throwable)
        .isInstanceOf(ResourceNotFound.class)
        .hasMessage("No user found with username [toto]");
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    UserWithRolesRepositoryPort userWithRolesRepository(UserJpaRepository userJpaRepository) {
      return new UserWithRolesRepositoryAdapter(userJpaRepository);
    }
  }
}
