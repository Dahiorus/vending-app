import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { ItemList } from './item-list';

describe('ItemList', () => {
  let fixture: ComponentFixture<ItemList>;
  let component: ItemList;
  let backend: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ItemList],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(ItemList);
    component = fixture.componentInstance;
    backend = TestBed.inject(HttpTestingController);
    // `httpResource` keeps the fixture unstable while its request is pending, so
    // `whenStable()` would deadlock here: trigger change detection synchronously
    // instead and only await stability after the pending request is flushed.
    fixture.detectChanges();
    // The items page URL is only resolved once the `/api/v1` root link is
    // loaded, so every test needs that resolved first.
    backend.expectOne('/api/v1').flush({ _links: { items: { href: '/api/v1/items' } } });
    await Promise.resolve();
    fixture.detectChanges();
  });

  afterEach(() => backend.verify());

  it('requests the first page on load and exposes the unwrapped elements', async () => {
    const request = backend.expectOne('/api/v1/items?page=0&size=10');
    expect(request.request.method).toBe('GET');

    request.flush({
      _embedded: {
        elements: [{ id: 'i-1', name: 'Cola', type: 'COLD_BEVERAGE', price: 1.5 }],
      },
      page: { size: 10, totalElements: 1, totalPages: 1, number: 0 },
    });
    await fixture.whenStable();

    expect(component.items()).toHaveLength(1);
    expect(component.items()[0].name).toBe('Cola');
    expect(component.totalElements()).toBe(1);
  });

  it('requests the next page when the paginator moves', async () => {
    backend
      .expectOne('/api/v1/items?page=0&size=10')
      .flush({ page: { size: 10, totalElements: 30, totalPages: 3, number: 0 } });
    await fixture.whenStable();

    component.onPageChange({ pageIndex: 2, pageSize: 10, length: 30 });
    // Trigger CD synchronously so the resource issues its next request; awaiting
    // `whenStable()` here would deadlock while that request is still pending.
    fixture.detectChanges();

    backend
      .expectOne('/api/v1/items?page=2&size=10')
      .flush({ page: { size: 10, totalElements: 30, totalPages: 3, number: 2 } });
    await fixture.whenStable();

    expect(component.items()).toEqual([]);
    expect(component.totalElements()).toBe(30);
  });
});
