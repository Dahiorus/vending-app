import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { Register } from './register';

describe('Register', () => {
  let fixture: ComponentFixture<Register>;
  let component: Register;
  let backend: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    sessionStorage.clear();
    await TestBed.configureTestingModule({
      imports: [Register],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'machines', children: [] }, { path: 'login', children: [] }]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Register);
    component = fixture.componentInstance;
    await fixture.whenStable();
    backend = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => backend.verify());

  it('rejects an empty form without calling the API', () => {
    component.submit();

    backend.expectNone('/api/v1/register');
    expect(component.registerForm().valid()).toBe(false);
  });

  it('rejects mismatched passwords without calling the API', () => {
    component.registerForm.email().value.set('ada@vending.me');
    component.registerForm.password().value.set('S3cret!Passw0rd');
    component.registerForm.confirmPassword().value.set('another-password');
    component.registerForm.firstname().value.set('Ada');
    component.registerForm.lastname().value.set('Lovelace');

    component.submit();

    backend.expectNone('/api/v1/register');
    expect(component.passwordsMatch()).toBe(false);
    expect(component.registerForm().valid()).toBe(false);
  });

  it('sends the registration payload and navigates to machines on success', async () => {
    component.registerForm.email().value.set('ada@vending.me');
    component.registerForm.password().value.set('S3cret!Passw0rd');
    component.registerForm.confirmPassword().value.set('S3cret!Passw0rd');
    component.registerForm.firstname().value.set('Ada');
    component.registerForm.lastname().value.set('Lovelace');

    component.submit();

    const registerRequest = backend.expectOne('/api/v1/register');
    expect(registerRequest.request.method).toBe('POST');
    expect(registerRequest.request.body).toEqual({
      email: 'ada@vending.me',
      password: 'S3cret!Passw0rd',
      firstname: 'Ada',
      lastname: 'Lovelace',
    });
    registerRequest.flush({
      id: 'u-1',
      email: 'ada@vending.me',
      firstname: 'Ada',
      lastname: 'Lovelace',
    });

    const loginRequest = backend.expectOne('/api/v1/authenticate');
    expect(loginRequest.request.method).toBe('POST');
    expect(loginRequest.request.body).toEqual({
      username: 'ada@vending.me',
      password: 'S3cret!Passw0rd',
    });
    loginRequest.flush({ accessToken: 'header.eyJzdWIiOiJhIn0.sig', refreshToken: 'refresh-1' });
    backend.expectOne('/api/v1/me').flush({
      id: 'u-1',
      email: 'ada@vending.me',
      firstname: 'Ada',
      lastname: 'Lovelace',
    });

    await fixture.whenStable();

    expect(component.errorMessage()).toBeNull();
    expect(component.fieldErrors()).toEqual({});
    expect(component.submitting()).toBe(false);
    expect(router.url).toBe('/machines');
  });

  it('shows a generic error message when the API returns an object-level validation error', () => {
    component.registerForm.email().value.set('ada@vending.me');
    component.registerForm.password().value.set('S3cret!Passw0rd');
    component.registerForm.confirmPassword().value.set('S3cret!Passw0rd');
    component.registerForm.firstname().value.set('Ada');
    component.registerForm.lastname().value.set('Lovelace');

    component.submit();

    backend.expectOne('/api/v1/register').flush(
      {
        message: 'Validation failed',
        errors: [
          {
            code: 'validation.constraints.object.not_unique',
            defaultMessage: 'Another object exists with the same unique values',
          },
        ],
      },
      { status: 400, statusText: 'Bad Request' },
    );

    expect(component.errorMessage()).toBe('An account with this email may already exist.');
    expect(component.fieldErrors()).toEqual({});
    expect(component.submitting()).toBe(false);
  });

  it('shows field-specific validation messages returned by the API', () => {
    component.registerForm.email().value.set('ada@vending.me');
    component.registerForm.password().value.set('short');
    component.registerForm.confirmPassword().value.set('short');
    component.registerForm.firstname().value.set('Ada');
    component.registerForm.lastname().value.set('Lovelace');

    component.submit();

    backend.expectOne('/api/v1/register').flush(
      {
        message: 'Validation failed',
        errors: [
          {
            field: 'password',
            code: 'validation.constraints.password.min-length',
            defaultMessage: 'A password must contain at least 8 character(s)',
          },
        ],
      },
      { status: 400, statusText: 'Bad Request' },
    );

    expect(component.errorMessage()).toBeNull();
    expect(component.fieldErrors()).toEqual({
      password: 'A password must contain at least 8 character(s)',
    });
    expect(component.submitting()).toBe(false);
  });
});
