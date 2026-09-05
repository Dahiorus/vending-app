import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { MachineDetail } from './machine-detail';

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
});
