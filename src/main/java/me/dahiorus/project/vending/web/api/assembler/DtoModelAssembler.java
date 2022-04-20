package me.dahiorus.project.vending.web.api.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import java.util.List;

import javax.annotation.Nonnull;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;

import me.dahiorus.project.vending.common.HasLogger;
import me.dahiorus.project.vending.core.exception.AppException;
import me.dahiorus.project.vending.core.model.AbstractEntity;
import me.dahiorus.project.vending.core.model.dto.AbstractDTO;
import me.dahiorus.project.vending.web.api.RestService;

public abstract class DtoModelAssembler<E extends AbstractEntity, T extends AbstractDTO<E>>
    implements SimpleRepresentationModelAssembler<T>, HasLogger
{
  @Override
  public void addLinks(final EntityModel<T> resource)
  {
    T content = resource.getContent();

    if (content == null)
    {
      return;
    }

    try
    {
      resource.add(selfLink(content))
        .add(buildLinks(content));
    }
    catch (AppException e)
    {
      getLogger().error("Unable to build links for {}: {}", content, e.getMessage());
    }
  }

  @Override
  public void addLinks(final CollectionModel<EntityModel<T>> resources)
  {
    // empty method
  }

  protected Link selfLink(final T content) throws AppException
  {
    getLogger().trace("Building self link for {}", content);

    return linkTo(methodOn(getControllerClass()).read(content.getId())).withSelfRel();
  }

  protected Iterable<Link> buildLinks(@Nonnull final T content) throws AppException
  {
    getLogger().trace("Building links for {}", content);

    return List.of();
  }

  @Nonnull
  protected abstract Class<? extends RestService<E, T>> getControllerClass();
}
