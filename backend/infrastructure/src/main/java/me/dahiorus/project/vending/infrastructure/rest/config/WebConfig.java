package me.dahiorus.project.vending.infrastructure.rest.config;

import static io.swagger.v3.oas.annotations.enums.SecuritySchemeType.HTTP;
import static org.springframework.data.domain.Sort.Direction.DESC;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@OpenAPIDefinition(
    info =
        @Info(
            title = "Vending app API",
            description = "Simple vending application",
            version = "v1.0"))
@SecurityScheme(name = "bearerAuth", bearerFormat = "JWT", type = HTTP, scheme = "bearer")
public class WebConfig implements WebMvcConfigurer {
  @Override
  public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(pagingHandlerResolver());
  }

  private static HandlerMethodArgumentResolver pagingHandlerResolver() {
    PageableHandlerMethodArgumentResolver resolver = new PageableHandlerMethodArgumentResolver();
    resolver.setOneIndexedParameters(true);
    resolver.setFallbackPageable(PageRequest.of(0, 20, Sort.by(DESC, "createdAt")));

    return resolver;
  }
}
