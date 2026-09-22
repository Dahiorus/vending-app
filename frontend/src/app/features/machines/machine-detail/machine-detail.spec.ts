import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { TokenStore } from '../../../core/auth/token-store';
import { MachineDetail } from './machine-detail';

const fakeJwt = (value: unknown): string => {
  const base64Url = (json: unknown) =>
    btoa(JSON.stringify(json)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${base64Url({ alg: 'RS256' })}.${base64Url(value)}.signature`;
};

describe('MachineDetail', () => {
  let harness: RouterTestingHarness;
  let backend: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'machines/:id', component: MachineDetail }]),
      ],
    }).compileComponents();

    backend = TestBed.inject(HttpTestingController);
    harness = await RouterTestingHarness.create();
  });

  afterEach(() => backend.verify());

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
});
