package me.dahiorus.project.vending.core.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

import org.apache.commons.lang3.StringUtils;

@Entity
@Table(name = "comment", indexes = @Index(name = "IDX_COMMENT_RATE", columnList = "rate"))
public class Comment extends AbstractEntity
{
  private String content;

  private Integer rate;

  @Column(length = 1024)
  public String getContent()
  {
    return content;
  }

  public void setContent(final String content)
  {
    this.content = content;
  }

  @Column(nullable = false)
  public Integer getRate()
  {
    return rate;
  }

  public void setRate(final Integer rate)
  {
    this.rate = rate;
  }

  @Override
  public String toString()
  {
    return super.toString() + "[content=" + StringUtils.abbreviate(content, 50) + ", rate=" + rate + "]";
  }
}
