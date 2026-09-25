# Gestion des vending machines (édition, suppression) — Plan d'implémentation

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ajouter le CRUD admin manquant sur les vending machines côté Angular : édition complète par `PUT`, suppression par `DELETE`, actions disponibles depuis la liste et le détail, et non-régression e2e.

**Architecture:** La lecture reste hypermédia et signalée par `httpResource()` ; les mutations utilisent `HttpClient` sur le lien `self` si et seulement si l'affordance HAL-FORMS correspondante est présente. L'édition est une page dédiée `/machines/:id/edit` protégée par `adminGuard`, avec Signal Forms, payload complet de remplacement, validation client stricte et traitement des erreurs backend compatible `InvalidBusinessObject`.

**Tech Stack:** Angular 22 standalone/zoneless, Signal Forms (`@angular/forms/signals`), Angular Material, HAL/HAL-FORMS Spring HATEOAS, Vitest via `@angular/build:unit-test`, Playwright e2e mocké.

**Spec:** `docs/specs/frontend-gestion-vending-machines.md`

**Prérequis:** `docs/plans/00-frontend-prerequis-partages.md` exécuté.

La todo 1 de la spec (« Persister les specs ») est déjà faite : `docs/specs/frontend-gestion-vending-machines.md` existe et a été lu ; ce plan la saute volontairement.

## Global Constraints

- Nommage de fichiers style guide 2025 : `machine-edit.ts`, jamais `machine-edit.component.ts`.
- Services Angular : utiliser `@Service()` si un service est ajouté ; aucun service n'est nécessaire dans ce plan.
- Lectures HTTP : `httpResource()` ; mutations `POST`/`PUT`/`DELETE` : `HttpClient` directement.
- Nouveaux formulaires : Signal Forms (`@angular/forms/signals`).
- Générer les composants via Angular CLI : `cd frontend && npx ng generate component <path> --style=none`.
- Tests unitaires uniquement via `cd frontend && npm test` (jamais `npx vitest run`) ; ciblage avec `npm test -- --include src/app/...spec.ts`.
- E2E : `cd frontend && npm run e2e`.
- Gradle uniquement via `build-brief ./gradlew ...`; `build-brief ./gradlew clean build` doit passer avant chaque commit.
- Branche : `feat/frontend-machines-edit-delete` depuis `develop` ; `develop` n'existe pas encore au 2026-09-25, seulement `main`, donc partir de `main`.
- Commits : style `feat(frontend): ...` avec trailer `Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>`.
- Fusion : fast-forward only (`git merge --ff-only`) et suppression de branche locale après fusion.
- Aucun changement backend dans ce plan ; noter seulement que l'ajout de `@Valid` sur `VendingMachineCrudRestController.update` serait une amélioration backend distincte.

## Review Focus

- **Champ obligatoire vidé dans le formulaire d'édition** : aucun `PUT` ne doit partir, car `@RequestBody` n'a pas `@Valid` côté backend et un `null` peut produire une 500. Test ajouté en Task 3.
- **`lastIntervention` ISO string malgré le type existant `Date | null`** : le payload doit renvoyer la chaîne inchangée pour ne pas effacer/altérer le remplacement complet. Test ajouté en Task 3.
- **Affordance absente mais lien `self` présent** : les boutons edit/delete doivent rester masqués et les helpers doivent renvoyer `undefined`. Tests ajoutés en Tasks 1, 5 et 6.
- **Annulation de suppression** : aucune requête `DELETE` ne doit être émise après fermeture du dialog avec `false`/`undefined`. Tests ajoutés en Tasks 5 et 6.
- **Entrée directe `/machines/:id/edit` sans state navigation** : la page doit retomber sur `machineUrl(id)` et la route `machines/:id` ne doit pas capturer `machines/:id/edit`. Tests ajoutés en Tasks 2 et 3.

---

## File Structure

| Fichier | Action | Responsabilité |
| --- | --- | --- |
| `frontend/src/app/features/machines/models/enums.ts` | Modify | ajoute les tableaux de valeurs pour alimenter les `mat-select` des statuts |
| `frontend/src/app/features/machines/models/vending-machine.ts` | Modify | ajoute `VendingMachineToUpdate` et `toUpdatePayload(machine)` ; réutilise `Address` car `AddressDto` lecture/écriture a la même forme |
| `frontend/src/app/features/machines/vending-machine-api.ts` | Modify | ajoute `updateHref(machine)` et `deleteHref(machine)` basés sur `_templates` + `self` |
| `frontend/src/app/features/machines/vending-machine-api.spec.ts` | Modify | verrouille les helpers d'affordances |
| `frontend/src/app/app.routes.ts` | Modify | ajoute `/machines/:id/edit` après `/machines/new` et avant `/machines/:id`, avec `adminGuard` |
| `frontend/src/app/app.routes.spec.ts` | Create | test de routage pour garantir que `machines/:id/edit` matche `MachineEdit` |
| `frontend/src/app/features/machines/machine-edit/machine-edit.ts` | Create via CLI | page d'édition, lecture `httpResource`, Signal Form, `PUT`, erreurs |
| `frontend/src/app/features/machines/machine-edit/machine-edit.html` | Create via CLI | formulaire complet d'édition, serial number/last intervention en lecture seule |
| `frontend/src/app/features/machines/machine-edit/machine-edit.spec.ts` | Create via CLI | tests unitaires de chargement, fallback URL, payload, validations, erreurs |
| `frontend/src/app/features/machines/machine-delete-dialog/machine-delete-dialog.ts` | Create via CLI | dialog de confirmation destructive |
| `frontend/src/app/features/machines/machine-delete-dialog/machine-delete-dialog.html` | Create via CLI | UI de confirmation avec `data-testid` imposés |
| `frontend/src/app/features/machines/machine-delete-dialog/machine-delete-dialog.spec.ts` | Create via CLI | tests dialog |
| `frontend/src/app/features/machines/machine-list/machine-list.ts` | Modify | actions edit/delete admin, suppression avec dialog, reload liste, snackbar |
| `frontend/src/app/features/machines/machine-list/machine-list.html` | Modify | boutons edit/delete conditionnés par affordances |
| `frontend/src/app/features/machines/machine-list/machine-list.spec.ts` | Modify | non-régression liste + actions admin |
| `frontend/src/app/features/machines/machine-detail/machine-detail.ts` | Modify | actions edit/delete admin, suppression avec navigation liste |
| `frontend/src/app/features/machines/machine-detail/machine-detail.html` | Modify | boutons edit/delete conditionnés par affordances |
| `frontend/src/app/features/machines/machine-detail/machine-detail.spec.ts` | Modify | non-régression détail + actions admin |
| `frontend/e2e/manage-machines.spec.ts` | Create | scénario admin : édition puis suppression depuis la liste, API mockée |
| `frontend/README.md` | Modify | mettre à jour le périmètre actuel et le reste à faire |
| `docs/features-front-a-implementer.md` | Modify | retirer l'édition/suppression machine du backlog |

---

### Task 0: Vérifier les prérequis et créer la branche

**Files:**
- Read-only check: `frontend/src/app/shared/models/hal.ts`
- Read-only check: `docs/specs/frontend-gestion-vending-machines.md`
- No code change.

**Interfaces:**
- Consumes: `export function linkHref(resource: HalResource | null | undefined, rel: string): string | undefined` depuis `frontend/src/app/shared/models/hal.ts`.
- Produces: branche locale `feat/frontend-machines-edit-delete` basée sur `develop` si présent, sinon `main`.

- [ ] **Step 1: Vérifier que la spec est déjà persistée**

```bash
test -f docs/specs/frontend-gestion-vending-machines.md
```

Expected: PASS. Si ce fichier manque, arrêter : la todo 1 de la spec n'est alors pas réellement faite.

- [ ] **Step 2: Vérifier que le prérequis `linkHref` est disponible**

```bash
grep -R "export function linkHref" frontend/src/app/shared/models/hal.ts
```

Expected: sortie contenant exactement `export function linkHref(`. Si absent, exécuter d'abord `docs/plans/00-frontend-prerequis-partages.md`.

- [ ] **Step 3: Créer la branche de travail**

```bash
git switch develop 2>/dev/null || git switch main
git pull --ff-only
git switch -c feat/frontend-machines-edit-delete
```

Expected: branche `feat/frontend-machines-edit-delete` créée. Au 2026-09-25, `develop` n'existe pas encore : `main` est le point de départ attendu.

---

### Task 1: Modèles, enum arrays et helpers d'affordances machine

**Files:**
- Modify: `frontend/src/app/features/machines/models/enums.ts`
- Modify: `frontend/src/app/features/machines/models/vending-machine.ts`
- Modify: `frontend/src/app/features/machines/vending-machine-api.ts`
- Test: `frontend/src/app/features/machines/vending-machine-api.spec.ts`

**Interfaces:**
- Consumes:
  - `export function linkHref(resource: HalResource | null | undefined, rel: string): string | undefined`
  - `export interface HalTemplate { method: string; ... }`
- Produces:
  - `export const POWER_STATUSES: readonly PowerStatus[]`
  - `export const WORKING_STATUSES: readonly WorkingStatus[]`
  - `export const CARD_SYSTEM_STATUSES: readonly CardSystemStatus[]`
  - `export const CHANGE_SYSTEM_STATUSES: readonly ChangeSystemStatus[]`
  - `export interface VendingMachineToUpdate`
  - `export function toUpdatePayload(machine: VendingMachine): VendingMachineToUpdate`
  - `export function updateHref(machine: VendingMachine): string | undefined`
  - `export function deleteHref(machine: VendingMachine): string | undefined`

- [ ] **Step 1: Écrire le test en échec** — remplacer `frontend/src/app/features/machines/vending-machine-api.spec.ts` par :

