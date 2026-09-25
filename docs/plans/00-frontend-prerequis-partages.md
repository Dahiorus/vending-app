# Prérequis partagés du frontend — Plan d'implémentation

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Introduire, une seule fois et testés, les utilitaires frontend réutilisés par plusieurs plans de features (lecture d'un lien HAL, upload multipart d'image, cache-busting d'URL, URL d'item/d'image d'item, composant d'upload d'image).

**Architecture:** Fonctions pures et un composant standalone rangés dans `shared/` (transverse) et `features/items/item-api.ts` (URL propres au sous-domaine items). Aucun composant existant n'est modifié : ce plan ne fait qu'ajouter des briques, consommées ensuite par les plans de features.

**Tech Stack:** Angular 22 (standalone, zoneless, signals, `input()`/`output()`/`linkedSignal()`), Angular Material, Tailwind, Vitest via `@angular/build:unit-test`.

**Spec:** pas de spec propre — ce plan factorise les besoins communs de `docs/specs/frontend-crud-items.md`, `docs/specs/frontend-espace-profil.md`, `docs/specs/frontend-backoffice-machine-admin.md`, `docs/specs/frontend-gestion-vending-machines.md` et `docs/specs/frontend-image-item-stock-client.md`.

**À exécuter en premier**, avant tout autre plan de `docs/plans/`. Ordre recommandé ensuite (priorité de `docs/features-front-a-implementer.md`) :

1. `00-frontend-prerequis-partages.md` (ce plan)
2. `frontend-espace-profil.md`
3. `frontend-crud-items.md`
4. `frontend-gestion-vending-machines.md`
5. `frontend-backoffice-machine-admin.md`
6. `frontend-image-item-stock-client.md`

## Global Constraints

- Nommage de fichiers style guide 2025 : `image-upload.ts`, jamais `image-upload.component.ts`.
- Génération des composants via Angular CLI : `cd frontend && npx ng generate component <chemin> --style=none` (les composants existants n'ont pas de feuille de style), puis adapter le squelette.
- Skill `angular-developer` à utiliser pour tout code Angular.
- Tests unitaires **uniquement** via `cd frontend && npm test` (ciblage : `npm test -- --include <glob>`), jamais `npx vitest run`.
- Toute commande Gradle passe par `build-brief` : `build-brief ./gradlew clean build` doit réussir avant chaque commit (251 tests backend + agrégation frontend) ; conserver le chemin du log brut affiché en cas d'échec.
- Branche `feat/frontend-shared-prerequisites` créée depuis `develop` — au 2026-09-25 `develop` n'existe pas, seulement `main` : créer alors depuis `main`.
- Messages de commit `feat(frontend): ...` avec le trailer `Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>`.
- Fusion en fast-forward uniquement (`git merge --ff-only`), suppression de la branche locale après fusion.
- Aucune modification backend.
- Interdits : `innerHTML`, `bypassSecurityTrust*`, jeton dans une URL.
- Libellés d'UI en anglais (cohérence avec l'existant : « Sign in », « Log out »…).

## Review Focus

- **Href HAL absolu** (`https://api.example.test/api/v1/items/i-1/image`) passé à `withCacheBuster` : doit rester absolu, origine conservée — piège connu de `pagedUrl`, qui ne renvoie que `pathname + search`. Test ajouté en Task 2.
- **Lien HAL multiple** (rel porté sous forme de tableau, cas de `item`/`order` sur le stock) : `linkHref` doit renvoyer le premier href, pas `undefined`. Test ajouté en Task 1.
- **Fichier au type refusé (`image/gif`) après un fichier accepté** : l'aperçu précédent doit être révoqué et effacé, et `null` émis, pour que le parent n'envoie pas l'ancien fichier. Test ajouté en Task 4.
- **Changement de `currentImageUrl` après un 404** (cache-busting après upload) : l'état « image en échec » doit être réinitialisé pour retenter l'affichage. Test ajouté en Task 4.
- **Destruction du composant avec un aperçu actif** : l'object URL doit être révoqué (fuite mémoire sinon). Test ajouté en Task 4.

