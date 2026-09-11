package me.dahiorus.project.vending.infrastructure.rest.assembler;

import org.springframework.data.domain.Page;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;

public interface PagedModelAssembler<T> {
  PagedModel<EntityModel<T>> toModel(Page<T> page);
}
