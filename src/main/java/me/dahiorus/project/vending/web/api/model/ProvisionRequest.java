package me.dahiorus.project.vending.web.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import io.swagger.v3.oas.annotations.media.SchemaProperty;

@SchemaProperty(name = "quantity",
  schema = @Schema(example = "1", type = "integer", requiredMode = RequiredMode.REQUIRED))
public record ProvisionRequest(int quantity)
{
  // empty record
}