```ts
import { describe, expect, it } from 'vitest';
import { VendingMachine } from './models/vending-machine';
import { deleteHref, machinesPageUrl, updateHref } from './vending-machine-api';

describe('machinesPageUrl', () => {
  it('builds the paged URL from the resolved vending-machines href, converting the 0-based pageIndex to the backend’s 1-based page param', () => {
    expect(machinesPageUrl('/api/v1/vending-machines', 2, 10)).toBe(
      '/api/v1/vending-machines?page=3&size=10',
    );
  });

  it('returns undefined while the vending-machines href is not resolved yet', () => {
    expect(machinesPageUrl(undefined, 0, 10)).toBeUndefined();
  });

  it('preserves query params already present on the href (e.g. advanced search filters)', () => {
    expect(machinesPageUrl('/api/v1/vending-machines?city=Paris', 2, 10)).toBe(
      '/api/v1/vending-machines?city=Paris&page=3&size=10',
    );
  });
});

describe('updateHref', () => {
  it('returns the self href when a PUT affordance template is present', () => {
    const machine = machineWithTemplates({
      default: { method: 'PUT' },
      delete: { method: 'DELETE' },
    });

    expect(updateHref(machine)).toBe('/api/v1/vending-machines/m-1');
  });

  it('returns undefined when self exists but no PUT affordance template is present', () => {
    const machine = machineWithTemplates({ delete: { method: 'DELETE' } });

    expect(updateHref(machine)).toBeUndefined();
  });

  it('matches affordance methods case-insensitively', () => {
    const machine = machineWithTemplates({ update: { method: 'put' } });

    expect(updateHref(machine)).toBe('/api/v1/vending-machines/m-1');
  });

  it('returns undefined when the self link is missing', () => {
    const machine = { ...machineWithTemplates({ update: { method: 'PUT' } }), _links: {} };

    expect(updateHref(machine)).toBeUndefined();
  });
});

describe('deleteHref', () => {
  it('returns the self href when a DELETE affordance template is present', () => {
    const machine = machineWithTemplates({
      default: { method: 'PUT' },
      delete: { method: 'DELETE' },
    });

    expect(deleteHref(machine)).toBe('/api/v1/vending-machines/m-1');
  });

  it('returns undefined when self exists but no DELETE affordance template is present', () => {
    const machine = machineWithTemplates({ default: { method: 'PUT' } });

    expect(deleteHref(machine)).toBeUndefined();
  });
});

function machineWithTemplates(
  templates: NonNullable<VendingMachine['_templates']>,
): VendingMachine {
  return {
    id: 'm-1',
    serialNumber: 'SN-1',
    address: {
      latitude: 45.76,
      longitude: 4.84,
      streetNumber: 10,
      streetName: 'Rue de la Paix',
      postalCode: '69001',
      city: 'Lyon',
    },
    lastIntervention: '2026-09-25T10:15:00' as unknown as Date,
    temperature: 4,
    itemType: 'SNACK',
    powerStatus: 'POWER_ON',
    workingStatus: 'WORKING',
    rfidStatus: 'OK',
    smartCardStatus: 'OK',
    changeMoneyStatus: 'NORMAL',
    _links: { self: { href: '/api/v1/vending-machines/m-1' } },
    _templates: templates,
  };
}
```

- [ ] **Step 2: Lancer le test, vérifier l'échec**

Run: `cd frontend && npm test -- --include src/app/features/machines/vending-machine-api.spec.ts`

Expected: FAIL — `updateHref` et `deleteHref` ne sont pas exportés.

- [ ] **Step 3: Implémentation minimale** — remplacer `frontend/src/app/features/machines/models/enums.ts` par :

```ts
export type PowerStatus = 'POWER_ON' | 'POWER_OFF';
export type WorkingStatus = 'WORKING' | 'WARNING' | 'ERROR' | 'ALERT';
export type CardSystemStatus = 'FAILED' | 'OK';
export type ChangeSystemStatus = 'FULL' | 'EMPTY' | 'NORMAL';

export const POWER_STATUSES: readonly PowerStatus[] = ['POWER_ON', 'POWER_OFF'];
export const WORKING_STATUSES: readonly WorkingStatus[] = ['WORKING', 'WARNING', 'ERROR', 'ALERT'];
export const CARD_SYSTEM_STATUSES: readonly CardSystemStatus[] = ['OK', 'FAILED'];
export const CHANGE_SYSTEM_STATUSES: readonly ChangeSystemStatus[] = ['NORMAL', 'FULL', 'EMPTY'];
```

Remplacer `frontend/src/app/features/machines/models/vending-machine.ts` par :

```ts
import { HalResource } from '../../../shared/models/hal';
import { ItemType } from '../../../shared/models/item-type';
import { CardSystemStatus, ChangeSystemStatus, PowerStatus, WorkingStatus } from './enums';

export interface Address {
  latitude: number | null;
  longitude: number | null;
  streetNumber: number | null;
  streetName: string | null;
  postalCode: string | null;
  city: string | null;
}

export interface AddressToCreate {
  latitude: number | null;
  longitude: number | null;
  streetNumber: number | null;
  streetName: string;
  postalCode: string;
  city: string;
}

export interface VendingMachineToCreate {
  serialNumber: string;
  address: AddressToCreate;
  itemType: ItemType | null;
}

export interface VendingMachineToUpdate {
  address: Address;
  temperature: number | null;
  itemType: ItemType | null;
  powerStatus: PowerStatus | null;
  workingStatus: WorkingStatus | null;
  rfidStatus: CardSystemStatus | null;
  smartCardStatus: CardSystemStatus | null;
  changeMoneyStatus: ChangeSystemStatus | null;
  /**
   * Spring serialises LocalDateTime as an ISO string. The current read model is
   * typed as Date | null for display compatibility, but PUT must resend the raw
   * string unchanged when it came from the backend.
   */
  lastIntervention: string | null;
}

export interface VendingMachine extends HalResource {
  id: string;
  serialNumber: string | null;
  address: Address | null;
  lastIntervention: Date | string | null;
  temperature: number | null;
  itemType: ItemType | null;
  powerStatus: PowerStatus | null;
  workingStatus: WorkingStatus | null;
  rfidStatus: CardSystemStatus | null;
  smartCardStatus: CardSystemStatus | null;
  changeMoneyStatus: ChangeSystemStatus | null;
}

export interface ItemQuantity {
  itemId: string;
  itemName: string;
  quantity: number;
}

export interface VendingMachineStock extends HalResource {
  itemQuantities: ItemQuantity[];
}

export function toUpdatePayload(machine: VendingMachine): VendingMachineToUpdate {
  return {
    address: machine.address ?? {
      latitude: null,
      longitude: null,
      streetNumber: null,
      streetName: null,
      postalCode: null,
      city: null,
    },
    temperature: machine.temperature,
    itemType: machine.itemType,
    powerStatus: machine.powerStatus,
    workingStatus: machine.workingStatus,
    rfidStatus: machine.rfidStatus,
    smartCardStatus: machine.smartCardStatus,
    changeMoneyStatus: machine.changeMoneyStatus,
    lastIntervention:
      typeof machine.lastIntervention === 'string'
        ? machine.lastIntervention
        : machine.lastIntervention?.toISOString() ?? null,
  };
}
```

Remplacer `frontend/src/app/features/machines/vending-machine-api.ts` par :

```ts
import { environment } from '../../../environments/environment';
import { pagedUrl } from '../../shared/http/paged-url';
import { linkHref } from '../../shared/models/hal';
import { VendingMachine } from './models/vending-machine';

/** Used for the create mutation, out of scope for the root-link discovery below. */
export function machinesUrl(): string {
  return `${environment.apiBaseUrl}/vending-machines`;
}

/** `undefined` until the `vendingMachines` root link is resolved (see `ApiRootApi`). */
export function machinesPageUrl(
  vendingMachinesHref: string | undefined,
  pageIndex: number,
  pageSize: number,
): string | undefined {
  return pagedUrl(vendingMachinesHref, { page: pageIndex + 1, size: pageSize });
}

/**
 * Fallback used only when no HATEOAS `self` link is available (e.g. direct
 * navigation/page refresh on the detail or edit page). Prefer following the
 * resource's own `_links.self.href` when it is known.
 */
export function machineUrl(id: string): string {
  return `${machinesUrl()}/${id}`;
}

export function updateHref(machine: VendingMachine): string | undefined {
  return affordanceHref(machine, 'PUT');
}

export function deleteHref(machine: VendingMachine): string | undefined {
  return affordanceHref(machine, 'DELETE');
}

function affordanceHref(machine: VendingMachine, method: string): string | undefined {
  const hasAffordance = Object.values(machine._templates ?? {}).some(
    (template) => template.method.toUpperCase() === method,
  );

  return hasAffordance ? linkHref(machine, 'self') : undefined;
}
```

Note de vérification backend à conserver dans la PR : `AddressDto` lecture/écriture est exactement `{ latitude: Double, longitude: Double, streetNumber: Integer, streetName, postalCode, city }`; le type `Address` frontend est donc réutilisé pour `VendingMachineToUpdate.address`. Le nullability reste large côté TypeScript car la ressource lue peut être incomplète, mais le formulaire de la Task 3 rend tous les champs requis avant le `PUT`.

- [ ] **Step 4: Relancer le test, vérifier le succès**

Run: `cd frontend && npm test -- --include src/app/features/machines/vending-machine-api.spec.ts`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
build-brief ./gradlew clean build
git add frontend/src/app/features/machines/models/enums.ts \
  frontend/src/app/features/machines/models/vending-machine.ts \
  frontend/src/app/features/machines/vending-machine-api.ts \
  frontend/src/app/features/machines/vending-machine-api.spec.ts
git commit -m "feat(frontend): add machine affordance helpers

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

### Task 2: Route `/machines/:id/edit`

**Files:**
- Modify: `frontend/src/app/app.routes.ts`
- Test: `frontend/src/app/app.routes.spec.ts`

**Interfaces:**
- Consumes: `adminGuard: CanActivateFn`
- Produces: route lazy `machines/:id/edit` → `MachineEdit`, title `Edit vending machine`, protégée par `canActivate: [adminGuard]`.

- [ ] **Step 1: Écrire le test en échec** — créer `frontend/src/app/app.routes.spec.ts` :

```ts
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { beforeEach, describe, expect, it } from 'vitest';
import { TokenStore } from './core/auth/token-store';
import { MachineEdit } from './features/machines/machine-edit/machine-edit';
import { routes } from './app.routes';

function fakeJwt(payload: Record<string, unknown>): string {
  const encode = (value: unknown) =>
    btoa(JSON.stringify(value)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${encode({ alg: 'none' })}.${encode(payload)}.signature`;
}

