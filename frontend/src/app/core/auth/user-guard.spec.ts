import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CanActivateFn, provideRouter, UrlTree } from '@angular/router';
import { beforeEach, describe, expect, it } from 'vitest';
import { TokenStore } from './token-store';
import { userGuard } from './user-guard';

function fakeJwt(payload: Record<string, unknown>): string {
  const encode = (value: unknown) =>
    btoa(JSON.stringify(value)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${encode({ alg: 'none' })}.${encode(payload)}.signature`;
}

describe('userGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) =>
    TestBed.runInInjectionContext(() => userGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([
          { path: 'login', children: [] },
          { path: 'machines', children: [] },
        ]),
      ],
    });
  });

  it('redirects anonymous users to /login', () => {
    const result = executeGuard({} as never, { url: '/profile' } as never);

    expect(result).toBeInstanceOf(UrlTree);
    expect((result as UrlTree).toString()).toBe('/login');
  });

  it('redirects admins to /machines because admins have no /me profile', () => {
    TestBed.inject(TokenStore).setAccessToken(
      fakeJwt({ sub: 'admin@vending.me', roles: ['ROLE_ADMIN'], exp: 1 }),
    );

    const result = executeGuard({} as never, { url: '/profile' } as never);

    expect(result).toBeInstanceOf(UrlTree);
    expect((result as UrlTree).toString()).toBe('/machines');
  });

  it('allows an authenticated non-admin user', () => {
    TestBed.inject(TokenStore).setAccessToken(
      fakeJwt({ sub: 'user@vending.me', roles: ['ROLE_USER'], exp: 1 }),
    );

    const result = executeGuard({} as never, { url: '/profile' } as never);

    expect(result).toBe(true);
  });
});
