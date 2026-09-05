import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { MachineCreate } from './machine-create';

describe('MachineCreate', () => {
  let fixture: ComponentFixture<MachineCreate>;
  let component: MachineCreate;
  let backend: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [MachineCreate],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'machines', children: [] }]),
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(MachineCreate);
    component = fixture.componentInstance;
    await fixture.whenStable();
    backend = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => backend.verify());

  function fillValidForm(): void {
    component.machineForm.serialNumber().value.set('SN-001');
    component.machineForm.itemType().value.set('SNACK');
    component.machineForm.address.latitude().value.set(48.85);
    component.machineForm.address.longitude().value.set(2.35);
    component.machineForm.address.streetNumber().value.set(10);
    component.machineForm.address.streetName().value.set('Rue de Rivoli');
    component.machineForm.address.postalCode().value.set('75001');
    component.machineForm.address.city().value.set('Paris');
  }

  it('rejects an empty form without calling the API and marks fields as touched', () => {
    component.submit();

    backend.expectNone('/api/v1/vending-machines');
    expect(component.machineForm().valid()).toBe(false);
    expect(component.machineForm.serialNumber().touched()).toBe(true);
    expect(component.machineForm.serialNumber().errors().length).toBeGreaterThan(0);
  });

  it('sends the creation payload and navigates to machines on success', async () => {
    fillValidForm();

    component.submit();

    const request = backend.expectOne('/api/v1/vending-machines');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      serialNumber: 'SN-001',
      itemType: 'SNACK',
      address: {
        latitude: 48.85,
        longitude: 2.35,
        streetNumber: 10,
        streetName: 'Rue de Rivoli',
        postalCode: '75001',
        city: 'Paris',
      },
    });
    request.flush({ id: 'm-1' });

    await fixture.whenStable();

    expect(component.errorMessage()).toBeNull();
    expect(component.fieldErrors()).toEqual({});
    expect(component.submitting()).toBe(false);
    expect(router.url).toBe('/machines');
  });

  it('shows a generic error message when the API returns an object-level validation error', async () => {
    fillValidForm();

    component.submit();

    backend.expectOne('/api/v1/vending-machines').flush(
      {
        message: 'Validation failed',
        errors: [
          {
            code: 'validation.constraints.object.not_unique',
            defaultMessage: 'Another object exists with the same unique values',
          },
        ],
      },
      { status: 400, statusText: 'Bad Request' },
    );
    await fixture.whenStable();

    expect(component.errorMessage()).toBe('The vending machine could not be created.');
    expect(component.fieldErrors()).toEqual({});
    expect(component.submitting()).toBe(false);
  });

  it('shows field-specific validation messages returned by the API', async () => {
    fillValidForm();

    component.submit();

    backend.expectOne('/api/v1/vending-machines').flush(
      {
        message: 'Validation failed',
        errors: [
          {
            field: 'serialNumber',
            code: 'validation.constraints.not_blank',
            defaultMessage: 'must not be blank',
          },
        ],
      },
      { status: 400, statusText: 'Bad Request' },
    );
    await fixture.whenStable();

    expect(component.errorMessage()).toBeNull();
    expect(component.fieldErrors()).toEqual({
      serialNumber: 'must not be blank',
    });
    expect(component.submitting()).toBe(false);
  });
});
