import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { ItemList } from './item-list';

describe('ItemList', () => {
  let harness: RouterTestingHarness;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'items', component: ItemList }]),
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

  it('requests the first page on load and exposes the unwrapped elements', async () => {
    const component = await navigate('/items');

    const request = http.expectOne('/api/v1/items?page=1&size=20');
    expect(request.request.method).toBe('GET');

    request.flush({
      _embedded: {
        elements: [{ id: 'i-1', name: 'Cola', type: 'COLD_BEVERAGE', price: 1.5 }],
      },
      page: { size: 20, totalElements: 1, totalPages: 1, number: 0 },
    });
    await harness.fixture.whenStable();

    expect(component.items().elements).toHaveLength(1);
    expect(component.items().elements[0].name).toBe('Cola');
    expect(component.totalElements()).toBe(1);
  });

  it('reads the initial page from the URL query params', async () => {
    const component = await navigate('/items?page=2&size=10&type=SNACK');

    http
      .expectOne('/api/v1/items?page=3&size=10')
      .flush({ page: { size: 10, totalElements: 30, totalPages: 3, number: 2 } });
    await harness.fixture.whenStable();

    expect(component.pageIndex()).toBe(2);
    expect(component.pageSize()).toBe(10);
  });

  it('navigates to the new page/size while preserving other query params', async () => {
    const component = await navigate('/items?type=SNACK');

    http
      .expectOne('/api/v1/items?page=1&size=20')
      .flush({ page: { size: 20, totalElements: 30, totalPages: 2, number: 0 } });
    await harness.fixture.whenStable();

    component.onPageChange({ pageIndex: 2, pageSize: 10, length: 30 });
    await new Promise((resolve) => setTimeout(resolve));
    harness.fixture.detectChanges();

    expect(TestBed.inject(Router).url).toBe('/items?type=SNACK&page=2&size=10');
    http
      .expectOne('/api/v1/items?page=3&size=10')
      .flush({ page: { size: 10, totalElements: 30, totalPages: 3, number: 2 } });
    await harness.fixture.whenStable();

    expect(component.items().elements).toEqual([]);
    expect(component.totalElements()).toBe(30);
  });
});
