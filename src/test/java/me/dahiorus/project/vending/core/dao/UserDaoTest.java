package me.dahiorus.project.vending.core.dao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import me.dahiorus.project.vending.core.config.DaoConfig;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.model.AppUser;
import me.dahiorus.project.vending.util.UserBuilder;

@Import(DaoConfig.class)
@DataJpaTest
@ActiveProfiles("jpa-test")
class UserDaoTest
{
  @Autowired
  UserDAO dao;

  AppUser entity;

  @BeforeEach
  void initTestData()
  {
    entity = dao.save(UserBuilder.builder()
      .id(UUID.fromString("2b2b7269-b56e-492c-96df-b2499633cc9d"))
      .email("user.test")
      .firstName("User")
      .lastName("Test")
      .build());
    assertThat(entity.getId()).isNotNull();
  }

  @Test
  void findUserByEmail()
  {
    Optional<AppUser> user = dao.findByEmail("user.test");

    assertThat(user).contains(entity);
  }

  @Test
  void noUserFindByEmail()
  {
    Optional<AppUser> user = dao.findByEmail("user.test2");

    assertThat(user).isEmpty();
  }

  @Test
  void readEntity() throws Exception
  {
    AppUser readEntity = dao.read(entity.getId());

    assertThat(readEntity).isEqualTo(entity);
  }

  @Test
  void readNonExistingEntity()
  {
    assertThatExceptionOfType(EntityNotFound.class)
      .isThrownBy(() -> dao.read(UUID.fromString("c8d4ac44-4e1f-441f-8e64-16f85ee529b2")));
  }

  @Test
  void updateEntity()
  {
    entity.setLastName("NewLastname");

    AppUser updatedEntity = dao.save(entity);

    assertThat(updatedEntity.getLastName()).isEqualTo("NewLastname");
  }

  @Test
  void findAll()
  {
    List<AppUser> entities = dao.findAll();

    assertThat(entities).containsExactly(entity);
  }
}