---

## File Structure

| Fichier | Action | Responsabilité |
| --- | --- | --- |
| `frontend/src/app/shared/models/hal.ts` | Modify | ajoute `linkHref(resource, rel)` : lecture d'un href HAL (premier si tableau) |
| `frontend/src/app/shared/models/hal.spec.ts` | Modify | tests de `linkHref` |
| `frontend/src/app/shared/http/cache-buster.ts` | Create | `withCacheBuster(href, version)` : ajoute `?v=` sans perdre l'origine |
| `frontend/src/app/shared/http/cache-buster.spec.ts` | Create | tests |
| `frontend/src/app/shared/http/upload-image-file.ts` | Create | `uploadImageFile(http, href, file)` : `POST` multipart, champ `file` |
| `frontend/src/app/shared/http/upload-image-file.spec.ts` | Create | tests |
| `frontend/src/app/features/items/item-api.ts` | Modify | ajoute `itemUrl(id)` et `itemImageUrl(id)` (replis sans HATEOAS) |
| `frontend/src/app/features/items/item-api.spec.ts` | Modify | tests |
| `frontend/src/app/shared/image-upload/image-upload.ts` | Create | composant de sélection/aperçu/validation d'image |
| `frontend/src/app/shared/image-upload/image-upload.html` | Create | template |
| `frontend/src/app/shared/image-upload/image-upload.spec.ts` | Create | tests |

`uploadImageFile` remplace volontairement les helpers `uploadItemImage(http, id, file)` (spec items) et `uploadProfilePicture(http, href, file)` (spec profil) : même contrat backend (`MultipartFileValidator`, champ `file`) des deux côtés, une seule implémentation.

---

### Task 0: Créer la branche

- [ ] **Step 1: Créer la branche de travail**

```bash
cd /home/bung@france.groupe.intra/IdeaProjects/vending-app
git switch develop 2>/dev/null || git switch main
git pull --ff-only
git switch -c feat/frontend-shared-prerequisites
```

---

### Task 1: `linkHref` — lecture d'un lien HAL

**Files:**
- Modify: `frontend/src/app/shared/models/hal.ts`
- Test: `frontend/src/app/shared/models/hal.spec.ts`

**Interfaces:**
- Consumes: `HalResource` (existant, même fichier)
- Produces: `export function linkHref(resource: HalResource | null | undefined, rel: string): string | undefined`

