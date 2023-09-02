package me.dahiorus.project.vending.domain.service.validation.impl;

import static me.dahiorus.project.vending.domain.service.validation.FieldValidationError.notUniqueValue;

import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.domain.dao.UserDao;
import me.dahiorus.project.vending.domain.model.AppUser;
import me.dahiorus.project.vending.domain.model.AppUser_;
import me.dahiorus.project.vending.domain.model.dto.UserDto;
import me.dahiorus.project.vending.domain.service.validation.ValidationResults;

@Log4j2
@Component
public class UserDtoValidator extends DtoValidatorImpl<AppUser, UserDto, UserDao>
{
  public UserDtoValidator(final UserDao dao)
  {
    super(dao);
  }

  @Override
  public Logger getLogger()
  {
    return log;
  }

  @Override
  protected void doValidate(final UserDto dto, final ValidationResults results)
  {
    rejectIfBlank(AppUser_.EMAIL, dto.getEmail(), results);
    rejectIfInvalidLength(AppUser_.EMAIL, dto.getEmail(), 255, results);
    rejectIfBlank(AppUser_.FIRST_NAME, dto.getFirstName(), results);
    rejectIfInvalidLength(AppUser_.FIRST_NAME, dto.getFirstName(), 255, results);
    rejectIfBlank(AppUser_.LAST_NAME, dto.getLastName(), results);
    rejectIfInvalidLength(AppUser_.LAST_NAME, dto.getLastName(), 255, results);

    if (!results.hasFieldError(AppUser_.EMAIL) && otherExists(dto.getId(),
      (root, query, cb) -> cb.equal(root.get(AppUser_.email), dto.getEmail())))
    {
      results.addError(notUniqueValue(AppUser_.EMAIL, dto.getEmail()));
    }
  }
}
