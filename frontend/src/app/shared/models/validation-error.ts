import { HttpErrorResponse } from '@angular/common/http';

/** Generic validation error contract returned by RestResponseExceptionHandler. */
export interface ValidationError {
  field?: string;
  code: string;
  defaultMessage: string;
  errorArgs?: unknown[];
}

export interface ValidationErrorResponse {
  message: string;
  errors: ValidationError[];
}

/** Validation errors dispatched between form fields and the form-level message. */
export interface ParsedValidationErrors {
  /** Message indexed by form field path, for the fields listed in `formFields`. */
  fieldErrors: Record<string, string>;
  /** True when at least one error targets no known form field (object-level error). */
  hasObjectLevelError: boolean;
}

function isValidationErrorResponse(error: unknown): error is ValidationErrorResponse {
  return (
    typeof error === 'object' && error !== null && 'errors' in error && Array.isArray(error.errors)
  );
}

/**
 * Parses a failed HTTP response into per-field and object-level validation errors.
 * Returns `null` when the response does not carry a validation error payload,
 * letting the caller fall back to a generic error message.
 */
export function parseValidationErrors(
  error: unknown,
  formFields: readonly string[],
): ParsedValidationErrors | null {
  if (!(error instanceof HttpErrorResponse) || !isValidationErrorResponse(error.error)) {
    return null;
  }

  const fieldErrors: Record<string, string> = {};
  let hasObjectLevelError = false;

  for (const validationError of error.error.errors) {
    if (validationError.field && formFields.includes(validationError.field)) {
      fieldErrors[validationError.field] = validationError.defaultMessage;
      continue;
    }

    hasObjectLevelError = true;
  }

  return { fieldErrors, hasObjectLevelError };
}
