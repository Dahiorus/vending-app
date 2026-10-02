import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { By } from '@angular/platform-browser';
import { Router, RouterLink, provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { TokenStore } from '../../../core/auth/token-store';
import { ItemDeleteDialog } from '../item-delete-dialog/item-delete-dialog';
import { ItemDetail } from './item-detail';

function fakeJwt(payload: Record<string, unknown>): string {
  const encode = (value: unknown) =>
    btoa(JSON.stringify(value)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${encode({ alg: 'none' })}.${encode(payload)}.signature`;
}

describe('ItemDetail', () => {
  let harness: RouterTestingHarness;
  let backend: HttpTestingController;
  let dialog: { open: ReturnType<typeof vi.fn> };
  let snackBar: { open: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    dialog = { open: vi.fn(() => ({ afterClosed: () => of(undefined) })) };
    snackBar = { open: vi.fn() };

    await TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([
          { path: 'items', children: [] },
          { path: 'items/:id', component: ItemDetail },
        ]),
        { provide: MatDialog, useValue: dialog },
        { provide: MatSnackBar, useValue: snackBar },
      ],
    }).compileComponents();

    backend = TestBed.inject(HttpTestingController);
    harness = await RouterTestingHarness.create();
  });

  afterEach(() => backend.verify());

  async function navigate(state: unknown = null) {
    history.replaceState(state, '');
    const component = await harness.navigateByUrl('/items/i-1', ItemDetail);
    harness.fixture.detectChanges();
    return component;
  }

  function flushItem(): void {
    backend.expectOne('/api/v1/items/i-1').flush({
      id: 'i-1',
      name: 'Cola',
      type: 'COLD_BEVERAGE',
      price: 1.5,
      _links: {
        self: { href: '/api/v1/items/i-1' },
        'item:image': { href: '/api/v1/items/i-1/image' },
      },
    });
  }

  function authenticateAdmin(): void {
    TestBed.inject(TokenStore).setAccessToken(
      fakeJwt({ sub: 'admin@vending.me', roles: ['ROLE_ADMIN'], exp: 4102444800 }),
    );
  }

  it('falls back to the id-based URL when no HATEOAS state is available', async () => {
    const component = await navigate();

    flushItem();
    await harness.fixture.whenStable();

    expect(component.item()?.name).toBe('Cola');
  });

  it('displays the price as a French euro amount', async () => {
    await navigate();
    flushItem();
    await harness.fixture.whenStable();

    const text = (harness.fixture.nativeElement as HTMLElement).textContent ?? '';
    expect(text.replace(/\s/g, ' ')).toContain('1,50 €');
  });

  it('follows the HATEOAS self link carried over via router navigation state', async () => {
    const component = await navigate({ href: 'https://api.example.test/items/i-1' });

    backend.expectOne('https://api.example.test/items/i-1').flush({
      id: 'i-1',
      name: 'Cola',
      type: 'COLD_BEVERAGE',
      price: 1.5,
      _links: {},
    });
    await harness.fixture.whenStable();

    expect(component.item()?.name).toBe('Cola');
  });

  it('builds the image URL from the item:image link and switches to fallback on image error', async () => {
    const component = await navigate();
    flushItem();
    await harness.fixture.whenStable();

    expect(component.imageUrl()).toBe('/api/v1/items/i-1/image');

    component.imageFailed();

    expect(component.showImageFallback()).toBe(true);
  });

  it('seeds the image cache-buster from the imageVersion carried in navigation state', async () => {
    const component = await navigate({ href: '/api/v1/items/i-1', imageVersion: 1234 });
    flushItem();
    await harness.fixture.whenStable();
    harness.fixture.detectChanges();

    expect(component.imageUrl()).toBe('/api/v1/items/i-1/image?v=1234');
    const img: HTMLImageElement = harness.fixture.nativeElement.querySelector('img');
    expect(img.getAttribute('src')).toBe('/api/v1/items/i-1/image?v=1234');
  });

  it('carries the image cache-buster in the Edit link state', async () => {
    authenticateAdmin();
    await navigate({ href: '/api/v1/items/i-1', imageVersion: 1234 });
    flushItem();
    await harness.fixture.whenStable();
    harness.fixture.detectChanges();

    const link = harness.fixture.debugElement
      .queryAll(By.directive(RouterLink))
      .map((el) => el.injector.get(RouterLink))
      .find((l) => l.href?.endsWith('/items/i-1/edit'));
    expect(link?.state).toEqual({ href: '/api/v1/items/i-1', imageVersion: 1234 });
  });

  it('renders the visual fallback instead of a broken image when the image fails to load', async () => {
    await navigate();
    flushItem();
    await harness.fixture.whenStable();
    harness.fixture.detectChanges();

    const img: HTMLImageElement = harness.fixture.nativeElement.querySelector('img');
    expect(img.getAttribute('src')).toBe('/api/v1/items/i-1/image');
    img.dispatchEvent(new Event('error'));
    harness.fixture.detectChanges();

    expect(harness.fixture.nativeElement.querySelector('img')).toBeNull();
    expect(harness.fixture.nativeElement.querySelector('mat-icon')).not.toBeNull();
  });

  it('does not render admin actions for a non-admin token', async () => {
    await navigate();
    flushItem();
    await harness.fixture.whenStable();
    harness.fixture.detectChanges();

    expect(harness.fixture.nativeElement.textContent).not.toContain('Edit');
    expect(harness.fixture.nativeElement.textContent).not.toContain('Delete');
  });

  it('opens the delete dialog and does not delete when cancelled', async () => {
    authenticateAdmin();
    dialog.open.mockReturnValue({ afterClosed: () => of(false) });
    const component = await navigate();
    flushItem();
    await harness.fixture.whenStable();

    component.deleteItem();

    expect(dialog.open).toHaveBeenCalledWith(ItemDeleteDialog, {
      data: { itemName: 'Cola' },
    });
    backend.expectNone('/api/v1/items/i-1');
  });

  it('deletes through the self link and navigates back to the list when confirmed', async () => {
    authenticateAdmin();
    dialog.open.mockReturnValue({ afterClosed: () => of(true) });
    const component = await navigate();
    flushItem();
    await harness.fixture.whenStable();

    component.deleteItem();

    const request = backend.expectOne('/api/v1/items/i-1');
    expect(request.request.method).toBe('DELETE');
    request.flush(null, { status: 204, statusText: 'No Content' });
    await harness.fixture.whenStable();

    expect(snackBar.open).toHaveBeenCalledWith('Deleted Cola', 'Close', { duration: 5000 });
    expect(TestBed.inject(Router).url).toBe('/items');
  });
});
