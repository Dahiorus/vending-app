package me.dahiorus.project.vending.infrastructure.jpa.repository;

import me.dahiorus.project.vending.infrastructure.jpa.config.JpaRepositoryConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("int-test")
@Import(JpaRepositoryConfig.class)
public abstract class H2DbContainer {
  @Autowired protected TestEntityManager entityManager;

  @BeforeEach
  @AfterEach
  void clearDatabase() {
    entityManager.clear();
  }
}
