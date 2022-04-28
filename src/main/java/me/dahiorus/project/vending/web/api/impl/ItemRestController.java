package me.dahiorus.project.vending.web.api.impl;

import org.apache.logging.log4j.Logger;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.core.model.dto.ItemDTO;
import me.dahiorus.project.vending.core.service.ItemDtoService;

@Tag(name = "Item", description = "Operations on Item")
@RestController
@RequestMapping(value = "/api/v1/items")
@Log4j2
public class ItemRestController extends RestControllerImpl<ItemDTO, ItemDtoService>
{
  public ItemRestController(final ItemDtoService dtoService,
      final RepresentationModelAssembler<ItemDTO, EntityModel<ItemDTO>> modelAssembler,
      final PagedResourcesAssembler<ItemDTO> pageModelAssembler)
  {
    super(dtoService, modelAssembler, pageModelAssembler);
  }

  @Override
  public Logger getLogger()
  {
    return log;
  }
}
