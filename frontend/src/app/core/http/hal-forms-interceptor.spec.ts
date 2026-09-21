import { HttpClient, provideHttpClient, withInterceptors } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { environment } from '../../../environments/environment';
import { halFormsInterceptor } from './hal-forms-interceptor';

describe('halFormsInterceptor', () => {
  let http: HttpClient;
  let backend: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(withInterceptors([halFormsInterceptor])),
        provideHttpClientTesting(),
      ],
    });
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('requests HAL-FORMS with a JSON fallback on API requests', () => {
    http.get(`${environment.apiBaseUrl}/vending-machines`).subscribe();

    const request = backend.expectOne(`${environment.apiBaseUrl}/vending-machines`);
    expect(request.request.headers.get('Accept')).toBe(
      'application/prs.hal-forms+json, application/hal+json;q=0.9, application/json;q=0.8',
    );
    request.flush({});
  });

  it('leaves non-API requests untouched', () => {
    http.get('/assets/config.json').subscribe();

    const request = backend.expectOne('/assets/config.json');
    expect(request.request.headers.has('Accept')).toBe(false);
    request.flush({});
  });

  it('does not override an Accept header explicitly set by the caller', () => {
    http
      .get(`${environment.apiBaseUrl}/items/1/image`, { headers: { Accept: 'image/png' } })
      .subscribe();

    const request = backend.expectOne(`${environment.apiBaseUrl}/items/1/image`);
    expect(request.request.headers.get('Accept')).toBe('image/png');
    request.flush({});
  });
});
