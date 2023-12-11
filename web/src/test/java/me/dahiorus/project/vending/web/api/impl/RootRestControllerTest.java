package me.dahiorus.project.vending.web.api.impl;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithAnonymousUser;

@WebMvcTest(RootRestController.class)
class RootRestControllerTest extends RestControllerTest
{
  @Test
  @WithAnonymousUser
  void getLinks() throws Exception
  {
    mockMvc.perform(get("/api/v1/"))
        .andExpect(status().isOk())
        .andExpect(result -> {
          jsonPath("_links").hasJsonPath();
          jsonPath("_links.public:authenticate").isNotEmpty();
          jsonPath("_links.public:register").isNotEmpty();
          jsonPath("_links.public:refresh-access").isNotEmpty();
          jsonPath("_links.items").isNotEmpty();
          jsonPath("_links.vending-machines").isNotEmpty();
          jsonPath("_links.users").isNotEmpty();
          jsonPath("_links.reports").isNotEmpty();
          jsonPath("_links.me").isNotEmpty();
        });
  }
}
