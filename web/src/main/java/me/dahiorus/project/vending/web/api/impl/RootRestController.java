package me.dahiorus.project.vending.web.api.impl;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import org.springframework.hateoas.MediaTypes;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;
import me.dahiorus.project.vending.domain.exception.AppException;
import me.dahiorus.project.vending.web.api.AppWebService;
import me.dahiorus.project.vending.web.api.model.RootEntryPointResponse;

@Tag(name = "Root")
@RestController
@RequestMapping(value = "/api/v1/", produces = MediaTypes.HAL_JSON_VALUE)
public class RootRestController implements AppWebService
{
  @GetMapping
  public ResponseEntity<RootEntryPointResponse> getRoot() throws AppException
  {
    RootEntryPointResponse response = new RootEntryPointResponse().add(
        linkTo(methodOn(PublicRestController.class).authenticate(null)).withRel("public:authenticate"),
        linkTo(methodOn(PublicRestController.class).create(null)).withRel("public:register"),
        linkTo(methodOn(PublicRestController.class).refreshToken(null)).withRel("public:refresh-access"),
        linkTo(ItemRestController.class).withRel("items"),
        linkTo(VendingMachineRestController.class).withRel("vending-machines"),
        linkTo(UserRestController.class).withRel("users"),
        linkTo(SelfServiceRestController.class).withRel("me"),
        linkTo(methodOn(ReportRestController.class).list(null, null, null)).withRel("reports"));

    return ResponseEntity.ok(response);
  }
}
