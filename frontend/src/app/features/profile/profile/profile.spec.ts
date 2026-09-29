import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { TokenStore } from '../../../core/auth/token-store';
import { Profile } from './profile';

function fakeJwt(payload: Record<string, unknown>): string {
  const encode = (value: unknown) =>
    btoa(JSON.stringify(value)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${encode({ alg: 'none' })}.${encode(payload)}.signature`;
}

describe('Profile', () => {
  let harness: RouterTestingHarness;
  let backend: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'profile', component: Profile }]),
        { provide: MatSnackBar, useValue: { open: vi.fn() } },
      ],
    }).compileComponents();

    TestBed.inject(TokenStore).setAccessToken(
      fakeJwt({ sub: 'user@vending.me', roles: ['ROLE_USER'], exp: 1 }),
    );
    backend = TestBed.inject(HttpTestingController);
    harness = await RouterTestingHarness.create();
  });

  afterEach(() => backend.verify());

  it('loads the profile with httpResource and renders all three sections after the GET is flushed', async () => {
    const component = await harness.navigateByUrl('/profile', Profile);
    harness.fixture.detectChanges();

    backend.expectOne('/api/v1/me').flush({
      id: 'u-1',
      email: 'ada@vending.me',
      firstname: 'Ada',
      lastname: 'Lovelace',
      _links: {
        self: { href: '/api/v1/me' },
        'me:picture': { href: '/api/v1/me/picture' },
        'me:password': { href: '/api/v1/me/password' },
      },
    });
    await harness.fixture.whenStable();
    harness.fixture.detectChanges();

    const page = harness.fixture.nativeElement as HTMLElement;
    expect(component.profile()?.email).toBe('ada@vending.me');
    expect(page.textContent).toContain('My profile');
    expect(page.textContent).toContain('Profile information');
    expect(page.textContent).toContain('Profile picture');
    expect(page.textContent).toContain('Change password');
  });

  it('reloads the profile resource when a child emits saved', async () => {
    const component = await harness.navigateByUrl('/profile', Profile);
    harness.fixture.detectChanges();

    backend.expectOne('/api/v1/me').flush({
      id: 'u-1',
      email: 'ada@vending.me',
      firstname: 'Ada',
      lastname: 'Lovelace',
      _links: { self: { href: '/api/v1/me' } },
    });
    await harness.fixture.whenStable();

    component.reload();
    harness.fixture.detectChanges();

    backend.expectOne('/api/v1/me').flush({
      id: 'u-1',
      email: 'ada@vending.me',
      firstname: 'Grace',
      lastname: 'Hopper',
      _links: { self: { href: '/api/v1/me' } },
    });
    await harness.fixture.whenStable();

    expect(component.profile()?.firstname).toBe('Grace');
  });

  it('keeps the profile-picture instance alive across a reload so its cache-busted preview is not lost', async () => {
    const component = await harness.navigateByUrl('/profile', Profile);
    harness.fixture.detectChanges();

    backend.expectOne('/api/v1/me').flush({
      id: 'u-1',
      email: 'ada@vending.me',
      firstname: 'Ada',
      lastname: 'Lovelace',
      _links: {
        self: { href: '/api/v1/me' },
        'me:picture': { href: '/api/v1/me/picture' },
      },
    });
    await harness.fixture.whenStable();
    harness.fixture.detectChanges();

    const page = harness.fixture.nativeElement as HTMLElement;
    const pictureBefore = page.querySelector('app-profile-picture');
    expect(pictureBefore).not.toBeNull();

    component.reload();
    harness.fixture.detectChanges();

    // The <app-profile-picture> element must stay the same DOM node while the
    // resource is reloading, otherwise the component's local cache-buster
    // state (and any optimistic preview) is lost and the uploaded picture
    // appears not to update.
    const pictureDuringReload = page.querySelector('app-profile-picture');
    expect(pictureDuringReload).toBe(pictureBefore);

    backend.expectOne('/api/v1/me').flush({
      id: 'u-1',
      email: 'ada@vending.me',
      firstname: 'Ada',
      lastname: 'Lovelace',
      _links: {
        self: { href: '/api/v1/me' },
        'me:picture': { href: '/api/v1/me/picture' },
      },
    });
    await harness.fixture.whenStable();
    harness.fixture.detectChanges();

    expect(page.querySelector('app-profile-picture')).toBe(pictureBefore);
  });
});
