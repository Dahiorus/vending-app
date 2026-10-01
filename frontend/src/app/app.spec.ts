import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { beforeEach, describe, expect, it } from 'vitest';
import { App } from './app';
import { AuthService } from './core/auth/auth';

/** Builds an unsigned JWT whose payload is readable by the frontend. */
function fakeJwt(payload: Record<string, unknown>): string {
  const encode = (value: unknown) =>
    btoa(JSON.stringify(value)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${encode({ alg: 'none' })}.${encode(payload)}.signature`;
}

describe('App', () => {
  let http: HttpTestingController;

  beforeEach(async () => {
    sessionStorage.clear();
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
  });

  it('offers a sign-in link to anonymous visitors', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();

    const toolbar = fixture.nativeElement as HTMLElement;
    expect(toolbar.textContent).toContain('Sign in');
    expect(toolbar.textContent).not.toContain('My profile');
  });

  it('offers a profile link and sign-out button to a logged-in user', async () => {
    const auth = TestBed.inject(AuthService);
    auth.login({ username: 'ada@vending.me', password: 'secret' }).subscribe();
    http.expectOne('/api/v1/authenticate').flush({
      accessToken: fakeJwt({ sub: 'ada@vending.me', roles: ['ROLE_USER'], exp: 1 }),
    });
    http.expectOne('/api/v1/me').flush({
      id: 'u-1',
      email: 'ada@vending.me',
      firstname: 'Ada',
      lastname: 'Lovelace',
    });

    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    await fixture.whenStable();

    const toolbar = fixture.nativeElement as HTMLElement;
    expect(toolbar.textContent).toContain('My profile');
    expect(toolbar.querySelector('a[routerLink="/profile"]')?.textContent).toContain('My profile');
    expect(toolbar.textContent).toContain('Log out');
  });

  it('offers a sign-out button to a logged-in admin without a /me profile', async () => {
    const auth = TestBed.inject(AuthService);
    auth.login({ username: 'admin@vending.me', password: 'secret' }).subscribe();
    http.expectOne('/api/v1/authenticate').flush({
      accessToken: fakeJwt({ sub: 'admin@vending.me', roles: ['ROLE_ADMIN'], exp: 1 }),
    });

    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    await fixture.whenStable();

    const toolbar = fixture.nativeElement as HTMLElement;
    expect(toolbar.textContent).toContain('Log out');
    expect(toolbar.textContent).toContain('Admin');
    expect(toolbar.textContent).not.toContain('My profile');
  });
});
