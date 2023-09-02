package me.dahiorus.project.vending.web.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import me.dahiorus.project.vending.domain.dao.UserDao;

@Component
@RequiredArgsConstructor
public class UserDaoDetailsService implements UserDetailsService
{
  private final UserDao userDao;

  @Override
  public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException
  {
    return userDao.findByEmail(username)
      .map(user -> User.withUsername(user.getEmail())
        .password(user.getEncodedPassword())
        .authorities(user.getRoles()
          .toArray(String[]::new))
        .build())
      .orElseThrow(() -> new UsernameNotFoundException("No user found with username " + username));
  }
}