describe('routes', () => {
  let backend: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [provideRouter(routes), provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    backend = TestBed.inject(HttpTestingController);
  });

  it('matches /machines/:id/edit with MachineEdit instead of the detail route', async () => {
    TestBed.inject(TokenStore).setAccessToken(
      fakeJwt({ sub: 'admin@vending.me', roles: ['ROLE_ADMIN'], exp: 4102444800 }),
    );

    const harness = await RouterTestingHarness.create();
    const component = await harness.navigateByUrl('/machines/m-1/edit', MachineEdit);
    harness.fixture.detectChanges();

    backend.expectOne('/api/v1/vending-machines/m-1').flush({
      id: 'm-1',
      serialNumber: 'SN-1',
      address: {
        latitude: 45.76,
        longitude: 4.84,
        streetNumber: 10,
        streetName: 'Rue de la Paix',
        postalCode: '69001',
        city: 'Lyon',
      },
      lastIntervention: null,
      temperature: 4,
      itemType: 'SNACK',
      powerStatus: 'POWER_ON',
      workingStatus: 'WORKING',
      rfidStatus: 'OK',
      smartCardStatus: 'OK',
      changeMoneyStatus: 'NORMAL',
      _links: { self: { href: '/api/v1/vending-machines/m-1' } },
      _templates: { default: { method: 'PUT' } },
    });
    await harness.fixture.whenStable();

    expect(component).toBeInstanceOf(MachineEdit);
  });
});
```

- [ ] **Step 2: Lancer le test, vérifier l'échec**

Run: `cd frontend && npm test -- --include src/app/app.routes.spec.ts`

Expected: FAIL — module `./features/machines/machine-edit/machine-edit` introuvable.

- [ ] **Step 3: Ajouter la route** — après la route `machines/new` et avant `machines/:id`, modifier `frontend/src/app/app.routes.ts` :

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
    path: 'machines/:id/edit',
    loadComponent: () =>
      import('./features/machines/machine-edit/machine-edit').then((m) => m.MachineEdit),
    canActivate: [adminGuard],
    title: 'Edit vending machine',
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

Ne pas relancer encore : le composant `MachineEdit` est créé en Task 3. Cette task est validée avec la Task 3.

- [ ] **Step 4: Commit après Task 3 seulement**

Le commit de `app.routes.ts` et `app.routes.spec.ts` se fait dans la Task 3 avec le composant, car la route ne compile pas sans `MachineEdit`.

---

### Task 3: Page `MachineEdit`

**Files:**
- Create via CLI: `frontend/src/app/features/machines/machine-edit/machine-edit.ts`
- Create via CLI: `frontend/src/app/features/machines/machine-edit/machine-edit.html`
- Test: `frontend/src/app/features/machines/machine-edit/machine-edit.spec.ts`
- Modify from Task 2: `frontend/src/app/app.routes.ts`
- Test from Task 2: `frontend/src/app/app.routes.spec.ts`

**Interfaces:**
- Consumes:
  - `export function machineUrl(id: string): string`
  - `export function updateHref(machine: VendingMachine): string | undefined`
  - `export function toUpdatePayload(machine: VendingMachine): VendingMachineToUpdate`
  - enum arrays from `models/enums.ts`
  - `ITEM_TYPES: readonly ItemType[]`
- Produces:
  - `export class MachineEdit`
  - route state type `{ href?: string }`
  - `submit(): void`
  - reads `history.state.href ?? machineUrl(id)`
  - emits `PUT` payload `VendingMachineToUpdate` to `updateHref(machine) ?? resourceUrl`

- [ ] **Step 1: Générer le composant**

```bash
cd frontend && npx ng generate component features/machines/machine-edit --style=none
```

Expected: création de `machine-edit.ts`, `machine-edit.html`, `machine-edit.spec.ts`.

- [ ] **Step 2: Écrire le test en échec** — remplacer `frontend/src/app/features/machines/machine-edit/machine-edit.spec.ts` par :

```ts
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { MachineEdit } from './machine-edit';

describe('MachineEdit', () => {
  let harness: RouterTestingHarness;
  let backend: HttpTestingController;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([
          { path: 'machines/:id/edit', component: MachineEdit },
          { path: 'machines/:id', children: [] },
        ]),
      ],
    }).compileComponents();

    backend = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    harness = await RouterTestingHarness.create();
  });

  afterEach(() => backend.verify());

  async function navigate(state: { href?: string } | null = null) {
    history.replaceState(state, '');
    const component = await harness.navigateByUrl('/machines/m-1/edit', MachineEdit);
    harness.fixture.detectChanges();
    return component;
  }

  function flushMachine(overrides: Partial<Record<string, unknown>> = {}) {
    backend.expectOne(overrides['expectedUrl'] === 'state' ? '/custom/machines/m-1' : '/api/v1/vending-machines/m-1').flush({
      id: 'm-1',
      serialNumber: 'SN-1',
      address: {
        latitude: 45.76,
        longitude: 4.84,
        streetNumber: 10,
        streetName: 'Rue de la Paix',
        postalCode: '69001',
        city: 'Lyon',
      },
      lastIntervention: '2026-09-25T10:15:00',
      temperature: 4,
      itemType: 'SNACK',
      powerStatus: 'POWER_ON',
      workingStatus: 'WORKING',
      rfidStatus: 'OK',
      smartCardStatus: 'OK',
      changeMoneyStatus: 'NORMAL',
      _links: { self: { href: '/api/v1/vending-machines/m-1' } },
      _templates: { default: { method: 'PUT' }, delete: { method: 'DELETE' } },
      ...overrides,
    });
    harness.fixture.detectChanges();
  }

  it('falls back to the id-based URL when no HATEOAS state is available', async () => {
    const component = await navigate();

    flushMachine();
    await harness.fixture.whenStable();

    expect(component.machine()?.serialNumber).toBe('SN-1');
    expect(component.machineForm.serialNumber().value()).toBe('SN-1');
  });

  it('follows the HATEOAS self link carried over via router navigation state', async () => {
    const component = await navigate({ href: '/custom/machines/m-1' });

    flushMachine({ expectedUrl: 'state' });
    await harness.fixture.whenStable();

    expect(component.machine()?.id).toBe('m-1');
  });

  it('prefills every editable field from the loaded resource', async () => {
    const component = await navigate();

    flushMachine();
    await harness.fixture.whenStable();

    expect(component.machineForm.address.latitude().value()).toBe(45.76);
    expect(component.machineForm.address.longitude().value()).toBe(4.84);
    expect(component.machineForm.address.streetNumber().value()).toBe(10);
    expect(component.machineForm.address.streetName().value()).toBe('Rue de la Paix');
    expect(component.machineForm.address.postalCode().value()).toBe('69001');
    expect(component.machineForm.address.city().value()).toBe('Lyon');
    expect(component.machineForm.temperature().value()).toBe(4);
    expect(component.machineForm.itemType().value()).toBe('SNACK');
    expect(component.machineForm.powerStatus().value()).toBe('POWER_ON');
    expect(component.machineForm.workingStatus().value()).toBe('WORKING');
    expect(component.machineForm.rfidStatus().value()).toBe('OK');
    expect(component.machineForm.smartCardStatus().value()).toBe('OK');
    expect(component.machineForm.changeMoneyStatus().value()).toBe('NORMAL');
    expect(component.machineForm.lastIntervention().value()).toBe('2026-09-25T10:15:00');
  });

  it('rejects missing required fields without calling PUT', async () => {
    const component = await navigate();
    flushMachine();
    await harness.fixture.whenStable();

    component.machineForm.temperature().value.set(null);
    component.submit();

    backend.expectNone('/api/v1/vending-machines/m-1');
    expect(component.machineForm().valid()).toBe(false);
    expect(component.machineForm.temperature().touched()).toBe(true);
  });

  it('sends a complete replacement payload to the self href and keeps lastIntervention unchanged', async () => {
    const component = await navigate();
    flushMachine();
    await harness.fixture.whenStable();

    component.machineForm.address.city().value.set('Paris');
    component.machineForm.temperature().value.set(7);
    component.machineForm.workingStatus().value.set('WARNING');
    component.submit();

    const request = backend.expectOne('/api/v1/vending-machines/m-1');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({
      address: {
        latitude: 45.76,
        longitude: 4.84,
        streetNumber: 10,
        streetName: 'Rue de la Paix',
        postalCode: '69001',
        city: 'Paris',
      },
      temperature: 7,
      itemType: 'SNACK',
      powerStatus: 'POWER_ON',
      workingStatus: 'WARNING',
      rfidStatus: 'OK',
      smartCardStatus: 'OK',
      changeMoneyStatus: 'NORMAL',
      lastIntervention: '2026-09-25T10:15:00',
    });
    request.flush({ id: 'm-1' });
    await harness.fixture.whenStable();

    expect(component.errorMessage()).toBeNull();
    expect(component.submitting()).toBe(false);
    expect(router.url).toBe('/machines/m-1');
  });

  it('uses the loaded resource URL as mutation fallback when the PUT affordance is missing', async () => {
    const component = await navigate({ href: '/custom/machines/m-1' });
    flushMachine({ expectedUrl: 'state', _templates: {} });
    await harness.fixture.whenStable();

    component.submit();

    const request = backend.expectOne('/custom/machines/m-1');
    expect(request.request.method).toBe('PUT');
    request.flush({ id: 'm-1' });
    await harness.fixture.whenStable();
  });

  it('shows field-specific validation messages returned by InvalidBusinessObject', async () => {
    const component = await navigate();
    flushMachine();
    await harness.fixture.whenStable();

    component.submit();

    backend.expectOne('/api/v1/vending-machines/m-1').flush(
      {
        message: 'Validation failed',
        errors: [
          {
            field: 'address.city',
            code: 'validation.constraints.not_blank',
            defaultMessage: 'must not be blank',
          },
        ],
      },
      { status: 400, statusText: 'Bad Request' },
    );
    await harness.fixture.whenStable();

    expect(component.fieldErrors()).toEqual({ 'address.city': 'must not be blank' });
    expect(component.errorMessage()).toBeNull();
    expect(component.submitting()).toBe(false);
  });

  it('shows the update-specific generic message for non-validation failures', async () => {
    const component = await navigate();
    flushMachine();
    await harness.fixture.whenStable();

    component.submit();

    backend
      .expectOne('/api/v1/vending-machines/m-1')
      .flush({ message: 'Internal Server Error' }, { status: 500, statusText: 'Server Error' });
    await harness.fixture.whenStable();

    expect(component.errorMessage()).toBe('The vending machine could not be updated.');
    expect(component.fieldErrors()).toEqual({});
    expect(component.submitting()).toBe(false);
  });
});
```

- [ ] **Step 3: Lancer le test, vérifier l'échec**

Run: `cd frontend && npm test -- --include src/app/features/machines/machine-edit/machine-edit.spec.ts`

Expected: FAIL — le squelette généré n'a pas `machine`, `machineForm`, `submit`, ni le chargement `httpResource()`.

- [ ] **Step 4: Implémentation minimale** — remplacer `frontend/src/app/features/machines/machine-edit/machine-edit.ts` par :

```ts
import { DatePipe } from '@angular/common';
import { HttpClient, httpResource } from '@angular/common/http';
import { Component, computed, effect, inject, signal } from '@angular/core';
import { form, FormField, required, submit } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { ITEM_TYPES } from '../../../shared/models/item-type';
import { parseValidationErrors } from '../../../shared/models/validation-error';
import { ValueOrEmptyPipe } from '../../../shared/value-or-empty-pipe';
import {
  CARD_SYSTEM_STATUSES,
  CHANGE_SYSTEM_STATUSES,
  POWER_STATUSES,
  WORKING_STATUSES,
} from '../models/enums';
import { toUpdatePayload, VendingMachine, VendingMachineToUpdate } from '../models/vending-machine';
import { machineUrl, updateHref } from '../vending-machine-api';

