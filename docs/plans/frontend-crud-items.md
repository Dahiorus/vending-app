# CRUD des items — Plan d'implémentation

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** livrer dans le frontend Angular le CRUD admin complet des items, avec détail, création, édition du prix, suppression et upload d'image.
**Architecture:** La feature reste sous `features/items/` et suit les patrons existants des machines : `httpResource()` pour les lectures, `HttpClient` pour les mutations, liens HAL/HAL-FORMS en priorité et URL construites seulement en repli de navigation directe. Les formulaires utilisent Signal Forms, l'upload réutilise les prérequis partagés du plan 00, et les routes `/items/**` restent back-office avec `adminGuard`.
**Tech Stack:** Angular 22 standalone zoneless, Angular Material, Signal Forms, `httpResource`, `HttpClient`, Vitest via `npm test`, Playwright, Spring HAL/HAL-FORMS.
**Spec:** `docs/specs/frontend-crud-items.md`
**Prérequis:** `docs/plans/00-frontend-prerequis-partages.md` exécuté.

## Global Constraints

- naming 2025 : fichiers `item-create.ts`, `item-detail.ts`, `item-edit.ts`, jamais `*.component.ts`.
- `@Service()` pour les services Angular, jamais `@Injectable({ providedIn: 'root' })` pour un singleton standard.
- `httpResource` reads / `HttpClient` mutations.
- Signal Forms.
- `npx ng generate component <path> --style=none` pour chaque composant Angular créé, car les composants existants n'ont pas de CSS.
- tests ONLY `cd frontend && npm test` (never npx vitest).
- tests ciblés : `cd frontend && npm test -- --include src/app/...spec.ts`; le builder `@angular/build:unit-test` supporte `--include`.
- e2e `npm run e2e`.
- Gradle ONLY via `build-brief ./gradlew ...`.
- branche `feat/frontend-items-crud` depuis `develop` — `develop` does not exist yet on 2026-09-25, only `main`; use `main` then —.
- commit style `feat(frontend): ...` + trailer `Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>`.
- `build-brief ./gradlew clean build` must pass before each commit.
- ff-only merge.
- no backend change.

## Review Focus

- Navigation directe vers `/items/new` sans `history.state.createHref` : le formulaire doit lire `ApiRootApi.link('items')`, puis poster sur ce lien, sans réintroduire `itemsUrl()`.
- Création avec POST réussi puis upload échoué : rester sur le formulaire, mémoriser l'item créé, et le prochain submit ne doit refaire que l'upload.
- Réponse HAL-FORMS de page sans `_templates.default.target` : `createItemHref(page)` doit retomber sur le lien `self`.
- Image absente ou 404 sur détail : afficher le fallback visuel, pas une image cassée.
- Actions de suppression : annulation du dialog ne doit pas envoyer de `DELETE`; confirmation doit utiliser le lien `self` et recharger/naviguer selon la page propriétaire.

---

## File Structure

- `frontend/src/app/features/items/models/item.ts` : modèle `Item`, commandes `ItemToCreate` et `ItemToUpdate`.
- `frontend/src/app/features/items/item-api.ts` : helpers d'URL et de liens HAL (`itemsPageUrl`, `itemUrl`, `itemImageUrl`, `createItemHref`, `itemImageHref`, `itemSelfHref`).
- `frontend/src/app/features/items/item-api.spec.ts` : verrouille pagination, repli HAL-FORMS et liens image/self.
- `frontend/src/app/features/items/item-create/` : composant standalone `/items/new`, formulaire Signal Forms et upload optionnel.
- `frontend/src/app/features/items/item-detail/` : composant standalone `/items/:id`, lecture HAL, image, actions admin et suppression.
- `frontend/src/app/features/items/item-edit/` : composant standalone `/items/:id/edit`, prix modifiable, nom/type en lecture seule, upload optionnel.
- `frontend/src/app/features/items/item-delete-dialog/` : dialog Material de confirmation destructive.
- `frontend/src/app/features/items/item-list/` : liste paginée enrichie avec bouton de création, lignes cliquables et colonne actions.
- `frontend/src/app/app.routes.ts` : routes `items/new`, `items/:id`, `items/:id/edit` avant le wildcard; garder `adminGuard` aussi sur le détail, même si `GET /items/{id}` est `permitAll`, car la feature `items` est back-office.
- `frontend/e2e/manage-items.spec.ts` : parcours admin mocké création → édition image → suppression.
- `frontend/README.md`, `docs/features-front-a-implementer.md` : documentation à mettre à jour sur la branche de feature; ces fichiers ont des modifications non liées sur `main`, ne pas les écraser.

### Interfaces partagées consommées depuis le plan 00

```ts
export function linkHref(resource: HalResource | null | undefined, rel: string): string | undefined;
export function uploadImageFile<T = unknown>(
  http: HttpClient,
  href: string,
  file: File,
): Observable<T>;
export function withCacheBuster(href: string, version: number): string;
export function itemUrl(id: string): string;
export function itemImageUrl(id: string): string;
export function itemsPageUrl(
  itemsHref: string | undefined,
  pageIndex: number,
  pageSize: number,
): string | undefined;
```

`app-image-upload` est fourni par `shared/image-upload/image-upload.ts` avec les inputs `currentImageUrl`, `label`, `fallbackIcon`, `error`, l'output `fileSelected`, et les testids `image-upload-preview`, `image-upload-fallback`, `image-upload-input`.

---

### Task 0: Vérifier les prérequis partagés

**Files:**
- Create: aucun
- Modify: aucun
- Test: aucun

**Interfaces:**
- Consumes: prérequis du plan `docs/plans/00-frontend-prerequis-partages.md`.
- Produces: confirmation locale que les tâches suivantes peuvent utiliser `linkHref`, `uploadImageFile`, `withCacheBuster`, `ImageUpload`, `itemUrl`, `itemImageUrl`, `itemsPageUrl`.

- [ ] **Step 1: Vérifier que la branche est isolée**

Run:

```bash
git switch main
git pull --ff-only
git switch -c feat/frontend-items-crud
```

Expected: la branche `feat/frontend-items-crud` existe depuis `main`, car `develop` n'existe pas encore au 2026-09-25.

- [ ] **Step 2: Vérifier les helpers et composants partagés sans les réimplémenter**

Run:

```bash
grep -R "export function linkHref" -n frontend/src/app/shared/models/hal.ts &&
grep -R "export function uploadImageFile" -n frontend/src/app/shared/http/upload-image-file.ts &&
grep -R "export function withCacheBuster" -n frontend/src/app/shared/http/cache-buster.ts &&
grep -R "selector: 'app-image-upload'" -n frontend/src/app/shared/image-upload/image-upload.ts &&
grep -R "export function itemUrl" -n frontend/src/app/features/items/item-api.ts &&
grep -R "export function itemImageUrl" -n frontend/src/app/features/items/item-api.ts &&
grep -R "export function itemsPageUrl" -n frontend/src/app/features/items/item-api.ts
```

Expected: toutes les commandes réussissent. Si une ligne manque, arrêter l'exécution de ce plan et exécuter d'abord `docs/plans/00-frontend-prerequis-partages.md`; ne pas ajouter ces prérequis dans ce plan.

- [ ] **Step 3: Vérifier l'absence de modifications backend**

Run:

```bash
git status --short backend
```

Expected: aucune sortie.

---

### Task 1: Modèles items et helpers HAL

**Files:**
- Modify: `frontend/src/app/features/items/models/item.ts`
- Modify: `frontend/src/app/features/items/item-api.ts`
- Test: `frontend/src/app/features/items/item-api.spec.ts`

**Interfaces:**
- Consumes:
  - `linkHref(resource: HalResource | null | undefined, rel: string): string | undefined`
  - `pagedUrl(href: string | undefined, params: Record<string, string | number | undefined>): string | undefined`
  - `environment.apiBaseUrl: string`
- Produces:
  - `export interface ItemToCreate { name: string; type: ItemType; price: number }`
  - `export interface ItemToUpdate { price: number }`
  - `export function itemsPageUrl(itemsHref: string | undefined, pageIndex: number, pageSize: number): string | undefined`
  - `export function itemUrl(id: string): string`
  - `export function itemImageUrl(id: string): string`
  - `export function createItemHref(page: HalPage<Item>): string | undefined`
  - `export function itemSelfHref(item: Item | null | undefined): string | undefined`
  - `export function itemImageHref(item: Item | null | undefined): string | undefined`

- [ ] **Step 1: Write the failing test**

Replace `frontend/src/app/features/items/item-api.spec.ts` with:

