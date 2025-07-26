package me.dahiorus.project.vending.infrastructure.jpa.repository.user;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.UUID;
import me.dahiorus.project.vending.domain.user.entity.AdminUser;
import me.dahiorus.project.vending.domain.user.entity.AdminUserToCreate;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.Firstname;
import me.dahiorus.project.vending.domain.user.entity.Lastname;
import me.dahiorus.project.vending.domain.user.entity.Password;
import me.dahiorus.project.vending.domain.user.entity.UserId;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaUser;
import me.dahiorus.project.vending.infrastructure.jpa.repository.H2DbContainer;
import me.dahiorus.project.vending.infrastructure.jpa.repository.user.AdminUserJpaRepositoryIT.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestConfig.class)
class AdminUserJpaRepositoryIT extends H2DbContainer {

  @Autowired PasswordEncoder passwordEncoder;
  @Autowired AdminUserJpaRepository repository;

  @Nested
  class Create {
    @Test
    void should_create_admin_user_with_encoded_password() {
      var adminUserToCreate =
          new AdminUserToCreate(
              EmailAddress.of("admin@vending-app.fr"),
              Password.of("password"),
              Firstname.of("Admin"),
              Lastname.of("User"));

      var result = repository.create(adminUserToCreate);
      entityManager.flush();

      assertThat(result)
          .usingRecursiveComparison()
          .ignoringFields("id")
          .isEqualTo(
              new AdminUser(
                  new UserId(UUID.randomUUID()),
                  EmailAddress.of("admin@vending-app.fr"),
                  Firstname.of("Admin"),
                  Lastname.of("User")));
    }

    @Test
    void should_create_admin_user_with_password_and_role() {
      var adminUserToCreate =
          new AdminUserToCreate(
              EmailAddress.of("admin@vending-app.fr"),
              Password.of("password"),
              Firstname.of("Admin"),
              Lastname.of("User"));

      var result = repository.create(adminUserToCreate);
      entityManager.flush();

      assertThat(entityManager.find(JpaUser.class, result.id().value()))
          .satisfies(
              jpaUser -> {
                assertThat(jpaUser.getRoles()).containsExactly("ADMIN");
                assertThat(passwordEncoder.matches("password", jpaUser.getEncodedPassword()))
                    .isTrue();
              });
    }
  }

  @Nested
  class FindById {

    AdminUser adminUser;

    @BeforeEach
    void setUpAdminUser() {
      var adminUserToCreate =
          new AdminUserToCreate(
              EmailAddress.of("admin@vending-app.fr"),
              Password.of("password"),
              Firstname.of("Admin"),
              Lastname.of("User"));

      adminUser = repository.create(adminUserToCreate);
      entityManager.flush();
    }

    @Test
    void should_find_admin_user_by_id() {
      var result = repository.find(adminUser.id());

      assertThat(result).contains(adminUser);
    }

    @Test
    void should_not_find_other_user_by_id() {
      var otherUser =
          JpaUser.toCreateFrom(
              new AdminUserToCreate(
                  EmailAddress.of("other-user@vending-app.fr"),
                  Password.of("password"),
                  Firstname.of("Other"),
                  Lastname.of("User")));
      otherUser.setRoles(Set.of("ROLE_USER"));
      entityManager.persistAndFlush(otherUser);

      var result = repository.find(new UserId(otherUser.getId()));

      assertThat(result).isEmpty();
    }
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
      return new BCryptPasswordEncoder();
    }

    @Bean
    AdminUserJpaRepository adminUserJpaRepository(
        final JpaUserDao jpaUserRepository, final PasswordEncoder passwordEncoder) {
      return new AdminUserJpaRepository(jpaUserRepository, passwordEncoder);
    }
  }
}