const FORM_FIELDS = [
  'address.latitude',
  'address.longitude',
  'address.streetNumber',
  'address.streetName',
  'address.postalCode',
  'address.city',
  'temperature',
  'itemType',
  'powerStatus',
  'workingStatus',
  'rfidStatus',
  'smartCardStatus',
  'changeMoneyStatus',
];

interface EditNavigationState {
  href?: string;
}

@Component({
  selector: 'app-machine-edit',
  imports: [
    DatePipe,
    FormField,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressBarModule,
    MatSelectModule,
    RouterLink,
    ValueOrEmptyPipe,
  ],
  templateUrl: './machine-edit.html',
})
export class MachineEdit {
  private readonly route = inject(ActivatedRoute);
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  protected readonly itemTypes = ITEM_TYPES;
  protected readonly powerStatuses = POWER_STATUSES;
  protected readonly workingStatuses = WORKING_STATUSES;
  protected readonly cardSystemStatuses = CARD_SYSTEM_STATUSES;
  protected readonly changeSystemStatuses = CHANGE_SYSTEM_STATUSES;

  private readonly id = this.route.snapshot.paramMap.get('id')!;
  private readonly resourceUrl =
    (history.state as EditNavigationState | null)?.href ?? machineUrl(this.id);
  private readonly resource = httpResource<VendingMachine>(() => this.resourceUrl);

  readonly isLoading = this.resource.isLoading;
  readonly hasError = computed(() => this.resource.error() !== undefined);
  readonly machine = computed(() => this.resource.value());

  readonly machineFormModel = signal<VendingMachineToUpdate>({
    address: {
      latitude: null,
      longitude: null,
      streetNumber: null,
      streetName: '',
      postalCode: '',
      city: '',
    },
    temperature: null,
    itemType: null,
    powerStatus: null,
    workingStatus: null,
    rfidStatus: null,
    smartCardStatus: null,
    changeMoneyStatus: null,
    lastIntervention: null,
  });

  readonly machineForm = form(this.machineFormModel, (path) => {
    required(path.address.latitude);
    required(path.address.longitude);
    required(path.address.streetNumber);
    required(path.address.streetName);
    required(path.address.postalCode);
    required(path.address.city);
    required(path.temperature);
    required(path.itemType);
    required(path.powerStatus);
    required(path.workingStatus);
    required(path.rfidStatus);
    required(path.smartCardStatus);
    required(path.changeMoneyStatus);
  });

  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly fieldErrors = signal<Record<string, string>>({});

  constructor() {
    effect(() => {
      const machine = this.machine();
      if (!machine) {
        return;
      }

      this.machineFormModel.set(toUpdatePayload(machine));
    });
  }

  submit(): void {
    void submit(this.machineForm, async () => {
      const machine = this.machine();
      if (!machine) {
        return;
      }

      this.submitting.set(true);
      this.errorMessage.set(null);
      this.fieldErrors.set({});

      try {
        await firstValueFrom(
          this.http.put(updateHref(machine) ?? this.resourceUrl, this.machineFormModel()),
        );
        this.submitting.set(false);
        void this.router.navigate(['/machines', machine.id]);
      } catch (error) {
        this.submitting.set(false);

        const parsed = parseValidationErrors(error, FORM_FIELDS);
        if (parsed) {
          this.fieldErrors.set(parsed.fieldErrors);
          this.errorMessage.set(
            parsed.hasObjectLevelError ? 'The vending machine could not be updated.' : null,
          );
          return;
        }

        this.errorMessage.set('The vending machine could not be updated.');
      }
    });
  }
}
```

Remplacer `frontend/src/app/features/machines/machine-edit/machine-edit.html` par :

```html
<div class="mb-4 flex items-center justify-between">
  <h1 class="text-xl font-semibold">Edit vending machine</h1>
  <a mat-button [routerLink]="['/machines', machine()?.id ?? '']">Back to details</a>
</div>

@if (isLoading()) {
  <mat-progress-bar mode="indeterminate" />
}

