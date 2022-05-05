package me.dahiorus.project.vending.core.dao;

import me.dahiorus.project.vending.core.model.Comment;

public interface CommentDAO extends DAO<Comment>
{
  @Override
  default Class<Comment> getDomainClass()
  {
    return Comment.class;
  }
}
