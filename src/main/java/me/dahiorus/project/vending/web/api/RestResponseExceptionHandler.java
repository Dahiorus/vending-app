package me.dahiorus.project.vending.web.api;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import me.dahiorus.project.vending.core.exception.AppRuntimeException;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ItemMissing;
import me.dahiorus.project.vending.core.exception.ValidationException;

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

  @ExceptionHandler(ValidationException.class)
  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  public static Object handleValidationException(final ValidationException e)
  {
    Map<String, Object> body = initResponseBody(e);

    body.put("errorCount", e.getCount());
    body.put("globalErrors", e.getGlobalErrors());
    body.put("fieldErrors", e.getFieldErrors());

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
}
