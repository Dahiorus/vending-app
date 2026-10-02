import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { ItemEdit } from './item-edit';

describe('ItemEdit', () => {
  let harness: RouterTestingHarness;
  let backend: HttpTestingController;
  let navigateSpy: ReturnType<typeof vi.spyOn>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([
          { path: 'items/:id', children: [] },
          { path: 'items/:id/edit', component: ItemEdit },
        ]),
      ],
    }).compileComponents();

    backend = TestBed.inject(HttpTestingController);
    harness = await RouterTestingHarness.create();
    navigateSpy = vi.spyOn(TestBed.inject(Router), 'navigate');
  });

  afterEach(() => backend.verify());

  const nextTick = () => new Promise<void>((resolve) => setTimeout(resolve, 10));

  async function navigate(state: unknown = null) {
    history.replaceState(state, '');
    const component = await harness.navigateByUrl('/items/i-1/edit', ItemEdit);
    harness.fixture.detectChanges();
    backend.expectOne('/api/v1/items/i-1').flush({
      id: 'i-1',
      name: 'Cola',
      type: 'COLD_BEVERAGE',
      price: 1.5,
      _links: {
        self: { href: '/api/v1/items/i-1' },
        'item:image': { href: '/api/v1/items/i-1/image' },
      },
    });
    await harness.fixture.whenStable();
    harness.fixture.detectChanges();
    return component;
  }

  it('loads the item and initializes the price form', async () => {
    const component = await navigate();

    expect(component.item()?.name).toBe('Cola');
    expect(component.itemForm.price().value()).toBe(1.5);
    expect(component.currentImageUrl()).toBe('/api/v1/items/i-1/image');
  });

  it('renders a price input accepting decimal prices', async () => {
    await navigate();
    const input = harness.fixture.nativeElement.querySelector(
      'input[type="number"]',
    ) as HTMLInputElement;
    expect(input.getAttribute('step')).toBe('0.01');
    input.value = '1.5';
    expect(input.checkValidity()).toBe(true);
  });

  it('puts the updated price and navigates to the item detail without image upload', async () => {
    const component = await navigate();
    component.itemForm.price().value.set(2);

    component.submit();

    const request = backend.expectOne('/api/v1/items/i-1');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({ price: 2 });
    request.flush({
      id: 'i-1',
      name: 'Cola',
      type: 'COLD_BEVERAGE',
      price: 2,
      _links: { self: { href: '/api/v1/items/i-1' } },
    });
    await harness.fixture.whenStable();
    await nextTick();

    expect(TestBed.inject(Router).url).toBe('/items/i-1');
    expect(navigateSpy).toHaveBeenCalledWith(['/items', 'i-1'], {
      state: { href: '/api/v1/items/i-1' },
    });
  });

  it('uploads the selected image after the price update', async () => {
    const component = await navigate();
    const file = new File(['image'], 'cola.jpg', { type: 'image/jpeg' });
    component.itemForm.price().value.set(2);
    component.onFileSelected(file);

    component.submit();

    backend.expectOne('/api/v1/items/i-1').flush({
      id: 'i-1',
      name: 'Cola',
      type: 'COLD_BEVERAGE',
      price: 2,
      _links: {
        self: { href: '/api/v1/items/i-1' },
        'item:image': { href: '/api/v1/items/i-1/image' },
      },
    });

    await nextTick();
    const upload = backend.expectOne('/api/v1/items/i-1/image');
    expect(upload.request.method).toBe('POST');
    expect(upload.request.body instanceof FormData).toBe(true);
    expect(upload.request.body.get('file')).toBe(file);
    upload.flush({ id: 'i-1' });
    await harness.fixture.whenStable();
    await nextTick();

    expect(TestBed.inject(Router).url).toBe('/items/i-1');
    expect(navigateSpy).toHaveBeenCalledWith(['/items', 'i-1'], {
      state: { href: '/api/v1/items/i-1', imageVersion: expect.any(Number) },
    });
  });

  it('keeps the form open with an image field error when only the upload fails', async () => {
    const component = await navigate();
    component.itemForm.price().value.set(2);
    component.onFileSelected(new File(['image'], 'cola.jpg', { type: 'image/jpeg' }));

    component.submit();

    backend.expectOne('/api/v1/items/i-1').flush({
      id: 'i-1',
      name: 'Cola',
      type: 'COLD_BEVERAGE',
      price: 2,
      _links: {
        self: { href: '/api/v1/items/i-1' },
        'item:image': { href: '/api/v1/items/i-1/image' },
      },
    });
    await nextTick();
    backend
      .expectOne('/api/v1/items/i-1/image')
      .flush(null, { status: 500, statusText: 'Internal Server Error' });
    await harness.fixture.whenStable();
    await nextTick();

    expect(component.imageError()).toBe('The image could not be uploaded.');
    expect(TestBed.inject(Router).url).toBe('/items/i-1/edit');
  });

  it('shows field-specific validation messages returned by the update API', async () => {
    const component = await navigate();
    // Client-side valid (>= 0.01): an invalid value would make `submit()` skip the request entirely.
    component.itemForm.price().value.set(0.5);

    component.submit();

    backend.expectOne('/api/v1/items/i-1').flush(
      {
        message: 'Validation failed',
        errors: [
          {
            field: 'price',
            code: 'Positive',
            defaultMessage: 'must be greater than 0',
          },
        ],
      },
      { status: 400, statusText: 'Bad Request' },
    );
    await harness.fixture.whenStable();

    expect(component.fieldErrors()).toEqual({ price: 'must be greater than 0' });
    expect(component.errorMessage()).toBeNull();
  });
});
