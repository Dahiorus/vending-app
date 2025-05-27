package me.dahiorus.project.vending.infrastructure.jpa.repository.user;

import static org.assertj.core.api.Assertions.assertThat;

import me.dahiorus.project.vending.domain.user.entity.AdminUserToCreate;
import me.dahiorus.project.vending.domain.user.entity.AppUserToCreate;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.Firstname;
import me.dahiorus.project.vending.domain.user.entity.Lastname;
import me.dahiorus.project.vending.domain.user.entity.Password;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaUser;
import me.dahiorus.project.vending.infrastructure.jpa.repository.H2DbContainer;
import me.dahiorus.project.vending.infrastructure.jpa.repository.user.UserToCreateJpaRepositoryIT.TestConfig;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestConfig.class)
class UserToCreateJpaRepositoryIT extends H2DbContainer {

  @Autowired UserToCreateJpaRepository repository;

  @Nested
  class FindDuplicateOf {
    @Test
    void should_return_empty_given_no_duplicate() {
      var result =
          repository.findDuplicateOf(
              new AppUserToCreate(
                  EmailAddress.of("user@test.org"),
                  Password.of("password"),
                  Firstname.of("Admin"),
                  Lastname.of("Test")));

      assertThat(result).isEmpty();
    }

    @Test
    void should_return_userId_given_existing_duplicate_by_email() {
      var duplicate =
          entityManager
              .persistAndFlush(
                  JpaUser.toCreateFrom(
                      new AppUserToCreate(
                          EmailAddress.of("user@test.org"),
                          Password.of("password"),
                          Firstname.of("User"),
                          Lastname.of("Test"))))
              .toUser();

      var result =
          repository.findDuplicateOf(
              new AdminUserToCreate(
                  EmailAddress.of("user@test.org"),
                  Password.of("password"),
                  Firstname.of("Admin"),
                  Lastname.of("Test")));

      assertThat(result).contains(duplicate.id());
    }
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    UserToCreateJpaRepository userToCreateJpaRepository(JpaUserDao jpaUserDao) {
      return new UserToCreateJpaRepository(jpaUserDao);
    }
  }
}
