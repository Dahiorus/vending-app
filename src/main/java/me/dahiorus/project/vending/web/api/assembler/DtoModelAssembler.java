package me.dahiorus.project.vending.web.api.assembler;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import me.dahiorus.project.vending.common.HasLogger;
import me.dahiorus.project.vending.core.exception.AppException;
import me.dahiorus.project.vending.core.model.AbstractEntity;
import me.dahiorus.project.vending.core.model.dto.AbstractDTO;

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
      Link selfLink = selfLink(content);
      if (selfLink != null)
      {
        resource.add(selfLink);
      }
      resource.add(buildLinks(content));
    }
    catch (AppException e)
    {
      getLogger().error("Unable to build links for {}: {}", content, e.getMessage());
    }
  }

  @Override
  public void addLinks(final CollectionModel<EntityModel<T>> resources)
  {
    UriComponents requestUri = ServletUriComponentsBuilder.fromCurrentRequest()
      .build();

    resources.add(Link.of(requestUri.toString()));
  }

  @Nullable
  protected Link selfLink(final T content) throws AppException
  {
    return null;
  }

  protected Iterable<Link> buildLinks(@Nonnull final T content) throws AppException
  {
    getLogger().trace("Building links for {}", content);

    return List.of();
  }
}
