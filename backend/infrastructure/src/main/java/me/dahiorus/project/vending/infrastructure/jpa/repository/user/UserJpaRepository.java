package me.dahiorus.project.vending.infrastructure.jpa.repository.user;

import static org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.*;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaUser;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
interface UserJpaRepository extends JpaRepository<JpaUser, UUID> {
  @Query(
      """
        FROM JpaUser user
        JOIN FETCH user.roles roles
        WHERE user.email = :email
        """)
  Optional<JpaUser> findByEmail(final String email);

  @Query(
      """
          FROM JpaUser user
          JOIN user.roles roles
          WHERE user.id = :id AND roles IN :roles
          """)
  Optional<JpaUser> findByIdAndRoles(final UUID id, final Set<String> roles);

  @Query(
      """
          FROM JpaUser user
          JOIN user.roles roles
          WHERE user.email = :email AND roles IN :roles
          """)
  Optional<JpaUser> findByEmailAndRoles(final String email, final Set<String> roles);

  @EntityGraph(value = "JpaUser.profilePicture", type = FETCH)
  Optional<JpaUser> findWithProfilePictureById(final UUID id);

  boolean existsByEmail(String email);
}
