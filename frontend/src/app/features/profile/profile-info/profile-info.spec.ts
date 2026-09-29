import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { User } from '../../../core/auth/models/user';
import { ProfileInfo } from './profile-info';

const user: User = {
  id: 'u-1',
  email: 'ada@vending.me',
  firstname: 'Ada',
  lastname: 'Lovelace',
  _links: { self: { href: '/api/v1/me' } },
};

describe('ProfileInfo', () => {
  let fixture: ComponentFixture<ProfileInfo>;
  let component: ProfileInfo;
  let backend: HttpTestingController;
  let snackBar: { open: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    snackBar = { open: vi.fn() };
    await TestBed.configureTestingModule({
      imports: [ProfileInfo],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: MatSnackBar, useValue: snackBar },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ProfileInfo);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('user', user);
    fixture.detectChanges();
    await fixture.whenStable();
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('shows email as read-only and initializes firstname and lastname', () => {
    const emailInput = fixture.nativeElement.querySelector(
      'input[type="email"]',
    ) as HTMLInputElement;

    expect(emailInput.value).toBe('ada@vending.me');
    expect(emailInput.readOnly).toBe(true);
    expect(component.infoForm.firstname().value()).toBe('Ada');
    expect(component.infoForm.lastname().value()).toBe('Lovelace');
  });

  it('saves firstname and lastname with a JSON body and emits the updated user', async () => {
    const emitted: User[] = [];
    component.saved.subscribe((value) => emitted.push(value));
    component.infoForm.firstname().value.set('Grace');
    component.infoForm.lastname().value.set('Hopper');

    component.submit();

    const request = backend.expectOne('/api/v1/me');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({ firstname: 'Grace', lastname: 'Hopper' });
    request.flush({ ...user, firstname: 'Grace', lastname: 'Hopper' });
    await fixture.whenStable();

    expect(emitted[0].firstname).toBe('Grace');
    expect(snackBar.open).toHaveBeenCalledWith('Profile updated.', 'Close', { duration: 5000 });
    expect(component.submitting()).toBe(false);
  });

  it('does not send a request when the self link is missing', async () => {
    fixture.componentRef.setInput('user', { ...user, _links: {} });
    fixture.detectChanges();
    await fixture.whenStable();

    component.submit();

    backend.expectNone('/api/v1/me');
    expect(component.errorMessage()).toBe('Profile update link is unavailable.');
  });

  it('shows field validation errors returned by the backend', async () => {
    component.infoForm.firstname().value.set(' ');
    component.infoForm.lastname().value.set('Hopper');

    component.submit();

    backend.expectOne('/api/v1/me').flush(
      {
        message: 'Validation failed',
        errors: [{ field: 'firstname', code: 'not_blank', defaultMessage: 'must not be blank' }],
      },
      { status: 400, statusText: 'Bad Request' },
    );
    await fixture.whenStable();

    expect(component.fieldErrors()).toEqual({ firstname: 'must not be blank' });
    expect(component.errorMessage()).toBeNull();
  });
});
