package me.dahiorus.project.vending.web.api;

import org.springdoc.api.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;

import io.swagger.v3.oas.annotations.Operation;
import me.dahiorus.project.vending.core.model.dto.AbstractDTO;
import me.dahiorus.project.vending.web.api.model.ExampleMatcherAdapter;

public interface ListRestAPI<D extends AbstractDTO<?>>
{
  @Operation(description = "Get a page of entities")
  ResponseEntity<PagedModel<EntityModel<D>>> list(@ParameterObject Pageable pageable, @ParameterObject D criteria,
      @ParameterObject ExampleMatcherAdapter exampleMatcherAdapter);
}
