package me.dahiorus.project.vending.web.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import me.dahiorus.project.vending.core.model.AbstractEntity_;

@Configuration
@OpenAPIDefinition(
    info = @Info(title = "Vending app API", description = "Simple vending application", version = "v1.0"))
@SecurityScheme(name = "bearerAuth", bearerFormat = "JWT", type = SecuritySchemeType.HTTP, scheme = "bearer")
public class WebMvcConfig implements WebMvcConfigurer
{
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
