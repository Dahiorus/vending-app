package me.dahiorus.project.vending.web.api;

import me.dahiorus.project.vending.domain.model.dto.AbstractDto;

public interface RestController<D extends AbstractDto<?>>
  extends ReadOnlyRestController<D>, CreateRestApi<D>, UpdateRestApi<D>,
  DeleteRestApi, PatchRestApi<D>
{
  // marker interface
}
