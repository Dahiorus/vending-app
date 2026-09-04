package me.dahiorus.project.vending.infrastructure.security;

import me.dahiorus.project.vending.infrastructure.jpa.repository.user.UserJpaRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class JpaUserDetailsService implements UserDetailsService {

  private final UserJpaRepository userJpaRepository;

  public JpaUserDetailsService(final UserJpaRepository userJpaRepository) {
    this.userJpaRepository = userJpaRepository;
  }

  @Override
  public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
    return userJpaRepository
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
