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
import me.dahiorus.project.vending.domain.exception.AppRuntimeException;
import me.dahiorus.project.vending.domain.model.AbstractEntity;
import me.dahiorus.project.vending.domain.model.dto.AbstractDTO;

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
      JsonNode json = mapper.valueToTree(source);
      JsonNode patchedJson = jsonPatch.apply(json);

      return mapper.treeToValue(patchedJson, (Class<D>) source.getClass());
    }
    catch (JsonProcessingException | JsonPatchException e)
    {
      throw new AppRuntimeException(e.getMessage(), e);
    }
  }
}
