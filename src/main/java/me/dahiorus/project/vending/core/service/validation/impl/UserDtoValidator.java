package me.dahiorus.project.vending.core.service.validation.impl;

import static me.dahiorus.project.vending.core.service.validation.FieldValidationError.notUniqueValue;

import java.util.Objects;

import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.core.dao.UserDAO;
import me.dahiorus.project.vending.core.model.AppUser;
import me.dahiorus.project.vending.core.model.AppUser_;
import me.dahiorus.project.vending.core.model.dto.UserDTO;
import me.dahiorus.project.vending.core.service.validation.ValidationResults;

@Log4j2
@Component
public class UserDtoValidator extends DtoValidatorImpl<AppUser, UserDTO, UserDAO>
{
  public UserDtoValidator(final UserDAO dao)
  {
    super(dao);
  }

  @Override
  public Logger getLogger()
  {
    return log;
  }

  @Override
  protected void doValidate(final UserDTO dto, final ValidationResults results)
  {
    rejectIfBlank(AppUser_.EMAIL, dto.getEmail(), results);
    rejectIfInvalidLength(AppUser_.EMAIL, dto.getEmail(), 255, results);
    rejectIfBlank(AppUser_.FIRST_NAME, dto.getFirstName(), results);
    rejectIfInvalidLength(AppUser_.FIRST_NAME, dto.getFirstName(), 255, results);
    rejectIfBlank(AppUser_.LAST_NAME, dto.getLastName(), results);
    rejectIfInvalidLength(AppUser_.LAST_NAME, dto.getLastName(), 255, results);

    if (!results.hasFieldError(AppUser_.EMAIL))
    {
      log.trace("Validating email uniqueness...");
      dao.findByEmail(dto.getEmail())
        .ifPresent(other -> {
          if (!Objects.equals(dto.getId(), other.getId()))
          {
            results.addError(notUniqueValue(AppUser_.EMAIL, dto.getEmail()));
          }
        });
    }
  }
}
