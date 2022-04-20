package me.dahiorus.project.vending.web.api;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;

import me.dahiorus.project.vending.core.exception.AppRuntimeException;
import me.dahiorus.project.vending.core.model.AbstractEntity;
import me.dahiorus.project.vending.core.model.dto.AbstractDTO;

public class JsonPatchHandler
{
  private static final ObjectMapper mapper = new ObjectMapper();

  @SuppressWarnings("unchecked")
  public static <E extends AbstractEntity, D extends AbstractDTO<E>> D applyPatch(@Nonnull final D source,
      @Nonnull final JsonPatch jsonPatch)
  {
    try
    {
      String json = mapper.writeValueAsString(source);
      JsonNode patchedJson = jsonPatch.apply(mapper.readTree(json));

      return mapper.treeToValue(patchedJson, (Class<D>) source.getClass());
    }
    catch (JsonProcessingException | JsonPatchException e)
    {
      throw new AppRuntimeException("Unable to patch an entity", e);
    }
  }

  private JsonPatchHandler()
  {
    // util class
  }
}
