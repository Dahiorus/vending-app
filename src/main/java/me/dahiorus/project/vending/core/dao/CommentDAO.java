package me.dahiorus.project.vending.core.dao;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import me.dahiorus.project.vending.core.model.Comment;

@Repository
public class CommentDAO extends AbstractDAO<Comment>
{
  public CommentDAO(final EntityManager em)
  {
    super(Comment.class, em);
  }
}