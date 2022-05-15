package me.dahiorus.project.vending.core.model;

import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "app_user", uniqueConstraints = @UniqueConstraint(columnNames = "email", name = "UK_USER_EMAIL"),
    indexes = {
        @Index(columnList = "firstName, lastName", name = "IDX_USER_FIRST_NAME_LAST_NAME"),
        @Index(columnList = "firstName", name = "IDX_USER_FIRST_NAME"),
        @Index(columnList = "lastName", name = "IDX_USER_LAST_NAME"),
        @Index(columnList = "email", name = "IDX_USER_EMAIL")
    })
public class AppUser extends AbstractEntity
{
  private String firstName;

  private String lastName;

  private String email;

  private String encodedPassword;

  private String password;

  private BinaryData picture;

  private List<String> roles;

  public String getFirstName()
  {
    return firstName;
  }

  public void setFirstName(final String firstName)
  {
    this.firstName = firstName;
  }

  @Column(nullable = false)
  public String getLastName()
  {
    return lastName;
  }

  public void setLastName(final String lastName)
  {
    this.lastName = lastName;
  }

  @Column(nullable = false)
  public String getEmail()
  {
    return email;
  }

  public void setEmail(final String email)
  {
    this.email = email;
  }

  @Column(name = "password")
  public String getEncodedPassword()
  {
    return encodedPassword;
  }

  public void setEncodedPassword(final String encodedPassword)
  {
    this.encodedPassword = encodedPassword;
  }

  @Transient
  public String getPassword()
  {
    return password;
  }

  public void setPassword(final String password)
  {
    this.password = password;
  }

  @OneToOne(fetch = FetchType.LAZY, optional = true, orphanRemoval = true)
  @JoinColumn(name = "picture_id", foreignKey = @ForeignKey(name = "FK_USER_PICTURE_ID"))
  public BinaryData getPicture()
  {
    return picture;
  }

  public void setPicture(final BinaryData picture)
  {
    this.picture = picture;
  }

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "app_user_role", indexes = @Index(columnList = "role_name", name = "IDX_USER_ROLE_NAME"),
      joinColumns = @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "FK_USER_ROLE_USER_ID")))
  @Column(name = "role_name", nullable = false)
  public List<String> getRoles()
  {
    return roles;
  }

  public void setRoles(final List<String> roles)
  {
    this.roles = roles;
  }
}
