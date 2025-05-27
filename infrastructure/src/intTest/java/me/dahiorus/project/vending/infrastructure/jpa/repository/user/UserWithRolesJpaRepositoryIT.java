package me.dahiorus.project.vending.infrastructure.jpa.repository.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaUser;
import me.dahiorus.project.vending.infrastructure.jpa.repository.H2DbContainer;
import me.dahiorus.project.vending.infrastructure.jpa.repository.user.UserWithRolesJpaRepositoryIT.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestConfig.class)
class UserWithRolesJpaRepositoryIT extends H2DbContainer {

  @Autowired UserWithRolesJpaRepository userWithRolesJpaRepository;

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
  void should_get_admin_user_given_username() {
    var result = userWithRolesJpaRepository.getByUsername(EmailAddress.of("admin@test.org"));

    assertThat(result)
        .usingRecursiveComparison()
        .ignoringFields("id")
        .isEqualTo(
            new UserWithRoles(null, EmailAddress.of("admin@test.org"), Set.of(new Role("ADMIN"))));
  }

  @Test
  void should_get_app_user_given_username() {
    var result = userWithRolesJpaRepository.getByUsername(EmailAddress.of("user@test.org"));

    assertThat(result)
        .usingRecursiveComparison()
        .ignoringFields("id")
        .isEqualTo(
            new UserWithRoles(null, EmailAddress.of("user@test.org"), Set.of(new Role("USER"))));
  }

  @Test
  void should_throw_exception_given_unknown_username() {
    assertThatThrownBy(() -> userWithRolesJpaRepository.getByUsername(EmailAddress.of("toto")))
        .isInstanceOf(ResourceNotFound.class)
        .hasMessage("No user found with username [toto]");
  }

  @Configuration
  static class TestConfig {
    @Bean
    UserWithRolesJpaRepository userWithRolesJpaRepository(JpaUserDao jpaUserDao) {
      return new UserWithRolesJpaRepository(jpaUserDao);
    }
  }
}