- [ ] **Step 1: Écrire le test en échec** — ajouter à la fin de `hal.spec.ts` (et compléter l'import en tête : `import { HalResource, linkHref } from './hal';`)

```ts
describe('linkHref', () => {
  it('returns the href of a single link', () => {
    const resource: HalResource = { _links: { self: { href: '/api/v1/me' } } };

    expect(linkHref(resource, 'self')).toBe('/api/v1/me');
  });

  it('returns the first href when the relation holds an array of links', () => {
    const resource: HalResource = {
      _links: { item: [{ href: '/api/v1/items/i-1' }, { href: '/api/v1/items/i-2' }] },
    };

    expect(linkHref(resource, 'item')).toBe('/api/v1/items/i-1');
  });

  it('returns undefined for an empty array of links', () => {
    const resource: HalResource = { _links: { order: [] } };

    expect(linkHref(resource, 'order')).toBeUndefined();
  });

  it('returns undefined when the relation is missing', () => {
    expect(linkHref({ _links: {} }, 'me:picture')).toBeUndefined();
  });

  it('returns undefined when the resource has no _links or is not loaded yet', () => {
    expect(linkHref({}, 'self')).toBeUndefined();
    expect(linkHref(undefined, 'self')).toBeUndefined();
    expect(linkHref(null, 'self')).toBeUndefined();
  });
});
```

- [ ] **Step 2: Lancer le test, vérifier l'échec**

Run: `cd frontend && npm test -- --include src/app/shared/models/hal.spec.ts`
Expected: FAIL — `linkHref` n'est pas exporté par `./hal` (erreur de compilation TS2305 / `linkHref is not a function`).

- [ ] **Step 3: Implémentation minimale** — ajouter à la fin de `hal.ts`

```ts
/**
 * Reads the href of a HAL relation, returning the first href when the relation
 * holds an array of links (Spring HATEOAS serialises repeated rels as arrays).
 */
export function linkHref(
  resource: HalResource | null | undefined,
  rel: string,
): string | undefined {
  const link = resource?._links?.[rel];
  if (!link) return undefined;
  return Array.isArray(link) ? link[0]?.href : link.href;
}
```

- [ ] **Step 4: Relancer le test, vérifier le succès**

Run: `cd frontend && npm test -- --include src/app/shared/models/hal.spec.ts`
Expected: PASS (6 tests : le test existant `HalResource` + 5 nouveaux).

- [ ] **Step 5: Commit**

```bash
build-brief ./gradlew clean build
git add frontend/src/app/shared/models/hal.ts frontend/src/app/shared/models/hal.spec.ts
git commit -m "feat(frontend): add linkHref helper to read HAL links

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

### Task 2: Helpers HTTP — cache-busting et upload multipart d'image

**Files:**
- Create: `frontend/src/app/shared/http/cache-buster.ts`
- Create: `frontend/src/app/shared/http/upload-image-file.ts`
- Test: `frontend/src/app/shared/http/cache-buster.spec.ts`
- Test: `frontend/src/app/shared/http/upload-image-file.spec.ts`

**Interfaces:**
- Consumes: `HttpClient` (`@angular/common/http`)
- Produces:
  - `export function withCacheBuster(href: string, version: number): string`
  - `export function uploadImageFile<T = unknown>(http: HttpClient, href: string, file: File): Observable<T>`

- [ ] **Step 1: Écrire les tests en échec**

`frontend/src/app/shared/http/cache-buster.spec.ts` :

```ts
import { describe, expect, it } from 'vitest';
import { withCacheBuster } from './cache-buster';

describe('withCacheBuster', () => {
  it('returns the href unchanged for the initial version 0', () => {
    expect(withCacheBuster('/api/v1/me/picture', 0)).toBe('/api/v1/me/picture');
  });

  it('appends the version as a `v` query param on a relative href', () => {
    expect(withCacheBuster('/api/v1/me/picture', 2)).toBe('/api/v1/me/picture?v=2');
  });

  it('keeps an absolute href absolute, preserving its origin', () => {
    expect(withCacheBuster('https://api.example.test/api/v1/items/i-1/image', 1)).toBe(
      'https://api.example.test/api/v1/items/i-1/image?v=1',
    );
  });

  it('preserves existing query params and overwrites a previous version', () => {
    expect(withCacheBuster('/api/v1/items/i-1/image?size=small&v=1', 3)).toBe(
      '/api/v1/items/i-1/image?size=small&v=3',
    );
  });
});
```

`frontend/src/app/shared/http/upload-image-file.spec.ts` :

```ts
import { HttpClient, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { uploadImageFile } from './upload-image-file';

describe('uploadImageFile', () => {
  let http: HttpClient;
  let backend: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('POSTs the file as multipart form data under the `file` key', () => {
    const file = new File(['png-bytes'], 'cola.png', { type: 'image/png' });
    let response: unknown;

    uploadImageFile(http, '/api/v1/items/i-1/image', file).subscribe((body) => (response = body));

    const request = backend.expectOne('/api/v1/items/i-1/image');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toBeInstanceOf(FormData);
    expect((request.request.body as FormData).get('file')).toBe(file);
    request.flush({ id: 'i-1' });

    expect(response).toEqual({ id: 'i-1' });
  });

  it('does not set the Content-Type header, letting the browser add the multipart boundary', () => {
    const file = new File(['jpg-bytes'], 'me.jpg', { type: 'image/jpeg' });

    uploadImageFile(http, '/api/v1/me/picture', file).subscribe();

    const request = backend.expectOne('/api/v1/me/picture');
    expect(request.request.headers.has('Content-Type')).toBe(false);
    request.flush({});
  });
});
```

- [ ] **Step 2: Lancer les tests, vérifier l'échec**

Run: `cd frontend && npm test -- --include "src/app/shared/http/{cache-buster,upload-image-file}.spec.ts"`
Expected: FAIL — modules `./cache-buster` et `./upload-image-file` introuvables.

- [ ] **Step 3: Implémentation minimale**

`frontend/src/app/shared/http/cache-buster.ts` :

```ts
const ABSOLUTE_URL = /^[a-z][a-z\d+\-.]*:/i;

/**
 * Forces the browser to refetch a resource whose URL does not change after an
 * update (e.g. an image re-uploaded at the same `/image` URL) by setting a `v`
 * query param. Unlike `pagedUrl`, absolute hrefs keep their origin.
 */
export function withCacheBuster(href: string, version: number): string {
  if (version === 0) return href;

  const url = new URL(href, window.location.origin);
  url.searchParams.set('v', String(version));

  return ABSOLUTE_URL.test(href) ? url.toString() : `${url.pathname}${url.search}${url.hash}`;
}
```

`frontend/src/app/shared/http/upload-image-file.ts` :

```ts
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * Uploads an image to an endpoint validated by the backend `MultipartFileValidator`
 * (multipart param `file`, JPEG/PNG only). The Content-Type header is left to the
 * browser so that it carries the multipart boundary.
 */
export function uploadImageFile<T = unknown>(
  http: HttpClient,
  href: string,
  file: File,
): Observable<T> {
  const body = new FormData();
  body.append('file', file);

  return http.post<T>(href, body);
}
```

- [ ] **Step 4: Relancer les tests, vérifier le succès**

Run: `cd frontend && npm test -- --include "src/app/shared/http/{cache-buster,upload-image-file}.spec.ts"`
Expected: PASS (6 tests).

- [ ] **Step 5: Commit**

```bash
build-brief ./gradlew clean build
git add frontend/src/app/shared/http/cache-buster.ts frontend/src/app/shared/http/cache-buster.spec.ts \
  frontend/src/app/shared/http/upload-image-file.ts frontend/src/app/shared/http/upload-image-file.spec.ts
git commit -m "feat(frontend): add image upload and cache-busting HTTP helpers

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

### Task 3: URL d'item et d'image d'item (replis sans HATEOAS)

**Files:**
- Modify: `frontend/src/app/features/items/item-api.ts`
- Test: `frontend/src/app/features/items/item-api.spec.ts`

**Interfaces:**
- Consumes: `environment.apiBaseUrl` (`'/api/v1'`)
- Produces:
  - `export function itemUrl(id: string): string` → `/api/v1/items/<id>`
  - `export function itemImageUrl(id: string): string` → `/api/v1/items/<id>/image`

- [ ] **Step 1: Écrire le test en échec** — remplacer l'import en tête de `item-api.spec.ts` par `import { itemImageUrl, itemsPageUrl, itemUrl } from './item-api';` et ajouter à la fin :

```ts
describe('itemUrl', () => {
  it('builds the item resource URL from its id', () => {
    expect(itemUrl('i-1')).toBe('/api/v1/items/i-1');
  });
});

describe('itemImageUrl', () => {
  it('builds the public item image URL from its id', () => {
    expect(itemImageUrl('i-1')).toBe('/api/v1/items/i-1/image');
  });
});
```

- [ ] **Step 2: Lancer le test, vérifier l'échec**

Run: `cd frontend && npm test -- --include src/app/features/items/item-api.spec.ts`
Expected: FAIL — `itemUrl` / `itemImageUrl` non exportés.

- [ ] **Step 3: Implémentation minimale** — `item-api.ts` complet :

```ts
import { environment } from '../../../environments/environment';
import { pagedUrl } from '../../shared/http/paged-url';

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
 * Fallback used only when no HATEOAS `self` link is available (e.g. direct
 * navigation/page refresh on an item page). Prefer following the resource's own
 * `_links.self.href` when it is known.
 */
export function itemUrl(id: string): string {
  return `${environment.apiBaseUrl}/items/${id}`;
}

/**
 * Public (`permitAll`) item image URL. Prefer the `item:image` link of an `Item`
 * when it is loaded; this fallback serves places that only know the item id
 * (e.g. the stock of a vending machine, which exposes no `item:image` rel).
 */
export function itemImageUrl(id: string): string {
  return `${itemUrl(id)}/image`;
}
```

- [ ] **Step 4: Relancer le test, vérifier le succès**

Run: `cd frontend && npm test -- --include src/app/features/items/item-api.spec.ts`
Expected: PASS (5 tests).

- [ ] **Step 5: Commit**

```bash
build-brief ./gradlew clean build
git add frontend/src/app/features/items/item-api.ts frontend/src/app/features/items/item-api.spec.ts
git commit -m "feat(frontend): add item and item image fallback URLs

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

### Task 4: Composant partagé `ImageUpload`

**Files:**
- Create (via CLI): `frontend/src/app/shared/image-upload/image-upload.ts`
- Create: `frontend/src/app/shared/image-upload/image-upload.html`
- Test: `frontend/src/app/shared/image-upload/image-upload.spec.ts`

**Interfaces:**
- Consumes: Angular Material `MatIconModule`
- Produces (contrat utilisé par les plans items et profil) :
  - `export const ACCEPTED_IMAGE_TYPES: readonly string[]` (`['image/jpeg', 'image/png']`)
  - `export function isAcceptedImage(file: File): boolean`
  - composant `ImageUpload`, selector `app-image-upload`
  - inputs : `currentImageUrl = input<string | null>(null)`, `label = input('Image')`, `fallbackIcon = input('image')`, `error = input<string | null>(null)`
  - output : `fileSelected = output<File | null>()` (`null` quand la sélection est vidée ou refusée)
  - DOM : `img[data-testid="image-upload-preview"]`, `mat-icon[data-testid="image-upload-fallback"]`, `input[type=file][data-testid="image-upload-input"]`, message d'erreur en `p[role="alert"]`
  - message de refus : `Only JPEG and PNG images are accepted.`

Comportement : l'aperçu local (`URL.createObjectURL`) prime sur `currentImageUrl` ; si l'image courante échoue (404 : aucune image), l'icône de repli est affichée ; l'état d'échec est réinitialisé quand `currentImageUrl` change (cache-busting après upload) ; l'object URL est révoqué à chaque nouvelle sélection et à la destruction. Le contrôle de `file.type` double l'attribut `accept`, contournable par l'utilisateur — le backend répondrait sinon un 500 sans payload.

- [ ] **Step 1: Générer le squelette**

```bash
cd frontend && npx ng generate component shared/image-upload --style=none
```

Expected: création de `image-upload.ts`, `image-upload.html`, `image-upload.spec.ts` sous `src/app/shared/image-upload/`.

- [ ] **Step 2: Écrire le test en échec** — remplacer le contenu de `image-upload.spec.ts` :

```ts
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { ImageUpload, isAcceptedImage } from './image-upload';

describe('isAcceptedImage', () => {
  it('accepts JPEG and PNG files only', () => {
    expect(isAcceptedImage(new File([''], 'a.jpg', { type: 'image/jpeg' }))).toBe(true);
    expect(isAcceptedImage(new File([''], 'a.png', { type: 'image/png' }))).toBe(true);
    expect(isAcceptedImage(new File([''], 'a.gif', { type: 'image/gif' }))).toBe(false);
    expect(isAcceptedImage(new File([''], 'a.png', { type: '' }))).toBe(false);
  });
});

describe('ImageUpload', () => {
  let fixture: ComponentFixture<ImageUpload>;
  let emitted: Array<File | null>;
  let createObjectURL: ReturnType<typeof vi.fn>;
  let revokeObjectURL: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    // jsdom does not implement object URLs.
    let counter = 0;
    createObjectURL = vi.fn(() => `blob:preview-${++counter}`);
    revokeObjectURL = vi.fn();
    URL.createObjectURL = createObjectURL as unknown as typeof URL.createObjectURL;
    URL.revokeObjectURL = revokeObjectURL as unknown as typeof URL.revokeObjectURL;

    await TestBed.configureTestingModule({ imports: [ImageUpload] }).compileComponents();

    fixture = TestBed.createComponent(ImageUpload);
    emitted = [];
    fixture.componentInstance.fileSelected.subscribe((file) => emitted.push(file));
    fixture.detectChanges();
    await fixture.whenStable();
  });

  afterEach(() => vi.restoreAllMocks());

  const query = <T extends Element>(selector: string): T | null =>
    (fixture.nativeElement as HTMLElement).querySelector<T>(selector);

  function selectFile(file: File | null): void {
    const input = query<HTMLInputElement>('[data-testid="image-upload-input"]')!;
    Object.defineProperty(input, 'files', { value: file ? [file] : [], configurable: true });
    input.dispatchEvent(new Event('change'));
    fixture.detectChanges();
  }

  it('shows the fallback icon when there is no current image', () => {
    expect(query('[data-testid="image-upload-fallback"]')).not.toBeNull();
    expect(query('[data-testid="image-upload-preview"]')).toBeNull();
  });

  it('shows the current image and uses the label as alt text', () => {
    fixture.componentRef.setInput('currentImageUrl', '/api/v1/items/i-1/image');
    fixture.componentRef.setInput('label', 'Item image');
    fixture.detectChanges();

    const img = query<HTMLImageElement>('[data-testid="image-upload-preview"]')!;
    expect(img.getAttribute('src')).toBe('/api/v1/items/i-1/image');
    expect(img.getAttribute('alt')).toBe('Item image');
  });

  it('restricts the file picker to JPEG and PNG', () => {
    const input = query<HTMLInputElement>('[data-testid="image-upload-input"]')!;
    expect(input.getAttribute('accept')).toBe('image/jpeg,image/png');
  });

  it('falls back to the icon when the current image fails to load (404)', () => {
    fixture.componentRef.setInput('currentImageUrl', '/api/v1/me/picture');
    fixture.componentRef.setInput('fallbackIcon', 'account_circle');
    fixture.detectChanges();

    query('[data-testid="image-upload-preview"]')!.dispatchEvent(new Event('error'));
    fixture.detectChanges();

    const icon = query('[data-testid="image-upload-fallback"]')!;
    expect(icon.textContent?.trim()).toBe('account_circle');
    expect(query('[data-testid="image-upload-preview"]')).toBeNull();
  });

  it('retries displaying the image when the current image URL changes after a failure', () => {
    fixture.componentRef.setInput('currentImageUrl', '/api/v1/me/picture');
    fixture.detectChanges();
    query('[data-testid="image-upload-preview"]')!.dispatchEvent(new Event('error'));
    fixture.detectChanges();

    fixture.componentRef.setInput('currentImageUrl', '/api/v1/me/picture?v=1');
    fixture.detectChanges();

    const img = query<HTMLImageElement>('[data-testid="image-upload-preview"]')!;
    expect(img.getAttribute('src')).toBe('/api/v1/me/picture?v=1');
  });

  it('emits an accepted file and previews it instead of the current image', () => {
    fixture.componentRef.setInput('currentImageUrl', '/api/v1/items/i-1/image');
    fixture.detectChanges();
    const file = new File(['png'], 'cola.png', { type: 'image/png' });

    selectFile(file);

    expect(emitted).toEqual([file]);
    expect(createObjectURL).toHaveBeenCalledWith(file);
    const img = query<HTMLImageElement>('[data-testid="image-upload-preview"]')!;
    expect(img.getAttribute('src')).toBe('blob:preview-1');
    expect(query('[role="alert"]')).toBeNull();
  });

  it('rejects a non JPEG/PNG file, emits null and drops the previous preview', () => {
    selectFile(new File(['png'], 'cola.png', { type: 'image/png' }));

    selectFile(new File(['gif'], 'cola.gif', { type: 'image/gif' }));

    expect(emitted.at(-1)).toBeNull();
    expect(revokeObjectURL).toHaveBeenCalledWith('blob:preview-1');
    expect(query('[role="alert"]')?.textContent).toContain(
      'Only JPEG and PNG images are accepted.',
    );
    expect(query('[data-testid="image-upload-preview"]')).toBeNull();
    expect(query<HTMLInputElement>('[data-testid="image-upload-input"]')!.value).toBe('');
  });

  it('emits null when the selection is cleared', () => {
    selectFile(new File(['png'], 'cola.png', { type: 'image/png' }));

    selectFile(null);

    expect(emitted).toHaveLength(2);
    expect(emitted[1]).toBeNull();
  });

  it('revokes the previous preview when another file is selected', () => {
    selectFile(new File(['1'], 'one.png', { type: 'image/png' }));

    selectFile(new File(['2'], 'two.jpg', { type: 'image/jpeg' }));

    expect(revokeObjectURL).toHaveBeenCalledWith('blob:preview-1');
    const img = query<HTMLImageElement>('[data-testid="image-upload-preview"]')!;
    expect(img.getAttribute('src')).toBe('blob:preview-2');
  });

  it('revokes the active preview when destroyed', () => {
    selectFile(new File(['png'], 'cola.png', { type: 'image/png' }));

    fixture.destroy();

    expect(revokeObjectURL).toHaveBeenCalledWith('blob:preview-1');
  });

  it('displays an error provided by the parent (e.g. upload failure)', () => {
    fixture.componentRef.setInput('error', 'The image could not be uploaded.');
    fixture.detectChanges();

    expect(query('[role="alert"]')?.textContent).toContain('The image could not be uploaded.');
  });
});
```

- [ ] **Step 3: Lancer le test, vérifier l'échec**

Run: `cd frontend && npm test -- --include src/app/shared/image-upload/image-upload.spec.ts`
Expected: FAIL — `isAcceptedImage` non exporté, inputs `currentImageUrl`/`label`/`fallbackIcon`/`error` inconnus du squelette généré.

- [ ] **Step 4: Implémentation minimale**

`frontend/src/app/shared/image-upload/image-upload.ts` :

```ts
import { Component, computed, DestroyRef, inject, input, linkedSignal, output, signal } from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

