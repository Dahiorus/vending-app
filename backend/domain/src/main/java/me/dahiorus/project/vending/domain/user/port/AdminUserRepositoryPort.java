package me.dahiorus.project.vending.domain.user.port;

import me.dahiorus.project.vending.domain.Creatable;
import me.dahiorus.project.vending.domain.Findable;
import me.dahiorus.project.vending.domain.user.entity.AdminUser;
import me.dahiorus.project.vending.domain.user.entity.AdminUserToCreate;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.UserId;

public interface AdminUserRepositoryPort
    extends Creatable<AdminUserToCreate, AdminUser>, Findable<UserId, AdminUser> {
  boolean existsByEmail(EmailAddress email);
}
