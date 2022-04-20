package me.dahiorus.project.vending.core.model.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import me.dahiorus.project.vending.core.model.Comment;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class CommentDTO extends AbstractDTO<Comment>
{
  @Getter
  @Setter
  private String content;

  @Getter
  @Setter
  private Integer rate;

  @JsonIgnore
  @Getter
  @Setter
  private UUID vendingMachineId;

  @Override
  public Class<Comment> getEntityClass()
  {
    return Comment.class;
  }
}
