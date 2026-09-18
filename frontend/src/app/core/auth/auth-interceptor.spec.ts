import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { authInterceptor } from './auth-interceptor';
import { TokenStore } from './token-store';

describe('authInterceptor', () => {
  let http: HttpClient;
  let backend: HttpTestingController;
  let tokens: TokenStore;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([authInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
    tokens = TestBed.inject(TokenStore);
  });

  afterEach(() => backend.verify());

  it('leaves anonymous requests untouched', () => {
    http.get('/api/v1/vending-machines').subscribe();

    const request = backend.expectOne('/api/v1/vending-machines');
    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush({});
  });

  it('attaches the bearer token to API requests', () => {
    tokens.setAccessToken('access-1');

    http.get('/api/v1/me').subscribe();

    const request = backend.expectOne('/api/v1/me');
    expect(request.request.headers.get('Authorization')).toBe('Bearer access-1');
    request.flush({});
  });

  it('never attaches the bearer token to the authentication endpoints', () => {
    tokens.setAccessToken('access-1');

    http.post('/api/v1/authenticate/refresh', {}).subscribe();

    const request = backend.expectOne('/api/v1/authenticate/refresh');
    expect(request.request.headers.has('Authorization')).toBe(false);
    request.flush({ accessToken: 'access-2' });
  });

  it('refreshes once and replays both requests when two calls fail with 401', () => {
    tokens.setAccessToken('expired');
    const answers: unknown[] = [];

    http.get('/api/v1/me').subscribe((body) => answers.push(body));
    http.get('/api/v1/vending-machines').subscribe((body) => answers.push(body));

    backend.match('/api/v1/me')[0].flush(null, { status: 401, statusText: 'Unauthorized' });
    backend
      .match('/api/v1/vending-machines')[0]
      .flush(null, { status: 401, statusText: 'Unauthorized' });

    const refreshRequests = backend.match('/api/v1/authenticate/refresh');
    expect(refreshRequests.length).toBe(1);
    refreshRequests[0].flush({ accessToken: 'access-2' });

    const replayedMe = backend.expectOne('/api/v1/me');
    expect(replayedMe.request.headers.get('Authorization')).toBe('Bearer access-2');
    replayedMe.flush({ email: 'admin@vending.me' });

    const replayedMachines = backend.expectOne('/api/v1/vending-machines');
    expect(replayedMachines.request.headers.get('Authorization')).toBe('Bearer access-2');
    replayedMachines.flush({ page: { size: 20, totalElements: 0, totalPages: 0, number: 0 } });

    expect(answers.length).toBe(2);
  });

  it('clears the session when the refresh itself fails', () => {
    tokens.setAccessToken('expired');
    let failed = false;

    http.get('/api/v1/me').subscribe({ error: () => (failed = true) });

    backend.expectOne('/api/v1/me').flush(null, { status: 401, statusText: 'Unauthorized' });
    backend
      .expectOne('/api/v1/authenticate/refresh')
      .flush(null, { status: 401, statusText: 'Unauthorized' });
    backend.expectOne('/api/v1/authenticate/logout').flush(null);

    expect(failed).toBe(true);
    expect(tokens.accessToken()).toBeNull();
  });
});
