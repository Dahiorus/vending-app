package me.dahiorus.project.vending.web.api;

import me.dahiorus.project.vending.domain.model.dto.AbstractDTO;

public interface RestController<D extends AbstractDTO<?>>
    extends ReadOnlyRestController<D>, CreateRestAPI<D>, UpdateRestAPI<D>,
    DeleteRestAPI, PatchRestAPI<D>
{
  // marker interface
}
