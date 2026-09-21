import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { ApiRootApi } from './api-root-api';

describe('ApiRootApi', () => {
  let service: ApiRootApi;
  let backend: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });

    service = TestBed.inject(ApiRootApi);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('resolves the href for a known relation once the root resource is loaded', async () => {
    TestBed.tick();
    backend.expectOne('/api/v1').flush({
      _links: {
        vendingMachines: { href: '/api/v1/vending-machines' },
      },
    });
    await Promise.resolve();
    TestBed.tick();

    expect(service.link('vendingMachines')).toBe('/api/v1/vending-machines');
  });

  it('returns undefined for an unknown relation or while the root resource is loading', async () => {
    expect(service.link('vendingMachines')).toBeUndefined();

    TestBed.tick();
    backend.expectOne('/api/v1').flush({
      _links: {
        vendingMachines: { href: '/api/v1/vending-machines' },
      },
    });
    await Promise.resolve();
    TestBed.tick();

    expect(service.link('unknown')).toBeUndefined();
  });
});
