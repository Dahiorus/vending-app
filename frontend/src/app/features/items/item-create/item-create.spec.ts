import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { ItemCreate } from './item-create';

describe('ItemCreate', () => {
  let fixture: ComponentFixture<ItemCreate>;
  let component: ItemCreate;
  let backend: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ItemCreate],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'items', children: [] }]),
      ],
    }).compileComponents();

    history.replaceState(null, '');
    fixture = TestBed.createComponent(ItemCreate);
    component = fixture.componentInstance;
    fixture.detectChanges();
    backend = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => backend.verify());

  async function resolveRootItemsLink(): Promise<void> {
    backend.expectOne('/api/v1').flush({ _links: { items: { href: '/api/v1/items' } } });
    fixture.detectChanges();
    await fixture.whenStable();
  }

  // Lets the awaited HTTP promise chain advance without waiting for pending requests.
  const nextTick = () => new Promise<void>((resolve) => setTimeout(resolve));

  function fillValidForm(): void {
    component.itemForm.name().value.set('Cola');
    component.itemForm.type().value.set('COLD_BEVERAGE');
    component.itemForm.price().value.set(1.5);
  }

  it('offers a link back to the items list', async () => {
    await resolveRootItemsLink();
    const link = fixture.nativeElement.querySelector(
      'a[href="/items"]',
    ) as HTMLAnchorElement | null;
    expect(link?.textContent?.trim()).toBe('Back to items');
  });

  it('renders a euro price input accepting decimal prices with comma or dot', async () => {
    await resolveRootItemsLink();
    const input = fixture.nativeElement.querySelector('app-price-input input') as HTMLInputElement;
    expect(input.type).toBe('text');
    expect(fixture.nativeElement.querySelector('app-price-input')?.textContent).toContain('€');

    for (const text of ['1,5', '1.5']) {
      input.value = text;
      input.dispatchEvent(new Event('input'));
      fixture.detectChanges();
      await fixture.whenStable();
      expect(component.itemForm.price().value()).toBe(1.5);
      component.itemForm.price().value.set(0);
      fixture.detectChanges();
      await fixture.whenStable();
    }
  });

  it('rejects an empty form without calling the API and marks fields as touched', async () => {
    await resolveRootItemsLink();
    component.submit();

    backend.expectNone('/api/v1/items');
    expect(component.itemForm().valid()).toBe(false);
    expect(component.itemForm.name().touched()).toBe(true);
    expect(component.itemForm.name().errors().length).toBeGreaterThan(0);
  });

  it('uses the createHref navigation state when present and navigates back to items without image upload', async () => {
    history.pushState({ createHref: 'https://api.example.test/items' }, '');
    await resolveRootItemsLink();
    fillValidForm();

    component.submit();

    const request = backend.expectOne('https://api.example.test/items');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      name: 'Cola',
      type: 'COLD_BEVERAGE',
      price: 1.5,
    });
    request.flush({
      id: 'i-1',
      name: 'Cola',
      type: 'COLD_BEVERAGE',
      price: 1.5,
      _links: {
        self: { href: 'https://api.example.test/items/i-1' },
        'item:image': { href: 'https://api.example.test/items/i-1/image' },
      },
    });

    await fixture.whenStable();

    expect(component.errorMessage()).toBeNull();
    expect(component.imageError()).toBeNull();
    expect(component.submitting()).toBe(false);
    expect(router.url).toBe('/items');
  });

  it('falls back to the API root items link on direct navigation to /items/new', async () => {
    fillValidForm();
    await resolveRootItemsLink();

    component.submit();

    const request = backend.expectOne('/api/v1/items');
    expect(request.request.method).toBe('POST');
    request.flush({ id: 'i-1', name: 'Cola', type: 'COLD_BEVERAGE', price: 1.5, _links: {} });
    await fixture.whenStable();

    expect(router.url).toBe('/items');
  });

  it('uploads the selected image after the item is created', async () => {
    history.pushState({ createHref: '/api/v1/items' }, '');
    await resolveRootItemsLink();
    fillValidForm();
    const file = new File(['image'], 'cola.png', { type: 'image/png' });
    component.onFileSelected(file);

    component.submit();

    backend.expectOne('/api/v1/items').flush({
      id: 'i-1',
      name: 'Cola',
      type: 'COLD_BEVERAGE',
      price: 1.5,
      _links: {
        'item:image': { href: '/api/v1/items/i-1/image' },
      },
    });

    await nextTick();
    const upload = backend.expectOne('/api/v1/items/i-1/image');
    expect(upload.request.method).toBe('POST');
    expect(upload.request.body instanceof FormData).toBe(true);
    expect(upload.request.body.get('file')).toBe(file);
    expect(upload.request.headers.has('Content-Type')).toBe(false);
    upload.flush({ id: 'i-1' });

    await nextTick();
    await fixture.whenStable();

    expect(router.url).toBe('/items');
  });

  it('keeps the created item and retries only the image upload after a partial failure', async () => {
    history.pushState({ createHref: '/api/v1/items' }, '');
    await resolveRootItemsLink();
    fillValidForm();
    const file = new File(['image'], 'cola.png', { type: 'image/png' });
    component.onFileSelected(file);

    component.submit();

    backend.expectOne('/api/v1/items').flush({
      id: 'i-1',
      name: 'Cola',
      type: 'COLD_BEVERAGE',
      price: 1.5,
      _links: {
        'item:image': { href: '/api/v1/items/i-1/image' },
      },
    });
    await nextTick();
    backend
      .expectOne('/api/v1/items/i-1/image')
      .flush(null, { status: 500, statusText: 'Internal Server Error' });
    await nextTick();
    await fixture.whenStable();

    expect(component.imageError()).toBe('The image could not be uploaded.');
    expect(component.createdItem()?.id).toBe('i-1');
    expect(router.url).not.toBe('/items');

    component.submit();

    await nextTick();
    backend.expectNone('/api/v1/items');
    const retryUpload = backend.expectOne('/api/v1/items/i-1/image');
    expect(retryUpload.request.method).toBe('POST');
    retryUpload.flush({ id: 'i-1' });
    await nextTick();
    await fixture.whenStable();

    expect(router.url).toBe('/items');
  });

  it('shows field-specific validation messages returned by the create API', async () => {
    history.pushState({ createHref: '/api/v1/items' }, '');
    await resolveRootItemsLink();
    fillValidForm();

    component.submit();

    backend.expectOne('/api/v1/items').flush(
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
    await fixture.whenStable();

    expect(component.errorMessage()).toBeNull();
    expect(component.fieldErrors()).toEqual({ price: 'must be greater than 0' });
    expect(component.submitting()).toBe(false);
  });
});