```ts
import { describe, expect, it } from 'vitest';
import { createItemHref, itemImageHref, itemsPageUrl, itemSelfHref } from './item-api';
import { Item } from './models/item';

describe('itemsPageUrl', () => {
  it('builds the paged URL from the resolved items href, converting the 0-based pageIndex to the backend’s 1-based page param', () => {
    expect(itemsPageUrl('/api/v1/items', 2, 10)).toBe('/api/v1/items?page=3&size=10');
  });

  it('returns undefined while the items href is not resolved yet', () => {
    expect(itemsPageUrl(undefined, 0, 10)).toBeUndefined();
  });

  it('preserves query params already present on the href (e.g. advanced search filters)', () => {
    expect(itemsPageUrl('/api/v1/items?type=SNACK', 2, 10)).toBe(
      '/api/v1/items?type=SNACK&page=3&size=10',
    );
  });
});

describe('item HAL helpers', () => {
  it('reads the create target from the HAL-FORMS default template when Spring exposes one', () => {
    expect(
      createItemHref({
        _links: { self: { href: '/api/v1/items?page=1&size=20' } },
        _templates: {
          default: {
            method: 'post',
            target: '/api/v1/items',
          },
        },
        page: { size: 20, totalElements: 0, totalPages: 0, number: 0 },
      }),
    ).toBe('/api/v1/items');
  });

  it('falls back to the page self link when Spring omits the HAL-FORMS target because it equals self', () => {
    expect(
      createItemHref({
        _links: { self: { href: '/api/v1/items' } },
        _templates: {
          default: {
            method: 'post',
          },
        },
        page: { size: 20, totalElements: 0, totalPages: 0, number: 0 },
      }),
    ).toBe('/api/v1/items');
  });

  it('returns undefined when the page has no create template and no self link yet', () => {
    expect(
      createItemHref({
        page: { size: 20, totalElements: 0, totalPages: 0, number: 0 },
      }),
    ).toBeUndefined();
  });

  it('reads self and image links from an item resource', () => {
    const item: Item = {
      id: 'i-1',
      name: 'Cola',
      type: 'COLD_BEVERAGE',
      price: 1.5,
      _links: {
        self: { href: '/api/v1/items/i-1' },
        'item:image': { href: '/api/v1/items/i-1/image' },
      },
    };

    expect(itemSelfHref(item)).toBe('/api/v1/items/i-1');
    expect(itemImageHref(item)).toBe('/api/v1/items/i-1/image');
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd frontend && npm test -- --include src/app/features/items/item-api.spec.ts`

Expected: FAIL with TypeScript errors that `createItemHref`, `itemSelfHref`, `itemImageHref`, `itemUrl`, `itemImageUrl`, `ItemToCreate`, or `ItemToUpdate` are missing, depending on the current state after Task 0.

- [ ] **Step 3: Write minimal implementation**

Replace `frontend/src/app/features/items/models/item.ts` with:

```ts
import { HalResource } from '../../../shared/models/hal';
import { ItemType } from '../../../shared/models/item-type';

export interface Item extends HalResource {
  id: string;
  name: string | null;
  type: ItemType | null;
  price: number | null;
}

export interface ItemToCreate {
  name: string;
  type: ItemType;
  price: number;
}

export interface ItemToUpdate {
  price: number;
}
```

Replace `frontend/src/app/features/items/item-api.ts` with:

```ts
import { environment } from '../../../environments/environment';
import { pagedUrl } from '../../shared/http/paged-url';
import { HalPage, linkHref } from '../../shared/models/hal';
import { Item } from './models/item';

/**
 * `undefined` until the `items` root link is resolved (see `ApiRootApi`).
 */
export function itemsPageUrl(
  itemsHref: string | undefined,
  pageIndex: number,
  pageSize: number,
): string | undefined {
  return pagedUrl(itemsHref, { page: pageIndex + 1, size: pageSize });
}

/**
 * Fallback for direct navigation to `/items/:id`, where no HATEOAS `self` link
 * can be carried in router state from the listing.
 */
export function itemUrl(id: string): string {
  return `${environment.apiBaseUrl}/items/${id}`;
}

/**
 * Fallback for a freshly created item response that unexpectedly lacks the
 * `item:image` link.
 */
export function itemImageUrl(id: string): string {
  return `${environment.apiBaseUrl}/items/${id}/image`;
}

/**
 * Spring HAL-FORMS omits `_templates.default.target` when the affordance target
 * equals the page `self` link, so callers must accept both shapes.
 */
export function createItemHref(page: HalPage<Item>): string | undefined {
  return page._templates?.['default']?.target ?? linkHref(page, 'self');
}

export function itemSelfHref(item: Item | null | undefined): string | undefined {
  return linkHref(item, 'self');
}

export function itemImageHref(item: Item | null | undefined): string | undefined {
  return linkHref(item, 'item:image');
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd frontend && npm test -- --include src/app/features/items/item-api.spec.ts`

Expected: PASS.

- [ ] **Step 5: Commit**

Run:

