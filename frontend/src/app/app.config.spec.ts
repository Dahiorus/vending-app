import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { restoreSessionOnStartup } from './app.config';
import { TokenStore } from './core/auth/token-store';

/** Builds an unsigned JWT whose payload is readable by the frontend. */
function fakeJwt(payload: Record<string, unknown>): string {
  const encode = (value: unknown) =>
    btoa(JSON.stringify(value)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${encode({ alg: 'none' })}.${encode(payload)}.signature`;
}

describe('restoreSessionOnStartup', () => {
  let http: HttpTestingController;
  let tokens: TokenStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpTestingController);
    tokens = TestBed.inject(TokenStore);
  });

  afterEach(() => http.verify());

  it('refreshes the access token from the refresh cookie during startup', async () => {
    const startup = TestBed.runInInjectionContext(() => restoreSessionOnStartup());

    const request = http.expectOne('/api/v1/authenticate/refresh');
    request.flush({
      accessToken: fakeJwt({ sub: 'ada@vending.me', roles: ['ROLE_USER'], exp: 1 }),
    });

    const meRequest = http.expectOne('/api/v1/me');
    meRequest.flush({
      id: 'u-1',
      email: 'ada@vending.me',
      firstname: 'Ada',
      lastname: 'Lovelace',
    });

    await startup;

    expect(tokens.accessToken()).toBe(
      fakeJwt({ sub: 'ada@vending.me', roles: ['ROLE_USER'], exp: 1 }),
    );
  });

  it('swallows refresh failures during startup', async () => {
    const startup = TestBed.runInInjectionContext(() => restoreSessionOnStartup());

    const request = http.expectOne('/api/v1/authenticate/refresh');
    request.flush(null, { status: 401, statusText: 'Unauthorized' });

    await expect(startup).resolves.toBeUndefined();
    expect(tokens.accessToken()).toBeNull();
  });
});
