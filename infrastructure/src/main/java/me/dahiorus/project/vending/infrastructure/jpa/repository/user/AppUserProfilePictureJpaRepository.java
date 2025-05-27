package me.dahiorus.project.vending.infrastructure.jpa.repository.user;

import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.file.entity.FileToUpload;
import me.dahiorus.project.vending.domain.file.entity.UploadedFile;
import me.dahiorus.project.vending.domain.user.entity.AppUserWithPicture;
import me.dahiorus.project.vending.domain.user.entity.UserId;
import me.dahiorus.project.vending.domain.user.port.AppUserProfilePictureRepositoryPort;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaUploadedFile;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaUser;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;

@CacheConfig(cacheNames = "userPictures")
@Repository
public class AppUserProfilePictureJpaRepository implements AppUserProfilePictureRepositoryPort {

  private final JpaRepository<JpaUser, UUID> jpaUserRepository;
  private final JpaRepository<JpaUploadedFile, UUID> jpaUploadedFileRepository;

  public AppUserProfilePictureJpaRepository(final EntityManager entityManager) {
    this.jpaUserRepository = new SimpleJpaRepository<>(JpaUser.class, entityManager);
    this.jpaUploadedFileRepository =
        new SimpleJpaRepository<>(JpaUploadedFile.class, entityManager);
  }

  @CachePut(key = "#result.user.id.value")
  @Override
  public AppUserWithPicture uploadPicture(final UserId userId, final FileToUpload profilePicture)
      throws ResourceNotFound {
    return jpaUserRepository
        .findById(userId.value())
        .map(
            jpaUser -> {
              var uploadedPicture =
                  jpaUploadedFileRepository.save(JpaUploadedFile.toCreate(profilePicture));
              jpaUser.setProfilePicture(uploadedPicture);
              jpaUserRepository.save(jpaUser);

              return new AppUserWithPicture(jpaUser.toUser(), uploadedPicture.toDomain());
            })
        .orElseThrow(() -> new ResourceNotFound(userId));
  }

  @Cacheable(key = "#userId.value")
  @Override
  public Optional<UploadedFile> findPicture(final UserId userId) throws ResourceNotFound {
    var jpaUser =
        jpaUserRepository.findById(userId.value()).orElseThrow(() -> new ResourceNotFound(userId));

    return jpaUser.maybeProfilePicture().map(JpaUploadedFile::toDomain);
  }
}
