package me.dahiorus.project.vending.infrastructure.rest.assembler;

import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.SELF_PASSWORD;
import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.SELF_PICTURE;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.Optional;
import java.util.Set;
import me.dahiorus.project.vending.infrastructure.rest.controller.user.SelfServiceRestController;
import me.dahiorus.project.vending.infrastructure.rest.entity.user.EditPasswordRequestDto;
import me.dahiorus.project.vending.infrastructure.rest.entity.user.UserDto;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class UserDtoModelAssembler implements SimpleRepresentationModelAssembler<UserDto> {

  @Override
  public void addLinks(final EntityModel<UserDto> resource) {
    Optional.ofNullable(resource.getContent())
        .map(UserDtoModelAssembler::linksOf)
        .ifPresent(resource::add);
  }

  private static Set<Link> linksOf(UserDto content) {
    return Set.of(
        linkTo(methodOn(SelfServiceRestController.class).get(null))
            .withSelfRel()
            .andAffordance(afford(methodOn(SelfServiceRestController.class).update(null, null))),
        linkTo(methodOn(SelfServiceRestController.class).updatePassword(null, null))
            .withRel(SELF_PASSWORD)
            .andAffordance(
                afford(
                    methodOn(SelfServiceRestController.class)
                        .updatePassword(null, new EditPasswordRequestDto(null, null)))),
        linkTo(methodOn(SelfServiceRestController.class).getProfilePicture(null))
            .withRel(SELF_PICTURE)
            .andAffordance(
                afford(
                    methodOn(SelfServiceRestController.class).uploadProfilePicture(null, null))));
  }

  @Override
  public void addLinks(final CollectionModel<EntityModel<UserDto>> resources) {
    // no action
  }
}
