import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CanActivateFn, provideRouter, UrlTree } from '@angular/router';
import { beforeEach, describe, expect, it } from 'vitest';
import { adminGuard } from './admin-guard';
import { TokenStore } from './token-store';

function fakeJwt(payload: Record<string, unknown>): string {
  const encode = (value: unknown) =>
    btoa(JSON.stringify(value)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${encode({ alg: 'none' })}.${encode(payload)}.signature`;
}

describe('adminGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) =>
    TestBed.runInInjectionContext(() => adminGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'machines', children: [] }]),
      ],
    });
  });

  it('denies access and redirects to /machines when the user is not an admin', () => {
    const result = executeGuard({} as never, { url: '/machines/new' } as never);

    expect(result).toBeInstanceOf(UrlTree);
    expect((result as UrlTree).toString()).toBe('/machines');
  });

  it('allows access when the user has the ROLE_ADMIN role', () => {
    const tokenStore = TestBed.inject(TokenStore);
    tokenStore.setTokens({
      accessToken: fakeJwt({ sub: 'admin@vending.me', roles: ['ROLE_ADMIN'], exp: 1 }),
      refreshToken: 'refresh-1',
    });

    const result = executeGuard({} as never, { url: '/machines/new' } as never);

    expect(result).toBe(true);
  });
});
