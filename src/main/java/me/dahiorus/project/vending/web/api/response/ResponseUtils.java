package me.dahiorus.project.vending.web.api.response;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.fromMethodCall;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

import java.net.URI;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.model.dto.AbstractDTO;
import me.dahiorus.project.vending.web.api.ReadRestAPI;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ResponseUtils
{
  public static <D extends AbstractDTO<?>, A extends ReadRestAPI<D>> URI buildLocation(final D createdEntity,
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
