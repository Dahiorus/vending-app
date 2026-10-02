import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { TokenStore } from '../../../core/auth/token-store';
import { ItemDeleteDialog } from '../item-delete-dialog/item-delete-dialog';
import { ItemList } from './item-list';

function fakeJwt(payload: Record<string, unknown>): string {
  const encode = (value: unknown) =>
    btoa(JSON.stringify(value)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${encode({ alg: 'none' })}.${encode(payload)}.signature`;
}

describe('ItemList', () => {
  let harness: RouterTestingHarness;
  let http: HttpTestingController;
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
          { path: 'items', component: ItemList },
          { path: 'items/new', children: [] },
          { path: 'items/:id', children: [] },
          { path: 'items/:id/edit', children: [] },
        ]),
        { provide: MatDialog, useValue: dialog },
        { provide: MatSnackBar, useValue: snackBar },
      ],
    }).compileComponents();

    http = TestBed.inject(HttpTestingController);
    harness = await RouterTestingHarness.create();
  });

  afterEach(() => http.verify());

  async function navigate(url: string) {
    const component = await harness.navigateByUrl(url, ItemList);
    harness.fixture.detectChanges();
    http.expectOne('/api/v1').flush({ _links: { items: { href: '/api/v1/items' } } });
    await Promise.resolve();
    harness.fixture.detectChanges();
    return component;
  }

  function flushItemsPage(): void {
    http.expectOne('/api/v1/items?page=1&size=20').flush({
      _embedded: {
        elements: [
          {
            id: 'i-1',
            name: 'Cola',
            type: 'COLD_BEVERAGE',
            price: 1.5,
            _links: { self: { href: '/api/v1/items/i-1' } },
          },
        ],
      },
      _links: { self: { href: '/api/v1/items' } },
      _templates: { default: { method: 'post' } },
      page: { size: 20, totalElements: 1, totalPages: 1, number: 0 },
    });
  }

  function authenticateAdmin(): void {
    TestBed.inject(TokenStore).setAccessToken(
      fakeJwt({ sub: 'admin@vending.me', roles: ['ROLE_ADMIN'], exp: 4102444800 }),
    );
  }

  it('requests the first page on load and exposes the unwrapped elements', async () => {
    const component = await navigate('/items');

    const request = http.expectOne('/api/v1/items?page=1&size=20');
    expect(request.request.method).toBe('GET');

    request.flush({
      _embedded: {
        elements: [{ id: 'i-1', name: 'Cola', type: 'COLD_BEVERAGE', price: 1.5 }],
      },
      _links: { self: { href: '/api/v1/items' } },
      page: { size: 20, totalElements: 1, totalPages: 1, number: 0 },
    });
    await harness.fixture.whenStable();

    expect(component.items().elements).toHaveLength(1);
    expect(component.items().elements[0].name).toBe('Cola');
    expect(component.totalElements()).toBe(1);
    expect(component.displayedColumns).toEqual(['name', 'type', 'price', 'actions']);
  });

  it('reads the initial page from the URL query params', async () => {
    const component = await navigate('/items?page=2&size=10&type=SNACK');

    http.expectOne('/api/v1/items?page=3&size=10').flush({
      _links: { self: { href: '/api/v1/items' } },
      page: { size: 10, totalElements: 30, totalPages: 3, number: 2 },
    });
    await harness.fixture.whenStable();

    expect(component.pageIndex()).toBe(2);
    expect(component.pageSize()).toBe(10);
  });

  it('navigates to the new page/size while preserving other query params', async () => {
    const component = await navigate('/items?type=SNACK');

    http.expectOne('/api/v1/items?page=1&size=20').flush({
      _links: { self: { href: '/api/v1/items' } },
      page: { size: 20, totalElements: 30, totalPages: 2, number: 0 },
    });
    await harness.fixture.whenStable();

    component.onPageChange({ pageIndex: 2, pageSize: 10, length: 30 });
    await new Promise((resolve) => setTimeout(resolve));
    harness.fixture.detectChanges();

    expect(TestBed.inject(Router).url).toBe('/items?type=SNACK&page=2&size=10');
    http.expectOne('/api/v1/items?page=3&size=10').flush({
      _links: { self: { href: '/api/v1/items' } },
      page: { size: 10, totalElements: 30, totalPages: 3, number: 2 },
    });
    await harness.fixture.whenStable();

    expect(component.items().elements).toEqual([]);
    expect(component.totalElements()).toBe(30);
  });

  it('exposes the create href from the page affordance fallback for the New item route state', async () => {
    authenticateAdmin();
    const component = await navigate('/items');
    flushItemsPage();
    await harness.fixture.whenStable();

    expect(component.createHref()).toBe('/api/v1/items');
  });

  it('opens the delete dialog and does not delete when cancelled', async () => {
    authenticateAdmin();
    dialog.open.mockReturnValue({ afterClosed: () => of(false) });
    const component = await navigate('/items');
    flushItemsPage();
    await harness.fixture.whenStable();

    component.deleteItem(component.items().elements[0]);

    expect(dialog.open).toHaveBeenCalledWith(ItemDeleteDialog, {
      data: { itemName: 'Cola' },
    });
    http.expectNone('/api/v1/items/i-1');
  });

  it('deletes through the item self link and reloads the list when confirmed', async () => {
    authenticateAdmin();
    dialog.open.mockReturnValue({ afterClosed: () => of(true) });
    const component = await navigate('/items');
    flushItemsPage();
    await harness.fixture.whenStable();

    component.deleteItem(component.items().elements[0]);

    const request = http.expectOne('/api/v1/items/i-1');
    expect(request.request.method).toBe('DELETE');
    request.flush(null, { status: 204, statusText: 'No Content' });
    harness.fixture.detectChanges();
    await Promise.resolve();
    harness.fixture.detectChanges();
    http.expectOne('/api/v1/items?page=1&size=20').flush({
      _links: { self: { href: '/api/v1/items' } },
      page: { size: 20, totalElements: 0, totalPages: 0, number: 0 },
    });
    await harness.fixture.whenStable();

    expect(snackBar.open).toHaveBeenCalledWith('Deleted Cola', 'Close', { duration: 5000 });
    expect(component.items().elements).toEqual([]);
  });
});
