package me.dahiorus.project.vending.infrastructure.jpa.repository.user;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.user.entity.AdminUserToCreate;
import me.dahiorus.project.vending.domain.user.entity.AppUser;
import me.dahiorus.project.vending.domain.user.entity.AppUserToCreate;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.Firstname;
import me.dahiorus.project.vending.domain.user.entity.Lastname;
import me.dahiorus.project.vending.domain.user.entity.Password;
import me.dahiorus.project.vending.domain.user.entity.UserId;
import me.dahiorus.project.vending.domain.user.port.AppUserRepositoryPort;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaUser;
import me.dahiorus.project.vending.infrastructure.jpa.repository.H2DbContainer;
import me.dahiorus.project.vending.infrastructure.jpa.repository.user.AppUserRepositoryAdapterIT.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestConfig.class)
class AppUserRepositoryAdapterIT extends H2DbContainer {

  @Autowired PasswordEncoder passwordEncoder;
  @Autowired AppUserRepositoryAdapter repository;

  @Test
  void should_create_app_user() {
    var user =
        new AppUserToCreate(
            EmailAddress.of("user@test.org"),
            Password.of("password"),
            Firstname.of("User"),
            Lastname.of("Test"));

    var result = repository.create(user);
    entityManager.flush();

    assertThat(result)
        .satisfies(u -> assertThat(u.id()).isNotNull())
        .usingRecursiveComparison()
        .ignoringFields("id")
        .isEqualTo(
            new AppUser(
                null, EmailAddress.of("user@test.org"), Firstname.of("User"), Lastname.of("Test")));
    assertThat(entityManager.find(JpaUser.class, result.id().value()))
        .satisfies(
            jpaUser -> {
              assertThat(jpaUser.getRoles()).containsExactly("USER");
              assertThat(passwordEncoder.matches("password", jpaUser.getEncodedPassword()))
                  .isTrue();
            });
  }

  @Nested
  class FindById {
    @Test
    void should_get_user_by_id() {
      var user =
          repository.create(
              new AppUserToCreate(
                  EmailAddress.of("user@test.org"),
                  Password.of("password"),
                  Firstname.of("User"),
                  Lastname.of("Test")));
      entityManager.flush();

      var result = repository.find(user.id());

      assertThat(result).contains(user);
    }

    @Test
    void should_return_empty_when_user_not_found() {
      var result = repository.find(new UserId(randomUUID()));

      assertThat(result).isEmpty();
    }

    @Test
    void should_not_get_admin_user_by_id() {
      var admin =
          new AdminUserToCreate(
              EmailAddress.of("admin@test.org"),
              Password.of("secret"),
              Firstname.of("Admin"),
              Lastname.of("Test"));
      var adminCreated = entityManager.persist(JpaUser.toCreateFrom(admin));
      entityManager.flush();

      var result = repository.find(new UserId(adminCreated.getId()));

      assertThat(result).isEmpty();
    }
  }

  @Nested
  class FindByUsername {
    @Test
    void should_get_user_by_username() {
      var user =
          repository.create(
              new AppUserToCreate(
                  EmailAddress.of("user@test.org"),
                  Password.of("password"),
                  Firstname.of("User"),
                  Lastname.of("Test")));
      entityManager.flush();

      var result = repository.findByUsername(user.email());

      assertThat(result).contains(user);
    }

    @Test
    void should_return_empty_when_user_not_found() {
      var result = repository.findByUsername(EmailAddress.of("user@test.org"));

      assertThat(result).isEmpty();
    }
  }

  @Nested
  class Update {
    @Test
    void should_update_given_user_by_id() {
      var userCreated =
          repository.create(
              new AppUserToCreate(
                  EmailAddress.of("user@test.org"),
                  Password.of("password"),
                  Firstname.of("User"),
                  Lastname.of("Test")));
      entityManager.flush();

      var result =
          repository.update(
              new AppUser(
                  userCreated.id(),
                  userCreated.email(),
                  Firstname.of("Modified"),
                  Lastname.of("Other")));
      entityManager.flush();

      assertThat(result)
          .usingRecursiveComparison()
          .isEqualTo(
              new AppUser(
                  userCreated.id(),
                  userCreated.email(),
                  Firstname.of("Modified"),
                  Lastname.of("Other")));
    }

    @Test
    void should_throw_exception_when_update_non_existent_vending_machine() {
      var userId = new UserId(randomUUID());
      var userToUpdate =
          new AppUser(
              userId, EmailAddress.of("edited@test.fr"), Firstname.of("User"), Lastname.of("Test"));

      assertThatThrownBy(() -> repository.update(userToUpdate))
          .isInstanceOf(ResourceNotFound.class)
          .hasMessageContaining("Resource not found with ID: " + userId);
    }
  }

  @Test
  void should_delete_given_user() {
    var user =
        repository.create(
            new AppUserToCreate(
                EmailAddress.of("user@test.org"),
                Password.of("password"),
                Firstname.of("User"),
                Lastname.of("Test")));
    entityManager.flush();

    repository.delete(user.id());
    entityManager.flush();

    assertThat(repository.find(user.id())).isEmpty();
  }

  @Nested
  class UpdatePassword {
    @Test
    void should_update_password_of_given_user_by_id() {
      var userCreated =
          repository.create(
              new AppUserToCreate(
                  EmailAddress.of("user@test.org"),
                  Password.of("password"),
                  Firstname.of("User"),
                  Lastname.of("Test")));
      entityManager.flush();

      repository.updatePassword(userCreated.id(), Password.of("newPassword"));
      entityManager.flush();

      assertThat(repository.matches(userCreated.id(), Password.of("newPassword"))).isTrue();
    }

    @Test
    void should_throw_exception_given_non_existing_user() {
      assertThatThrownBy(
              () -> repository.updatePassword(new UserId(randomUUID()), Password.of("newPassword")))
          .isInstanceOf(ResourceNotFound.class);
    }
  }

  @Nested
  class PasswordMatches {

    AppUser appUser;

    @BeforeEach
    void setUpUser() {
      appUser =
          repository.create(
              new AppUserToCreate(
                  EmailAddress.of("user@test.org"),
                  Password.of("password"),
                  Firstname.of("User"),
                  Lastname.of("Test")));
      entityManager.flush();
    }

    private static Stream<Arguments> passwordAndExpectedValue() {
      return Stream.of(
          Arguments.of(Password.of("password"), true), Arguments.of(Password.of("other"), false));
    }

    @ParameterizedTest
    @MethodSource("passwordAndExpectedValue")
    void should_update_password_of_given_user_by_id(Password password, boolean expected) {
      assertThat(repository.matches(appUser.id(), password)).isEqualTo(expected);
    }
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
    }

    @Bean
    AppUserRepositoryPort appUserJpaRepository(
        UserJpaRepository jpaUserDao, PasswordEncoder passwordEncoder) {
      return new AppUserRepositoryAdapter(jpaUserDao, passwordEncoder);
    }
  }
}
