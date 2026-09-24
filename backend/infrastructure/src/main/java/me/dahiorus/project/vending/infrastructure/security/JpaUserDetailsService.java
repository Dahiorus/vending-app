package me.dahiorus.project.vending.infrastructure.security;

import static java.lang.String.format;

import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.port.UserWithRolesRepositoryPort;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class JpaUserDetailsService implements UserDetailsService {

  private final UserWithRolesRepositoryPort userWithRolesRepository;

  public JpaUserDetailsService(final UserWithRolesRepositoryPort userWithRolesRepository) {
    this.userWithRolesRepository = userWithRolesRepository;
  }

  @Override
  public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
    try {
      var user = userWithRolesRepository.getByUsername(EmailAddress.of(username));

      return User.withUsername(user.username().value())
          .password(user.encodedPassword().value())
          .roles(user.rolesAsStringArray())
          .build();
    } catch (ResourceNotFound _) {
      throw new UsernameNotFoundException(format("No user found with username '%s'.", username));
    }
  }
}