```bash
build-brief ./gradlew clean build
git add frontend/src/app/features/items/models/item.ts frontend/src/app/features/items/item-api.ts frontend/src/app/features/items/item-api.spec.ts
git commit -m "feat(frontend): add item HAL helpers

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

Expected: Gradle aggregate build passes before the commit.

---

### Task 2: Création d'item avec upload optionnel

**Files:**
- Create: `frontend/src/app/features/items/item-create/item-create.ts`
- Create: `frontend/src/app/features/items/item-create/item-create.html`
- Test: `frontend/src/app/features/items/item-create/item-create.spec.ts`

**Interfaces:**
- Consumes:
  - `ApiRootApi.link(rel: string): string | undefined`
  - `uploadImageFile<T = unknown>(http: HttpClient, href: string, file: File): Observable<T>`
  - `itemImageUrl(id: string): string`
  - `itemImageHref(item: Item | null | undefined): string | undefined`
  - `ItemToCreate`
  - `ITEM_TYPES: readonly ItemType[]`
  - `parseValidationErrors(error: unknown, formFields: readonly string[]): ParsedValidationErrors | null`
  - `ImageUpload` standalone component
- Produces:
  - `export class ItemCreate`
  - route target component for `/items/new`
  - method `onFileSelected(file: File | null): void`
  - method `submit(): void`

- [ ] **Step 1: Generate the component skeleton**

Run:

```bash
cd frontend
npx ng generate component features/items/item-create --style=none
```

Expected: Angular CLI creates `item-create.ts`, `item-create.html`, and `item-create.spec.ts` using naming 2025.

- [ ] **Step 2: Write the failing test**

Replace `frontend/src/app/features/items/item-create/item-create.spec.ts` with:

```ts
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

  function resolveRootItemsLink(): void {
    backend.expectOne('/api/v1').flush({ _links: { items: { href: '/api/v1/items' } } });
    fixture.detectChanges();
  }

  function fillValidForm(): void {
    component.itemForm.name().value.set('Cola');
    component.itemForm.type().value.set('COLD_BEVERAGE');
    component.itemForm.price().value.set(1.5);
  }

  it('rejects an empty form without calling the API and marks fields as touched', () => {
    component.submit();

    backend.expectNone('/api/v1/items');
    expect(component.itemForm().valid()).toBe(false);
    expect(component.itemForm.name().touched()).toBe(true);
    expect(component.itemForm.name().errors().length).toBeGreaterThan(0);
  });

  it('uses the createHref navigation state when present and navigates back to items without image upload', async () => {
    history.pushState({ createHref: 'https://api.example.test/items' }, '');
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
    resolveRootItemsLink();

    component.submit();

    const request = backend.expectOne('/api/v1/items');
    expect(request.request.method).toBe('POST');
    request.flush({ id: 'i-1', name: 'Cola', type: 'COLD_BEVERAGE', price: 1.5, _links: {} });
    await fixture.whenStable();

    expect(router.url).toBe('/items');
  });

  it('uploads the selected image after the item is created', async () => {
    history.pushState({ createHref: '/api/v1/items' }, '');
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

    const upload = backend.expectOne('/api/v1/items/i-1/image');
    expect(upload.request.method).toBe('POST');
    expect(upload.request.body instanceof FormData).toBe(true);
    expect(upload.request.body.get('file')).toBe(file);
    expect(upload.request.headers.has('Content-Type')).toBe(false);
    upload.flush({ id: 'i-1' });

    await fixture.whenStable();

    expect(router.url).toBe('/items');
  });

  it('keeps the created item and retries only the image upload after a partial failure', async () => {
    history.pushState({ createHref: '/api/v1/items' }, '');
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
    backend
      .expectOne('/api/v1/items/i-1/image')
      .flush(null, { status: 500, statusText: 'Internal Server Error' });
    await fixture.whenStable();

    expect(component.imageError()).toBe('The image could not be uploaded.');
    expect(component.createdItem()?.id).toBe('i-1');
    expect(router.url).not.toBe('/items');

    component.submit();

    backend.expectNone('/api/v1/items');
    const retryUpload = backend.expectOne('/api/v1/items/i-1/image');
    expect(retryUpload.request.method).toBe('POST');
    retryUpload.flush({ id: 'i-1' });
    await fixture.whenStable();

    expect(router.url).toBe('/items');
  });

  it('shows field-specific validation messages returned by the create API', async () => {
    history.pushState({ createHref: '/api/v1/items' }, '');
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
```

- [ ] **Step 3: Run test to verify it fails**

Run: `cd frontend && npm test -- --include src/app/features/items/item-create/item-create.spec.ts`

Expected: FAIL because `ItemCreate` has only the generated skeleton or missing members such as `itemForm`, `submit`, `onFileSelected`, `imageError`, and `createdItem`.

- [ ] **Step 4: Write minimal implementation**

Replace `frontend/src/app/features/items/item-create/item-create.ts` with:

```ts
import { HttpClient } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { form, FormField, min, required, submit } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { ApiRootApi } from '../../../core/http/api-root-api';
import { uploadImageFile } from '../../../shared/http/upload-image-file';
import { ImageUpload } from '../../../shared/image-upload/image-upload';
import { ITEM_TYPES, ItemType } from '../../../shared/models/item-type';
import { parseValidationErrors } from '../../../shared/models/validation-error';
import { itemImageHref, itemImageUrl } from '../item-api';
import { Item, ItemToCreate } from '../models/item';

const FORM_FIELDS = ['name', 'type', 'price'];

interface CreateNavigationState {
  createHref?: string;
}

interface ItemCreateForm {
  name: string;
  type: ItemType | '';
  price: number;
}

@Component({
  selector: 'app-item-create',
  imports: [
    FormField,
    ImageUpload,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
  ],
  templateUrl: './item-create.html',
})
export class ItemCreate {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly apiRoot = inject(ApiRootApi);

  protected readonly itemTypes = ITEM_TYPES;

  readonly item = signal<ItemCreateForm>({
    name: '',
    type: '',
    price: 0,
  });

  readonly itemForm = form(this.item, (path) => {
    required(path.name);
    required(path.type);
    min(path.price, 0.01);
  });

  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly imageError = signal<string | null>(null);
  readonly fieldErrors = signal<Record<string, string>>({});
  readonly selectedImage = signal<File | null>(null);
  readonly createdItem = signal<Item | null>(null);

  private get createHref(): string | undefined {
    return (history.state as CreateNavigationState | null)?.createHref ?? this.apiRoot.link('items');
  }

  onFileSelected(file: File | null): void {
    this.selectedImage.set(file);
    this.imageError.set(null);
  }

  submit(): void {
    void submit(this.itemForm, async () => {
      this.submitting.set(true);
      this.errorMessage.set(null);
      this.imageError.set(null);
      this.fieldErrors.set({});

      try {
        const existingItem = this.createdItem();
        if (existingItem) {
          await this.uploadSelectedImage(existingItem);
          this.submitting.set(false);
          void this.router.navigate(['/items']);
          return;
        }

        const href = this.createHref;
        if (!href) {
          this.errorMessage.set('Creation is not available yet. Please try again.');
          this.submitting.set(false);
          return;
        }

        const created = await firstValueFrom(this.http.post<Item>(href, this.payload()));
        this.createdItem.set(created);
        await this.uploadSelectedImage(created);
        this.submitting.set(false);
        void this.router.navigate(['/items']);
      } catch (error) {
        this.submitting.set(false);

        const parsed = parseValidationErrors(error, FORM_FIELDS);
        if (parsed) {
          this.fieldErrors.set(parsed.fieldErrors);
          this.errorMessage.set(parsed.hasObjectLevelError ? 'The item could not be created.' : null);
          return;
        }

        if (this.createdItem()) {
          this.imageError.set('The image could not be uploaded.');
          return;
        }

        this.errorMessage.set('Creation failed. Please try again.');
      }
    });
  }

  private payload(): ItemToCreate {
    const value = this.item();
    return {
      name: value.name,
      type: value.type as ItemType,
      price: value.price,
    };
  }

  private async uploadSelectedImage(item: Item): Promise<void> {
    const file = this.selectedImage();
    if (!file) {
      return;
    }

    const href = itemImageHref(item) ?? itemImageUrl(item.id);
    await firstValueFrom(uploadImageFile<Item>(this.http, href, file));
  }
}
```

Replace `frontend/src/app/features/items/item-create/item-create.html` with:

```html
<div class="flex min-h-screen items-center justify-center p-4">
  <mat-card class="w-full max-w-md">
    <mat-card-header>
      <mat-card-title>New item</mat-card-title>
    </mat-card-header>
    <mat-card-content>
      <form class="flex flex-col gap-4 pt-4" (submit)="$event.preventDefault(); submit()">
        <mat-form-field>
          <mat-label>Name</mat-label>
          <input matInput type="text" [formField]="itemForm.name" />
        </mat-form-field>
        @if (itemForm.name().touched() && itemForm.name().errors().length) {
          <p class="text-sm text-red-600" role="alert">Name is required.</p>
        }
        @if (fieldErrors()['name']; as message) {
          <p class="text-sm text-red-600" role="alert">{{ message }}</p>
        }

        <mat-form-field>
          <mat-label>Type</mat-label>
          <mat-select [formField]="itemForm.type">
            @for (itemType of itemTypes; track itemType) {
              <mat-option [value]="itemType">{{ itemType }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
        @if (itemForm.type().touched() && itemForm.type().errors().length) {
          <p class="text-sm text-red-600" role="alert">Type is required.</p>
        }
        @if (fieldErrors()['type']; as message) {
          <p class="text-sm text-red-600" role="alert">{{ message }}</p>
        }

        <mat-form-field>
          <mat-label>Price</mat-label>
          <input matInput type="number" [formField]="itemForm.price" />
        </mat-form-field>
        @if (itemForm.price().touched() && itemForm.price().errors().length) {
          <p class="text-sm text-red-600" role="alert">Price must be greater than 0.</p>
        }
        @if (fieldErrors()['price']; as message) {
          <p class="text-sm text-red-600" role="alert">{{ message }}</p>
        }

        <app-image-upload
          label="Item image"
          fallbackIcon="image"
          [error]="imageError()"
          (fileSelected)="onFileSelected($event)"
        />

        @if (createdItem() && imageError()) {
          <p class="text-sm text-amber-700" role="status">
            The item was created. Retry the image upload or leave the page from the items list.
          </p>
        }

        @if (errorMessage(); as message) {
          <p class="text-sm text-red-600" role="alert">{{ message }}</p>
        }

        <button mat-flat-button type="submit" [disabled]="submitting() || !itemForm().valid()">
          @if (submitting()) {
            <mat-progress-spinner diameter="16" mode="indeterminate" class="mr-2 inline-block align-middle" />
          }
          {{ createdItem() ? 'Retry image upload' : 'Create item' }}
        </button>
      </form>
    </mat-card-content>
  </mat-card>
</div>
```

- [ ] **Step 5: Run test to verify it passes**

Run: `cd frontend && npm test -- --include src/app/features/items/item-create/item-create.spec.ts`

Expected: PASS.

- [ ] **Step 6: Commit**

Run:

```bash
build-brief ./gradlew clean build
git add frontend/src/app/features/items/item-create/item-create.ts frontend/src/app/features/items/item-create/item-create.html frontend/src/app/features/items/item-create/item-create.spec.ts
git commit -m "feat(frontend): add item creation form

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

Expected: Gradle aggregate build passes before the commit.

---

### Task 3: Détail d'item et dialog de suppression

**Files:**
- Create: `frontend/src/app/features/items/item-delete-dialog/item-delete-dialog.ts`
- Create: `frontend/src/app/features/items/item-delete-dialog/item-delete-dialog.html`
- Test: `frontend/src/app/features/items/item-delete-dialog/item-delete-dialog.spec.ts`
- Create: `frontend/src/app/features/items/item-detail/item-detail.ts`
- Create: `frontend/src/app/features/items/item-detail/item-detail.html`
- Test: `frontend/src/app/features/items/item-detail/item-detail.spec.ts`

**Interfaces:**
- Consumes:
  - `itemUrl(id: string): string`
  - `itemImageHref(item: Item | null | undefined): string | undefined`
  - `itemSelfHref(item: Item | null | undefined): string | undefined`
  - `itemImageUrl(id: string): string`
  - `withCacheBuster(href: string, version: number): string`
  - `AuthService.isAdmin: Signal<boolean>`
- Produces:
  - `export interface ItemDeleteDialogData { itemName: string }`
  - `export class ItemDeleteDialog`
  - `export class ItemDetail`
  - methods `imageFailed(): void`, `deleteItem(): void`

- [ ] **Step 1: Generate the component skeletons**

Run:

```bash
cd frontend
npx ng generate component features/items/item-delete-dialog --style=none
npx ng generate component features/items/item-detail --style=none
```

Expected: Angular CLI creates both standalone components.

- [ ] **Step 2: Write the failing tests**

Replace `frontend/src/app/features/items/item-delete-dialog/item-delete-dialog.spec.ts` with:

```ts
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ItemDeleteDialog } from './item-delete-dialog';

describe('ItemDeleteDialog', () => {
  let fixture: ComponentFixture<ItemDeleteDialog>;
  let dialogRef: { close: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    dialogRef = { close: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [ItemDeleteDialog],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: { itemName: 'Cola' } },
        { provide: MatDialogRef, useValue: dialogRef },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ItemDeleteDialog);
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('renders the injected item name', () => {
    expect(fixture.nativeElement.textContent).toContain('Delete Cola?');
  });

  it('closes the dialog when clicking cancel', () => {
    const cancelButton: HTMLButtonElement = fixture.nativeElement.querySelector(
      '[data-testid="item-delete-dialog-cancel"]',
    );

    cancelButton.click();

    expect(dialogRef.close).toHaveBeenCalledWith(false);
  });

  it('confirms the deletion when clicking delete', () => {
    const confirmButton: HTMLButtonElement = fixture.nativeElement.querySelector(
      '[data-testid="item-delete-dialog-confirm"]',
    );

    confirmButton.click();

    expect(dialogRef.close).toHaveBeenCalledWith(true);
  });
});
```

Replace `frontend/src/app/features/items/item-detail/item-detail.spec.ts` with:

```ts
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { TokenStore } from '../../../core/auth/token-store';
import { ItemDeleteDialog } from '../item-delete-dialog/item-delete-dialog';
import { ItemDetail } from './item-detail';

function fakeJwt(payload: Record<string, unknown>): string {
  const encode = (value: unknown) =>
    btoa(JSON.stringify(value)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${encode({ alg: 'none' })}.${encode(payload)}.signature`;
}

describe('ItemDetail', () => {
  let harness: RouterTestingHarness;
  let backend: HttpTestingController;
  let dialog: { open: ReturnType<typeof vi.fn> };
  let snackBar: { open: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    dialog = { open: vi.fn(() => ({ afterClosed: () => of(undefined) })) };
    snackBar = { open: vi.fn() };

    await TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([
          { path: 'items', children: [] },
          { path: 'items/:id', component: ItemDetail },
        ]),
        { provide: MatDialog, useValue: dialog },
        { provide: MatSnackBar, useValue: snackBar },
      ],
    }).compileComponents();

    backend = TestBed.inject(HttpTestingController);
    harness = await RouterTestingHarness.create();
  });

  afterEach(() => backend.verify());

  async function navigate(state: unknown = null) {
    history.replaceState(state, '');
    const component = await harness.navigateByUrl('/items/i-1', ItemDetail);
    harness.fixture.detectChanges();
    return component;
  }

  function flushItem(): void {
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
  }

  function authenticateAdmin(): void {
    TestBed.inject(TokenStore).setAccessToken(
      fakeJwt({ sub: 'admin@vending.me', roles: ['ROLE_ADMIN'], exp: 4102444800 }),
    );
  }

  it('falls back to the id-based URL when no HATEOAS state is available', async () => {
    const component = await navigate();

    flushItem();
    await harness.fixture.whenStable();

    expect(component.item()?.name).toBe('Cola');
  });

  it('follows the HATEOAS self link carried over via router navigation state', async () => {
    const component = await navigate({ href: 'https://api.example.test/items/i-1' });

    backend.expectOne('https://api.example.test/items/i-1').flush({
      id: 'i-1',
      name: 'Cola',
      type: 'COLD_BEVERAGE',
      price: 1.5,
      _links: {},
    });
    await harness.fixture.whenStable();

    expect(component.item()?.name).toBe('Cola');
  });

  it('builds the image URL from the item:image link and switches to fallback on image error', async () => {
    const component = await navigate();
    flushItem();
    await harness.fixture.whenStable();

    expect(component.imageUrl()).toBe('/api/v1/items/i-1/image');

    component.imageFailed();

    expect(component.showImageFallback()).toBe(true);
  });

  it('does not render admin actions for a non-admin token', async () => {
    await navigate();
    flushItem();
    await harness.fixture.whenStable();
    harness.fixture.detectChanges();

    expect(harness.fixture.nativeElement.textContent).not.toContain('Edit');
    expect(harness.fixture.nativeElement.textContent).not.toContain('Delete');
  });

  it('opens the delete dialog and does not delete when cancelled', async () => {
    authenticateAdmin();
    dialog.open.mockReturnValue({ afterClosed: () => of(false) });
    const component = await navigate();
    flushItem();
    await harness.fixture.whenStable();

    component.deleteItem();

    expect(dialog.open).toHaveBeenCalledWith(ItemDeleteDialog, {
      data: { itemName: 'Cola' },
    });
    backend.expectNone('/api/v1/items/i-1');
  });

  it('deletes through the self link and navigates back to the list when confirmed', async () => {
    authenticateAdmin();
    dialog.open.mockReturnValue({ afterClosed: () => of(true) });
    const component = await navigate();
    flushItem();
    await harness.fixture.whenStable();

    component.deleteItem();

    const request = backend.expectOne('/api/v1/items/i-1');
    expect(request.request.method).toBe('DELETE');
    request.flush(null, { status: 204, statusText: 'No Content' });
    await harness.fixture.whenStable();

    expect(snackBar.open).toHaveBeenCalledWith('Deleted Cola', 'Close', { duration: 5000 });
    expect(harness.router.url).toBe('/items');
  });
});
```

- [ ] **Step 3: Run test to verify it fails**

Run: `cd frontend && npm test -- --include src/app/features/items/item-delete-dialog/item-delete-dialog.spec.ts --include src/app/features/items/item-detail/item-detail.spec.ts`

Expected: FAIL because `ItemDeleteDialogData`, dialog testids, `ItemDetail.item`, `imageUrl`, `showImageFallback`, `deleteItem`, and delete behavior are missing.

- [ ] **Step 4: Write minimal implementation**

Replace `frontend/src/app/features/items/item-delete-dialog/item-delete-dialog.ts` with:

```ts
import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';

