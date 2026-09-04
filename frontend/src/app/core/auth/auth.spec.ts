import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { AuthService } from './auth';
import { TokenStore } from './token-store';

/** Builds an unsigned JWT whose payload is readable by the frontend. */
function fakeJwt(payload: Record<string, unknown>): string {
  const encode = (value: unknown) =>
    btoa(JSON.stringify(value)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${encode({ alg: 'none' })}.${encode(payload)}.signature`;
}

describe('AuthService', () => {
  let service: AuthService;
  let http: HttpTestingController;
  let tokens: TokenStore;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    http = TestBed.inject(HttpTestingController);
    tokens = TestBed.inject(TokenStore);
  });

  afterEach(() => http.verify());

  it('is anonymous before login', () => {
    expect(service.isAuthenticated()).toBe(false);
    expect(service.roles()).toEqual([]);
    expect(service.currentUser()).toBeNull();
  });

  it('stores the tokens, exposes the roles and loads the current user on login', () => {
    let loaded: unknown = null;
    service.login({ username: 'ada@vending.me', password: 'secret' }).subscribe((user) => {
      loaded = user;
    });

    const loginRequest = http.expectOne('/api/v1/authenticate');
    expect(loginRequest.request.method).toBe('POST');
    expect(loginRequest.request.body).toEqual({
      username: 'ada@vending.me',
      password: 'secret',
    });
    loginRequest.flush({
      accessToken: fakeJwt({ sub: 'ada@vending.me', roles: ['ROLE_USER'], exp: 1 }),
      refreshToken: 'refresh-1',
    });

    const meRequest = http.expectOne('/api/v1/me');
    meRequest.flush({
      id: 'u-1',
      email: 'ada@vending.me',
      firstname: 'Ada',
      lastname: 'Lovelace',
    });

    expect(service.isAuthenticated()).toBe(true);
    expect(service.roles()).toEqual(['ROLE_USER']);
    expect(service.currentUser()?.email).toBe('ada@vending.me');
    expect(loaded).not.toBeNull();
  });

  it('registers the user then logs in and loads the current user profile', () => {
    let registered: unknown = null;
    service
      .register({
        email: 'ada@vending.me',
        password: 'secret',
        firstname: 'Ada',
        lastname: 'Lovelace',
      })
      .subscribe((user) => {
        registered = user;
      });

    const registerRequest = http.expectOne('/api/v1/register');
    expect(registerRequest.request.method).toBe('POST');
    expect(registerRequest.request.body).toEqual({
      email: 'ada@vending.me',
      password: 'secret',
      firstname: 'Ada',
      lastname: 'Lovelace',
    });
    registerRequest.flush({
      id: 'u-1',
      email: 'ada@vending.me',
      firstname: 'Ada',
      lastname: 'Lovelace',
    });

    const loginRequest = http.expectOne('/api/v1/authenticate');
    expect(loginRequest.request.method).toBe('POST');
    expect(loginRequest.request.body).toEqual({
      username: 'ada@vending.me',
      password: 'secret',
    });
    loginRequest.flush({
      accessToken: fakeJwt({ sub: 'ada@vending.me', roles: ['ROLE_USER'], exp: 1 }),
      refreshToken: 'refresh-1',
    });

    const meRequest = http.expectOne('/api/v1/me');
    meRequest.flush({
      id: 'u-1',
      email: 'ada@vending.me',
      firstname: 'Ada',
      lastname: 'Lovelace',
    });

    expect(service.isAuthenticated()).toBe(true);
    expect(service.roles()).toEqual(['ROLE_USER']);
    expect(service.currentUser()).toEqual({
      id: 'u-1',
      email: 'ada@vending.me',
      firstname: 'Ada',
      lastname: 'Lovelace',
    });
    expect(registered).toEqual({
      id: 'u-1',
      email: 'ada@vending.me',
      firstname: 'Ada',
      lastname: 'Lovelace',
    });
  });

  it('skips loading /me for admin accounts, which have no self-service profile', () => {
    let loaded: unknown = 'not-set';
    service.login({ username: 'admin@vending.me', password: 'secret' }).subscribe((user) => {
      loaded = user;
    });

    const loginRequest = http.expectOne('/api/v1/authenticate');
    loginRequest.flush({
      accessToken: fakeJwt({ sub: 'admin@vending.me', roles: ['ROLE_ADMIN'], exp: 1 }),
      refreshToken: 'refresh-1',
    });

    http.expectNone('/api/v1/me');

    expect(service.isAuthenticated()).toBe(true);
    expect(service.roles()).toEqual(['ROLE_ADMIN']);
    expect(service.currentUser()).toBeNull();
    expect(loaded).toBeNull();
  });

  it('reuses the same refresh token because the backend does not rotate it', () => {
    tokens.setTokens({ accessToken: 'expired', refreshToken: 'refresh-1' });

    let refreshed: string | null = null;
    service.refreshAccessToken().subscribe((token) => (refreshed = token));

    const request = http.expectOne('/api/v1/authenticate/refresh');
    expect(request.request.body).toEqual({ token: 'refresh-1' });
    request.flush({ accessToken: 'access-2', refreshToken: 'refresh-1' });

    expect(refreshed).toBe('access-2');
    expect(tokens.accessToken()).toBe('access-2');
    expect(tokens.refreshToken()).toBe('refresh-1');
  });

  it('fails the refresh when no refresh token is available', () => {
    let error: unknown = null;
    service.refreshAccessToken().subscribe({ error: (e) => (error = e) });

    expect(error).toBeInstanceOf(Error);
  });

  it('clears every trace of the session on logout', () => {
    tokens.setTokens({ accessToken: 'access-1', refreshToken: 'refresh-1' });

    service.logout();

    expect(service.isAuthenticated()).toBe(false);
    expect(service.currentUser()).toBeNull();
    expect(sessionStorage.getItem('vending.refreshToken')).toBeNull();
  });
});
