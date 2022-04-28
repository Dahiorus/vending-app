package me.dahiorus.project.vending.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "app_user", uniqueConstraints = @UniqueConstraint(columnNames = "email", name = "UK_USER_EMAIL"),
    indexes = {
        @Index(columnList = "firstName, lastName", name = "IDX_USER_FIRST_NAME_LAST_NAME"),
        @Index(columnList = "firstName", name = "IDX_USER_FIRST_NAME"),
        @Index(columnList = "lastName", name = "IDX_USER_LAST_NAME"),
        @Index(columnList = "email", name = "IDX_USER_EMAIL")
    })
public class User extends AbstractEntity
{
  private String firstName;

  private String lastName;

  private String email;

  private String password;

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

  @Column
  public String getPassword()
  {
    return password;
  }

  public void setPassword(final String password)
  {
    this.password = password;
  }
}
