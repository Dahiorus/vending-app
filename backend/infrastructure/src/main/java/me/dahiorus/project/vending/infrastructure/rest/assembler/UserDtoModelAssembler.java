package me.dahiorus.project.vending.infrastructure.rest.assembler;

import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.SELF_PASSWORD;
import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.SELF_PICTURE;
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
        .map(UserDtoModelAssembler::buildLinks)
        .ifPresent(resource::add);
  }

  private static Set<Link> buildLinks(UserDto content) {
    return Set.of(
        linkTo(methodOn(SelfServiceRestController.class).get(null)).withSelfRel(),
        linkTo(methodOn(SelfServiceRestController.class).getProfilePicture(null))
            .withRel(SELF_PICTURE),
        linkTo(
                methodOn(SelfServiceRestController.class)
                    .updatePassword(null, new EditPasswordRequestDto(null, null)))
            .withRel(SELF_PASSWORD));
  }

  @Override
  public void addLinks(final CollectionModel<EntityModel<UserDto>> resources) {
    // no action
  }
}
