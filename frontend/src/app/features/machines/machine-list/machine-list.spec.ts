import { provideHttpClient } from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { MachineList } from './machine-list';

describe('MachineList', () => {
  let fixture: ComponentFixture<MachineList>;
  let component: MachineList;
  let backend: HttpTestingController;

  beforeEach(async () => {
    sessionStorage.clear();
    await TestBed.configureTestingModule({
      imports: [MachineList],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(MachineList);
    component = fixture.componentInstance;
    backend = TestBed.inject(HttpTestingController);
    // `httpResource` keeps the fixture unstable while its request is pending, so
    // `whenStable()` would deadlock here: trigger change detection synchronously
    // instead and only await stability after the pending request is flushed.
    fixture.detectChanges();
  });

  afterEach(() => backend.verify());

  it('requests the first page on load and exposes the unwrapped elements', async () => {
    const request = backend.expectOne('/api/v1/vending-machines?page=0&size=10');
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
      page: { size: 10, totalElements: 1, totalPages: 1, number: 0 },
    });
    await fixture.whenStable();

    expect(component.machines()).toHaveLength(1);
    expect(component.machines()[0].serialNumber).toBe('SN-1');
    expect(component.totalElements()).toBe(1);
  });

  it('requests the next page when the paginator moves', async () => {
    backend
      .expectOne('/api/v1/vending-machines?page=0&size=10')
      .flush({ page: { size: 10, totalElements: 30, totalPages: 3, number: 0 } });
    await fixture.whenStable();

    component.onPageChange({ pageIndex: 2, pageSize: 10, length: 30 });
    // Trigger CD synchronously so the resource issues its next request; awaiting
    // `whenStable()` here would deadlock while that request is still pending.
    fixture.detectChanges();

    backend
      .expectOne('/api/v1/vending-machines?page=2&size=10')
      .flush({ page: { size: 10, totalElements: 30, totalPages: 3, number: 2 } });
    await fixture.whenStable();

    expect(component.machines()).toEqual([]);
    expect(component.totalElements()).toBe(30);
  });
});
