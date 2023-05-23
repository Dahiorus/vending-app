package me.dahiorus.project.vending.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "binary_data",
  uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "UK_BINARY_DATA_NAME"),
  indexes = @Index(columnList = "name, contentType", name = "IDX_BINARY_DATA_CONTENT_TYPE_NAME"))
public class BinaryData extends AbstractEntity
{
  private String name;

  private String contentType;

  private byte[] content;

  @Column(nullable = false)
  public String getName()
  {
    return name;
  }

  public void setName(final String name)
  {
    this.name = name;
  }

  @Column(nullable = false)
  public String getContentType()
  {
    return contentType;
  }

  public void setContentType(final String contentType)
  {
    this.contentType = contentType;
  }

  @Lob
  @Column(nullable = false)
  public byte[] getContent()
  {
    return content;
  }

  public void setContent(final byte[] content)
  {
    this.content = content;
  }
}
