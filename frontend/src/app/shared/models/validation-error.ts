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
