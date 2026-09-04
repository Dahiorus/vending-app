package me.dahiorus.project.vending.infrastructure.command;

import me.dahiorus.project.vending.domain.user.entity.AdminUserToCreate;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.Firstname;
import me.dahiorus.project.vending.domain.user.entity.Lastname;
import me.dahiorus.project.vending.domain.user.entity.Password;
import me.dahiorus.project.vending.domain.user.port.AdminUserRepositoryPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class CreateDevEnvironmentAdmin implements ApplicationRunner {

  private static final Logger logger = LoggerFactory.getLogger(CreateDevEnvironmentAdmin.class);

  private final AdminUserRepositoryPort adminUserRepository;

  public CreateDevEnvironmentAdmin(final AdminUserRepositoryPort adminUserRepository) {
    this.adminUserRepository = adminUserRepository;
  }

  @Override
  public void run(ApplicationArguments args) {
    logger.info("Creating default admin user...");

    var username = EmailAddress.of("admin@vending-app.org");
    if (adminUserRepository.existsByEmail(username)) {
      return;
    }

    try {
      adminUserRepository.create(
          new AdminUserToCreate(
              username, Password.of("secret"), Firstname.of("Admin"), Lastname.of("User")));
      logger.info("Admin user created successfully.");
    } catch (Exception e) {
      logger.error("Failed to create admin user: {}", e.getMessage());
    }
  }
}
