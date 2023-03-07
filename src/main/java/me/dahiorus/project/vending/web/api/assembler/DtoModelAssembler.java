package me.dahiorus.project.vending.web.api.assembler;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.UriComponents;

import me.dahiorus.project.vending.common.HasLogger;
import me.dahiorus.project.vending.domain.exception.AppException;
import me.dahiorus.project.vending.domain.model.dto.AbstractDTO;

public abstract class DtoModelAssembler<T extends AbstractDTO<?>>
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
      selfLink(content).ifPresent(resource::add);
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

  @SuppressWarnings({ "java:S1172", "java:S1130" })
  protected Optional<Link> selfLink(final T content) throws AppException
  {
    return Optional.empty();
  }

  @SuppressWarnings("java:S1130")
  protected Iterable<Link> buildLinks(@Nonnull final T content) throws AppException
  {
    getLogger().trace("Building links for {}", content);

    return List.of();
  }
}
