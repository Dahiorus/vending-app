package me.dahiorus.project.vending.util;

import static org.mockito.ArgumentMatchers.any;

import org.springframework.data.jpa.domain.Specification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.dahiorus.project.vending.domain.model.AbstractEntity;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestUtils
{
  private static final ObjectMapper MAPPER = JsonMapper.builder()
    .build();

  public static String jsonValue(final Object object) throws Exception
  {
    return MAPPER.writerWithDefaultPrettyPrinter()
      .writeValueAsString(object);
  }

  public static <T extends AbstractEntity> Specification<T> anySpec()
  {
    return any();
  }
}
