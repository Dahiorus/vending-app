package me.dahiorus.project.vending.infrastructure.security;

import me.dahiorus.project.vending.infrastructure.jpa.repository.user.JpaUserDao;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class UserDaoDetailsService implements UserDetailsService {

  private final JpaUserDao jpaUserDao;

  public UserDaoDetailsService(final JpaUserDao jpaUserDao) {
    this.jpaUserDao = jpaUserDao;
  }

  @Override
  public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
    return jpaUserDao
        .findByEmail(username)
        .map(
            appUser ->
                User.withUsername(appUser.getEmail())
                    .password(appUser.getEncodedPassword())
                    .roles(appUser.getRoles().toArray(String[]::new))
                    .build())
        .orElseThrow(
            () ->
                new UsernameNotFoundException(
                    String.format("No user found with username '%s'.", username)));
  }
}
