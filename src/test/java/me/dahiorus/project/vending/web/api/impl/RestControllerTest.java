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
import me.dahiorus.project.vending.web.security.AppUserDetailsService;
import me.dahiorus.project.vending.web.security.impl.JwtServiceImpl;

@Import(WebSecurityConfig.class)
public abstract class RestControllerTest
{
  // FIXME rework WebSecurityConfig to use getSharedClass instead of spring injection
  @MockBean
  AppUserDetailsService userDetailsService;

  @MockBean
  JwtServiceImpl jwtService;
  // end FIXME

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
