package me.dahiorus.project.vending.core.service.validation.impl;

import static me.dahiorus.project.vending.core.service.validation.FieldValidationError.notUniqueValue;

import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import me.dahiorus.project.vending.core.dao.UserDAO;
import me.dahiorus.project.vending.core.model.User_;
import me.dahiorus.project.vending.core.model.dto.UserDTO;
import me.dahiorus.project.vending.core.service.validation.ValidationResults;

@Component
public class UserDtoValidator extends DtoValidatorImpl<UserDTO, UserDAO>
{
  private static final Logger logger = LogManager.getLogger(UserDtoValidator.class);

  public UserDtoValidator(final UserDAO dao)
  {
    super(dao);
  }

  @Override
  public Logger getLogger()
  {
    return logger;
  }

  @Override
  protected void doValidate(final UserDTO dto, final ValidationResults results)
  {
    rejectIfBlank(User_.EMAIL, dto.getEmail(), results);
    rejectIfBlank(User_.FIRST_NAME, dto.getFirstName(), results);
    rejectIfBlank(User_.LAST_NAME, dto.getLastName(), results);

    if (!results.hasFieldError(User_.EMAIL))
    {
      logger.trace("Validating email uniqueness...");
      dao.findByEmail(dto.getEmail())
        .ifPresent(other -> {
          if (!Objects.equals(dto.getId(), other.getId()))
          {
            results.addError(notUniqueValue(User_.EMAIL, dto.getEmail()));
          }
        });
    }
  }
}
