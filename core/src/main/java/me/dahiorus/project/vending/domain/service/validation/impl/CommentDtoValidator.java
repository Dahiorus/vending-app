package me.dahiorus.project.vending.domain.service.validation.impl;

import static me.dahiorus.project.vending.domain.service.validation.FieldValidationError.fieldError;
import static me.dahiorus.project.vending.domain.service.validation.ValidationError.getFullCode;

import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.domain.dao.Dao;
import me.dahiorus.project.vending.domain.model.Comment;
import me.dahiorus.project.vending.domain.model.Comment_;
import me.dahiorus.project.vending.domain.model.dto.CommentDto;
import me.dahiorus.project.vending.domain.service.validation.ValidationResults;

@Log4j2
@Component
public class CommentDtoValidator extends DtoValidatorImpl<Comment, CommentDto, Dao<Comment>>
{
  public CommentDtoValidator(final Dao<Comment> dao)
  {
    super(dao);
  }

  @Override
  public Logger getLogger()
  {
    return log;
  }

  @Override
  protected void doValidate(final CommentDto dto, final ValidationResults results)
  {
    Integer rate = dto.getRate();
    if (rate == null || rate < 0 || rate > 5)
    {
      results.addError(fieldError(Comment_.RATE, getFullCode("comment.rate_interval"),
        "A comment rate must be between 0 and 5 (included)", rate));
    }

    rejectIfInvalidLength(Comment_.CONTENT, dto.getContent(), 1024, results);
  }
}
