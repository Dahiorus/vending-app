package me.dahiorus.project.vending.core.service.validation.impl;

import static me.dahiorus.project.vending.core.service.validation.FieldValidationError.fieldError;
import static me.dahiorus.project.vending.core.service.validation.ValidationError.getFullCode;

import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.core.dao.CommentDAO;
import me.dahiorus.project.vending.core.model.Comment;
import me.dahiorus.project.vending.core.model.Comment_;
import me.dahiorus.project.vending.core.model.dto.CommentDTO;
import me.dahiorus.project.vending.core.service.validation.ValidationResults;

@Log4j2
@Component
public class CommentDtoValidator extends DtoValidatorImpl<Comment, CommentDTO, CommentDAO>
{
  public CommentDtoValidator(final CommentDAO dao)
  {
    super(dao);
  }

  @Override
  public Logger getLogger()
  {
    return log;
  }

  @Override
  protected void doValidate(final CommentDTO dto, final ValidationResults results)
  {
    Integer rate = dto.getRate();
    if (rate == null || rate < 0 || rate > 5)
    {
      results.addError(fieldError(Comment_.RATE, getFullCode("comment.rate_interval"),
          "A comment rate must be between 0 and 5 (included)", rate));
    }
  }
}
