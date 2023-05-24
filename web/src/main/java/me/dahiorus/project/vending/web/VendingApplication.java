package me.dahiorus.project.vending.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
@ConfigurationPropertiesScan(basePackages = "me.dahiorus.project.vending")
public class VendingApplication
{
  public static void main(final String[] args)
  {
    SpringApplication.run(VendingApplication.class, args);
  }
}
