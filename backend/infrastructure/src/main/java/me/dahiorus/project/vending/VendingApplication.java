package me.dahiorus.project.vending;

import static org.springframework.boot.SpringApplication.run;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan(basePackages = "me.dahiorus.project.vending")
public class VendingApplication {
  public static void main(final String[] args) {
    run(VendingApplication.class, args);
  }
}
