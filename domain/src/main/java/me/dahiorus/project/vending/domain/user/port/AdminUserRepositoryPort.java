package me.dahiorus.project.vending.domain.user.port;

import me.dahiorus.project.vending.domain.CreateSpi;
import me.dahiorus.project.vending.domain.FindSpi;
import me.dahiorus.project.vending.domain.user.entity.AdminUser;
import me.dahiorus.project.vending.domain.user.entity.AdminUserToCreate;
import me.dahiorus.project.vending.domain.user.entity.EmailAddress;
import me.dahiorus.project.vending.domain.user.entity.UserId;

public interface AdminUserRepositoryPort
    extends CreateSpi<AdminUserToCreate, AdminUser>, FindSpi<UserId, AdminUser> {
  boolean existsByEmail(EmailAddress email);
}
