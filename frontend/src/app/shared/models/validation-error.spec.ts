import { HttpErrorResponse } from '@angular/common/http';
import { describe, expect, it } from 'vitest';
import { parseValidationErrors } from './validation-error';

const FORM_FIELDS = ['email', 'address.city'];

function validationErrorResponse(errors: unknown[]): HttpErrorResponse {
  return new HttpErrorResponse({
    status: 400,
    error: { message: 'Validation failed', errors },
  });
}

describe('parseValidationErrors', () => {
  it('returns null when the error is not an HTTP error response', () => {
    expect(parseValidationErrors(new Error('boom'), FORM_FIELDS)).toBeNull();
  });

  it('returns null when the response carries no validation error payload', () => {
    const error = new HttpErrorResponse({ status: 500, error: 'Internal Server Error' });

    expect(parseValidationErrors(error, FORM_FIELDS)).toBeNull();
  });

  it('maps errors of known fields to field errors', () => {
    const error = validationErrorResponse([
      { field: 'email', code: 'Email', defaultMessage: 'Invalid email.' },
      { field: 'address.city', code: 'NotBlank', defaultMessage: 'City is required.' },
    ]);

    expect(parseValidationErrors(error, FORM_FIELDS)).toEqual({
      fieldErrors: { email: 'Invalid email.', 'address.city': 'City is required.' },
      hasObjectLevelError: false,
    });
  });

  it('flags errors without field or on unknown fields as object-level', () => {
    const error = validationErrorResponse([
      { code: 'Duplicate', defaultMessage: 'Already exists.' },
      { field: 'unknown', code: 'NotBlank', defaultMessage: 'Unmapped.' },
    ]);

    expect(parseValidationErrors(error, FORM_FIELDS)).toEqual({
      fieldErrors: {},
      hasObjectLevelError: true,
    });
  });
});
