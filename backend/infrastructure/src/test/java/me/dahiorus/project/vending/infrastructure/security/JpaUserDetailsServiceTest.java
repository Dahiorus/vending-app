package me.dahiorus.project.vending.infrastructure.security;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.BDDMockito.given;

import java.util.Set;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.Password;
import me.dahiorus.project.vending.domain.user.entity.Role;
import me.dahiorus.project.vending.domain.user.entity.UserId;
import me.dahiorus.project.vending.domain.user.entity.UserWithRoles;
import me.dahiorus.project.vending.domain.user.port.UserWithRolesRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class JpaUserDetailsServiceTest {
  @Mock UserWithRolesRepositoryPort userWithRolesRepository;
  @InjectMocks JpaUserDetailsService userDetailsService;

  @Test
  void should_load_user_by_username() {
    var user =
        new UserWithRoles(
            new UserId(randomUUID()),
            EmailAddress.of("user@test.fr"),
            Password.of("encoded-password"),
            Set.of(new Role("USER")));

    given(userWithRolesRepository.getByUsername(user.username())).willReturn(user);

    var result = userDetailsService.loadUserByUsername("user@test.fr");

    assertThat(result)
        .satisfies(
            userDetails -> {
              assertThat(userDetails.getUsername()).isEqualTo("user@test.fr");
              assertThat(userDetails.getPassword()).isEqualTo("encoded-password");
              assertThat(userDetails.getAuthorities())
                  .map(GrantedAuthority::getAuthority)
                  .containsExactly("ROLE_USER");
            });
  }

  @Test
  void should_throw_username_not_found_given_user_not_found() {
    var emailAddress = EmailAddress.of("user_not_found@test.fr");
    given(userWithRolesRepository.getByUsername(emailAddress))
        .willThrow(new ResourceNotFound("User not found"));

    var throwable =
        catchThrowable(() -> userDetailsService.loadUserByUsername("user_not_found@test.fr"));

    assertThat(throwable).isInstanceOf(UsernameNotFoundException.class);
  }
}
