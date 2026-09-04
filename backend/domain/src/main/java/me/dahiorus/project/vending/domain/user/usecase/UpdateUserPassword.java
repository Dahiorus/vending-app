package me.dahiorus.project.vending.domain.user.usecase;

import me.dahiorus.project.vending.domain.documentation.DomainService;
import me.dahiorus.project.vending.domain.exception.OldPasswordNotMatch;
import me.dahiorus.project.vending.domain.user.entity.EditPassword;
import me.dahiorus.project.vending.domain.user.entity.UserId;
import me.dahiorus.project.vending.domain.user.port.AppUserRepositoryPort;
import me.dahiorus.project.vending.domain.user.port.PasswordMatcherPort;

@DomainService
public class UpdateUserPassword {
  private final AppUserRepositoryPort appUserRepository;
  private final PasswordMatcherPort passwordMatcher;
  private final UserPasswordValidator passwordValidator;

  public UpdateUserPassword(
      final AppUserRepositoryPort appUserRepository,
      final PasswordMatcherPort passwordMatcher,
      final UserPasswordValidator passwordValidator) {
    this.appUserRepository = appUserRepository;
    this.passwordMatcher = passwordMatcher;
    this.passwordValidator = passwordValidator;
  }

  public void execute(UserId userId, EditPassword editPassword) {
    if (!passwordMatcher.matches(userId, editPassword.oldPassword())) {
      throw new OldPasswordNotMatch(userId);
    }
    passwordValidator.validate(editPassword.newPassword());
    appUserRepository.updatePassword(userId, editPassword.newPassword());
  }
}