@if (hasError()) {
  <p class="text-red-600" role="alert">The vending machine could not be loaded.</p>
} @else if (machine(); as machine) {
  <mat-card class="w-full max-w-3xl">
    <mat-card-content>
      <form class="flex flex-col gap-4 pt-4" (submit)="$event.preventDefault(); submit()">
        <div class="grid grid-cols-2 gap-4">
          <div>
            <p class="text-sm font-medium text-gray-600">Serial number</p>
            <p>{{ machine.serialNumber | valueOrEmpty }}</p>
          </div>
          <div>
            <p class="text-sm font-medium text-gray-600">Last intervention</p>
            <p>{{ machineForm.lastIntervention().value() | date: 'short' | valueOrEmpty }}</p>
          </div>
        </div>

        <mat-form-field>
          <mat-label>Item type</mat-label>
          <mat-select [formField]="machineForm.itemType">
            @for (itemType of itemTypes; track itemType) {
              <mat-option [value]="itemType">{{ itemType }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
        @if (machineForm.itemType().touched() && machineForm.itemType().errors().length) {
          <p class="text-sm text-red-600" role="alert">Item type is required.</p>
        }
        @if (fieldErrors()['itemType']; as message) {
          <p class="text-sm text-red-600" role="alert">{{ message }}</p>
        }

        <div class="grid grid-cols-2 gap-4">
          <mat-form-field>
            <mat-label>Latitude</mat-label>
            <input matInput type="number" [formField]="machineForm.address.latitude" />
          </mat-form-field>
          <mat-form-field>
            <mat-label>Longitude</mat-label>
            <input matInput type="number" [formField]="machineForm.address.longitude" />
          </mat-form-field>
        </div>
        @if (
          (machineForm.address.latitude().touched() &&
            machineForm.address.latitude().errors().length) ||
          (machineForm.address.longitude().touched() &&
            machineForm.address.longitude().errors().length)
        ) {
          <p class="text-sm text-red-600" role="alert">Latitude and longitude are required.</p>
        }
        @if (fieldErrors()['address.latitude'] ?? fieldErrors()['address.longitude']; as message) {
          <p class="text-sm text-red-600" role="alert">{{ message }}</p>
        }

        <div class="grid grid-cols-3 gap-4">
          <mat-form-field class="col-span-1">
            <mat-label>Street number</mat-label>
            <input matInput type="number" [formField]="machineForm.address.streetNumber" />
          </mat-form-field>
          <mat-form-field class="col-span-2">
            <mat-label>Street name</mat-label>
            <input matInput type="text" [formField]="machineForm.address.streetName" />
          </mat-form-field>
        </div>
        @if (
          (machineForm.address.streetNumber().touched() &&
            machineForm.address.streetNumber().errors().length) ||
          (machineForm.address.streetName().touched() &&
            machineForm.address.streetName().errors().length)
        ) {
          <p class="text-sm text-red-600" role="alert">Street number and name are required.</p>
        }
        @if (
          fieldErrors()['address.streetNumber'] ?? fieldErrors()['address.streetName'];
          as message
        ) {
          <p class="text-sm text-red-600" role="alert">{{ message }}</p>
        }

        <div class="grid grid-cols-2 gap-4">
          <mat-form-field>
            <mat-label>Postal code</mat-label>
            <input matInput type="text" [formField]="machineForm.address.postalCode" />
          </mat-form-field>
          <mat-form-field>
            <mat-label>City</mat-label>
            <input matInput type="text" [formField]="machineForm.address.city" />
          </mat-form-field>
        </div>
        @if (
          (machineForm.address.postalCode().touched() &&
            machineForm.address.postalCode().errors().length) ||
          (machineForm.address.city().touched() && machineForm.address.city().errors().length)
        ) {
          <p class="text-sm text-red-600" role="alert">Postal code and city are required.</p>
        }
        @if (fieldErrors()['address.postalCode'] ?? fieldErrors()['address.city']; as message) {
          <p class="text-sm text-red-600" role="alert">{{ message }}</p>
        }

        <mat-form-field>
          <mat-label>Temperature</mat-label>
          <input matInput type="number" [formField]="machineForm.temperature" />
        </mat-form-field>
        @if (machineForm.temperature().touched() && machineForm.temperature().errors().length) {
          <p class="text-sm text-red-600" role="alert">Temperature is required.</p>
        }
        @if (fieldErrors()['temperature']; as message) {
          <p class="text-sm text-red-600" role="alert">{{ message }}</p>
        }

        <div class="grid grid-cols-2 gap-4">
          <mat-form-field>
            <mat-label>Power status</mat-label>
            <mat-select [formField]="machineForm.powerStatus">
              @for (status of powerStatuses; track status) {
                <mat-option [value]="status">{{ status }}</mat-option>
              }
            </mat-select>
          </mat-form-field>
          <mat-form-field>
            <mat-label>Working status</mat-label>
            <mat-select [formField]="machineForm.workingStatus">
              @for (status of workingStatuses; track status) {
                <mat-option [value]="status">{{ status }}</mat-option>
              }
            </mat-select>
          </mat-form-field>
        </div>
        @if (
          (machineForm.powerStatus().touched() && machineForm.powerStatus().errors().length) ||
          (machineForm.workingStatus().touched() && machineForm.workingStatus().errors().length)
        ) {
          <p class="text-sm text-red-600" role="alert">Power and working statuses are required.</p>
        }
        @if (fieldErrors()['powerStatus'] ?? fieldErrors()['workingStatus']; as message) {
          <p class="text-sm text-red-600" role="alert">{{ message }}</p>
        }

        <div class="grid grid-cols-3 gap-4">
          <mat-form-field>
            <mat-label>RFID status</mat-label>
            <mat-select [formField]="machineForm.rfidStatus">
              @for (status of cardSystemStatuses; track status) {
                <mat-option [value]="status">{{ status }}</mat-option>
              }
            </mat-select>
          </mat-form-field>
          <mat-form-field>
            <mat-label>Smart card status</mat-label>
            <mat-select [formField]="machineForm.smartCardStatus">
              @for (status of cardSystemStatuses; track status) {
                <mat-option [value]="status">{{ status }}</mat-option>
              }
            </mat-select>
          </mat-form-field>
          <mat-form-field>
            <mat-label>Change money status</mat-label>
            <mat-select [formField]="machineForm.changeMoneyStatus">
              @for (status of changeSystemStatuses; track status) {
                <mat-option [value]="status">{{ status }}</mat-option>
              }
            </mat-select>
          </mat-form-field>
        </div>
        @if (
          (machineForm.rfidStatus().touched() && machineForm.rfidStatus().errors().length) ||
          (machineForm.smartCardStatus().touched() &&
            machineForm.smartCardStatus().errors().length) ||
          (machineForm.changeMoneyStatus().touched() &&
            machineForm.changeMoneyStatus().errors().length)
        ) {
          <p class="text-sm text-red-600" role="alert">Payment statuses are required.</p>
        }
        @if (
          fieldErrors()['rfidStatus'] ??
          fieldErrors()['smartCardStatus'] ??
          fieldErrors()['changeMoneyStatus'];
          as message
        ) {
          <p class="text-sm text-red-600" role="alert">{{ message }}</p>
        }

        @if (errorMessage(); as message) {
          <p class="text-sm text-red-600" role="alert">{{ message }}</p>
        }

        <div class="flex gap-2">
          <button mat-flat-button type="submit" [disabled]="submitting() || !machineForm().valid()">
            {{ submitting() ? 'Saving…' : 'Save changes' }}
          </button>
          <a mat-button [routerLink]="['/machines', machine.id]">Cancel</a>
        </div>
      </form>
    </mat-card-content>
  </mat-card>
}
```

- [ ] **Step 5: Relancer les tests, vérifier le succès**

Run: `cd frontend && npm test -- --include "src/app/features/machines/machine-edit/machine-edit.spec.ts" --include "src/app/app.routes.spec.ts"`

Expected: PASS. Si le test de route échoue sur `adminGuard`, vérifier que le JWT de test contient `roles: ['ROLE_ADMIN']`.

- [ ] **Step 6: Commit**

```bash
build-brief ./gradlew clean build
git add frontend/src/app/app.routes.ts frontend/src/app/app.routes.spec.ts \
  frontend/src/app/features/machines/machine-edit/
git commit -m "feat(frontend): add machine edit page

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

### Task 4: Dialog `MachineDeleteDialog`

**Files:**
- Create via CLI: `frontend/src/app/features/machines/machine-delete-dialog/machine-delete-dialog.ts`
- Create via CLI: `frontend/src/app/features/machines/machine-delete-dialog/machine-delete-dialog.html`
- Test: `frontend/src/app/features/machines/machine-delete-dialog/machine-delete-dialog.spec.ts`

**Interfaces:**
- Consumes: Angular Material `MatDialogModule`, `MatDialogRef`, `MAT_DIALOG_DATA`.
- Produces:
  - `export interface MachineDeleteDialogData { serialNumber: string }`
  - `export class MachineDeleteDialog`
  - `data-testid="machine-delete-dialog-cancel"`
  - `data-testid="machine-delete-dialog-confirm"`

- [ ] **Step 1: Générer le composant**

```bash
cd frontend && npx ng generate component features/machines/machine-delete-dialog --style=none
```

- [ ] **Step 2: Écrire le test en échec** — remplacer `machine-delete-dialog.spec.ts` par :

```ts
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { MachineDeleteDialog } from './machine-delete-dialog';

describe('MachineDeleteDialog', () => {
  let fixture: ComponentFixture<MachineDeleteDialog>;
  let dialogRef: { close: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    dialogRef = { close: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [MachineDeleteDialog],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: { serialNumber: 'SN-1' } },
        { provide: MatDialogRef, useValue: dialogRef },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(MachineDeleteDialog);
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('renders the injected serial number', () => {
    expect(fixture.nativeElement.textContent).toContain('Delete vending machine SN-1?');
  });

  it('closes the dialog when clicking cancel', () => {
    const cancelButton: HTMLButtonElement = fixture.nativeElement.querySelector(
      '[data-testid="machine-delete-dialog-cancel"]',
    );

    cancelButton.click();

    expect(dialogRef.close).toHaveBeenCalledWith(false);
  });

  it('confirms deletion when clicking delete', () => {
    const confirmButton: HTMLButtonElement = fixture.nativeElement.querySelector(
      '[data-testid="machine-delete-dialog-confirm"]',
    );

    confirmButton.click();

    expect(dialogRef.close).toHaveBeenCalledWith(true);
  });
});
```

- [ ] **Step 3: Lancer le test, vérifier l'échec**

Run: `cd frontend && npm test -- --include src/app/features/machines/machine-delete-dialog/machine-delete-dialog.spec.ts`

Expected: FAIL — le squelette ne rend pas le texte ni les `data-testid`.

- [ ] **Step 4: Implémentation minimale** — remplacer `machine-delete-dialog.ts` par :

```ts
import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';

export interface MachineDeleteDialogData {
  serialNumber: string;
}

@Component({
  imports: [MatButtonModule, MatDialogModule],
  selector: 'app-machine-delete-dialog',
  templateUrl: './machine-delete-dialog.html',
})
export class MachineDeleteDialog {
  readonly data = inject<MachineDeleteDialogData>(MAT_DIALOG_DATA);
  readonly dialogRef = inject(MatDialogRef<MachineDeleteDialog, boolean>);
}
```

Remplacer `machine-delete-dialog.html` par :

```html
<h2 mat-dialog-title>Delete vending machine</h2>

<mat-dialog-content>
  <p>Delete vending machine {{ data.serialNumber }}?</p>
  <p class="text-sm text-gray-600">This action cannot be undone.</p>
</mat-dialog-content>

<mat-dialog-actions align="end">
  <button
    mat-button
    type="button"
    data-testid="machine-delete-dialog-cancel"
    (click)="dialogRef.close(false)"
  >
    Cancel
  </button>
  <button
    mat-flat-button
    type="button"
    color="warn"
    data-testid="machine-delete-dialog-confirm"
    (click)="dialogRef.close(true)"
  >
    Delete
  </button>
</mat-dialog-actions>
```

- [ ] **Step 5: Relancer le test, vérifier le succès**

Run: `cd frontend && npm test -- --include src/app/features/machines/machine-delete-dialog/machine-delete-dialog.spec.ts`

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
build-brief ./gradlew clean build
git add frontend/src/app/features/machines/machine-delete-dialog/
git commit -m "feat(frontend): add machine delete confirmation dialog

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

### Task 5: Actions edit/delete dans `MachineList`

**Files:**
- Modify: `frontend/src/app/features/machines/machine-list/machine-list.ts`
- Modify: `frontend/src/app/features/machines/machine-list/machine-list.html`
- Test: `frontend/src/app/features/machines/machine-list/machine-list.spec.ts`

**Interfaces:**
- Consumes:
  - `updateHref(machine: VendingMachine): string | undefined`
  - `deleteHref(machine: VendingMachine): string | undefined`
  - `MachineDeleteDialog`, `MachineDeleteDialogData`
- Produces:
  - `editHref(machine: VendingMachine): string | undefined`
  - `deleteHref(machine: VendingMachine): string | undefined` (méthode de composant exposant le helper)
  - `deleteMachine(machine: VendingMachine): void`
  - reload de la page après `DELETE 204`

- [ ] **Step 1: Écrire le test en échec** — remplacer `machine-list.spec.ts` par :

```ts
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router, provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { of } from 'rxjs';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { TokenStore } from '../../../core/auth/token-store';
import { MachineDeleteDialog } from '../machine-delete-dialog/machine-delete-dialog';
import { MachineList } from './machine-list';

function fakeJwt(payload: Record<string, unknown>): string {
  const encode = (value: unknown) =>
    btoa(JSON.stringify(value)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${encode({ alg: 'none' })}.${encode(payload)}.signature`;
}

describe('MachineList', () => {
  let harness: RouterTestingHarness;
  let backend: HttpTestingController;
  let dialog: { open: ReturnType<typeof vi.fn> };
  let snackBar: { open: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    sessionStorage.clear();
    dialog = { open: vi.fn(() => ({ afterClosed: () => of(undefined) })) };
    snackBar = { open: vi.fn() };

    await TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([
          { path: 'machines', component: MachineList },
          { path: 'machines/new', children: [] },
          { path: 'machines/:id/edit', children: [] },
        ]),
        { provide: MatDialog, useValue: dialog },
        { provide: MatSnackBar, useValue: snackBar },
      ],
    }).compileComponents();

    backend = TestBed.inject(HttpTestingController);
    harness = await RouterTestingHarness.create();
  });

  afterEach(() => backend.verify());

  async function navigate(url: string) {
    const component = await harness.navigateByUrl(url, MachineList);
    harness.fixture.detectChanges();
    backend
      .expectOne('/api/v1')
      .flush({ _links: { vendingMachines: { href: '/api/v1/vending-machines' } } });
    await Promise.resolve();
    harness.fixture.detectChanges();
    return component;
  }

  function flushMachines(templates: Record<string, { method: string }> = {}) {
    backend.expectOne('/api/v1/vending-machines?page=1&size=20').flush({
      _embedded: {
        elements: [
          {
            id: 'm-1',
            serialNumber: 'SN-1',
            address: { city: 'Lyon', streetName: 'Rue A', postalCode: '69001' },
            itemType: 'SNACK',
            workingStatus: 'WORKING',
            powerStatus: 'POWER_ON',
            _links: { self: { href: '/api/v1/vending-machines/m-1' } },
            _templates: templates,
          },
        ],
      },
      page: { size: 20, totalElements: 1, totalPages: 1, number: 0 },
    });
  }

  function authenticateAdmin() {
    TestBed.inject(TokenStore).setAccessToken(
      fakeJwt({ sub: 'admin@vending.me', roles: ['ROLE_ADMIN'], exp: 4102444800 }),
    );
  }

  it('requests the first page on load and exposes the unwrapped elements', async () => {
    const component = await navigate('/machines');

    const request = backend.expectOne('/api/v1/vending-machines?page=1&size=20');
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
      page: { size: 20, totalElements: 1, totalPages: 1, number: 0 },
    });
    await harness.fixture.whenStable();

    expect(component.machines().elements).toHaveLength(1);
    expect(component.machines().elements[0].serialNumber).toBe('SN-1');
    expect(component.totalElements()).toBe(1);
  });

  it('reads the initial page from the URL query params', async () => {
    const component = await navigate('/machines?page=2&size=10&city=Paris');

    backend
      .expectOne('/api/v1/vending-machines?page=3&size=10')
      .flush({ page: { size: 10, totalElements: 30, totalPages: 3, number: 2 } });
    await harness.fixture.whenStable();

    expect(component.pageIndex()).toBe(2);
    expect(component.pageSize()).toBe(10);
  });

  it('navigates to the new page/size while preserving other query params', async () => {
    const component = await navigate('/machines?city=Paris');

    backend
      .expectOne('/api/v1/vending-machines?page=1&size=20')
      .flush({ page: { size: 20, totalElements: 30, totalPages: 2, number: 0 } });
    await harness.fixture.whenStable();

    component.onPageChange({ pageIndex: 2, pageSize: 10, length: 30 });
    await new Promise((resolve) => setTimeout(resolve));
    harness.fixture.detectChanges();

    expect(TestBed.inject(Router).url).toBe('/machines?city=Paris&page=2&size=10');
    backend
      .expectOne('/api/v1/vending-machines?page=3&size=10')
      .flush({ page: { size: 10, totalElements: 30, totalPages: 3, number: 2 } });
    await harness.fixture.whenStable();

    expect(component.machines().elements).toEqual([]);
    expect(component.totalElements()).toBe(30);
  });

  it('exposes isAdmin based on the current JWT roles', async () => {
    const component = await navigate('/machines');

    backend
      .expectOne('/api/v1/vending-machines?page=1&size=20')
      .flush({ page: { size: 20, totalElements: 0, totalPages: 0, number: 0 } });

    expect(component.isAdmin()).toBe(false);

    authenticateAdmin();

    expect(component.isAdmin()).toBe(true);
  });

  it('shows admin edit and delete actions only when matching affordances are present', async () => {
    authenticateAdmin();
    await navigate('/machines');
    flushMachines({ default: { method: 'PUT' }, delete: { method: 'DELETE' } });
    await harness.fixture.whenStable();
    harness.fixture.detectChanges();

    const editLink = harness.fixture.nativeElement.querySelector(
      'a[aria-label="Edit vending machine"]',
    ) as HTMLAnchorElement;
    const deleteButton = harness.fixture.nativeElement.querySelector(
      'button[aria-label="Delete vending machine"]',
    ) as HTMLButtonElement;

    expect(editLink).not.toBeNull();
    expect(deleteButton).not.toBeNull();
  });

  it('hides edit and delete actions for non-admin users even when affordances are present', async () => {
    await navigate('/machines');
    flushMachines({ default: { method: 'PUT' }, delete: { method: 'DELETE' } });
    await harness.fixture.whenStable();
    harness.fixture.detectChanges();

    expect(
      harness.fixture.nativeElement.querySelector('a[aria-label="Edit vending machine"]'),
    ).toBeNull();
    expect(
      harness.fixture.nativeElement.querySelector('button[aria-label="Delete vending machine"]'),
    ).toBeNull();
  });

  it('hides admin edit and delete actions when affordances are absent', async () => {
    authenticateAdmin();
    await navigate('/machines');
    flushMachines({});
    await harness.fixture.whenStable();
    harness.fixture.detectChanges();

    expect(
      harness.fixture.nativeElement.querySelector('a[aria-label="Edit vending machine"]'),
    ).toBeNull();
    expect(
      harness.fixture.nativeElement.querySelector('button[aria-label="Delete vending machine"]'),
    ).toBeNull();
  });

  it('opens the delete dialog and does not delete when cancelled', async () => {
    authenticateAdmin();
    dialog.open.mockReturnValue({ afterClosed: () => of(false) });
    const component = await navigate('/machines');
    flushMachines({ delete: { method: 'DELETE' } });
    await harness.fixture.whenStable();

    component.deleteMachine(component.machines().elements[0]);

    expect(dialog.open).toHaveBeenCalledWith(MachineDeleteDialog, {
      data: { serialNumber: 'SN-1' },
    });
    backend.expectNone('/api/v1/vending-machines/m-1');
  });

  it('deletes the machine, shows a snackbar and reloads the list when confirmed', async () => {
    authenticateAdmin();
    dialog.open.mockReturnValue({ afterClosed: () => of(true) });
    const component = await navigate('/machines');
    flushMachines({ delete: { method: 'DELETE' } });
    await harness.fixture.whenStable();

    component.deleteMachine(component.machines().elements[0]);

    const request = backend.expectOne('/api/v1/vending-machines/m-1');
    expect(request.request.method).toBe('DELETE');
    request.flush(null, { status: 204, statusText: 'No Content' });
    harness.fixture.detectChanges();
    await Promise.resolve();
    harness.fixture.detectChanges();

    backend.expectOne('/api/v1/vending-machines?page=1&size=20').flush({
      page: { size: 20, totalElements: 0, totalPages: 0, number: 0 },
    });
    await harness.fixture.whenStable();

    expect(snackBar.open).toHaveBeenCalledWith('Vending machine deleted.', 'Close', {
      duration: 5000,
    });
  });
});
```

- [ ] **Step 2: Lancer le test, vérifier l'échec**

Run: `cd frontend && npm test -- --include src/app/features/machines/machine-list/machine-list.spec.ts`

Expected: FAIL — `MatDialog`, `MatSnackBar`, `deleteMachine`, `editHref`/`deleteHref` composant non câblés.

- [ ] **Step 3: Implémentation minimale** — remplacer `machine-list.ts` par :

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
import { ValueOrEmptyPipe } from '../../../shared/value-or-empty-pipe';
import {
  MachineDeleteDialog,
  MachineDeleteDialogData,
} from '../machine-delete-dialog/machine-delete-dialog';
import { VendingMachine } from '../models/vending-machine';
import { deleteHref as machineDeleteHref, machinesPageUrl, updateHref } from '../vending-machine-api';

const DEFAULT_PAGE_SIZE = 20;

@Component({
  selector: 'app-machine-list',
  imports: [
    MatButtonModule,
    MatIconModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatTableModule,
    RouterLink,
    ValueOrEmptyPipe,
  ],
  templateUrl: './machine-list.html',
})
export class MachineList {
  private readonly auth = inject(AuthService);
  private readonly apiRoot = inject(ApiRootApi);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly http = inject(HttpClient);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly isAdmin = this.auth.isAdmin;

  protected readonly displayedColumns = [
    'serialNumber',
    'city',
    'itemType',
    'workingStatus',
    'powerStatus',
    'actions',
  ];

  private readonly queryParamMap = toSignal(this.route.queryParamMap, { requireSync: true });
  readonly pageIndex = computed(() => intQueryParam(this.queryParamMap().get('page'), 0));
  readonly pageSize = computed(() =>
    intQueryParam(this.queryParamMap().get('size'), DEFAULT_PAGE_SIZE),
  );

  private readonly resource = httpResource<HalPage<VendingMachine>>(() =>
    machinesPageUrl(this.apiRoot.link('vendingMachines'), this.pageIndex(), this.pageSize()),
  );

  readonly isLoading = this.resource.isLoading;
  readonly hasError = computed(() => this.resource.error() !== undefined);
  readonly machines = computed(() =>
    this.resource.hasValue()
      ? Page.fromHalPage(this.resource.value())
      : Page.empty<VendingMachine>(),
  );
  readonly totalElements = computed(() => this.machines().totalElements);

  onPageChange(event: Pick<PageEvent, 'pageIndex' | 'pageSize' | 'length'>): void {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { page: event.pageIndex, size: event.pageSize },
      queryParamsHandling: 'merge',
    });
  }

  editHref(machine: VendingMachine): string | undefined {
    return updateHref(machine);
  }

  deleteHref(machine: VendingMachine): string | undefined {
    return machineDeleteHref(machine);
  }

  deleteMachine(machine: VendingMachine): void {
    const href = this.deleteHref(machine);
    if (!href) {
      return;
    }

    this.dialog
      .open(MachineDeleteDialog, {
        data: { serialNumber: machine.serialNumber ?? machine.id } satisfies MachineDeleteDialogData,
      })
      .afterClosed()
      .subscribe((confirmed) => {
        if (confirmed !== true) {
          return;
        }

        this.http.delete(href).subscribe({
          next: () => {
            this.snackBar.open('Vending machine deleted.', 'Close', { duration: 5000 });
            this.resource.reload();
          },
          error: () => {
            this.snackBar.open('The vending machine could not be deleted.', 'Close', {
              duration: 5000,
            });
            this.resource.reload();
          },
        });
      });
  }
}
```