/** Image types accepted by the backend `MultipartFileValidator`. */
export const ACCEPTED_IMAGE_TYPES: readonly string[] = ['image/jpeg', 'image/png'];

export function isAcceptedImage(file: File): boolean {
  return ACCEPTED_IMAGE_TYPES.includes(file.type);
}

@Component({
  selector: 'app-image-upload',
  imports: [MatIconModule],
  templateUrl: './image-upload.html',
})
export class ImageUpload {
  readonly currentImageUrl = input<string | null>(null);
  readonly label = input('Image');
  readonly fallbackIcon = input('image');
  readonly error = input<string | null>(null);

  readonly fileSelected = output<File | null>();

  protected readonly accept = ACCEPTED_IMAGE_TYPES.join(',');
  protected readonly previewUrl = signal<string | null>(null);
  protected readonly typeError = signal<string | null>(null);
  // Reset whenever the parent points to a new URL (e.g. cache-busted after an upload).
  private readonly currentImageFailed = linkedSignal({
    source: this.currentImageUrl,
    computation: () => false,
  });

  protected readonly displayedUrl = computed(
    () => this.previewUrl() ?? (this.currentImageFailed() ? null : this.currentImageUrl()),
  );
  protected readonly message = computed(() => this.typeError() ?? this.error());

