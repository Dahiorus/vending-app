package me.dahiorus.project.vending.web.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import me.dahiorus.project.vending.core.dao.UserDAO;
import me.dahiorus.project.vending.core.model.AppUser;
import me.dahiorus.project.vending.util.UserBuilder;

@ExtendWith(MockitoExtension.class)
class AppUserDetailsServiceTest
{
  AppUserDetailsService userDetailsService;

  @Mock
  UserDAO userDao;

  @BeforeEach
  void setUp()
  {
    userDetailsService = new AppUserDetailsService(userDao);
  }

  @Test
  void loadUserByUsername() throws Exception
  {
    AppUser user = UserBuilder.builder()
      .email("user@test.com")
      .password("secret")
      .roles(List.of("ROLE_USER"))
      .build();
    when(userDao.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

    UserDetails userDetails = userDetailsService.loadUserByUsername("user@test.com");

    assertThat(userDetails).satisfies(u -> assertThat(u.getUsername()).isEqualTo(user.getEmail()))
      .satisfies(u -> assertThat(u.getPassword()).isEqualTo(user.getPassword()))
      .satisfies(u -> assertThat(u.getAuthorities()).extracting(GrantedAuthority::getAuthority)
        .containsExactlyElementsOf(user.getRoles()));
  }

  @Test
  void loadUserByUsernameThrowsException() throws Exception
  {
    when(userDao.findByEmail("user@test.fr")).thenReturn(Optional.empty());
    assertThatExceptionOfType(UsernameNotFoundException.class)
      .isThrownBy(() -> userDetailsService.loadUserByUsername("user@test.fr"));
  }
}
