package me.dahiorus.project.vending.infrastructure.jpa.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("int-test")
public abstract class H2DbContainer {
  @Autowired protected TestEntityManager entityManager;

  @BeforeEach
  @AfterEach
  void clearDatabase() {
    entityManager.clear();
  }
}
