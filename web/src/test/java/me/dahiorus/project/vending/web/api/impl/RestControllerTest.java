package me.dahiorus.project.vending.web.api.impl;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import me.dahiorus.project.vending.web.config.WebSecurityConfig;
import me.dahiorus.project.vending.web.security.UserDaoDetailsService;
import me.dahiorus.project.vending.web.security.impl.JwtServiceImpl;

@Import(WebSecurityConfig.class)
public abstract class RestControllerTest
{
  @MockBean
  UserDaoDetailsService userDetailsService;

  @MockBean
  JwtServiceImpl jwtService;

  @Autowired
  WebApplicationContext appContext;

  MockMvc mockMvc;

  @BeforeEach
  void initMockMvc() throws Exception
  {
    mockMvc = webAppContextSetup(appContext)
      .apply(springSecurity())
      .build();
  }
}
