package me.dahiorus.project.vending.infrastructure.rest.exception;

import static java.time.LocalDateTime.now;
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.stream.Collectors.toSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import me.dahiorus.project.vending.domain.exception.InvalidBusinessObject;
import me.dahiorus.project.vending.domain.exception.ItemStockIsEmpty;
import me.dahiorus.project.vending.domain.exception.NotWorkingVendingMachine;
import me.dahiorus.project.vending.domain.exception.OldPasswordNotMatch;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.exception.UnsupportedItemToProvision;
import me.dahiorus.project.vending.domain.validation.FieldValidationError;
import me.dahiorus.project.vending.domain.validation.ValidationError;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "me.dahiorus.project.vending.infrastructure.rest")
public class RestResponseExceptionHandler {
  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  public Object handleValidationException(final MethodArgumentNotValidException ex) {
    var errors = initResponseBody(ex);
    errors.put(
        "message",
        String.format(
            "%d errors found in [%s]", ex.getBindingResult().getErrorCount(), ex.getObjectName()));
    errors.put("errors", mapToFieldErrors(ex.getBindingResult()));

    return errors;
  }

  private static Set<ValidationError> mapToFieldErrors(BindingResult bindingResult) {
    return bindingResult.getFieldErrors().stream()
        .map(
            fieldError ->
                new FieldValidationError(
                    fieldError.getField(),
                    fieldError.getCode(),
                    Optional.ofNullable(fieldError.getDefaultMessage())
                        .orElse(String.format("Invalid value [%s]", fieldError.getRejectedValue())),
                    fieldError.getArguments()))
        .collect(toSet());
  }

  @ExceptionHandler(InvalidBusinessObject.class)
  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  public Object handleInvalidBusinessObject(InvalidBusinessObject ex) {
    Map<String, Object> errors = initResponseBody(ex);
    errors.put("errors", ex.getErrors());

    return errors;
  }

  @ExceptionHandler({
    UnsupportedItemToProvision.class,
    NotWorkingVendingMachine.class,
    OldPasswordNotMatch.class
  })
  @ResponseStatus(code = HttpStatus.BAD_REQUEST)
  public Object handleBadRequest(RuntimeException ex) {
    return initResponseBody(ex);
  }

  @ExceptionHandler({ResourceNotFound.class, ItemStockIsEmpty.class})
  @ResponseStatus(code = HttpStatus.NOT_FOUND)
  public Object handleNotFound(RuntimeException ex) {
    return initResponseBody(ex);
  }

  private static Map<String, Object> initResponseBody(final Exception e) {
    Map<String, Object> body = new HashMap<>();

    body.put("timestamp", now().truncatedTo(MILLIS));
    body.put("message", e.getMessage());

    return body;
  }
}
