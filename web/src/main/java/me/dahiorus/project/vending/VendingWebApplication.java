package me.dahiorus.project.vending;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan(basePackages = "me.dahiorus.project.vending")
public class VendingWebApplication
{
  public static void main(final String[] args)
  {
    SpringApplication.run(VendingWebApplication.class, args);
  }
}