export interface ItemDeleteDialogData {
  itemName: string;
}

@Component({
  imports: [MatButtonModule, MatDialogModule],
  selector: 'app-item-delete-dialog',
  templateUrl: './item-delete-dialog.html',
})
export class ItemDeleteDialog {
  readonly data = inject<ItemDeleteDialogData>(MAT_DIALOG_DATA);
  readonly dialogRef = inject(MatDialogRef<ItemDeleteDialog, boolean>);
}
```

Replace `frontend/src/app/features/items/item-delete-dialog/item-delete-dialog.html` with:

```html
<h2 mat-dialog-title>Confirm deletion</h2>

<mat-dialog-content>
  <p>Delete {{ data.itemName }}?</p>
</mat-dialog-content>

<mat-dialog-actions align="end">
  <button
    mat-button
    type="button"
    data-testid="item-delete-dialog-cancel"
    (click)="dialogRef.close(false)"
  >
    Cancel
  </button>
  <button
    mat-flat-button
    type="button"
    color="warn"
    data-testid="item-delete-dialog-confirm"
    (click)="dialogRef.close(true)"
  >
    Delete
  </button>
</mat-dialog-actions>
```

Replace `frontend/src/app/features/items/item-detail/item-detail.ts` with:

```ts
import { HttpClient } from '@angular/common/http';
import { httpResource } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth';
import { withCacheBuster } from '../../../shared/http/cache-buster';
import { ValueOrEmptyPipe } from '../../../shared/value-or-empty-pipe';
import { itemImageHref, itemImageUrl, itemSelfHref, itemUrl } from '../item-api';
import {
  ItemDeleteDialog,
  ItemDeleteDialogData,
} from '../item-delete-dialog/item-delete-dialog';
import { Item } from '../models/item';

interface DetailNavigationState {
  href?: string;
}

@Component({
  selector: 'app-item-detail',
  imports: [
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressBarModule,
    RouterLink,
    ValueOrEmptyPipe,
  ],
  templateUrl: './item-detail.html',
})
export class ItemDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly http = inject(HttpClient);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly auth = inject(AuthService);

  readonly isAdmin = this.auth.isAdmin;
  readonly showImageFallback = signal(false);
  readonly imageVersion = signal(0);

  private readonly resourceUrl =
    (history.state as DetailNavigationState | null)?.href ??
    itemUrl(this.route.snapshot.paramMap.get('id')!);

  private readonly resource = httpResource<Item>(() => this.resourceUrl);

  readonly isLoading = this.resource.isLoading;
  readonly hasError = computed(() => this.resource.error() !== undefined);
  readonly item = computed(() => this.resource.value());
  readonly imageUrl = computed(() => {
    const href =
      itemImageHref(this.item()) ??
      itemImageUrl(this.route.snapshot.paramMap.get('id')!);
    return withCacheBuster(href, this.imageVersion());
  });

  imageFailed(): void {
    this.showImageFallback.set(true);
  }

  deleteItem(): void {
    const item = this.item();
    if (!item) {
      return;
    }

    this.dialog
      .open(ItemDeleteDialog, {
        data: { itemName: item.name ?? 'this item' } satisfies ItemDeleteDialogData,
      })
      .afterClosed()
      .subscribe((confirmed) => {
        if (confirmed !== true) {
          return;
        }

        this.http.delete(itemSelfHref(item) ?? itemUrl(item.id)).subscribe({
          next: () => {
            this.snackBar.open(`Deleted ${item.name ?? 'item'}`, 'Close', { duration: 5000 });
            void this.router.navigate(['/items']);
          },
          error: () => {
            this.snackBar.open('The item could not be deleted.', 'Close', { duration: 5000 });
          },
        });
      });
  }
}
```

Replace `frontend/src/app/features/items/item-detail/item-detail.html` with:

```html
<div class="mb-4 flex items-center justify-between">
  <h1 class="text-xl font-semibold">Item details</h1>
  <a mat-button routerLink="/items">Back to list</a>
</div>

@if (isLoading()) {
  <mat-progress-bar mode="indeterminate" />
}

