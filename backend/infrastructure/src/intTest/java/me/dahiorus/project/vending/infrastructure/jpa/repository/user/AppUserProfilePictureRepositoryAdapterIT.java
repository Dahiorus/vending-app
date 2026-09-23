package me.dahiorus.project.vending.infrastructure.jpa.repository.user;

import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.UUID.randomUUID;
import static me.dahiorus.project.vending.domain.file.entity.ContentType.JPG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

import jakarta.persistence.EntityManager;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.file.entity.BinaryContent;
import me.dahiorus.project.vending.domain.file.entity.FileToUpload;
import me.dahiorus.project.vending.domain.file.entity.Filename;
import me.dahiorus.project.vending.domain.file.entity.UploadedFile;
import me.dahiorus.project.vending.domain.user.entity.AppUser;
import me.dahiorus.project.vending.domain.user.entity.AppUserToCreate;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.Firstname;
import me.dahiorus.project.vending.domain.user.entity.Lastname;
import me.dahiorus.project.vending.domain.user.entity.Password;
import me.dahiorus.project.vending.domain.user.entity.UserId;
import me.dahiorus.project.vending.domain.user.port.AppUserProfilePictureRepositoryPort;
import me.dahiorus.project.vending.domain.user.port.AppUserRepositoryPort;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaUploadedFile;
import me.dahiorus.project.vending.infrastructure.jpa.repository.H2DbContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.quickperf.junit5.QuickPerfTest;
import org.quickperf.spring.sql.QuickPerfSqlConfig;
import org.quickperf.sql.annotation.ExpectDelete;
import org.quickperf.sql.annotation.ExpectInsert;
import org.quickperf.sql.annotation.ExpectSelect;
import org.quickperf.sql.annotation.ExpectUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;

@QuickPerfTest
@Import(QuickPerfSqlConfig.class)
@ContextConfiguration(classes = AppUserProfilePictureRepositoryAdapterIT.TestConfig.class)
class AppUserProfilePictureRepositoryAdapterIT extends H2DbContainer {

  @Autowired AppUserRepositoryAdapter appUserJpaRepository;
  @Autowired AppUserProfilePictureRepositoryAdapter repository;

  AppUser appUser;

  @BeforeEach
  void setUp() {
    appUser =
        createAndFlush(
            appUserJpaRepository,
            new AppUserToCreate(
                EmailAddress.of("user@test.org"),
                Password.of("password"),
                Firstname.of("User"),
                Lastname.of("Test")));
  }

  @Nested
  class UploadPicture {
    @Test
    @ExpectInsert
    @ExpectUpdate
    void should_upload_picture_for_user() {
      var picture =
          new FileToUpload(
              new Filename("profile-picture.jpg"), new BinaryContent(new byte[] {1, 2, 3}), JPG);

      var result = repository.uploadPicture(appUser.id(), picture);
      entityManager.flush();

      assertThat(result)
          .usingRecursiveComparison()
          .ignoringFields("uploadedAt")
          .isEqualTo(
              new UploadedFile(
                  result.id(),
                  new Filename("profile-picture.jpg"),
                  new BinaryContent(new byte[] {1, 2, 3}),
                  JPG,
                  null));
      assertThat(result.uploadedAt()).isCloseTo(now(), within(200, MILLIS));
    }

    @Test
    @ExpectSelect
    @ExpectInsert(0)
    @ExpectUpdate(0)
    void should_throw_exception_when_upload_picture_for_non_existent_user() {
      var userId = new UserId(randomUUID());
      var picture =
          new FileToUpload(
              new Filename("coca-cola.jpg"), new BinaryContent(new byte[] {1, 2, 3}), JPG);

      assertThatThrownBy(() -> repository.uploadPicture(userId, picture))
          .isInstanceOf(ResourceNotFound.class)
          .hasMessageContaining("Resource not found with ID: " + userId);
    }

    @Test
    @ExpectSelect
    @ExpectInsert(2)
    @ExpectUpdate(2)
    @ExpectDelete
    void should_upload_and_replace_old_picture() {
      // Given
      var oldPicture =
          new FileToUpload(
              new Filename("old-picture.jpg"), new BinaryContent(new byte[] {1, 2, 3}), JPG);
      var itemWithPictureToReplace = repository.uploadPicture(appUser.id(), oldPicture);
      entityManager.flush();

      // When
      var newPicture =
          new FileToUpload(
              new Filename("new-picture.jpg"), new BinaryContent(new byte[] {4, 5, 6}), JPG);
      var result = repository.uploadPicture(appUser.id(), newPicture);
      entityManager.flush();

      assertThat(result)
          .usingRecursiveComparison()
          .ignoringFields("uploadedAt")
          .isEqualTo(
              new UploadedFile(
                  result.id(),
                  new Filename("new-picture.jpg"),
                  new BinaryContent(new byte[] {4, 5, 6}),
                  JPG,
                  null));
      assertThat(entityManager.find(JpaUploadedFile.class, itemWithPictureToReplace.id().value()))
          .isNull();
    }
  }

  @Nested
  class FindPicture {
    @Test
    @ExpectSelect(0)
    void should_find_empty_picture_for_given_user() {
      var result = repository.findPicture(appUser.id());
      entityManager.flush();

      assertThat(result).isEmpty();
    }

    @Test
    @ExpectSelect
    void should_find_picture_for_given_user() {
      // Given
      var picture =
          new FileToUpload(
              new Filename("avatar.jpg"), new BinaryContent(new byte[] {1, 2, 3}), JPG);
      repository.uploadPicture(appUser.id(), picture);
      entityManager.flush();
      entityManager.clear();

      // When
      var result = repository.findPicture(appUser.id());
      entityManager.flush();

      // Then
      assertThat(result)
          .get()
          .usingRecursiveComparison()
          .ignoringFields("id", "uploadedAt")
          .isEqualTo(
              new UploadedFile(
                  null,
                  new Filename("avatar.jpg"),
                  new BinaryContent(new byte[] {1, 2, 3}),
                  JPG,
                  null));
    }

    @Test
    @ExpectSelect
    void should_throw_exception_when_find_picture_for_non_existent_user() {
      var userId = new UserId(randomUUID());

      assertThatThrownBy(() -> repository.findPicture(userId))
          .isInstanceOf(ResourceNotFound.class)
          .hasMessageContaining("Resource not found with ID: " + userId);
    }
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    AppUserRepositoryPort appUserRepository(UserJpaRepository jpaUserRepository) {
      return new AppUserRepositoryAdapter(jpaUserRepository, new BCryptPasswordEncoder());
    }

    @Bean
    AppUserProfilePictureRepositoryPort appUserProfilePictureRepository(
        UserJpaRepository userJpaRepository, EntityManager entityManager) {
      return new AppUserProfilePictureRepositoryAdapter(userJpaRepository, entityManager);
    }
  }
}
