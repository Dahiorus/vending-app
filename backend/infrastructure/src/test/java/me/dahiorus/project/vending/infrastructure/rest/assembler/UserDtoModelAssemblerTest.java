package me.dahiorus.project.vending.infrastructure.rest.assembler;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.SELF_PASSWORD;
import static me.dahiorus.project.vending.infrastructure.rest.assembler.Relation.SELF_PICTURE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import java.util.List;
import java.util.UUID;
import me.dahiorus.project.vending.infrastructure.rest.entity.user.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;

class UserDtoModelAssemblerTest {
  private final UserDtoModelAssembler assembler = new UserDtoModelAssembler();

  @Test
  void should_add_self_link_with_update_affordance() {
    // Given
    var user = new UserDto(UUID.randomUUID(), "user@test.com", "John", "Doe");
    var resource = EntityModel.of(user);

    // When
    assembler.addLinks(resource);

    // Then
    var selfLink = resource.getRequiredLink("self");
    assertThat(selfLink.getHref()).endsWith("/api/v1/me");

    var affordanceMethods =
        selfLink.getAffordances().stream()
            .flatMap(affordance -> stream(affordance.spliterator(), false))
            .map(model -> model.getName() + ":" + model.getHttpMethod())
            .collect(toSet());
    assertThat(affordanceMethods).containsExactlyInAnyOrder("update:" + PUT, "get:" + GET);
  }

  @Test
  void should_add_password_link_with_update_password_affordance() {
    // Given
    var user = new UserDto(UUID.randomUUID(), "user@test.com", "John", "Doe");
    var resource = EntityModel.of(user);

    // When
    assembler.addLinks(resource);

    // Then
    var passwordLink = resource.getRequiredLink(SELF_PASSWORD);
    assertThat(passwordLink.getHref()).endsWith("/api/v1/me/password");

    var affordanceMethods =
        passwordLink.getAffordances().stream()
            .flatMap(affordance -> stream(affordance.spliterator(), false))
            .map(model -> model.getName() + ":" + model.getHttpMethod())
            .toList();
    assertThat(affordanceMethods).contains("updatePassword:" + POST);
  }

  @Test
  void should_add_picture_link_with_upload_affordance() {
    // Given
    var user = new UserDto(UUID.randomUUID(), "user@test.com", "John", "Doe");
    var resource = EntityModel.of(user);

    // When
    assembler.addLinks(resource);

    // Then
    var pictureLink = resource.getRequiredLink(SELF_PICTURE);
    assertThat(pictureLink.getHref()).endsWith("/api/v1/me/picture");

    var affordanceMethods =
        pictureLink.getAffordances().stream()
            .flatMap(affordance -> stream(affordance.spliterator(), false))
            .map(model -> model.getName() + ":" + model.getHttpMethod())
            .toList();
    assertThat(affordanceMethods).contains("uploadProfilePicture:" + POST);
  }

  @Test
  void should_do_nothing_on_collection_model() {
    // Given
    var resources = CollectionModel.of(List.<EntityModel<UserDto>>of());

    // When
    assembler.addLinks(resources);

    // Then
    assertThat(resources.getLinks()).isEmpty();
  }
}