@if (hasError()) {
  <p class="text-red-600" role="alert">The item could not be loaded.</p>
} @else if (item(); as item) {
  <mat-card>
    <mat-card-content>
      <div class="mb-4 flex items-start justify-between gap-4">
        <div class="flex items-center gap-4">
          @if (!showImageFallback() && imageUrl(); as src) {
            <img
              class="h-24 w-24 rounded object-cover"
              [src]="src"
              [alt]="item.name ?? 'Item image'"
              (error)="imageFailed()"
            />
          } @else {
            <div class="flex h-24 w-24 items-center justify-center rounded bg-gray-100 text-gray-500">
              <mat-icon>image</mat-icon>
            </div>
          }

          <dl class="grid grid-cols-2 gap-x-4 gap-y-2">
            <dt class="font-medium">Name</dt>
            <dd>{{ item.name | valueOrEmpty }}</dd>

            <dt class="font-medium">Type</dt>
            <dd>{{ item.type | valueOrEmpty }}</dd>

            <dt class="font-medium">Price</dt>
            <dd>{{ item.price | valueOrEmpty }}</dd>
          </dl>
        </div>

        @if (isAdmin()) {
          <div class="flex gap-2">
            <a
              mat-stroked-button
              [routerLink]="['/items', item.id, 'edit']"
              [state]="{ href: item._links?.self?.href }"
            >
              Edit
            </a>
            <button mat-flat-button color="warn" type="button" (click)="deleteItem()">Delete</button>
          </div>
        }
      </div>
    </mat-card-content>
  </mat-card>
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `cd frontend && npm test -- --include src/app/features/items/item-delete-dialog/item-delete-dialog.spec.ts --include src/app/features/items/item-detail/item-detail.spec.ts`

Expected: PASS.

- [ ] **Step 6: Commit**

Run:

```bash
build-brief ./gradlew clean build
git add frontend/src/app/features/items/item-delete-dialog frontend/src/app/features/items/item-detail
git commit -m "feat(frontend): add item detail and delete dialog

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

Expected: Gradle aggregate build passes before the commit.

---

### Task 4: Édition du prix et upload d'image

**Files:**
- Create: `frontend/src/app/features/items/item-edit/item-edit.ts`
- Create: `frontend/src/app/features/items/item-edit/item-edit.html`
- Test: `frontend/src/app/features/items/item-edit/item-edit.spec.ts`

**Interfaces:**
- Consumes:
  - `itemUrl(id: string): string`
  - `itemImageHref(item: Item | null | undefined): string | undefined`
  - `itemImageUrl(id: string): string`
  - `itemSelfHref(item: Item | null | undefined): string | undefined`
  - `uploadImageFile<T = unknown>(http: HttpClient, href: string, file: File): Observable<T>`
  - `withCacheBuster(href: string, version: number): string`
  - `parseValidationErrors(error: unknown, formFields: readonly string[]): ParsedValidationErrors | null`
  - `ImageUpload`
- Produces:
  - `export class ItemEdit`
  - methods `onFileSelected(file: File | null): void`, `submit(): void`

- [ ] **Step 1: Generate the component skeleton**

Run:

```bash
cd frontend
npx ng generate component features/items/item-edit --style=none
```

Expected: Angular CLI creates `item-edit.ts`, `item-edit.html`, and `item-edit.spec.ts`.

- [ ] **Step 2: Write the failing test**

Replace `frontend/src/app/features/items/item-edit/item-edit.spec.ts` with:

```ts
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { ItemEdit } from './item-edit';

describe('ItemEdit', () => {
  let harness: RouterTestingHarness;
  let backend: HttpTestingController;

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
  });

  afterEach(() => backend.verify());

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

    expect(harness.router.url).toBe('/items/i-1');
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

    const upload = backend.expectOne('/api/v1/items/i-1/image');
    expect(upload.request.method).toBe('POST');
    expect(upload.request.body instanceof FormData).toBe(true);
    expect(upload.request.body.get('file')).toBe(file);
    upload.flush({ id: 'i-1' });
    await harness.fixture.whenStable();

    expect(harness.router.url).toBe('/items/i-1');
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
    backend
      .expectOne('/api/v1/items/i-1/image')
      .flush(null, { status: 500, statusText: 'Internal Server Error' });
    await harness.fixture.whenStable();

    expect(component.imageError()).toBe('The image could not be uploaded.');
    expect(harness.router.url).toBe('/items/i-1/edit');
  });

  it('shows field-specific validation messages returned by the update API', async () => {
    const component = await navigate();
    component.itemForm.price().value.set(-1);

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
```

- [ ] **Step 3: Run test to verify it fails**

Run: `cd frontend && npm test -- --include src/app/features/items/item-edit/item-edit.spec.ts`

Expected: FAIL because `ItemEdit` lacks `item`, `itemForm`, `currentImageUrl`, `onFileSelected`, `submit`, and upload handling.

- [ ] **Step 4: Write minimal implementation**

Replace `frontend/src/app/features/items/item-edit/item-edit.ts` with:

```ts
import { HttpClient, httpResource } from '@angular/common/http';
import { Component, computed, effect, inject, signal } from '@angular/core';
import { form, FormField, min, submit } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { withCacheBuster } from '../../../shared/http/cache-buster';
import { uploadImageFile } from '../../../shared/http/upload-image-file';
import { ImageUpload } from '../../../shared/image-upload/image-upload';
import { parseValidationErrors } from '../../../shared/models/validation-error';
import { ValueOrEmptyPipe } from '../../../shared/value-or-empty-pipe';
import { itemImageHref, itemImageUrl, itemSelfHref, itemUrl } from '../item-api';
import { Item, ItemToUpdate } from '../models/item';

const FORM_FIELDS = ['price'];

interface EditNavigationState {
  href?: string;
}

@Component({
  selector: 'app-item-edit',
  imports: [
    FormField,
    ImageUpload,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    RouterLink,
    ValueOrEmptyPipe,
  ],
  templateUrl: './item-edit.html',
})
export class ItemEdit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly http = inject(HttpClient);

  private readonly itemId = this.route.snapshot.paramMap.get('id')!;
  private readonly resourceUrl =
    (history.state as EditNavigationState | null)?.href ?? itemUrl(this.itemId);
  private readonly resource = httpResource<Item>(() => this.resourceUrl);

  readonly item = computed(() => this.resource.value());
  readonly isLoading = this.resource.isLoading;
  readonly hasError = computed(() => this.resource.error() !== undefined);
  readonly imageVersion = signal(0);
  readonly currentImageUrl = computed(() => {
    const href = itemImageHref(this.item()) ?? itemImageUrl(this.itemId);
    return withCacheBuster(href, this.imageVersion());
  });

  readonly itemPatch = signal<ItemToUpdate>({ price: 0 });
  readonly itemForm = form(this.itemPatch, (path) => {
    min(path.price, 0.01);
  });

  readonly initialized = signal(false);
  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly imageError = signal<string | null>(null);
  readonly fieldErrors = signal<Record<string, string>>({});
  readonly selectedImage = signal<File | null>(null);

  private readonly syncItemToForm = effect(() => {
    const item = this.item();
    if (!item || this.initialized()) {
      return;
    }

    this.itemPatch.set({ price: item.price ?? 0 });
    this.initialized.set(true);
  });

  onFileSelected(file: File | null): void {
    this.selectedImage.set(file);
    this.imageError.set(null);
  }

  submit(): void {
    void submit(this.itemForm, async () => {
      this.submitting.set(true);
      this.errorMessage.set(null);
      this.imageError.set(null);
      this.fieldErrors.set({});

      try {
        const updated = await firstValueFrom(
          this.http.put<Item>(this.resourceUrl, { price: this.itemPatch().price }),
        );
        await this.uploadSelectedImage(updated);
        this.submitting.set(false);
        void this.router.navigate(['/items', updated.id], {
          state: { href: itemSelfHref(updated) ?? this.resourceUrl },
        });
      } catch (error) {
        this.submitting.set(false);

        const parsed = parseValidationErrors(error, FORM_FIELDS);
        if (parsed) {
          this.fieldErrors.set(parsed.fieldErrors);
          this.errorMessage.set(parsed.hasObjectLevelError ? 'The item could not be updated.' : null);
          return;
        }

        if (this.selectedImage()) {
          this.imageError.set('The image could not be uploaded.');
          return;
        }

        this.errorMessage.set('Update failed. Please try again.');
      }
    });
  }

  private async uploadSelectedImage(item: Item): Promise<void> {
    const file = this.selectedImage();
    if (!file) {
      return;
    }

    await firstValueFrom(
      uploadImageFile<Item>(this.http, itemImageHref(item) ?? itemImageUrl(item.id), file),
    );
    this.imageVersion.update((version) => version + 1);
  }
}
```

Replace `frontend/src/app/features/items/item-edit/item-edit.html` with:

```html
<div class="mb-4 flex items-center justify-between">
  <h1 class="text-xl font-semibold">Edit item</h1>
  <a mat-button [routerLink]="['/items', item()?.id]">Back to item</a>
</div>

@if (isLoading()) {
  <mat-progress-bar mode="indeterminate" />
}

@if (hasError()) {
  <p class="text-red-600" role="alert">The item could not be loaded.</p>
} @else if (item(); as item) {
  <mat-card class="w-full max-w-md">
    <mat-card-content>
      <form class="flex flex-col gap-4 pt-4" (submit)="$event.preventDefault(); submit()">
        <dl class="grid grid-cols-2 gap-x-4 gap-y-2">
          <dt class="font-medium">Name</dt>
          <dd>{{ item.name | valueOrEmpty }}</dd>

          <dt class="font-medium">Type</dt>
          <dd>{{ item.type | valueOrEmpty }}</dd>
        </dl>

        <mat-form-field>
          <mat-label>Price</mat-label>
          <input matInput type="number" [formField]="itemForm.price" />
        </mat-form-field>
        @if (itemForm.price().touched() && itemForm.price().errors().length) {
          <p class="text-sm text-red-600" role="alert">Price must be greater than 0.</p>
        }
        @if (fieldErrors()['price']; as message) {
          <p class="text-sm text-red-600" role="alert">{{ message }}</p>
        }

        <app-image-upload
          label="Item image"
          fallbackIcon="image"
          [currentImageUrl]="currentImageUrl()"
          [error]="imageError()"
          (fileSelected)="onFileSelected($event)"
        />

        @if (errorMessage(); as message) {
          <p class="text-sm text-red-600" role="alert">{{ message }}</p>
        }

        <button mat-flat-button type="submit" [disabled]="submitting() || !itemForm().valid()">
          @if (submitting()) {
            <mat-progress-spinner diameter="16" mode="indeterminate" class="mr-2 inline-block align-middle" />
          }
          Save item
        </button>
      </form>
    </mat-card-content>
  </mat-card>
}
```

- [ ] **Step 5: Run test to verify it passes**

Run: `cd frontend && npm test -- --include src/app/features/items/item-edit/item-edit.spec.ts`

Expected: PASS.

- [ ] **Step 6: Commit**

Run:

```bash
build-brief ./gradlew clean build
git add frontend/src/app/features/items/item-edit
git commit -m "feat(frontend): add item edit form

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

Expected: Gradle aggregate build passes before the commit.

---

### Task 5: Liste enrichie et routes items

**Files:**
- Modify: `frontend/src/app/features/items/item-list/item-list.ts`
- Modify: `frontend/src/app/features/items/item-list/item-list.html`
- Modify: `frontend/src/app/features/items/item-list/item-list.spec.ts`
- Modify: `frontend/src/app/app.routes.ts`

**Interfaces:**
- Consumes:
  - `createItemHref(page: HalPage<Item>): string | undefined`
  - `itemSelfHref(item: Item | null | undefined): string | undefined`
  - `itemUrl(id: string): string`
  - `ItemDeleteDialog`
  - `AuthService.isAdmin`
- Produces:
  - `ItemList.createHref(): string | undefined`
  - `ItemList.deleteItem(item: Item, event?: Event): void`
  - routes `/items/new`, `/items/:id`, `/items/:id/edit`

- [ ] **Step 1: Write the failing test**

Replace `frontend/src/app/features/items/item-list/item-list.spec.ts` with:

```ts
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { provideRouter, Router } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { TokenStore } from '../../../core/auth/token-store';
import { ItemDeleteDialog } from '../item-delete-dialog/item-delete-dialog';
import { ItemList } from './item-list';

function fakeJwt(payload: Record<string, unknown>): string {
  const encode = (value: unknown) =>
    btoa(JSON.stringify(value)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${encode({ alg: 'none' })}.${encode(payload)}.signature`;
}

