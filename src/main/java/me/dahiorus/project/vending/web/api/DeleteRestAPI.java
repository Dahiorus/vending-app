package me.dahiorus.project.vending.web.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

public interface DeleteRestAPI
{
  @Operation(description = "Delete an existing entity targeted by its ID")
  @ApiResponse(responseCode = "204", description = "Entity deleted")
  ResponseEntity<Void> delete(@PathVariable UUID id);
}
