import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { TokenStore } from '../../../core/auth/token-store';
import { MachineList } from './machine-list';

function fakeJwt(payload: Record<string, unknown>): string {
  const encode = (value: unknown) =>
    btoa(JSON.stringify(value)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${encode({ alg: 'none' })}.${encode(payload)}.signature`;
}

describe('MachineList', () => {
  let harness: RouterTestingHarness;
  let backend: HttpTestingController;

  beforeEach(async () => {
    sessionStorage.clear();
    await TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([
          { path: 'machines', component: MachineList },
          { path: 'machines/new', children: [] },
        ]),
      ],
    }).compileComponents();

    backend = TestBed.inject(HttpTestingController);
    harness = await RouterTestingHarness.create();
  });

  afterEach(() => backend.verify());

  async function navigate(url: string) {
    const component = await harness.navigateByUrl(url, MachineList);
    // `httpResource` keeps the fixture unstable while its request is pending, so
    // `whenStable()` would deadlock here: trigger change detection synchronously
    // instead and only await stability after the pending request is flushed.
    harness.fixture.detectChanges();
    // The machines page URL is only resolved once the `/api/v1` root link is
    // loaded, so every test needs that resolved first.
    backend
      .expectOne('/api/v1')
      .flush({ _links: { vendingMachines: { href: '/api/v1/vending-machines' } } });
    await Promise.resolve();
    harness.fixture.detectChanges();
    return component;
  }

  it('requests the first page on load and exposes the unwrapped elements', async () => {
    const component = await navigate('/machines');

    const request = backend.expectOne('/api/v1/vending-machines?page=1&size=20');
    expect(request.request.method).toBe('GET');
    expect(request.request.headers.has('Authorization')).toBe(false);

    request.flush({
      _embedded: {
        elements: [
          {
            id: 'm-1',
            serialNumber: 'SN-1',
            address: { city: 'Lyon', streetName: 'Rue A', postalCode: '69001' },
            itemType: 'SNACK',
            workingStatus: 'WORKING',
            powerStatus: 'POWER_ON',
          },
        ],
      },
      page: { size: 20, totalElements: 1, totalPages: 1, number: 0 },
    });
    await harness.fixture.whenStable();

    expect(component.machines().elements).toHaveLength(1);
    expect(component.machines().elements[0].serialNumber).toBe('SN-1');
    expect(component.totalElements()).toBe(1);
  });

  it('reads the initial page from the URL query params', async () => {
    const component = await navigate('/machines?page=2&size=10&city=Paris');

    backend
      .expectOne('/api/v1/vending-machines?page=3&size=10')
      .flush({ page: { size: 10, totalElements: 30, totalPages: 3, number: 2 } });
    await harness.fixture.whenStable();

    expect(component.pageIndex()).toBe(2);
    expect(component.pageSize()).toBe(10);
  });

  it('navigates to the new page/size while preserving other query params', async () => {
    const component = await navigate('/machines?city=Paris');

    // The browser URL keeps `city` (e.g. carried over from a future search
    // feature), but forwarding it to the API request is that future
    // feature's responsibility, not this component's.
    backend
      .expectOne('/api/v1/vending-machines?page=1&size=20')
      .flush({ page: { size: 20, totalElements: 30, totalPages: 2, number: 0 } });
    await harness.fixture.whenStable();

    component.onPageChange({ pageIndex: 2, pageSize: 10, length: 30 });
    // Router navigation completes asynchronously (it's promise-based); wait a
    // macrotask for it before checking the URL and triggering CD so the
    // resource picks up the new query params.
    await new Promise((resolve) => setTimeout(resolve));
    harness.fixture.detectChanges();

    expect(TestBed.inject(Router).url).toBe('/machines?city=Paris&page=2&size=10');
    backend
      .expectOne('/api/v1/vending-machines?page=3&size=10')
      .flush({ page: { size: 10, totalElements: 30, totalPages: 3, number: 2 } });
    await harness.fixture.whenStable();

    expect(component.machines().elements).toEqual([]);
    expect(component.totalElements()).toBe(30);
  });

  it('exposes isAdmin based on the current JWT roles', async () => {
    const component = await navigate('/machines');

    backend
      .expectOne('/api/v1/vending-machines?page=1&size=20')
      .flush({ page: { size: 20, totalElements: 0, totalPages: 0, number: 0 } });

    expect(component.isAdmin()).toBe(false);

    TestBed.inject(TokenStore).setAccessToken(
      fakeJwt({ sub: 'admin@vending.me', roles: ['ROLE_ADMIN'], exp: 1 }),
    );

    expect(component.isAdmin()).toBe(true);
  });
});
