package me.dahiorus.project.vending;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import me.dahiorus.project.vending.core.model.dto.UserDTO;
import me.dahiorus.project.vending.core.service.UserDtoService;

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan(basePackages = "me.dahiorus.project.vending")
public class VendingApplication
{
  public static void main(final String[] args)
  {
    SpringApplication.run(VendingApplication.class, args);
  }

  @Bean
  CommandLineRunner adminCreator(final UserDtoService userDtoService)
  {
    return args -> {
      UserDTO admin = new UserDTO();
      admin.setFirstName("Admin");
      admin.setLastName("Admin");
      admin.setEmail("admin@vending-app.com");
      admin.setPassword("Secret123");
      admin.setRoles(List.of("ROLE_ADMIN"));

      try
      {
        userDtoService.create(admin);
      }
      catch (Exception ignore)
      {
        // empty block
      }
    };
  }
}
