package me.dahiorus.project.vending.infrastructure.jpa.repository;

import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

import me.dahiorus.project.vending.domain.Creatable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("int-test")
@AutoConfigureTestDatabase(replace = NONE)
public abstract class H2DbContainer {
  @Autowired protected TestEntityManager entityManager;

  @BeforeEach
  @AfterEach
  void clearDatabase() {
    entityManager.clear();
  }

  protected <P, D> D createAndFlush(Creatable<P, D> creatable, P toCreate) {
    D created = creatable.create(toCreate);
    entityManager.flush();
    return created;
  }
}
