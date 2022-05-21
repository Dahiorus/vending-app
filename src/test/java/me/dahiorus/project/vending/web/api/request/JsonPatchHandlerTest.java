package me.dahiorus.project.vending.web.api.request;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.fge.jsonpatch.JsonPatch;

import me.dahiorus.project.vending.domain.model.AbstractEntity;
import me.dahiorus.project.vending.domain.model.dto.AbstractDTO;

class JsonPatchHandlerTest
{
  static ObjectMapper MAPPER = new ObjectMapper();
  static
  {
    MAPPER.registerModule(new JavaTimeModule());
    MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
  }

  static class JsonPatchArgumentProvider implements ArgumentsProvider
  {
    @Override
    public Stream<? extends Arguments> provideArguments(final ExtensionContext context) throws Exception
    {
      InputStream testValuesInputStream = JsonPatchHandlerTest.class.getClassLoader()
        .getResourceAsStream("json-patch-tests-values.json");
      JsonPatchTestArgument[] values = MAPPER.readValue(testValuesInputStream, JsonPatchTestArgument[].class);

      return Stream.of(values)
        .map(Arguments::of);
    }
  }

  @SuppressWarnings("unchecked")
  static class JsonPatchTestArgument
  {
    @JsonProperty
    String description;

    @JsonProperty
    Class<? extends AbstractDTO<?>> objectClass;

    @JsonProperty
    Object source;

    @JsonProperty
    JsonPatch patch;

    @JsonProperty
    Object expected;

    @Override
    public String toString()
    {
      return description;
    }

    <D extends AbstractDTO<?>> D source()
    {
      return (D) MAPPER.convertValue(source, objectClass);
    }

    <D extends AbstractDTO<?>> D expected()
    {
      return (D) MAPPER.convertValue(expected, objectClass);
    }
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(JsonPatchArgumentProvider.class)
  void patchDto(final JsonPatchTestArgument arg)
  {
    AbstractDTO<? extends AbstractEntity> patched = JsonPatchHandler.applyPatch(arg.source(), arg.patch);
    assertThat(patched).isEqualTo(arg.expected());
  }
}
