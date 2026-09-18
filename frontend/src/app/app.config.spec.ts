import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { restoreSessionOnStartup } from './app.config';
import { TokenStore } from './core/auth/token-store';

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
    request.flush({ accessToken: 'access-2' });

    await startup;

    expect(tokens.accessToken()).toBe('access-2');
  });

  it('swallows refresh failures during startup', async () => {
    const startup = TestBed.runInInjectionContext(() => restoreSessionOnStartup());

    const request = http.expectOne('/api/v1/authenticate/refresh');
    request.flush(null, { status: 401, statusText: 'Unauthorized' });

    await expect(startup).resolves.toBeUndefined();
    expect(tokens.accessToken()).toBeNull();
  });
});
