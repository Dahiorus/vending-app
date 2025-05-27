package me.dahiorus.project.vending.domain.exception;

import static java.lang.String.format;

import me.dahiorus.project.vending.domain.user.entity.UserId;

public class OldPasswordNotMatch extends RuntimeException {
  public OldPasswordNotMatch(final UserId userId) {
    super(format("Old password not match for user %s", userId));
  }
}
