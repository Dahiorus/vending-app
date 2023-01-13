package me.dahiorus.project.vending.web.api;

import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;

import me.dahiorus.project.vending.domain.model.dto.AbstractDTO;
import me.dahiorus.project.vending.web.api.request.ExampleMatcherAdapter;

public interface ListRestAPI<D extends AbstractDTO<?>>
{
  ResponseEntity<PagedModel<EntityModel<D>>> list(Pageable pageable, D criteria,
    ExampleMatcherAdapter exampleMatcherAdapter);
}