describe('ItemList', () => {
  let harness: RouterTestingHarness;
  let http: HttpTestingController;
  let dialog: { open: ReturnType<typeof vi.fn> };
  let snackBar: { open: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    dialog = { open: vi.fn(() => ({ afterClosed: () => of(undefined) })) };
    snackBar = { open: vi.fn() };

    await TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([
          { path: 'items', component: ItemList },
          { path: 'items/new', children: [] },
          { path: 'items/:id', children: [] },
          { path: 'items/:id/edit', children: [] },
        ]),
        { provide: MatDialog, useValue: dialog },
        { provide: MatSnackBar, useValue: snackBar },
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

  function flushItemsPage(): void {
    http.expectOne('/api/v1/items?page=1&size=20').flush({
      _embedded: {
        elements: [
          {
            id: 'i-1',
            name: 'Cola',
            type: 'COLD_BEVERAGE',
            price: 1.5,
            _links: { self: { href: '/api/v1/items/i-1' } },
          },
        ],
      },
      _links: { self: { href: '/api/v1/items' } },
      _templates: { default: { method: 'post' } },
      page: { size: 20, totalElements: 1, totalPages: 1, number: 0 },
    });
  }

  function authenticateAdmin(): void {
    TestBed.inject(TokenStore).setAccessToken(
      fakeJwt({ sub: 'admin@vending.me', roles: ['ROLE_ADMIN'], exp: 4102444800 }),
    );
  }

  it('requests the first page on load and exposes the unwrapped elements', async () => {
    const component = await navigate('/items');

    const request = http.expectOne('/api/v1/items?page=1&size=20');
    expect(request.request.method).toBe('GET');

    request.flush({
      _embedded: {
        elements: [{ id: 'i-1', name: 'Cola', type: 'COLD_BEVERAGE', price: 1.5 }],
      },
      _links: { self: { href: '/api/v1/items' } },
      page: { size: 20, totalElements: 1, totalPages: 1, number: 0 },
    });
    await harness.fixture.whenStable();

    expect(component.items().elements).toHaveLength(1);
    expect(component.items().elements[0].name).toBe('Cola');
    expect(component.totalElements()).toBe(1);
    expect(component.displayedColumns).toEqual(['name', 'type', 'price', 'actions']);
  });

  it('reads the initial page from the URL query params', async () => {
    const component = await navigate('/items?page=2&size=10&type=SNACK');

    http
      .expectOne('/api/v1/items?page=3&size=10')
      .flush({ _links: { self: { href: '/api/v1/items' } }, page: { size: 10, totalElements: 30, totalPages: 3, number: 2 } });
    await harness.fixture.whenStable();

    expect(component.pageIndex()).toBe(2);
    expect(component.pageSize()).toBe(10);
  });

  it('navigates to the new page/size while preserving other query params', async () => {
    const component = await navigate('/items?type=SNACK');

    http
      .expectOne('/api/v1/items?page=1&size=20')
      .flush({ _links: { self: { href: '/api/v1/items' } }, page: { size: 20, totalElements: 30, totalPages: 2, number: 0 } });
    await harness.fixture.whenStable();

    component.onPageChange({ pageIndex: 2, pageSize: 10, length: 30 });
    await new Promise((resolve) => setTimeout(resolve));
    harness.fixture.detectChanges();

    expect(TestBed.inject(Router).url).toBe('/items?type=SNACK&page=2&size=10');
    http
      .expectOne('/api/v1/items?page=3&size=10')
      .flush({ _links: { self: { href: '/api/v1/items' } }, page: { size: 10, totalElements: 30, totalPages: 3, number: 2 } });
    await harness.fixture.whenStable();

    expect(component.items().elements).toEqual([]);
    expect(component.totalElements()).toBe(30);
  });

  it('exposes the create href from the page affordance fallback for the New item route state', async () => {
    authenticateAdmin();
    const component = await navigate('/items');
    flushItemsPage();
    await harness.fixture.whenStable();

    expect(component.createHref()).toBe('/api/v1/items');
  });

  it('opens the delete dialog and does not delete when cancelled', async () => {
    authenticateAdmin();
    dialog.open.mockReturnValue({ afterClosed: () => of(false) });
    const component = await navigate('/items');
    flushItemsPage();
    await harness.fixture.whenStable();

    component.deleteItem(component.items().elements[0]);

    expect(dialog.open).toHaveBeenCalledWith(ItemDeleteDialog, {
      data: { itemName: 'Cola' },
    });
    http.expectNone('/api/v1/items/i-1');
  });

  it('deletes through the item self link and reloads the list when confirmed', async () => {
    authenticateAdmin();
    dialog.open.mockReturnValue({ afterClosed: () => of(true) });
    const component = await navigate('/items');
    flushItemsPage();
    await harness.fixture.whenStable();

    component.deleteItem(component.items().elements[0]);

    const request = http.expectOne('/api/v1/items/i-1');
    expect(request.request.method).toBe('DELETE');
    request.flush(null, { status: 204, statusText: 'No Content' });
    harness.fixture.detectChanges();
    await Promise.resolve();
    harness.fixture.detectChanges();
    http.expectOne('/api/v1/items?page=1&size=20').flush({
      _links: { self: { href: '/api/v1/items' } },
      page: { size: 20, totalElements: 0, totalPages: 0, number: 0 },
    });
    await harness.fixture.whenStable();

    expect(snackBar.open).toHaveBeenCalledWith('Deleted Cola', 'Close', { duration: 5000 });
    expect(component.items().elements).toEqual([]);
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run: `cd frontend && npm test -- --include src/app/features/items/item-list/item-list.spec.ts`

Expected: FAIL because `displayedColumns` lacks `actions`, `createHref` and `deleteItem` do not exist, and list actions/routes are missing.

- [ ] **Step 3: Write minimal implementation**

Replace `frontend/src/app/features/items/item-list/item-list.ts` with:

```ts
import { HttpClient, httpResource } from '@angular/common/http';
import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth';
import { ApiRootApi } from '../../../core/http/api-root-api';
import { intQueryParam } from '../../../shared/http/query-params';
import { HalPage } from '../../../shared/models/hal';
import { Page } from '../../../shared/models/page';
import {
  ItemDeleteDialog,
  ItemDeleteDialogData,
} from '../item-delete-dialog/item-delete-dialog';
import { createItemHref, itemSelfHref, itemUrl, itemsPageUrl } from '../item-api';
import { Item } from '../models/item';

const DEFAULT_PAGE_SIZE = 20;

@Component({
  selector: 'app-item-list',
  imports: [
    MatButtonModule,
    MatIconModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatTableModule,
    RouterLink,
  ],
  templateUrl: './item-list.html',
})
export class ItemList {
  private readonly auth = inject(AuthService);
  private readonly apiRoot = inject(ApiRootApi);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly http = inject(HttpClient);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly isAdmin = this.auth.isAdmin;
  readonly displayedColumns = ['name', 'type', 'price', 'actions'];

  private readonly queryParamMap = toSignal(this.route.queryParamMap, { requireSync: true });
  readonly pageIndex = computed(() => intQueryParam(this.queryParamMap().get('page'), 0));
  readonly pageSize = computed(() =>
    intQueryParam(this.queryParamMap().get('size'), DEFAULT_PAGE_SIZE),
  );

  private readonly resource = httpResource<HalPage<Item>>(() =>
    itemsPageUrl(this.apiRoot.link('items'), this.pageIndex(), this.pageSize()),
  );

  readonly isLoading = this.resource.isLoading;
  readonly hasError = computed(() => this.resource.error() !== undefined);
  readonly items = computed(() =>
    this.resource.hasValue() ? Page.fromHalPage(this.resource.value()) : Page.empty<Item>(),
  );
  readonly totalElements = computed(() => this.items().totalElements);
  readonly createHref = computed(() =>
    this.resource.hasValue() ? createItemHref(this.resource.value()) : undefined,
  );

  onPageChange(event: Pick<PageEvent, 'pageIndex' | 'pageSize' | 'length'>): void {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { page: event.pageIndex, size: event.pageSize },
      queryParamsHandling: 'merge',
    });
  }

  deleteItem(item: Item, event?: Event): void {
    event?.stopPropagation();

    this.dialog
      .open(ItemDeleteDialog, {
        data: { itemName: item.name ?? 'this item' } satisfies ItemDeleteDialogData,
      })
      .afterClosed()
      .subscribe((confirmed) => {
        if (confirmed !== true) {
          return;
        }

        this.http.delete(itemSelfHref(item) ?? itemUrl(item.id)).subscribe({
          next: () => {
            this.snackBar.open(`Deleted ${item.name ?? 'item'}`, 'Close', { duration: 5000 });
            this.resource.reload();
          },
          error: () => {
            this.snackBar.open('The item could not be deleted.', 'Close', { duration: 5000 });
          },
        });
      });
  }
}
```

Replace `frontend/src/app/features/items/item-list/item-list.html` with:

```html
<div class="mb-4 flex items-center justify-between">
  <h1 class="text-xl font-semibold">Items</h1>
  @if (isAdmin()) {
    <a mat-stroked-button color="primary" routerLink="/items/new" [state]="{ createHref: createHref() }">
      <mat-icon>add_box</mat-icon>
      New item
    </a>
  }
</div>

@if (isLoading()) {
  <mat-progress-bar mode="indeterminate" />
}

@if (hasError()) {
  <p class="text-red-600" role="alert">Items could not be loaded.</p>
} @else {
  <table mat-table [dataSource]="items().elements" class="w-full">
    <ng-container matColumnDef="name">
      <th mat-header-cell *matHeaderCellDef>Name</th>
      <td mat-cell *matCellDef="let item">{{ item.name ?? '—' }}</td>
    </ng-container>

    <ng-container matColumnDef="type">
      <th mat-header-cell *matHeaderCellDef>Type</th>
      <td mat-cell *matCellDef="let item">{{ item.type ?? '—' }}</td>
    </ng-container>

    <ng-container matColumnDef="price">
      <th mat-header-cell *matHeaderCellDef>Price</th>
      <td mat-cell *matCellDef="let item">{{ item.price ?? '—' }}</td>
    </ng-container>

    <ng-container matColumnDef="actions">
      <th mat-header-cell *matHeaderCellDef>Actions</th>
      <td mat-cell *matCellDef="let item">
        <a
          mat-icon-button
          [routerLink]="['/items', item.id]"
          [state]="{ href: item._links?.self?.href }"
          aria-label="View details"
          (click)="$event.stopPropagation()"
        >
          <mat-icon>visibility</mat-icon>
        </a>
        @if (isAdmin()) {
          <a
            mat-icon-button
            [routerLink]="['/items', item.id, 'edit']"
            [state]="{ href: item._links?.self?.href }"
            aria-label="Edit item"
            (click)="$event.stopPropagation()"
          >
            <mat-icon>edit</mat-icon>
          </a>
          <button
            mat-icon-button
            type="button"
            aria-label="Delete item"
            (click)="deleteItem(item, $event)"
          >
            <mat-icon>delete</mat-icon>
          </button>
        }
      </td>
    </ng-container>

    <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
    <tr
      mat-row
      *matRowDef="let row; columns: displayedColumns"
      class="cursor-pointer"
      [routerLink]="['/items', row.id]"
      [state]="{ href: row._links?.self?.href }"
    ></tr>
  </table>

  @if (!isLoading() && items().isEmpty) {
    <p class="p-4 text-sm text-gray-600">No item found.</p>
  }

  <mat-paginator
    [length]="totalElements()"
    [pageIndex]="pageIndex()"
    [pageSize]="pageSize()"
    [pageSizeOptions]="[10, 20, 50]"
    (page)="onPageChange($event)"
  />
}
```

Replace the `items` route block in `frontend/src/app/app.routes.ts` with these blocks, keeping `items/new` before `items/:id`:

```ts
  {
    path: 'items',
    loadComponent: () => import('./features/items/item-list/item-list').then((m) => m.ItemList),
    canActivate: [adminGuard],
    title: 'Items',
  },
  {
    path: 'items/new',
    loadComponent: () =>
      import('./features/items/item-create/item-create').then((m) => m.ItemCreate),
    canActivate: [adminGuard],
    title: 'New item',
  },
  {
    path: 'items/:id',
    loadComponent: () =>
      import('./features/items/item-detail/item-detail').then((m) => m.ItemDetail),
    canActivate: [adminGuard],
    title: 'Item details',
  },
  {
    path: 'items/:id/edit',
    loadComponent: () => import('./features/items/item-edit/item-edit').then((m) => m.ItemEdit),
    canActivate: [adminGuard],
    title: 'Edit item',
  },
```

Full `frontend/src/app/app.routes.ts` after the edit:

```ts
import { Routes } from '@angular/router';
import { adminGuard } from './core/auth/admin-guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'machines' },
  {
    path: 'machines',
    loadComponent: () =>
      import('./features/machines/machine-list/machine-list').then((m) => m.MachineList),
    title: 'Vending machines',
  },
  {
    path: 'machines/new',
    loadComponent: () =>
      import('./features/machines/machine-create/machine-create').then((m) => m.MachineCreate),
    canActivate: [adminGuard],
    title: 'New vending machine',
  },
  {
    path: 'machines/:id',
    loadComponent: () =>
      import('./features/machines/machine-detail/machine-detail').then((m) => m.MachineDetail),
    title: 'Vending machine details',
  },
  {
    path: 'items',
    loadComponent: () => import('./features/items/item-list/item-list').then((m) => m.ItemList),
    canActivate: [adminGuard],
    title: 'Items',
  },
  {
    path: 'items/new',
    loadComponent: () =>
      import('./features/items/item-create/item-create').then((m) => m.ItemCreate),
    canActivate: [adminGuard],
    title: 'New item',
  },
  {
    path: 'items/:id',
    loadComponent: () =>
      import('./features/items/item-detail/item-detail').then((m) => m.ItemDetail),
    canActivate: [adminGuard],
    title: 'Item details',
  },
  {
    path: 'items/:id/edit',
    loadComponent: () => import('./features/items/item-edit/item-edit').then((m) => m.ItemEdit),
    canActivate: [adminGuard],
    title: 'Edit item',
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login').then((m) => m.Login),
    title: 'Sign in',
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register').then((m) => m.Register),
    title: 'Create account',
  },
  { path: '**', redirectTo: 'machines' },
];
```

- [ ] **Step 4: Run test to verify it passes**

Run: `cd frontend && npm test -- --include src/app/features/items/item-list/item-list.spec.ts`

Expected: PASS.

- [ ] **Step 5: Run all item unit tests**

Run: `cd frontend && npm test -- --include src/app/features/items/**/*.spec.ts`

Expected: PASS.

- [ ] **Step 6: Commit**

Run:

```bash
build-brief ./gradlew clean build
git add frontend/src/app/features/items/item-list/item-list.ts frontend/src/app/features/items/item-list/item-list.html frontend/src/app/features/items/item-list/item-list.spec.ts frontend/src/app/app.routes.ts
git commit -m "feat(frontend): wire item routes and list actions

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

Expected: Gradle aggregate build passes before the commit.

---

### Task 6: Parcours E2E admin de gestion des items

**Files:**
- Create: `frontend/e2e/manage-items.spec.ts`

**Interfaces:**
- Consumes:
  - UI routes `/login`, `/items`, `/items/new`, `/items/:id`, `/items/:id/edit`
  - root `/api/v1` mock with `items` link
  - admin JWT payload `roles: ['ROLE_ADMIN']`; `AuthService` skips `/me` for admins, so no `/api/v1/me` mock is required for this flow.
- Produces:
  - Playwright regression covering create → detail → edit with image → delete.

- [ ] **Step 1: Write the failing E2E test**

Create `frontend/e2e/manage-items.spec.ts`:

```ts
import { expect, type Page, type Route, test } from '@playwright/test';

/** Unsigned JWT: the frontend only reads the payload, the backend is mocked here. */
function fakeAdminAccessToken(): string {
  const encode = (value: unknown) =>
    btoa(JSON.stringify(value)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${encode({ alg: 'none' })}.${encode({
    sub: 'admin@vending.me',
    roles: ['ROLE_ADMIN'],
    exp: 4102444800,
    token_type: 'access',
  })}.signature`;
}

const itemId = '22222222-2222-2222-2222-222222222222';
const itemsPath = '/api/v1/items';
const itemPath = `${itemsPath}/${itemId}`;
const imagePath = `${itemPath}/image`;

function itemResource(price = 1.5) {
  return {
    id: itemId,
    name: 'Cola',
    type: 'COLD_BEVERAGE',
    price,
    _links: {
      self: { href: itemPath },
      'item:image': { href: imagePath },
    },
  };
}

function itemsPage(price = 1.5) {
  return {
    _embedded: { elements: [itemResource(price)] },
    _links: { self: { href: itemsPath } },
    _templates: { default: { method: 'post' } },
    page: { size: 20, totalElements: 1, totalPages: 1, number: 0 },
  };
}

async function fulfillJson(
  route: Route,
  body: unknown,
  contentType = 'application/hal+json',
  status = 200,
) {
  await route.fulfill({
    status,
    contentType,
    body: JSON.stringify(body),
  });
}

async function signInAsAdmin(page: Page) {
  await page.goto('/login');

  await page.getByLabel('Email').fill('admin@vending.me');
  await page.getByLabel('Password').fill('S3cret!Passw0rd');
  await page.getByRole('button', { name: 'Sign in' }).click();

  await expect(page).toHaveURL(/\/machines$/);
}

test.beforeEach(async ({ page }) => {
  await page.route('**/api/v1', async (route) => {
    await fulfillJson(route, {
      _links: {
        vendingMachines: { href: '/api/v1/vending-machines' },
        items: { href: itemsPath },
      },
    });
  });

  await page.route('**/api/v1/authenticate', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ accessToken: fakeAdminAccessToken() }),
    });
  });

  await page.route('**/api/v1/authenticate/refresh', async (route) => {
    await route.fulfill({ status: 401 });
  });

  await page.route('**/api/v1/authenticate/logout', async (route) => {
    await route.fulfill({ status: 204 });
  });

  await page.route('**/api/v1/vending-machines**', async (route) => {
    await fulfillJson(route, {
      page: { size: 20, totalElements: 0, totalPages: 0, number: 0 },
    });
  });
});

test('an admin can create, edit image, and delete an item', async ({ page }) => {
  let created = false;
  let price = 1.5;
  let uploadCount = 0;
  let deleteCount = 0;

  await page.route('**/api/v1/items**', async (route) => {
    const request = route.request();
    const pathname = new URL(request.url()).pathname;

    if (request.method() === 'GET' && pathname === itemsPath) {
      await fulfillJson(route, created ? itemsPage(price) : {
        _links: { self: { href: itemsPath } },
        _templates: { default: { method: 'post' } },
        page: { size: 20, totalElements: 0, totalPages: 0, number: 0 },
      });
      return;
    }

    if (request.method() === 'POST' && pathname === itemsPath) {
      created = true;
      await fulfillJson(route, itemResource(price), 'application/hal+json', 201);
      return;
    }

    if (request.method() === 'GET' && pathname === itemPath) {
      await fulfillJson(route, itemResource(price));
      return;
    }

    if (request.method() === 'PUT' && pathname === itemPath) {
      const body = JSON.parse(request.postData() ?? '{}') as { price: number };
      price = body.price;
      await fulfillJson(route, itemResource(price));
      return;
    }

    if (request.method() === 'POST' && pathname === imagePath) {
      uploadCount += 1;
      await fulfillJson(route, itemResource(price));
      return;
    }

    if (request.method() === 'GET' && pathname === imagePath) {
      await route.fulfill({ status: 404 });
      return;
    }

    if (request.method() === 'DELETE' && pathname === itemPath) {
      deleteCount += 1;
      created = false;
      await route.fulfill({ status: 204 });
      return;
    }

    throw new Error(`Unexpected items request: ${request.method()} ${pathname}`);
  });

  await signInAsAdmin(page);
  await page.goto('/items');

  await page.getByRole('link', { name: /New item/ }).click();
  await expect(page).toHaveURL(/\/items\/new$/);

  await page.getByLabel('Name').fill('Cola');
  await page.getByLabel('Type').click();
  await page.getByRole('option', { name: 'COLD_BEVERAGE' }).click();
  await page.getByLabel('Price').fill('1.5');
  await page.getByRole('button', { name: 'Create item' }).click();

  await expect(page).toHaveURL(/\/items$/);
  await expect(page.getByText('Cola')).toBeVisible();

  await page.getByRole('link', { name: 'View details' }).click();
  await expect(page).toHaveURL(new RegExp(`/items/${itemId}$`));
  await expect(page.getByRole('heading', { name: 'Item details' })).toBeVisible();

  await page.getByRole('link', { name: 'Edit' }).click();
  await expect(page).toHaveURL(new RegExp(`/items/${itemId}/edit$`));
  await page.getByLabel('Price').fill('2');
  await page.getByTestId('image-upload-input').setInputFiles({
    name: 'cola.png',
    mimeType: 'image/png',
    buffer: Buffer.from('image'),
  });
  await page.getByRole('button', { name: 'Save item' }).click();

  await expect(page).toHaveURL(new RegExp(`/items/${itemId}$`));
  await expect(page.getByText('2')).toBeVisible();
  expect(uploadCount).toBe(1);

  await page.getByRole('button', { name: 'Delete' }).click();
  await expect(page.getByText('Delete Cola?')).toBeVisible();
  await page.getByTestId('item-delete-dialog-confirm').click();

  await expect(page).toHaveURL(/\/items$/);
  await expect(page.getByText('No item found.')).toBeVisible();
  expect(deleteCount).toBe(1);
});
```

- [ ] **Step 2: Run E2E to verify it fails before the preceding UI wiring is complete**

Run: `cd frontend && npm run e2e -- --grep "an admin can create, edit image, and delete an item"`

Expected: FAIL before Tasks 2-5 are implemented, or PASS if those tasks are already complete in the current branch.

- [ ] **Step 3: Run E2E expecting PASS after Tasks 2-5**

Run: `cd frontend && npm run e2e -- --grep "an admin can create, edit image, and delete an item"`

Expected: PASS.

- [ ] **Step 4: Commit**

Run:

```bash
build-brief ./gradlew clean build
git add frontend/e2e/manage-items.spec.ts
git commit -m "feat(frontend): cover item management e2e

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

Expected: Gradle aggregate build passes before the commit.

---

### Task 7: Documentation frontend et backlog

**Files:**
- Modify: `frontend/README.md`
- Modify: `docs/features-front-a-implementer.md`

**Interfaces:**
- Consumes: feature livrée par Tasks 1-6.
- Produces: documentation alignée sur le CRUD items livré.

- [ ] **Step 1: Préserver les modifications non liées**

Run:

```bash
git status --short frontend/README.md docs/features-front-a-implementer.md
```

Expected: s'il existe des modifications non liées héritées de `main`, les lire et les intégrer sans les écraser. Ne pas utiliser `git checkout --` ni `git reset --hard`.

- [ ] **Step 2: Write the documentation diff**

In `frontend/README.md`, replace the current `Articles` bullet under `## Périmètre actuel` with:

```md
- **Articles** : liste paginée back-office (admin), détail, création,
  édition du prix, suppression et gestion d'image JPEG/PNG.
```

In the same file, remove this line from `**Reste à faire**`:

```md
- CRUD complet articles (création/édition/suppression — seule la liste
  existe) et gestion des images d'article
```

In `docs/features-front-a-implementer.md`, replace the `Déjà fait` items section with:

```md
## ✅ Déjà fait

- Login, inscription (sans auto-login — l'utilisateur doit se connecter
  ensuite), liste des machines (lecture seule, paginée)
- **Détail d'une machine** (`GET /vending-machines/{id}`) — infos machine,
  **stock consulté et commande d'un item** (`POST
  /vending-machines/{id}/order/{itemId}`) pour un utilisateur `ROLE_USER`
- **Création d'une machine** (`ROLE_ADMIN`, `POST /vending-machines`) —
  formulaire Signal Forms dédié (`machines/new`)
- **Gestion des items** (`ROLE_ADMIN`, `/items`) — liste paginée, détail,
  création, édition du prix, suppression et upload d'image
```

In the same file, remove the old backlog line:

```md
5. **Gestion des items** : création/édition/suppression + upload d'image
   (`/items`) — seule la liste en lecture seule existe
```

Renumber the remaining back-office entries so the list stays sequential.

- [ ] **Step 3: Run documentation-adjacent validation**

Run:

```bash
cd frontend && npm test -- --include src/app/features/items/**/*.spec.ts
cd frontend && npm run e2e -- --grep "an admin can create, edit image, and delete an item"
```

Expected: PASS.

- [ ] **Step 4: Commit**

Run:

```bash
build-brief ./gradlew clean build
git add frontend/README.md docs/features-front-a-implementer.md
git commit -m "feat(frontend): document item management

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

Expected: Gradle aggregate build passes before the commit.

---

### Task 8: Validation finale et fusion ff-only

**Files:**
- Create: aucun
- Modify: aucun
- Test: suite frontend, E2E, build agrégé.

**Interfaces:**
- Consumes: tous les commits précédents sur `feat/frontend-items-crud`.
- Produces: branche validée prête pour merge fast-forward.

- [ ] **Step 1: Run all frontend unit tests**

Run: `cd frontend && npm test`

Expected: PASS.

- [ ] **Step 2: Run all E2E tests**

Run: `cd frontend && npm run e2e`

Expected: PASS.

- [ ] **Step 3: Run aggregate build**

Run: `build-brief ./gradlew clean build`

Expected: PASS.

- [ ] **Step 4: Vérifier qu'aucun fichier backend n'a changé**

Run:

```bash
git status --short backend
```

Expected: aucune sortie.

- [ ] **Step 5: Fusionner en fast-forward après revue**

Run:

```bash
git switch main
git pull --ff-only
git merge --ff-only feat/frontend-items-crud
git branch -d feat/frontend-items-crud
```

Expected: merge fast-forward uniquement, sans commit de merge.

---

## Self-Review

- **Spec coverage:** le plan couvre modèles `ItemToCreate`/`ItemToUpdate`, helper `createItemHref`, création `/items/new`, détail `/items/:id`, édition `/items/:id/edit`, suppression dialog, liste avec actions/lignes cliquables, routes, E2E et docs. La décision de garder `adminGuard` sur `/items/:id` est explicitée dans la structure, car la feature reste back-office malgré le `GET` backend `permitAll`.
- **Placeholder scan:** aucun marqueur de contenu à compléter, aucune étape "faire pareil", aucun code volontairement omis; les snippets `.ts`, `.html`, unitaires et E2E sont écrits dans les tâches propriétaires.
- **Type consistency:** `ItemToCreate`, `ItemToUpdate`, `Item`, `ItemDeleteDialogData`, `createItemHref`, `itemSelfHref`, `itemImageHref`, `itemUrl`, `itemImageUrl`, `withCacheBuster`, `onFileSelected` et `submit` gardent les mêmes signatures entre tâches.
- **Review Focus:** les cinq risques listés sont chacun verrouillés : fallback `/items/new` dans Task 2, échec partiel création/upload dans Task 2, fallback HAL-FORMS dans Task 1 et Task 5, image absente/fallback dans Task 3, annulation/confirmation suppression dans Task 3 et Task 5.
- **Écarts spec résolus:** `uploadItemImage(http,id,file)` est remplacé par `uploadImageFile(http, href, file)` du plan 00 pour éviter la duplication avec le profil; le cache-busting image utilise `withCacheBuster(href, version)` et non `pagedUrl`, afin de préserver les origines absolues HAL; `ItemCreate` ne recrée pas `itemsUrl()` et utilise `history.state.createHref` puis `ApiRootApi.link('items')`; l'erreur serveur d'upload image reste un message générique car `IllegalArgumentException` n'est pas gérée par le backend et aucun changement backend n'est autorisé.
