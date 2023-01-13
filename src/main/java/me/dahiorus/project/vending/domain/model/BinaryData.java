package me.dahiorus.project.vending.domain.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "binary_data",
  uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "UK_BINARY_DATA_NAME"),
  indexes = @Index(columnList = "name, contentType", name = "IDX_BINARY_DATA_CONTENT_TYPE_NAME"))
public class BinaryData extends AbstractEntity
{
  private String name;

  private String contentType;

  private Long size;

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

  @Column(nullable = false)
  public Long getSize()
  {
    return size;
  }

  public void setSize(final Long size)
  {
    this.size = size;
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
