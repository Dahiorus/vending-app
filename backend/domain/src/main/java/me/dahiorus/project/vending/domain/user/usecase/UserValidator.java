package me.dahiorus.project.vending.domain.user.usecase;

import static me.dahiorus.project.vending.domain.validation.ObjectValidationError.notUnique;
import static me.dahiorus.project.vending.domain.validation.ValidationResults.validationResults;

import me.dahiorus.project.vending.domain.documentation.DomainService;
import me.dahiorus.project.vending.domain.exception.InvalidBusinessObject;
import me.dahiorus.project.vending.domain.user.entity.UserToCreate;
import me.dahiorus.project.vending.domain.user.port.UserToCreateRepositoryPort;
import me.dahiorus.project.vending.domain.validation.ValidationResults;
import me.dahiorus.project.vending.domain.validation.Validator;

@DomainService
public class UserValidator implements Validator<UserToCreate> {

  private final UserToCreateRepositoryPort userToCreateRepositoryPort;

  public UserValidator(final UserToCreateRepositoryPort userToCreateRepositoryPort) {
    this.userToCreateRepositoryPort = userToCreateRepositoryPort;
  }

  @Override
  public ValidationResults buildValidation(final UserToCreate userToCreate)
      throws InvalidBusinessObject {
    var validationResults = validationResults();

    userToCreateRepositoryPort
        .findDuplicateOf(userToCreate)
        .map(userId -> notUnique(userToCreate))
        .ifPresent(validationResults::addError);

    return validationResults;
  }
}
