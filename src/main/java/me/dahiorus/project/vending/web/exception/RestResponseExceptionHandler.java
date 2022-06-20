package me.dahiorus.project.vending.web.exception;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.domain.exception.AppRuntimeException;
import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.ItemMissing;
import me.dahiorus.project.vending.domain.exception.UserNotAuthenticated;
import me.dahiorus.project.vending.domain.exception.ValidationException;
import me.dahiorus.project.vending.domain.exception.VendingMachineNotWorking;

@RestControllerAdvice(basePackages = "me.dahiorus.project.vending.web")
@NoArgsConstructor
@Log4j2
public class RestResponseExceptionHandler
{
  @ExceptionHandler(EntityNotFound.class)
  @ResponseStatus(code = HttpStatus.NOT_FOUND)
  public Object handleEntityNotFound(final EntityNotFound e)
  {
    log.warn(e.getMessage());

    return initResponseBody(e);
  }

  @ExceptionHandler(ValidationException.class)
  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  public Object handleValidationException(final ValidationException e)
  {
    log.error(e.getMessage());

    Map<String, Object> body = initResponseBody(e);

    body.put("errorCount", e.getCount());
    body.put("globalErrors", e.getGlobalErrors());
    body.put("fieldErrors", e.getFieldErrors());

    return body;
  }

  @ExceptionHandler({ ItemMissing.class, VendingMachineNotWorking.class })
  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  public Object handleItemMissing(final Exception e)
  {
    log.error(e.getMessage());

    return initResponseBody(e);
  }

  @ExceptionHandler(UserNotAuthenticated.class)
  @ResponseStatus(code = HttpStatus.UNAUTHORIZED)
  public Object handleUserNotAuthenticated(final UserNotAuthenticated e)
  {
    log.warn("User not authenticated");

    return initResponseBody(e);
  }

  @ExceptionHandler({ AppRuntimeException.class, TokenException.class })
  @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR)
  public Object handleUnexpectedError(final Exception e)
  {
    log.error(e);

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
