package me.dahiorus.project.vending.core.service.validation;

enum CommonError
{
  EMPTY_VALUE("validation.constraints.empty_value", "is mandatory"),
  NOT_UNIQUE("validation.constraints.not_unique", "must be unique");

  final String code;

  final String defaultMessage;

  CommonError(final String code, final String defaultMessage)
  {
    this.code = code;
    this.defaultMessage = defaultMessage;
  }

  String getDefaultMessageFor(final String field)
  {
    return field + " " + defaultMessage;
  }
}