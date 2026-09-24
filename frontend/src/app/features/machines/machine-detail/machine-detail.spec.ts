import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { TokenStore } from '../../../core/auth/token-store';
import { OrderConfirmDialog } from '../order-confirm-dialog/order-confirm-dialog';
import { MachineDetail } from './machine-detail';

const fakeJwt = (value: unknown): string => {
  const base64Url = (json: unknown) =>
    btoa(JSON.stringify(json)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${base64Url({ alg: 'RS256' })}.${base64Url(value)}.signature`;
};

type MachineDetailWithOrdering = MachineDetail & {
  ordering(): string | null;
};

describe('MachineDetail', () => {
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
        provideRouter([{ path: 'machines/:id', component: MachineDetail }]),
        { provide: MatDialog, useValue: dialog },
        { provide: MatSnackBar, useValue: snackBar },
      ],
    }).compileComponents();

    backend = TestBed.inject(HttpTestingController);
    harness = await RouterTestingHarness.create();
  });

  afterEach(() => backend.verify());

  async function navigate() {
    history.replaceState(null, '');

    const component = await harness.navigateByUrl('/machines/m-1', MachineDetail);
    harness.fixture.detectChanges();

    return component;
  }

  function flushMachineAndStock(
    options: {
      stockLink?: string;
      stockOrderLinks?: Array<{ href: string }>;
    } = {},
  ) {
    const stockLink = options.stockLink ?? 'https://api.example.test/vending-machines/m-1/stock';

    backend.expectOne('/api/v1/vending-machines/m-1').flush({
      id: 'm-1',
      serialNumber: 'SN-1',
      address: null,
      lastIntervention: null,
      temperature: null,
      itemType: null,
      powerStatus: null,
      workingStatus: null,
      rfidStatus: null,
      smartCardStatus: null,
      changeMoneyStatus: null,
      _links: { stock: { href: stockLink } },
    });
    harness.fixture.detectChanges();

    return Promise.resolve().then(() => {
      harness.fixture.detectChanges();
      backend.expectOne(stockLink).flush({
        itemQuantities: [{ itemId: 'i-1', itemName: 'Water', quantity: 3 }],
        _links: options.stockOrderLinks ? { order: options.stockOrderLinks } : undefined,
      });
    });
  }

  function authenticate() {
    TestBed.inject(TokenStore).setAccessToken(
      fakeJwt({ sub: 'user@vending.me', roles: ['ROLE_USER'], exp: 1 }),
    );
  }

  function queryOrderButton(): HTMLButtonElement | null {
    return (
      Array.from(harness.fixture.nativeElement.querySelectorAll('button')).find(
        (button): button is HTMLButtonElement =>
          button instanceof HTMLButtonElement && button.textContent?.includes('Order') === true,
      ) ?? null
    );
  }

  it('falls back to the id-based URL when no HATEOAS state is available', async () => {
    history.replaceState(null, '');

    const component = await harness.navigateByUrl('/machines/m-1', MachineDetail);
    harness.fixture.detectChanges();

    const request = backend.expectOne('/api/v1/vending-machines/m-1');
    request.flush({
      id: 'm-1',
      serialNumber: 'SN-1',
      address: null,
      lastIntervention: null,
      temperature: null,
      itemType: null,
      powerStatus: null,
      workingStatus: null,
      rfidStatus: null,
      smartCardStatus: null,
      changeMoneyStatus: null,
    });
    await harness.fixture.whenStable();

    expect(component.machine()?.serialNumber).toBe('SN-1');
  });

  it('follows the HATEOAS self link carried over via router navigation state', async () => {
    history.pushState({ href: 'https://api.example.test/vending-machines/m-1' }, '');

    const component = await harness.navigateByUrl('/machines/m-1', MachineDetail);
    harness.fixture.detectChanges();

    const request = backend.expectOne('https://api.example.test/vending-machines/m-1');
    request.flush({
      id: 'm-1',
      serialNumber: 'SN-1',
      address: null,
      lastIntervention: null,
      temperature: null,
      itemType: null,
      powerStatus: null,
      workingStatus: null,
      rfidStatus: null,
      smartCardStatus: null,
      changeMoneyStatus: null,
    });
    await harness.fixture.whenStable();

    expect(component.machine()?.serialNumber).toBe('SN-1');
  });

  it('loads the stock via the `stock` HATEOAS link once the machine resource is loaded', async () => {
    history.replaceState(null, '');

    const component = await harness.navigateByUrl('/machines/m-1', MachineDetail);
    harness.fixture.detectChanges();

    backend.expectOne('/api/v1/vending-machines/m-1').flush({
      id: 'm-1',
      serialNumber: 'SN-1',
      address: null,
      lastIntervention: null,
      temperature: null,
      itemType: null,
      powerStatus: null,
      workingStatus: null,
      rfidStatus: null,
      smartCardStatus: null,
      changeMoneyStatus: null,
      _links: { stock: { href: 'https://api.example.test/vending-machines/m-1/stock' } },
    });
    harness.fixture.detectChanges();
    await Promise.resolve();
    harness.fixture.detectChanges();

    backend
      .expectOne('https://api.example.test/vending-machines/m-1/stock')
      .flush({ itemQuantities: [{ itemId: 'i-1', itemName: 'Water', quantity: 3 }] });
    await harness.fixture.whenStable();

    expect(component.itemQuantities()).toEqual([{ itemId: 'i-1', itemName: 'Water', quantity: 3 }]);
  });

  it('exposes the item link only for admins, matching the link href by item id', async () => {
    history.replaceState(null, '');

    const component = await harness.navigateByUrl('/machines/m-1', MachineDetail);
    harness.fixture.detectChanges();

    backend.expectOne('/api/v1/vending-machines/m-1').flush({
      id: 'm-1',
      serialNumber: 'SN-1',
      address: null,
      lastIntervention: null,
      temperature: null,
      itemType: null,
      powerStatus: null,
      workingStatus: null,
      rfidStatus: null,
      smartCardStatus: null,
      changeMoneyStatus: null,
      _links: { stock: { href: 'https://api.example.test/vending-machines/m-1/stock' } },
    });
    harness.fixture.detectChanges();
    await Promise.resolve();
    harness.fixture.detectChanges();

    backend.expectOne('https://api.example.test/vending-machines/m-1/stock').flush({
      itemQuantities: [{ itemId: 'i-1', itemName: 'Water', quantity: 3 }],
      _links: { item: [{ href: 'https://api.example.test/items/i-1' }] },
    });
    await harness.fixture.whenStable();

    expect(component.isAdmin()).toBe(false);
    expect(component.itemLink('i-1')).toBe('https://api.example.test/items/i-1');

    TestBed.inject(TokenStore).setAccessToken(
      fakeJwt({ sub: 'admin@vending.me', roles: ['ROLE_ADMIN'], exp: 1 }),
    );

    expect(component.isAdmin()).toBe(true);
  });

  it('hides the order button when the user is not authenticated', async () => {
    await navigate();
    await flushMachineAndStock({
      stockOrderLinks: [{ href: 'https://api.example.test/vending-machines/m-1/items/i-1/order' }],
    });
    await harness.fixture.whenStable();

    expect(queryOrderButton()).toBeNull();
  });

  it('hides the order button when the stock item has no order link', async () => {
    authenticate();

    await navigate();
    await flushMachineAndStock();
    await harness.fixture.whenStable();

    expect(queryOrderButton()).toBeNull();
  });

  it('opens the confirm dialog and does not post when the order is cancelled', async () => {
    authenticate();
    dialog.open.mockReturnValue({ afterClosed: () => of(false) });

    const component = (await navigate()) as MachineDetailWithOrdering;
    await flushMachineAndStock({
      stockOrderLinks: [{ href: 'https://api.example.test/vending-machines/m-1/items/i-1/order' }],
    });
    await harness.fixture.whenStable();

    const orderButton = queryOrderButton();
    expect(orderButton).not.toBeNull();

    orderButton!.click();

    expect(dialog.open).toHaveBeenCalledWith(OrderConfirmDialog, {
      data: { itemName: 'Water', quantity: 3 },
    });
    backend.expectNone('https://api.example.test/vending-machines/m-1/items/i-1/order');
    expect(component.ordering()).toBeNull();
  });

  it('posts to the item order link, shows a snackbar, and reloads the stock on success', async () => {
    authenticate();
    dialog.open.mockReturnValue({ afterClosed: () => of(true) });

    const component = (await navigate()) as MachineDetailWithOrdering;
    await flushMachineAndStock({
      stockOrderLinks: [{ href: 'https://api.example.test/vending-machines/m-1/items/i-1/order' }],
    });
    await harness.fixture.whenStable();

    queryOrderButton()!.click();

    expect(component.ordering()).toBe('i-1');

    const orderRequest = backend.expectOne(
      'https://api.example.test/vending-machines/m-1/items/i-1/order',
    );
    expect(orderRequest.request.method).toBe('POST');
    expect(orderRequest.request.body).toEqual({});

    orderRequest.flush({ amount: 1.5, createdAt: '2026-09-24T12:00:00Z', _links: {} });
    harness.fixture.detectChanges();
    await Promise.resolve();
    harness.fixture.detectChanges();

    backend.expectOne('https://api.example.test/vending-machines/m-1/stock').flush({
      itemQuantities: [{ itemId: 'i-1', itemName: 'Water', quantity: 2 }],
      _links: {
        order: [{ href: 'https://api.example.test/vending-machines/m-1/items/i-1/order' }],
      },
    });
    await harness.fixture.whenStable();

    expect(snackBar.open).toHaveBeenCalledWith('Ordered Water for 1.5 €', 'Close', {
      duration: 5000,
    });
    expect(component.ordering()).toBeNull();
    expect(component.itemQuantities()).toEqual([{ itemId: 'i-1', itemName: 'Water', quantity: 2 }]);
  });

  it('shows the backend business error and still reloads the stock on failure', async () => {
    authenticate();
    dialog.open.mockReturnValue({ afterClosed: () => of(true) });

    const component = (await navigate()) as MachineDetailWithOrdering;
    await flushMachineAndStock({
      stockOrderLinks: [{ href: 'https://api.example.test/vending-machines/m-1/items/i-1/order' }],
    });
    await harness.fixture.whenStable();

    queryOrderButton()!.click();

    const orderRequest = backend.expectOne(
      'https://api.example.test/vending-machines/m-1/items/i-1/order',
    );
    orderRequest.flush({ message: 'Item stock is empty' }, { status: 409, statusText: 'Conflict' });
    harness.fixture.detectChanges();
    await Promise.resolve();
    harness.fixture.detectChanges();

    backend.expectOne('https://api.example.test/vending-machines/m-1/stock').flush({
      itemQuantities: [{ itemId: 'i-1', itemName: 'Water', quantity: 0 }],
      _links: {},
    });
    await harness.fixture.whenStable();

    expect(snackBar.open).toHaveBeenCalledWith('Item stock is empty', 'Close', {
      duration: 5000,
    });
    expect(component.ordering()).toBeNull();
    expect(component.itemQuantities()).toEqual([{ itemId: 'i-1', itemName: 'Water', quantity: 0 }]);
  });
});
