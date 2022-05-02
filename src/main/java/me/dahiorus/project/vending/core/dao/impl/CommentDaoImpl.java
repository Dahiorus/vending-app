package me.dahiorus.project.vending.core.dao.impl;

import javax.persistence.EntityManager;

import org.springframework.stereotype.Repository;

import me.dahiorus.project.vending.core.model.Comment;

@Repository
public class CommentDaoImpl extends AbstractDAO<Comment>
{
  public CommentDaoImpl(final EntityManager em)
  {
    super(Comment.class, em);
  }
}