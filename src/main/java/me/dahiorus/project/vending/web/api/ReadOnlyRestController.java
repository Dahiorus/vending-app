package me.dahiorus.project.vending.web.api;

import me.dahiorus.project.vending.core.model.dto.AbstractDTO;

public interface ReadOnlyRestController<D extends AbstractDTO<?>>
    extends ListRestAPI<D>, ReadRestAPI<D>, AppWebService
{
  // marker interface
}
