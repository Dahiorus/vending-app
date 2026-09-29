import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router, provideRouter } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthService } from '../../../core/auth/auth';
import { ProfilePassword } from './profile-password';

describe('ProfilePassword', () => {
  let fixture: ComponentFixture<ProfilePassword>;
  let component: ProfilePassword;
  let backend: HttpTestingController;
  let router: Router;
  let auth: { logout: ReturnType<typeof vi.fn> };
  let snackBar: { open: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    auth = { logout: vi.fn() };
    snackBar = { open: vi.fn() };
    await TestBed.configureTestingModule({
      imports: [ProfilePassword],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'login', children: [] }]),
        { provide: AuthService, useValue: auth },
        { provide: MatSnackBar, useValue: snackBar },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ProfilePassword);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('passwordHref', '/api/v1/me/password');
    fixture.detectChanges();
    await fixture.whenStable();
    backend = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => backend.verify());

  function fillPasswords(confirm = 'NewPassw0rd!'): void {
    component.passwordForm.oldPassword().value.set('OldPassw0rd!');
    component.passwordForm.newPassword().value.set('NewPassw0rd!');
    component.passwordForm.confirmNewPassword().value.set(confirm);
  }

  it('rejects mismatched passwords without calling the API', () => {
    fillPasswords('DifferentPassw0rd!');

    component.submit();

    backend.expectNone('/api/v1/me/password');
    expect(component.passwordsMatch()).toBe(false);
    expect(component.passwordForm().valid()).toBe(false);
  });

  it('posts the password change, logs out, navigates to login, and shows the success snackbar', async () => {
    fillPasswords();

    component.submit();

    const request = backend.expectOne('/api/v1/me/password');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      oldPassword: 'OldPassw0rd!',
      newPassword: 'NewPassw0rd!',
    });
    request.flush(null, { status: 204, statusText: 'No Content' });
    await fixture.whenStable();

    expect(auth.logout).toHaveBeenCalledOnce();
    expect(router.url).toBe('/login');
    expect(snackBar.open).toHaveBeenCalledWith('Password changed. Please sign in again.', 'Close', {
      duration: 5000,
    });
  });

  it('maps a weak password validation error returned as field "password" to newPassword', async () => {
    fillPasswords();

    component.submit();

    backend.expectOne('/api/v1/me/password').flush(
      {
        timestamp: '2026-09-25T12:00:00Z',
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
    await fixture.whenStable();

    expect(component.fieldErrors()).toEqual({
      newPassword: 'A password must contain at least 8 character(s)',
    });
    expect(component.errorMessage()).toBeNull();
  });

  it('maps a 400 without validation errors to the current password field', async () => {
    fillPasswords();

    component.submit();

    backend
      .expectOne('/api/v1/me/password')
      .flush(
        { timestamp: '2026-09-25T12:00:00Z', message: 'Old password does not match' },
        { status: 400, statusText: 'Bad Request' },
      );
    await fixture.whenStable();

    expect(component.fieldErrors()).toEqual({
      oldPassword: 'Current password is incorrect.',
    });
    expect(component.errorMessage()).toBeNull();
  });
});
