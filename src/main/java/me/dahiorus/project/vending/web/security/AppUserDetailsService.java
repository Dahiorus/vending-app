package me.dahiorus.project.vending.web.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import me.dahiorus.project.vending.core.dao.UserDAO;
import me.dahiorus.project.vending.core.model.AppRole;

@Component
@RequiredArgsConstructor
public class AppUserDetailsService implements UserDetailsService
{
  private final UserDAO userDao;

  @Override
  public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException
  {
    return userDao.findByEmail(username)
      .map(user -> User.builder()
        .username(user.getEmail())
        .password(user.getPassword())
        .authorities(user.getRoles()
          .stream()
          .map(AppRole::getName)
          .toArray(String[]::new))
        .build())
      .orElseThrow(() -> new UsernameNotFoundException("No user found with username " + username));
  }
}
