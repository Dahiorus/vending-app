package me.dahiorus.project.vending;

import java.util.List;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import me.dahiorus.project.vending.core.model.AbstractEntity_;
import me.dahiorus.project.vending.web.api.AppWebService;

@SpringBootApplication
public class VendingApplication implements WebMvcConfigurer
{
  public static void main(final String[] args)
  {
    SpringApplication.run(VendingApplication.class, args);
  }

  @Bean
  public OpenAPI openApi()
  {
    return new OpenAPI().info(new Info().title("Vending app API")
      .description("Simple vending application")
      .version("v1.0"));
  }

  @Override
  public void configurePathMatch(final PathMatchConfigurer configurer)
  {
    configurer.addPathPrefix("/api", HandlerTypePredicate.forAssignableType(AppWebService.class));
  }

  @Override
  public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers)
  {
    resolvers.add(pagingHandlerResolver());
  }

  private static HandlerMethodArgumentResolver pagingHandlerResolver()
  {
    PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();
    resolver.setOneIndexedParameters(true);
    resolver.setFallbackPageable(PageRequest.of(0, 20, Sort.by(Direction.DESC, AbstractEntity_.CREATED_AT)));

    return resolver;
  }
}