  constructor() {
    inject(DestroyRef).onDestroy(() => this.revokePreview());
  }

  protected onFileChange(event: Event): void {
    const fileInput = event.target as HTMLInputElement;
    const file = fileInput.files?.[0] ?? null;

    this.revokePreview();

    if (file && !isAcceptedImage(file)) {
      this.typeError.set('Only JPEG and PNG images are accepted.');
      fileInput.value = '';
      this.fileSelected.emit(null);
      return;
    }

    this.typeError.set(null);
    this.previewUrl.set(file ? URL.createObjectURL(file) : null);
    this.fileSelected.emit(file);
  }

  protected onImageError(): void {
    if (!this.previewUrl()) {
      this.currentImageFailed.set(true);
    }
  }

  private revokePreview(): void {
    const url = this.previewUrl();
    if (url) {
      URL.revokeObjectURL(url);
      this.previewUrl.set(null);
    }
  }
}
```

`frontend/src/app/shared/image-upload/image-upload.html` :

```html
<div class="flex items-center gap-4">
  @if (displayedUrl(); as url) {
    <img
      [src]="url"
      [alt]="label()"
      class="h-24 w-24 rounded object-cover"
      data-testid="image-upload-preview"
      (error)="onImageError()"
    />
  } @else {
    <mat-icon
      class="!h-24 !w-24 !text-8xl text-gray-400"
      aria-hidden="true"
      data-testid="image-upload-fallback"
      >{{ fallbackIcon() }}</mat-icon
    >
  }

  <label class="flex flex-col gap-1">
    <span class="text-sm font-medium">{{ label() }}</span>
    <input
      type="file"
      [accept]="accept"
      data-testid="image-upload-input"
      (change)="onFileChange($event)"
    />
  </label>