Remplacer le `<ng-container matColumnDef="actions">` dans `machine-list.html` par :

```html
    <ng-container matColumnDef="actions">
      <th mat-header-cell *matHeaderCellDef></th>
      <td mat-cell *matCellDef="let machine">
        <a
          mat-icon-button
          [routerLink]="['/machines', machine.id]"
          [state]="{ href: machine._links?.self?.href }"
          aria-label="View details"
        >
          <mat-icon>visibility</mat-icon>
        </a>
        @if (isAdmin() && editHref(machine); as selfHref) {
          <a
            mat-icon-button
            [routerLink]="['/machines', machine.id, 'edit']"
            [state]="{ href: selfHref }"
            aria-label="Edit vending machine"
          >
            <mat-icon>edit</mat-icon>
          </a>
        }
        @if (isAdmin() && deleteHref(machine)) {
          <button
            mat-icon-button
            type="button"
            aria-label="Delete vending machine"
            (click)="deleteMachine(machine)"
          >
            <mat-icon>delete</mat-icon>
          </button>
        }
      </td>
    </ng-container>
```

- [ ] **Step 4: Relancer le test, vérifier le succès**

Run: `cd frontend && npm test -- --include src/app/features/machines/machine-list/machine-list.spec.ts`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
build-brief ./gradlew clean build
git add frontend/src/app/features/machines/machine-list/machine-list.ts \
  frontend/src/app/features/machines/machine-list/machine-list.html \
  frontend/src/app/features/machines/machine-list/machine-list.spec.ts
