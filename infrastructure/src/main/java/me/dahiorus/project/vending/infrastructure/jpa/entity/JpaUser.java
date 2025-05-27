package me.dahiorus.project.vending.infrastructure.jpa.entity;

import static jakarta.persistence.FetchType.EAGER;
import static jakarta.persistence.FetchType.LAZY;
import static java.util.stream.Collectors.toSet;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import me.dahiorus.project.vending.domain.user.entity.AdminUser;
import me.dahiorus.project.vending.domain.user.entity.AdminUserToCreate;
import me.dahiorus.project.vending.domain.user.entity.AppUser;
import me.dahiorus.project.vending.domain.user.entity.AppUserToCreate;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.Firstname;
import me.dahiorus.project.vending.domain.user.entity.Lastname;
import me.dahiorus.project.vending.domain.user.entity.Role;
import me.dahiorus.project.vending.domain.user.entity.UserId;
import me.dahiorus.project.vending.domain.user.entity.UserWithRoles;

@Entity
@Table(
    name = "app_user",
    uniqueConstraints = @UniqueConstraint(columnNames = "email", name = "UK_USER_EMAIL"),
    indexes = {
      @Index(columnList = "firstName, lastName", name = "IDX_USER_FIRST_NAME_LAST_NAME"),
      @Index(columnList = "firstName", name = "IDX_USER_FIRST_NAME"),
      @Index(columnList = "lastName", name = "IDX_USER_LAST_NAME"),
      @Index(columnList = "email", name = "IDX_USER_EMAIL")
    })
@AttributeOverride(name = "id", column = @Column(name = "user_id"))
public class JpaUser extends JpaEntity {
  public static final String ROLE_ADMIN = "ADMIN";
  public static final String ROLE_USER = "USER";

  @Column(nullable = false)
  private String firstName;

  @Column(nullable = false)
  private String lastName;

  @Column(nullable = false)
  private String email;

  @Column(name = "password")
  private String encodedPassword;

  @Transient private String password;

  @OneToOne(fetch = LAZY, orphanRemoval = true)
  @JoinColumn(name = "profile_picture_id", foreignKey = @ForeignKey(name = "FK_USER_PICTURE_ID"))
  private JpaUploadedFile profilePicture;

  @ElementCollection(fetch = EAGER)
  @CollectionTable(
      name = "app_user_role",
      indexes = @Index(columnList = "role_name", name = "IDX_USER_ROLE_NAME"),
      joinColumns =
          @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "FK_USER_ROLE_USER_ID")))
  @Column(name = "role_name", nullable = false)
  private Set<String> roles = new HashSet<>();

  public String getEmail() {
    return email;
  }

  public String getEncodedPassword() {
    return encodedPassword;
  }

  public void setEncodedPassword(final String encodedPassword) {
    this.encodedPassword = encodedPassword;
  }

  public Set<String> getRoles() {
    return roles;
  }

  public void setRoles(final Set<String> roles) {
    this.roles.clear();
    this.roles.addAll(roles);
  }

  public Optional<JpaUploadedFile> maybeProfilePicture() {
    return Optional.ofNullable(profilePicture);
  }

  public void setProfilePicture(final JpaUploadedFile profilePicture) {
    this.profilePicture = profilePicture;
  }

  public static JpaUser toCreateFrom(AdminUserToCreate toCreate) {
    var jpaUser = new JpaUser();
    jpaUser.email = toCreate.emailAddress().value();
    jpaUser.password = toCreate.password().value();
    jpaUser.firstName = toCreate.firstname().value();
    jpaUser.lastName = toCreate.lastname().value();
    jpaUser.setRoles(Set.of(ROLE_ADMIN));

    return jpaUser;
  }

  public AdminUser toAdminUser() {
    return new AdminUser(
        new UserId(getId()),
        EmailAddress.of(email),
        Firstname.of(firstName),
        Lastname.of(lastName));
  }

  public static JpaUser toCreateFrom(AppUserToCreate toCreate) {
    var jpaUser = new JpaUser();
    jpaUser.email = toCreate.emailAddress().value();
    jpaUser.password = toCreate.password().value();
    jpaUser.firstName = toCreate.firstname().value();
    jpaUser.lastName = toCreate.lastname().value();
    jpaUser.setRoles(Set.of(ROLE_USER));
    return jpaUser;
  }

  public AppUser toUser() {
    return new AppUser(
        new UserId(getId()),
        EmailAddress.of(email),
        Firstname.of(firstName),
        Lastname.of(lastName));
  }

  public UserWithRoles toUserWithRoles() {
    return new UserWithRoles(
        new UserId(getId()),
        EmailAddress.of(email),
        roles.stream().map(Role::new).collect(toSet()));
  }

  public static JpaUser fromDomain(AppUser appUser) {
    var jpaUser = new JpaUser();
    Optional.ofNullable(appUser.id()).map(UserId::value).ifPresent(jpaUser::setId);
    jpaUser.email = appUser.email().value();
    jpaUser.firstName = appUser.firstname().value();
    jpaUser.lastName = appUser.lastname().value();

    return jpaUser;
  }

  public JpaUser updateFrom(final AppUser toUpdate) {
    this.firstName = toUpdate.firstname().value();
    this.lastName = toUpdate.lastname().value();

    return this;
  }
}
