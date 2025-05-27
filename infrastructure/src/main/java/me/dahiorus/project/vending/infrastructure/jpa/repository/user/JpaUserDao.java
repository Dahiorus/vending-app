package me.dahiorus.project.vending.infrastructure.jpa.repository.user;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaUserDao extends JpaRepository<JpaUser, UUID> {
  Optional<JpaUser> findByEmail(final String email);

  @Query(
      """
          FROM JpaUser appUser
          JOIN appUser.roles roles
          WHERE appUser.id = :id AND roles IN :roles
          """)
  Optional<JpaUser> findByIdAndRoles(final UUID id, final Set<String> roles);

  @Query(
      """
          FROM JpaUser appUser
          JOIN appUser.roles roles
          WHERE appUser.email = :email AND roles IN :roles
          """)
  Optional<JpaUser> findByEmailAndRoles(final String email, final Set<String> roles);

  boolean existsByEmail(String email);
}
