package me.dahiorus.project.vending.domain.service.validation;

public interface PasswordValidator
{
  ValidationResults validate(String field, String rawPassword);
}
