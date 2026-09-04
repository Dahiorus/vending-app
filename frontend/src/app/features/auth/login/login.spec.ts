import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { Login } from './login';

describe('Login', () => {
  let fixture: ComponentFixture<Login>;
  let component: Login;
  let backend: HttpTestingController;

  beforeEach(async () => {
    sessionStorage.clear();
    await TestBed.configureTestingModule({
      imports: [Login],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'machines', children: [] }]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Login);
    component = fixture.componentInstance;
    await fixture.whenStable();
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('rejects an empty form without calling the API', () => {
    component.submit();

    backend.expectNone('/api/v1/authenticate');
    expect(component.loginForm().valid()).toBe(false);
  });

  it('sends the credentials and clears the error on success', () => {
    component.loginForm.username().value.set('admin@vending.me');
    component.loginForm.password().value.set('S3cret!Passw0rd');

    component.submit();

    const request = backend.expectOne('/api/v1/authenticate');
    expect(request.request.body).toEqual({
      username: 'admin@vending.me',
      password: 'S3cret!Passw0rd',
    });
    request.flush({ accessToken: 'header.eyJzdWIiOiJhIn0.sig', refreshToken: 'refresh-1' });
    backend.expectOne('/api/v1/me').flush({
      id: 'u-1',
      email: 'admin@vending.me',
      firstname: 'Ada',
      lastname: 'Lovelace',
    });

    expect(component.errorMessage()).toBeNull();
    expect(component.submitting()).toBe(false);
  });

  it('shows an error message when the credentials are rejected', () => {
    component.loginForm.username().value.set('admin@vending.me');
    component.loginForm.password().value.set('wrong');

    component.submit();

    backend
      .expectOne('/api/v1/authenticate')
      .flush({ message: 'Bad credentials' }, { status: 401, statusText: 'Unauthorized' });

    expect(component.errorMessage()).toBe('Invalid email or password.');
    expect(component.submitting()).toBe(false);
  });
});
