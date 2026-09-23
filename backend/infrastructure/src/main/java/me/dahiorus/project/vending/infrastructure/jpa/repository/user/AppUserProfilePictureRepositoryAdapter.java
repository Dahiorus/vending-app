package me.dahiorus.project.vending.infrastructure.jpa.repository.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.UUID;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.file.entity.FileToUpload;
import me.dahiorus.project.vending.domain.file.entity.UploadedFile;
import me.dahiorus.project.vending.domain.user.entity.UserId;
import me.dahiorus.project.vending.domain.user.port.AppUserProfilePictureRepositoryPort;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaUploadedFile;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@CacheConfig(cacheNames = "userPictures")
@Repository
public class AppUserProfilePictureRepositoryAdapter implements AppUserProfilePictureRepositoryPort {

  private final UserJpaRepository jpaUserRepository;
  private final CrudRepository<JpaUploadedFile, UUID> jpaUploadedFileRepository;

  public AppUserProfilePictureRepositoryAdapter(
      final UserJpaRepository jpaUserRepository, final EntityManager entityManager) {
    this.jpaUserRepository = jpaUserRepository;
    this.jpaUploadedFileRepository =
        new SimpleJpaRepository<>(JpaUploadedFile.class, entityManager);
  }

  @CachePut(key = "#userId.value")
  @Override
  public UploadedFile uploadPicture(final UserId userId, final FileToUpload profilePicture)
      throws ResourceNotFound {
    try {
      var userToUpdate = jpaUserRepository.getReferenceById(userId.value());
      var uploadedPicture =
          jpaUploadedFileRepository.save(JpaUploadedFile.toCreate(profilePicture));
      userToUpdate.setProfilePicture(uploadedPicture);
      jpaUserRepository.save(userToUpdate);

      return uploadedPicture.toDomain();
    } catch (EntityNotFoundException _) {
      throw new ResourceNotFound(userId);
    }
  }

  @Cacheable(key = "#userId.value", unless = "#result == null")
  @Override
  public Optional<UploadedFile> findPicture(final UserId userId) throws ResourceNotFound {
    var jpaUser =
        jpaUserRepository
            .findWithProfilePictureById(userId.value())
            .orElseThrow(() -> new ResourceNotFound(userId));

    return jpaUser.maybeProfilePicture().map(JpaUploadedFile::toDomain);
  }
}
