package me.dahiorus.project.vending.web.api.request;

import javax.annotation.Nonnull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchException;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.dahiorus.project.vending.core.exception.AppRuntimeException;
import me.dahiorus.project.vending.core.model.AbstractEntity;
import me.dahiorus.project.vending.core.model.dto.AbstractDTO;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonPatchHandler
{
  private static final ObjectMapper mapper = new ObjectMapper();

  static
  {
    mapper.registerModule(new JavaTimeModule());
    mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }

  @SuppressWarnings("unchecked")
  public static <D extends AbstractDTO<? extends AbstractEntity>> D applyPatch(@Nonnull final D source,
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
}
