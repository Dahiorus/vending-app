package me.dahiorus.project.vending.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "app_role", uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "UK_USER_ROLE_NAME"),
    indexes = @Index(columnList = "name", name = "IDX_USER_ROLE_NAME"))
public class AppRole extends AbstractEntity
{
  private String name;

  @Column(nullable = false)
  public String getName()
  {
    return name;
  }

  public void setName(final String name)
  {
    this.name = name;
  }
}