git commit -m "feat(frontend): add machine list edit and delete actions

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

### Task 6: Actions edit/delete dans `MachineDetail`

**Files:**
- Modify: `frontend/src/app/features/machines/machine-detail/machine-detail.ts`
- Modify: `frontend/src/app/features/machines/machine-detail/machine-detail.html`
- Test: `frontend/src/app/features/machines/machine-detail/machine-detail.spec.ts`

**Interfaces:**
- Consumes:
  - `updateHref(machine: VendingMachine): string | undefined`
  - `deleteHref(machine: VendingMachine): string | undefined`
  - `MachineDeleteDialog`, `MachineDeleteDialogData`
- Produces:
  - `editHref(machine: VendingMachine): string | undefined`
  - `deleteHref(machine: VendingMachine): string | undefined`
  - `deleteMachine(machine: VendingMachine): void`
  - navigation `/machines` après suppression réussie

- [ ] **Step 1: Écrire le test en échec** — ajouter ces tests à la fin de `machine-detail.spec.ts` (avant le `});` final), en réutilisant les helpers existants `navigate`, `flushMachineAndStock`, `fakeJwt`, `dialog`, `snackBar`, `backend` :

```ts
  it('shows admin edit and delete actions only when matching affordances are present', async () => {
    TestBed.inject(TokenStore).setAccessToken(
      fakeJwt({ sub: 'admin@vending.me', roles: ['ROLE_ADMIN'], exp: 1 }),
    );

    const component = await navigate();

    backend.expectOne('/api/v1/vending-machines/m-1').flush({
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
      _links: {
        self: { href: 'https://api.example.test/vending-machines/m-1' },
        stock: { href: 'https://api.example.test/vending-machines/m-1/stock' },
      },
      _templates: { default: { method: 'PUT' }, delete: { method: 'DELETE' } },
    });
    harness.fixture.detectChanges();
    await Promise.resolve();
    harness.fixture.detectChanges();
    backend
      .expectOne('https://api.example.test/vending-machines/m-1/stock')
      .flush({ itemQuantities: [] });
    await harness.fixture.whenStable();
    harness.fixture.detectChanges();

    expect(component.editHref(component.machine()!)).toBe(
      'https://api.example.test/vending-machines/m-1',
    );
    expect(component.deleteHref(component.machine()!)).toBe(
      'https://api.example.test/vending-machines/m-1',
    );
    expect(
      harness.fixture.nativeElement.querySelector('a[aria-label="Edit vending machine"]'),
    ).not.toBeNull();
    expect(
      harness.fixture.nativeElement.querySelector('button[aria-label="Delete vending machine"]'),
    ).not.toBeNull();
  });

  it('hides admin edit and delete actions when affordances are absent', async () => {
    TestBed.inject(TokenStore).setAccessToken(
      fakeJwt({ sub: 'admin@vending.me', roles: ['ROLE_ADMIN'], exp: 1 }),
    );

    await navigate();
    await flushMachineAndStock();
    await harness.fixture.whenStable();
    harness.fixture.detectChanges();

    expect(
      harness.fixture.nativeElement.querySelector('a[aria-label="Edit vending machine"]'),
    ).toBeNull();
    expect(
      harness.fixture.nativeElement.querySelector('button[aria-label="Delete vending machine"]'),
    ).toBeNull();
  });

  it('does not delete from detail when the confirmation dialog is cancelled', async () => {
    TestBed.inject(TokenStore).setAccessToken(
      fakeJwt({ sub: 'admin@vending.me', roles: ['ROLE_ADMIN'], exp: 1 }),
    );
    dialog.open.mockReturnValue({ afterClosed: () => of(false) });

    const component = await navigate();

    backend.expectOne('/api/v1/vending-machines/m-1').flush({
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
      _links: {
        self: { href: 'https://api.example.test/vending-machines/m-1' },
        stock: { href: 'https://api.example.test/vending-machines/m-1/stock' },
      },
      _templates: { delete: { method: 'DELETE' } },
    });
    harness.fixture.detectChanges();
    await Promise.resolve();
    harness.fixture.detectChanges();
    backend
      .expectOne('https://api.example.test/vending-machines/m-1/stock')
      .flush({ itemQuantities: [] });
    await harness.fixture.whenStable();

    component.deleteMachine(component.machine()!);

    backend.expectNone('https://api.example.test/vending-machines/m-1');
  });

  it('deletes from detail and navigates back to the list when confirmed', async () => {
    TestBed.inject(TokenStore).setAccessToken(
      fakeJwt({ sub: 'admin@vending.me', roles: ['ROLE_ADMIN'], exp: 1 }),
    );
    dialog.open.mockReturnValue({ afterClosed: () => of(true) });

    const component = await navigate();

    backend.expectOne('/api/v1/vending-machines/m-1').flush({
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
      _links: {
        self: { href: 'https://api.example.test/vending-machines/m-1' },
        stock: { href: 'https://api.example.test/vending-machines/m-1/stock' },
      },
      _templates: { delete: { method: 'DELETE' } },
    });
    harness.fixture.detectChanges();
    await Promise.resolve();
    harness.fixture.detectChanges();
    backend
      .expectOne('https://api.example.test/vending-machines/m-1/stock')
      .flush({ itemQuantities: [] });
    await harness.fixture.whenStable();

    component.deleteMachine(component.machine()!);

    const request = backend.expectOne('https://api.example.test/vending-machines/m-1');
    expect(request.request.method).toBe('DELETE');
    request.flush(null, { status: 204, statusText: 'No Content' });
    await harness.fixture.whenStable();

    expect(snackBar.open).toHaveBeenCalledWith('Vending machine deleted.', 'Close', {
      duration: 5000,
    });
    expect(TestBed.inject(Router).url).toBe('/machines');
  });
```

Ajouter aussi les imports manquants en tête :

```ts
import { HttpClient } from '@angular/common/http';
import { MachineDeleteDialog } from '../machine-delete-dialog/machine-delete-dialog';
```

Puis vérifier que le `beforeEach` de `machine-detail.spec.ts` fournit aussi une route liste :

```ts
provideRouter([
  { path: 'machines', children: [] },
  { path: 'machines/:id', component: MachineDetail },
]),
```

- [ ] **Step 2: Lancer le test, vérifier l'échec**

Run: `cd frontend && npm test -- --include src/app/features/machines/machine-detail/machine-detail.spec.ts`

Expected: FAIL — méthodes `editHref`, `deleteHref`, `deleteMachine` absentes.

- [ ] **Step 3: Implémentation minimale** — modifier `machine-detail.ts` :

Ajouter les imports :

```ts
import { Router, RouterLink } from '@angular/router';
import {
  MachineDeleteDialog,
  MachineDeleteDialogData,
} from '../machine-delete-dialog/machine-delete-dialog';
import { deleteHref as machineDeleteHref, machineUrl, updateHref } from '../vending-machine-api';
```

Remplacer l'import existant `ActivatedRoute, RouterLink` et `machineUrl` en conséquence. Ajouter l'injection :

```ts
private readonly router = inject(Router);
```

Ajouter les méthodes dans la classe `MachineDetail` :

```ts
  editHref(machine: VendingMachine): string | undefined {
    return updateHref(machine);
  }

  deleteHref(machine: VendingMachine): string | undefined {
    return machineDeleteHref(machine);
  }

  deleteMachine(machine: VendingMachine): void {
    const href = this.deleteHref(machine);
    if (!href) {
      return;
    }

    this.dialog
      .open(MachineDeleteDialog, {
        data: { serialNumber: machine.serialNumber ?? machine.id } satisfies MachineDeleteDialogData,
      })
      .afterClosed()
      .subscribe((confirmed) => {
        if (confirmed !== true) {
          return;
        }

        this.http.delete(href).subscribe({
          next: () => {
            this.snackBar.open('Vending machine deleted.', 'Close', { duration: 5000 });
            void this.router.navigate(['/machines']);
          },
          error: () => {
            this.snackBar.open('The vending machine could not be deleted.', 'Close', {
              duration: 5000,
            });
          },
        });
      });
  }
```

Dans `machine-detail.html`, remplacer le bloc d'en-tête par :

```html
<div class="mb-4 flex items-center justify-between">
  <h1 class="text-xl font-semibold">Vending machine details</h1>
  <div class="flex gap-2">
    @if (machine(); as machine) {
      @if (isAdmin() && editHref(machine); as selfHref) {
        <a
          mat-button
          [routerLink]="['/machines', machine.id, 'edit']"
          [state]="{ href: selfHref }"
          aria-label="Edit vending machine"
        >
          Edit
        </a>
      }
      @if (isAdmin() && deleteHref(machine)) {
        <button
          mat-button
          type="button"
          color="warn"
          aria-label="Delete vending machine"
          (click)="deleteMachine(machine)"
        >
          Delete
        </button>
      }
    }
    <a mat-button routerLink="/machines">Back to list</a>
  </div>
</div>
```

- [ ] **Step 4: Relancer le test, vérifier le succès**

Run: `cd frontend && npm test -- --include src/app/features/machines/machine-detail/machine-detail.spec.ts`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
build-brief ./gradlew clean build
git add frontend/src/app/features/machines/machine-detail/machine-detail.ts \
  frontend/src/app/features/machines/machine-detail/machine-detail.html \
  frontend/src/app/features/machines/machine-detail/machine-detail.spec.ts
git commit -m "feat(frontend): add machine detail edit and delete actions

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

### Task 7: E2E admin édition et suppression

**Files:**
- Create: `frontend/e2e/manage-machines.spec.ts`

**Interfaces:**
- Consumes: UI livrée par Tasks 3, 5, 6 ; `AuthService` saute `/me` pour les admins si JWT `roles: ['ROLE_ADMIN']`.
- Produces: scénario Playwright couvrant login admin, édition, retour liste, suppression confirmée.

