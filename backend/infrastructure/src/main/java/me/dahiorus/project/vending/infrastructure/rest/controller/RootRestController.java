package me.dahiorus.project.vending.infrastructure.rest.controller;

import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.AUTHENTICATE;
import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.DOCS;
import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.ITEMS;
import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.ME;
import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.REGISTER;
import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.VENDING_MACHINES;
import static me.dahiorus.project.vending.infrastructure.security.config.WebSecurityConfig.AUTHENTICATE_PATH;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.dahiorus.project.vending.infrastructure.rest.controller.item.ItemCrudRestController;
import me.dahiorus.project.vending.infrastructure.rest.controller.machine.VendingMachineCrudRestController;
import me.dahiorus.project.vending.infrastructure.rest.controller.user.AuthenticationRestController;
import me.dahiorus.project.vending.infrastructure.rest.controller.user.SelfServiceRestController;
import me.dahiorus.project.vending.infrastructure.rest.controller.user.UserRegistrationRestController;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Root", description = "API discovery entry point")
@RestController
@RequestMapping("/api/v1")
public class RootRestController {

  @Operation(description = "Discover the links to every top-level resource of the API")
  @ApiResponse(responseCode = "200", description = "Links to the top-level resources")
  @GetMapping
  public RepresentationModel<?> index() {
    return new RepresentationModel<>()
        .add(linkTo(methodOn(RootRestController.class).index()).withSelfRel())
        .add(
            linkTo(methodOn(ItemCrudRestController.class).search(null, null, null))
                .withRel(ITEMS))
        .add(
            linkTo(methodOn(VendingMachineCrudRestController.class).search(null, null, null))
                .withRel(VENDING_MACHINES))
        .add(linkTo(methodOn(SelfServiceRestController.class).get(null)).withRel(ME))
        .add(linkTo(methodOn(AuthenticationRestController.class).authenticate(null)).withRel(AUTHENTICATE))
        .add(
            linkTo(methodOn(UserRegistrationRestController.class).create(null))
                .withRel(REGISTER))
        .add(Link.of("/v3/api-docs").withRel(DOCS));
  }
}