</div>

@if (message(); as text) {
  <p class="mt-2 text-sm text-red-600" role="alert">{{ text }}</p>
}
```

Note : `mat-icon` a pour contenu textuel le nom de la ligature ; prettier peut reformater la balise, le test utilise `textContent.trim()`.

- [ ] **Step 5: Relancer le test, vérifier le succès**

Run: `cd frontend && npm test -- --include src/app/shared/image-upload/image-upload.spec.ts`
Expected: PASS (12 tests).

- [ ] **Step 6: Suite complète + format**

Run: `cd frontend && npm run format:check && npm test`
Expected: format OK (sinon `npm run format` puis relancer) ; toute la suite frontend PASS.

- [ ] **Step 7: Commit**

```bash
build-brief ./gradlew clean build
git add frontend/src/app/shared/image-upload/
git commit -m "feat(frontend): add shared image upload component

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

### Task 5: Intégration

- [ ] **Step 1: Vérification finale**

Run: `build-brief ./gradlew clean build`
Expected: BUILD SUCCESSFUL, 251 tests backend, `:frontend:npmBuild` et `:frontend:npmTest` verts.

- [ ] **Step 2: Fusion fast-forward et nettoyage**

```bash
BASE=$(git show-ref --verify --quiet refs/heads/develop && echo develop || echo main)
git switch "$BASE"
git merge --ff-only feat/frontend-shared-prerequisites
git branch -d feat/frontend-shared-prerequisites
```

Pas de mise à jour de `frontend/README.md` ni de `docs/features-front-a-implementer.md` dans ce plan : aucune fonctionnalité visible n'est livrée ; chaque plan de feature documente ce qu'il livre.