- [ ] **Step 1: Écrire le test e2e complet**

Créer `frontend/e2e/manage-machines.spec.ts` :

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

const machineId = '11111111-1111-1111-1111-111111111111';
const machinePath = `/api/v1/vending-machines/${machineId}`;
const stockPath = `${machinePath}/stock`;

let currentMachine = {
  id: machineId,
  serialNumber: 'SN-0001',
  address: {
    latitude: 45.76,
    longitude: 4.84,
    streetNumber: 1,
    streetName: 'Rue de la Paix',
    postalCode: '69001',
    city: 'Lyon',
  },
  itemType: 'SNACK',
  powerStatus: 'POWER_ON',
  workingStatus: 'WORKING',
  temperature: 4,
  lastIntervention: '2026-09-25T10:15:00',
  rfidStatus: 'OK',
  smartCardStatus: 'OK',
  changeMoneyStatus: 'NORMAL',
  _links: { self: { href: machinePath }, stock: { href: stockPath } },
  _templates: { default: { method: 'PUT' }, delete: { method: 'DELETE' } },
};

let machineDeleted = false;

function machinesPage() {
  return {
    _embedded: machineDeleted
      ? undefined
      : {
          elements: [
            {
              ...currentMachine,
              _links: { self: { href: machinePath } },
              _templates: currentMachine._templates,
            },
          ],
        },
    page: {
      size: 20,
      totalElements: machineDeleted ? 0 : 1,
      totalPages: machineDeleted ? 0 : 1,
      number: 0,
    },
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
  await expect(page.getByText('SN-0001')).toBeVisible();
}

test.beforeEach(async ({ page }) => {
  currentMachine = {
    id: machineId,
    serialNumber: 'SN-0001',
    address: {
      latitude: 45.76,
      longitude: 4.84,
      streetNumber: 1,
      streetName: 'Rue de la Paix',
      postalCode: '69001',
      city: 'Lyon',
    },
    itemType: 'SNACK',
    powerStatus: 'POWER_ON',
    workingStatus: 'WORKING',
    temperature: 4,
    lastIntervention: '2026-09-25T10:15:00',
    rfidStatus: 'OK',
    smartCardStatus: 'OK',
    changeMoneyStatus: 'NORMAL',
    _links: { self: { href: machinePath }, stock: { href: stockPath } },
    _templates: { default: { method: 'PUT' }, delete: { method: 'DELETE' } },
  };
  machineDeleted = false;

  await page.route('**/api/v1', async (route) => {
    await fulfillJson(route, {
      _links: {
        vendingMachines: { href: '/api/v1/vending-machines' },
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
    const request = route.request();
    const pathname = new URL(request.url()).pathname;

    if (request.method() === 'GET' && pathname === '/api/v1/vending-machines') {
      await fulfillJson(route, machinesPage());
      return;
    }

    if (request.method() === 'GET' && pathname === machinePath) {
      await fulfillJson(route, currentMachine);
      return;
    }

    if (request.method() === 'GET' && pathname === stockPath) {
      await fulfillJson(route, { itemQuantities: [], _links: {} });
      return;
    }

    if (request.method() === 'PUT' && pathname === machinePath) {
      const payload = request.postDataJSON();
      currentMachine = {
        ...currentMachine,
        ...payload,
        id: machineId,
        serialNumber: 'SN-0001',
        _links: { self: { href: machinePath }, stock: { href: stockPath } },
        _templates: { default: { method: 'PUT' }, delete: { method: 'DELETE' } },
      };
      await fulfillJson(route, currentMachine);
      return;
    }

    if (request.method() === 'DELETE' && pathname === machinePath) {
      machineDeleted = true;
      await route.fulfill({ status: 204 });
      return;
    }

    throw new Error(`Unexpected vending machines request: ${request.method()} ${pathname}`);
  });
});

test('an admin can edit and delete a vending machine', async ({ page }) => {
  await signInAsAdmin(page);

  await page.getByRole('link', { name: 'Edit vending machine' }).click();
  await expect(page).toHaveURL(new RegExp(`/machines/${machineId}/edit$`));
  await expect(page.getByRole('heading', { name: 'Edit vending machine' })).toBeVisible();

  await page.getByLabel('City').fill('Paris');
  await page.getByLabel('Temperature').fill('7');
  await page.getByLabel('Working status').click();
  await page.getByRole('option', { name: 'WARNING' }).click();
  await page.getByRole('button', { name: 'Save changes' }).click();

  await expect(page).toHaveURL(new RegExp(`/machines/${machineId}$`));
  await expect(page.getByText('Paris')).toBeVisible();
  await expect(page.getByText('WARNING')).toBeVisible();

  await page.getByRole('link', { name: 'Back to list' }).click();
  await expect(page).toHaveURL(/\/machines$/);

  await page.getByRole('button', { name: 'Delete vending machine' }).click();
  await expect(page.getByText('Delete vending machine SN-0001?')).toBeVisible();
  await page.getByTestId('machine-delete-dialog-confirm').click();

  await expect(page.getByText('Vending machine deleted.')).toBeVisible();
  await expect(page.getByText('No vending machine found.')).toBeVisible();
  await expect(page.getByText('SN-0001')).toBeHidden();
});
```

- [ ] **Step 2: Lancer l'e2e, vérifier l'échec éventuel puis corriger uniquement le frontend si nécessaire**

Run: `cd frontend && npm run e2e`

Expected: PASS. Si FAIL sur les sélecteurs Material `mat-select`, utiliser les libellés visibles exacts rendus par Angular Material, sans changer le backend.

- [ ] **Step 3: Lancer les unitaires ciblés de la feature**

Run: `cd frontend && npm test -- --include "src/app/features/machines/**/*.spec.ts" --include src/app/app.routes.spec.ts`

Expected: PASS.

- [ ] **Step 4: Commit**

```bash
build-brief ./gradlew clean build
git add frontend/e2e/manage-machines.spec.ts
git commit -m "feat(frontend): cover machine management with e2e

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

### Task 8: Documentation frontend et backlog

**Files:**
- Modify: `frontend/README.md`
- Modify: `docs/features-front-a-implementer.md`

**Interfaces:**
- Consumes: feature livrée par Tasks 1-7.
- Produces: documentation cohérente avec le périmètre réel. Important : ces deux fichiers ont des changements non commités non liés sur `main` au moment de rédaction du plan ; ne pas les éditer sur `main`, les intégrer seulement depuis la branche `feat/frontend-machines-edit-delete` en préservant les changements existants.

- [ ] **Step 1: Mettre à jour `frontend/README.md`**

Dans `## Périmètre actuel`, remplacer la puce distributeurs par :

```md
- **Distributeurs** : liste paginée (table Material + paginator), création
  (admin), édition complète (admin), suppression confirmée (admin), détail
  avec consultation du stock et commande d'un article (utilisateur connecté
  avec le rôle `ROLE_USER`).
```

Dans `**Reste à faire**`, supprimer la ligne :

```md
- Édition/suppression d'un distributeur (seules la liste et la création
  existent)
```

Expected: les autres lignes du backlog restent inchangées.

- [ ] **Step 2: Mettre à jour `docs/features-front-a-implementer.md`**

Dans `## ✅ Déjà fait`, ajouter cette puce après la création de machine :

```md
- **Édition/suppression d'une machine** (`ROLE_ADMIN`, `PUT`/`DELETE
  /vending-machines/{id}`) — page dédiée `machines/:id/edit`, actions
  hypermédia depuis la liste et le détail, suppression confirmée
```

Dans `## 🔐 Back-office admin`, remplacer la section machines par :

```md
6. **Gestion du stock d'une machine** : ajouter du stock, rapport de stock
   (`/vending-machines/{id}/stock`, `/stock/report`) — la consultation du
   stock existe déjà côté client (point commande), pas côté admin ni le
   rapport
7. **Statut machine** : reset (`/reset`), rapport de statut
   (`/status/report`)
8. **Rapport des commandes** par machine (`/orders/report`)
```

Expected: la gestion machines édition/suppression n'apparaît plus dans le backlog restant ; la numérotation reste continue.

- [ ] **Step 3: Vérifier le diff documentaire**

Run: `git --no-pager diff -- frontend/README.md docs/features-front-a-implementer.md`

Expected: le diff ne contient que les changements ci-dessus plus les éventuels changements préexistants à préserver ; ne pas écraser les changements non liés.

- [ ] **Step 4: Validation finale complète**

Run: `cd frontend && npm test`

Expected: PASS.

Run: `cd frontend && npm run e2e`

Expected: PASS.

Run: `build-brief ./gradlew clean build`

Expected: BUILD SUCCESSFUL ; conserver le chemin du log brut affiché en cas d'échec.

- [ ] **Step 5: Commit**

```bash
build-brief ./gradlew clean build
git add frontend/README.md docs/features-front-a-implementer.md
git commit -m "feat(frontend): document machine edit and delete support

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

---

## Self-Review

- **Spec coverage:** édition dédiée `/machines/:id/edit`, suppression via dialog, actions liste + détail, affordances HAL-FORMS, fallback `machineUrl(id)`, enum arrays, payload complet, `lastIntervention` renvoyé inchangé, tests unitaires, e2e et docs sont couverts par Tasks 1-8. Todo spec 1 déjà faite et explicitement sautée.
- **Placeholder scan:** aucune marque d'attente, aucun renvoi vague, aucune référence non définie et aucune étape sans commande/code requis.
- **Type consistency:** `updateHref`/`deleteHref`, `VendingMachineToUpdate`, `toUpdatePayload`, `MachineDeleteDialogData`, `MachineEdit.submit`, `MachineList.deleteMachine` et `MachineDetail.deleteMachine` gardent des signatures identiques entre les tâches.
- **Review Focus:** les cinq modes de défaillance listés ont chacun un test propriétaire : required/null et `lastIntervention` en Task 3, affordance absente en Tasks 1/5/6, annulation delete en Tasks 5/6, entrée directe/routage en Tasks 2/3.
- **Discrépances spec résolues:** `AddressDto` update/read a la même forme que `Address`; `@RequestBody` sans `@Valid` impose la validation client stricte et laisse les autres échecs en message générique ; HAL-FORMS sert uniquement à décider si l'action existe, le lien `self` reste la cible de mutation ; `lastIntervention` est typé payload `string | null`.
