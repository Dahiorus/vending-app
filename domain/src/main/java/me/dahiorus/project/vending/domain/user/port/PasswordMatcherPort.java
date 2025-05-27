package me.dahiorus.project.vending.domain.user.port;

import me.dahiorus.project.vending.domain.user.entity.Password;
import me.dahiorus.project.vending.domain.user.entity.UserId;

public interface PasswordMatcherPort {
  boolean matches(UserId userId, Password password);
}
