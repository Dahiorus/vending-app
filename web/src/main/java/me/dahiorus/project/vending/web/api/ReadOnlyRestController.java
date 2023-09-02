package me.dahiorus.project.vending.web.api;

import me.dahiorus.project.vending.domain.model.dto.AbstractDto;

public interface ReadOnlyRestController<D extends AbstractDto<?>>
  extends ListRestApi<D>, ReadRestApi<D>, AppWebService
{
  // marker interface
}
