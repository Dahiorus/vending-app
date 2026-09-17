package me.dahiorus.project.vending;

import static org.springframework.boot.SpringApplication.run;
import static org.springframework.hateoas.config.EnableHypermediaSupport.HypermediaType.HAL_FORMS;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.hateoas.config.EnableHypermediaSupport;

@SpringBootApplication
@EnableConfigurationProperties
@EnableSpringDataWebSupport
@EnableHypermediaSupport(type = HAL_FORMS)
@ConfigurationPropertiesScan(basePackages = "me.dahiorus.project.vending")
public class VendingApplication {
  public static void main(final String[] args) {
    run(VendingApplication.class, args);
  }
}
