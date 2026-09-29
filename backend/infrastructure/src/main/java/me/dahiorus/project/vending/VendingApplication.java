package me.dahiorus.project.vending;

import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.HAL_FORMS;

import java.util.Arrays;
import me.dahiorus.project.vending.infrastructure.command.PurgeExpiredRefreshTokenCommand;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties
@EnableSpringDataWebSupport
@EnableHypermediaSupport(type = HAL_FORMS)
@EnableScheduling
@ConfigurationPropertiesScan(basePackages = "me.dahiorus.project.vending")
public class VendingApplication {
  static void main(final String[] args) {
    // One-shot CLI commands (e.g. PurgeExpiredRefreshTokenCommand) run the full Spring context
    // but skip starting the embedded web server, and exit as soon as the ApplicationRunners
    // complete instead of staying up to serve HTTP requests.
    var cliMode = isCommand(args, PurgeExpiredRefreshTokenCommand.OPTION_NAME);

    var application = new SpringApplication(VendingApplication.class);
    if (cliMode) {
      application.setWebApplicationType(WebApplicationType.NONE);
    }

    var context = application.run(args);
    if (cliMode) {
      System.exit(SpringApplication.exit(context));
    }
  }

  private static boolean isCommand(final String[] args, final String optionName) {
    var option = "--" + optionName;
    return Arrays.stream(args).anyMatch(arg -> arg.equals(option) || arg.startsWith(option + "="));
  }
}
