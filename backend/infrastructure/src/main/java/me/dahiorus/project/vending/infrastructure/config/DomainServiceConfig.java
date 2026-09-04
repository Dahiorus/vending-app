package me.dahiorus.project.vending.infrastructure.config;

import static java.time.Clock.systemDefaultZone;
import static org.springframework.context.annotation.FilterType.ANNOTATION;

import java.time.Clock;
import me.dahiorus.project.vending.domain.documentation.DomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(
    basePackages = "me.dahiorus.project.vending.domain",
    includeFilters = @Filter(type = ANNOTATION, classes = DomainService.class))
public class DomainServiceConfig {
  @Bean
  Clock clock() {
    return systemDefaultZone();
  }
}
