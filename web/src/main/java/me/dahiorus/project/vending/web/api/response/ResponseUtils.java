package me.dahiorus.project.vending.web.api.response;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.fromMethodCall;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.net.URI;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.model.dto.AbstractDto;
import me.dahiorus.project.vending.web.api.ReadRestApi;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseUtils
{
  public static <D extends AbstractDto<?>, A extends ReadRestApi<D>> URI buildLocation(final D createdEntity,
    final Class<A> apiClass)
  {
    try
    {
      return fromMethodCall(on(apiClass).read(createdEntity.getId())).build()
        .toUri();
    }
    catch (EntityNotFound ignore)
    {
      // should not happen
      return null;
    }
  }
}
