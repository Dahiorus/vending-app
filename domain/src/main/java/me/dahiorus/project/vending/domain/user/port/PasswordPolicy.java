package me.dahiorus.project.vending.domain.user.port;

import java.util.Optional;

public interface PasswordPolicy {

  Integer minLength();

  Integer maxLength();

  Integer minLowerCaseCharCount();

  Integer minUpperCaseCharCount();

  Integer minDigitCount();

  Integer minSpecialCharCount();

  default Optional<Integer> maybeMinLength() {
    return Optional.ofNullable(minLength());
  }

  default Optional<Integer> maybeMaxLength() {
    return Optional.ofNullable(maxLength());
  }

  default Optional<Integer> maybeMinLowerCaseCharCount() {
    return Optional.ofNullable(minLowerCaseCharCount());
  }

  default Optional<Integer> maybeMinUpperCaseCharCount() {
    return Optional.ofNullable(minUpperCaseCharCount());
  }

  default Optional<Integer> maybeMinDigitCount() {
    return Optional.ofNullable(minDigitCount());
  }

  default Optional<Integer> maybeMinSpecialCharCount() {
    return Optional.ofNullable(minSpecialCharCount());
  }
}
