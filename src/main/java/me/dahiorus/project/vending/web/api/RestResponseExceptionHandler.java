package me.dahiorus.project.vending.web.api;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import me.dahiorus.project.vending.core.exception.AppRuntimeException;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.InvalidData;
import me.dahiorus.project.vending.core.exception.ItemMissing;

@RestControllerAdvice(basePackages = "me.dahiorus.project.vending.web.api")
public class RestResponseExceptionHandler
{
  private RestResponseExceptionHandler()
  {
    //
  }

  @ExceptionHandler(EntityNotFound.class)
  @ResponseStatus(code = HttpStatus.NOT_FOUND)
  public static Object handleEntityNotFound(final EntityNotFound e)
  {
    return initResponseBody(e);
  }

  @ExceptionHandler(InvalidData.class)
  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  public static Object handleInvalidData(final InvalidData e)
  {
    Map<String, Object> body = initResponseBody(e);
    Errors errors = e.getErrors();

    body.put("errorCount", errors.getErrorCount());

    if (errors.hasGlobalErrors())
    {
      body.put("globalErrors", errors.getGlobalErrors()
        .stream()
        .map(ErrorAdapter::new)
        .collect(Collectors.toList()));
    }

    if (errors.hasFieldErrors())
    {
      body.put("fieldErrors", errors.getFieldErrors()
        .stream()
        .map(FieldErrorAdapter::new)
        .collect(Collectors.toList()));
    }

    return body;
  }

  @ExceptionHandler(ItemMissing.class)
  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  public static Object handleItemMissing(final ItemMissing e)
  {
    return initResponseBody(e);
  }

  @ExceptionHandler(AppRuntimeException.class)
  @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
  public static Object handleRuntimeException(final AppRuntimeException e)
  {
    return initResponseBody(e);
  }

  private static Map<String, Object> initResponseBody(final Exception e)
  {
    Map<String, Object> body = new HashMap<>();

    body.put("timestamp", Instant.now());
    body.put("message", e.getMessage());

    return body;
  }

  private static class ErrorAdapter
  {
    public final String defaultMessage;

    public final String code;

    ErrorAdapter(final ObjectError error)
    {
      defaultMessage = error.getDefaultMessage();
      code = error.getCode();
    }

    @Override
    public String toString()
    {
      return getClass().getSimpleName() + " [defaultMessage=" + defaultMessage + ", code=" + code + "]";
    }
  }

  private static class FieldErrorAdapter extends ErrorAdapter
  {
    public final String field;

    public final Object rejectedValue;

    FieldErrorAdapter(final FieldError error)
    {
      super(error);
      field = error.getField();
      rejectedValue = error.getRejectedValue();
    }

    @Override
    public String toString()
    {
      return super.toString() + "[field=" + field + ", rejectedValue=" + rejectedValue + "]";
    }
  }
}
