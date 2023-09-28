package me.dahiorus.project.vending.web.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WebTestUtils
{
  private static final ObjectMapper MAPPER = JsonMapper.builder()
      .build();

  public static String jsonValue(final Object object) throws Exception
  {
    return MAPPER.writerWithDefaultPrettyPrinter()
        .writeValueAsString(object);
  }
}
