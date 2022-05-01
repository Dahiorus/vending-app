package me.dahiorus.project.vending.core.service.validation;

public interface PasswordValidator
{
  ValidationResults validate(String field, String rawPassword, boolean mandatory);
}
