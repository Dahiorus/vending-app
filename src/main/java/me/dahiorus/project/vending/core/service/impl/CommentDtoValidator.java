package me.dahiorus.project.vending.core.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import me.dahiorus.project.vending.core.dao.AbstractDAO;
import me.dahiorus.project.vending.core.model.Comment;
import me.dahiorus.project.vending.core.model.Comment_;
import me.dahiorus.project.vending.core.model.dto.CommentDTO;

@Component
public class CommentDtoValidator extends DtoValidator<Comment, CommentDTO>
{
  private static final Logger logger = LogManager.getLogger(CommentDtoValidator.class);

  @Autowired
  public CommentDtoValidator(final AbstractDAO<Comment> dao)
  {
    super(dao);
  }

  @Override
  public Logger getLogger()
  {
    return logger;
  }

  @Override
  protected Class<CommentDTO> getSupportedClass()
  {
    return CommentDTO.class;
  }

  @Override
  protected void doValidate(final CommentDTO dto, final Errors errors)
  {
    Integer rate = dto.getRate();
    if (rate == null || rate < 0 || rate > 5)
    {
      errors.rejectValue(Comment_.RATE, "validation.constraints.comment.rate_interval",
          "A comment rate must be between 0 and 5 (included)");
    }
  }
}
