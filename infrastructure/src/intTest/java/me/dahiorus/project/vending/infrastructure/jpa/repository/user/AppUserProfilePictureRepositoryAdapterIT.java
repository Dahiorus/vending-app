package me.dahiorus.project.vending.infrastructure.jpa.repository.user;

import static me.dahiorus.project.vending.domain.file.entity.ContentType.JPG;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import java.util.UUID;
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
import me.dahiorus.project.vending.infrastructure.jpa.repository.user.AppUserProfilePictureRepositoryAdapterIT.TestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestConfig.class)
class AppUserProfilePictureRepositoryAdapterIT extends H2DbContainer {

  @Autowired AppUserRepositoryAdapter appUserJpaRepository;
  @Autowired AppUserProfilePictureRepositoryAdapter repository;

  AppUser appUser;

  @BeforeEach
  void setUpItem() {
    appUser =
        appUserJpaRepository.create(
            new AppUserToCreate(
                EmailAddress.of("user@test.org"),
                Password.of("password"),
                Firstname.of("User"),
                Lastname.of("Test")));
    entityManager.flush();
  }

  @Nested
  class UploadPicture {
    @Test
    void should_upload_picture_for_item() {
      var picture =
          new FileToUpload(
              new Filename("profile-picture.jpg"), new BinaryContent(new byte[] {1, 2, 3}), JPG);

      var result = repository.uploadPicture(appUser.id(), picture);

      assertThat(result)
          .satisfies(
              itemWithImage -> {
                assertThat(itemWithImage.user()).isEqualTo(appUser);
                assertThat(itemWithImage.profilePicture())
                    .usingRecursiveComparison()
                    .ignoringFields("id", "uploadedAt")
                    .isEqualTo(
                        new UploadedFile(
                            null,
                            new Filename("profile-picture.jpg"),
                            new BinaryContent(new byte[] {1, 2, 3}),
                            JPG,
                            null));
                assertThat(itemWithImage.profilePicture().id()).isNotNull();
              });
    }

    @Test
    void should_throw_exception_when_upload_picture_for_non_existent_item() {
      var userId = new UserId(UUID.randomUUID());
      var picture =
          new FileToUpload(
              new Filename("coca-cola.jpg"), new BinaryContent(new byte[] {1, 2, 3}), JPG);

      assertThatThrownBy(() -> repository.uploadPicture(userId, picture))
          .isInstanceOf(ResourceNotFound.class)
          .hasMessageContaining("Resource not found with ID: " + userId);
    }

    @Test
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

      assertThat(result.profilePicture())
          .usingRecursiveComparison()
          .ignoringFields("id", "uploadedAt")
          .isEqualTo(
              new UploadedFile(
                  null,
                  new Filename("new-picture.jpg"),
                  new BinaryContent(new byte[] {4, 5, 6}),
                  JPG,
                  null));
      assertThat(
              entityManager.find(
                  JpaUploadedFile.class, itemWithPictureToReplace.profilePicture().id().value()))
          .isNull();
    }
  }

  @Nested
  class FindPicture {
    @Test
    void should_find_empty_picture_for_given_item() {
      var result = repository.findPicture(appUser.id());

      assertThat(result).isEmpty();
    }

    @Test
    void should_find_picture_for_given_item() {
      // Given
      var picture =
          new FileToUpload(
              new Filename("coca-cola.jpg"), new BinaryContent(new byte[] {1, 2, 3}), JPG);
      repository.uploadPicture(appUser.id(), picture);
      entityManager.flush();

      // When
      var result = repository.findPicture(appUser.id());

      // Then
      assertThat(result)
          .get()
          .usingRecursiveComparison()
          .ignoringFields("id", "uploadedAt")
          .isEqualTo(
              new UploadedFile(
                  null,
                  new Filename("coca-cola.jpg"),
                  new BinaryContent(new byte[] {1, 2, 3}),
                  JPG,
                  null));
    }

    @Test
    void should_throw_exception_when_find_picture_for_non_existent_item() {
      var userId = new UserId(UUID.randomUUID());

      assertThatThrownBy(() -> repository.findPicture(userId))
          .isInstanceOf(ResourceNotFound.class)
          .hasMessageContaining("Resource not found with ID: " + userId);
    }
  }

  @TestConfiguration
  static class TestConfig {
    @Bean
    AppUserRepositoryPort appUserJpaRepository(UserJpaRepository jpaUserDao) {
      return new AppUserRepositoryAdapter(jpaUserDao, new BCryptPasswordEncoder());
    }

    @Bean
    AppUserProfilePictureRepositoryPort appUserProfilePictureRepository(
        EntityManager entityManager) {
      return new AppUserProfilePictureRepositoryAdapter(entityManager);
    }
  }
}
