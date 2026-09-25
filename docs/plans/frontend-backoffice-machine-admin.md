# Back-office machine (stock, statuts, rapports) — Plan d'implémentation

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ajouter dans le frontend Angular le back-office d'une machine pour les administrateurs : approvisionnement du stock, reset des statuts anormaux et génération/affichage des trois rapports machine.
**Architecture:** La page existante `MachineDetail` reste la route unique, mais elle délègue les actions admin à un composant enfant `MachineAdminPanel` afin de ne pas grossir un fichier déjà proche de 170 lignes. Les mutations suivent les liens HAL exposés par les ressources machine/stock et notifient le parent via `machineChanged`/`stockChanged`; les rapports sont affichés dans un dialog éphémère, car le backend ne fournit aucun historique consultable. Le helper `isAbnormalStatus()` reflète strictement l'inverse de `VendingMachineStatus.isAllSystemClear()` et considère les champs `null` comme anormaux.
**Tech Stack:** Angular 22 standalone zoneless, Angular Material, Tailwind, Signal Forms (`@angular/forms/signals`), `httpResource()` pour les lectures, `HttpClient` pour les mutations, Vitest via `npm test`, Playwright via `npm run e2e`.
**Spec:** `docs/specs/frontend-backoffice-machine-admin.md`
**Prérequis:** `docs/plans/00-frontend-prerequis-partages.md` exécuté ; `docs/plans/frontend-gestion-vending-machines.md` recommandé avant (même page `MachineDetail`), mais pas bloquant.

## Global Constraints

- Nommage Angular 2025 : fichiers comme `machine-admin-panel.ts`, jamais `machine-admin-panel.component.ts`.
- Services Angular : `@Service()` uniquement, pas `@Injectable({ providedIn: 'root' })`.
- Lectures HTTP : `httpResource()` ; mutations `POST`/`PUT`/`DELETE` : `HttpClient`.
- Nouveaux formulaires : Signal Forms (`@angular/forms/signals`).
- Générer les composants avec `npx ng generate component <path> --style=none`, puis renommer/adapter les fichiers au style 2025 si nécessaire.
- Tests unitaires frontend : uniquement `cd frontend && npm test`, jamais `npx vitest`; pour cibler : `cd frontend && npm test -- --include src/app/...spec.ts`.
- Tests e2e : `cd frontend && npm run e2e`.
- Gradle : uniquement `build-brief ./gradlew ...`, jamais `./gradlew ...` brut.
- Branche : créer `feat/frontend-machine-admin` depuis `develop`; au 2026-09-25 `develop` n'existe pas encore et seul `main` existe, donc créer depuis `main`.
- Commits : message `feat(frontend): ...` avec le trailer `Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>`.
- Avant chaque commit : `build-brief ./gradlew clean build` doit passer.
- Merge final : fast-forward only.
- Aucun changement backend.

## Review Focus

- Statuts partiellement absents (`null`) : ils doivent rendre la machine anormale et afficher le reset si le rel `status:reset` existe ; test ajouté en Task 1.
- Chaque sous-système de statut non nominal (`powerStatus`, `workingStatus`, `rfidStatus`, `smartCardStatus`, `changeMoneyStatus`) : il doit être détecté séparément ; test table-driven ajouté en Task 1.
- Machine anormale sans rel `status:reset` : le bouton de reset doit rester masqué ; test ajouté en Task 4.
- Provisionnement d'un item d'un autre type : le sélecteur doit filtrer côté client et l'erreur backend `{message}` doit être affichée si elle arrive quand même ; tests ajoutés en Task 3.
- Rapports vides (stock ou commandes sans lignes) : le dialog doit afficher un état vide lisible au lieu d'une table ambiguë ; tests ajoutés en Task 2.

---

## File Structure

- `frontend/src/app/features/machines/models/vending-machine.ts` — modèles machine/stock existants et nouveaux DTO miroir backend : `ItemToProvision`, `VendingMachineStockReport`, `VendingMachineStatusReport`, `VendingMachineClientOrdersReport`.
- `frontend/src/app/features/machines/models/vending-machine-status.ts` — helper pur `isAbnormalStatus()` isolé et testé. Décision : tout champ `null` compte comme anormal, car côté domaine `null !== valeur attendue` et `isAllSystemClear()` ne serait pas vrai.
- `frontend/src/app/features/machines/models/vending-machine-status.spec.ts` — tests table-driven du helper, y compris la classe d'entrée `null`.
- `frontend/src/app/features/machines/extract-error-message.ts` — extraction du pattern local aujourd'hui dans `MachineDetail`; utilisé par `MachineDetail` et `MachineAdminPanel`.
- `frontend/src/app/features/machines/machine-report-dialog/machine-report-dialog.ts|html|spec.ts` — dialog unique pour les trois rapports, avec union discriminée `MachineReportDialogData`.
- `frontend/src/app/features/machines/machine-admin-panel/machine-admin-panel.ts|html|spec.ts` — composant enfant admin, généré via Angular CLI, responsable du formulaire d'approvisionnement, du reset conditionnel et des boutons de rapports. Cette séparation est volontaire : `MachineDetail` est déjà chargé (lecture machine, lecture stock, commande user, snackbar/dialog, navigation) et ajouter toutes les actions admin dedans mélangerait lectures, mutations admin et rendu de rapports.
- `frontend/src/app/features/machines/machine-detail/machine-detail.ts|html|spec.ts` — uniquement wiring du panneau admin, import de `extractErrorMessage`, exposition de `onMachineChanged()`/`onStockChanged()` pour appeler les `reload()` privés.
- `frontend/src/app/features/machines/vending-machine-api.spec.ts` — enrichir uniquement si le plan préalable n'a pas encore verrouillé `linkHref`; sinon ne pas modifier.
- `frontend/e2e/manage-machine-admin.spec.ts` — scénario Playwright admin complet, modelé sur `frontend/e2e/order-item.spec.ts`, JWT fake `roles: ['ROLE_ADMIN']`, pas de `/me` attendu.
- `frontend/README.md` et `docs/features-front-a-implementer.md` — documentation à éditer sur la branche feature malgré leurs modifications non commitées actuelles sur `main`; ne pas écraser les changements existants.

## Tasks

### Task 0: Vérifier les prérequis partagés et préparer la branche

**Files:**
- Verify only: `frontend/src/app/shared/models/hal.ts`
- Verify only: `frontend/src/app/features/machines/machine-detail/machine-detail.ts`
- Verify only: `frontend/README.md`
- Verify only: `docs/features-front-a-implementer.md`

**Interfaces:**
- Consumes: `export function linkHref(resource: HalResource | null | undefined, rel: string): string | undefined`
- Produces: branche `feat/frontend-machine-admin` basée sur `main` si `develop` est absent.

- [ ] **Step 1: Vérifier l'état git et la branche source**

Run:

```bash
git --no-pager status --short
git --no-pager branch --list develop main
```

Expected: `frontend/README.md` et `docs/features-front-a-implementer.md` peuvent déjà être modifiés sur `main`; les préserver. Si `develop` est absent, partir de `main`.

- [ ] **Step 2: Créer la branche de travail**

Run:

```bash
git switch main
git switch -c feat/frontend-machine-admin
```

Expected: branche `feat/frontend-machine-admin` créée sans commit.

- [ ] **Step 3: Vérifier le helper HAL livré par le plan 00**

Run:

```bash
grep -R "export function linkHref(resource: HalResource | null | undefined, rel: string): string | undefined" -n frontend/src/app/shared/models/hal.ts
```

Expected: une ligne trouvée. Si rien n'est trouvé, exécuter d'abord `docs/plans/00-frontend-prerequis-partages.md`; ne pas réimplémenter ce helper dans cette branche.

- [ ] **Step 4: Vérifier que `MachineDetail` expose encore des ressources privées**

Run:

```bash
grep -n "private readonly resource = httpResource" frontend/src/app/features/machines/machine-detail/machine-detail.ts
grep -n "private readonly stockResource = httpResource" frontend/src/app/features/machines/machine-detail/machine-detail.ts
```

Expected: les deux lignes existent. Elles resteront privées; Task 5 ajoute seulement `onMachineChanged()` et `onStockChanged()`.

- [ ] **Step 5: Vérifier le socle avant modification**

Run:

```bash
cd frontend && npm test -- --include src/app/features/machines/machine-detail/machine-detail.spec.ts
```

Expected: PASS.

### Task 1: Modèles d'administration, helper de statut anormal et extraction d'erreur

**Files:**
- Modify: `frontend/src/app/features/machines/models/vending-machine.ts`
- Create: `frontend/src/app/features/machines/models/vending-machine-status.ts`
- Create: `frontend/src/app/features/machines/models/vending-machine-status.spec.ts`
- Create: `frontend/src/app/features/machines/extract-error-message.ts`
- Modify: `frontend/src/app/features/machines/machine-detail/machine-detail.ts`
- Test: `frontend/src/app/features/machines/models/vending-machine-status.spec.ts`
- Test: `frontend/src/app/features/machines/machine-detail/machine-detail.spec.ts`

**Interfaces:**
- Consumes: `HalResource`, `PowerStatus`, `WorkingStatus`, `CardSystemStatus`, `ChangeSystemStatus`.
- Produces:
  - `export interface ItemToProvision { itemId: string; quantity: number; }`
  - `export interface VendingMachineStockReport { id: string; serialNumber: string; stockEntries: ReportedStockEntry[]; }`
  - `export interface VendingMachineStatusReport extends HalResource { serialNumber: string; lastIntervention: Date | null; temperature: number | null; powerStatus: PowerStatus | null; workingStatus: WorkingStatus | null; rfidStatus: CardSystemStatus | null; smartCardStatus: CardSystemStatus | null; changeMoneyStatus: ChangeSystemStatus | null; }`
  - `export interface VendingMachineClientOrdersReport { serialNumber: string; clientOrders: ReportedClientOrder[]; totalAmount: number; reportedAt: Date; }`
  - `export function isAbnormalStatus(machine: Pick<VendingMachine, 'powerStatus' | 'workingStatus' | 'rfidStatus' | 'smartCardStatus' | 'changeMoneyStatus'>): boolean`
  - `export function extractErrorMessage(error: unknown): string | undefined`

- [ ] **Step 1: Write the failing test**

Create `frontend/src/app/features/machines/models/vending-machine-status.spec.ts`:

```ts
import { describe, expect, it } from 'vitest';
import { VendingMachine } from './vending-machine';
import { isAbnormalStatus } from './vending-machine-status';

type StatusFields = Pick<
  VendingMachine,
  'powerStatus' | 'workingStatus' | 'rfidStatus' | 'smartCardStatus' | 'changeMoneyStatus'
>;

const allClear: StatusFields = {
  powerStatus: 'POWER_ON',
  workingStatus: 'WORKING',
  rfidStatus: 'OK',
  smartCardStatus: 'OK',
  changeMoneyStatus: 'NORMAL',
};

describe('isAbnormalStatus', () => {
  it('returns false only when every status matches VendingMachineStatus.isAllSystemClear()', () => {
    expect(isAbnormalStatus(allClear)).toBe(false);
  });

  it.each([
    ['powerStatus', 'POWER_OFF'],
    ['workingStatus', 'WARNING'],
    ['workingStatus', 'ERROR'],
    ['workingStatus', 'ALERT'],
    ['rfidStatus', 'FAILED'],
    ['smartCardStatus', 'FAILED'],
    ['changeMoneyStatus', 'FULL'],
    ['changeMoneyStatus', 'EMPTY'],
  ] as const)('returns true when %s is %s', (field, value) => {
    expect(isAbnormalStatus({ ...allClear, [field]: value })).toBe(true);
  });

  it.each([
    'powerStatus',
    'workingStatus',
    'rfidStatus',
    'smartCardStatus',
    'changeMoneyStatus',
  ] as const)('treats null %s as abnormal', (field) => {
    expect(isAbnormalStatus({ ...allClear, [field]: null })).toBe(true);
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
cd frontend && npm test -- --include src/app/features/machines/models/vending-machine-status.spec.ts
```

Expected: FAIL because `./vending-machine-status` does not exist.

- [ ] **Step 3: Write minimal implementation**

Replace `frontend/src/app/features/machines/models/vending-machine.ts` with:

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

export interface VendingMachine extends HalResource {
  id: string;
  serialNumber: string | null;
  address: Address | null;
  lastIntervention: Date | null;
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

export interface ItemToProvision {
  itemId: string;
  quantity: number;
}

export interface ReportedStockEntry {
  itemName: string;
  quantity: number;
}

export interface VendingMachineStockReport {
  id: string;
  serialNumber: string;
  stockEntries: ReportedStockEntry[];
}

export interface VendingMachineStatusReport extends HalResource {
  serialNumber: string;
  lastIntervention: Date | null;
  temperature: number | null;
  powerStatus: PowerStatus | null;
  workingStatus: WorkingStatus | null;
  rfidStatus: CardSystemStatus | null;
  smartCardStatus: CardSystemStatus | null;
  changeMoneyStatus: ChangeSystemStatus | null;
}

export interface ReportedClientOrder {
  itemName: string;
  itemPrice: number;
  orderedAt: Date;
}

export interface VendingMachineClientOrdersReport {
  serialNumber: string;
  clientOrders: ReportedClientOrder[];
  totalAmount: number;
  reportedAt: Date;
}
```

Create `frontend/src/app/features/machines/models/vending-machine-status.ts`:

```ts
import { VendingMachine } from './vending-machine';

export function isAbnormalStatus(
  machine: Pick<
    VendingMachine,
    'powerStatus' | 'workingStatus' | 'rfidStatus' | 'smartCardStatus' | 'changeMoneyStatus'
  >,
): boolean {
  return !(
    machine.powerStatus === 'POWER_ON' &&
    machine.workingStatus === 'WORKING' &&
    machine.rfidStatus === 'OK' &&
    machine.smartCardStatus === 'OK' &&
    machine.changeMoneyStatus === 'NORMAL'
  );
}
```

Create `frontend/src/app/features/machines/extract-error-message.ts`:

```ts
import { HttpErrorResponse } from '@angular/common/http';

export function extractErrorMessage(error: unknown): string | undefined {
  if (
    error instanceof HttpErrorResponse &&
    typeof error.error === 'object' &&
    error.error !== null &&
    'message' in error.error &&
    typeof error.error.message === 'string'
  ) {
    return error.error.message;
  }

  return undefined;
}
```

In `frontend/src/app/features/machines/machine-detail/machine-detail.ts`, remove the local `extractErrorMessage()` function and `HttpErrorResponse` import, then add:

```ts
import { extractErrorMessage } from '../extract-error-message';
```

The full first import line becomes:

```ts
import { HttpClient, httpResource } from '@angular/common/http';
```

- [ ] **Step 4: Run tests to verify they pass**

Run:

```bash
cd frontend && npm test -- --include src/app/features/machines/models/vending-machine-status.spec.ts --include src/app/features/machines/machine-detail/machine-detail.spec.ts
```

Expected: PASS.

- [ ] **Step 5: Commit**

Run:

```bash
build-brief ./gradlew clean build
git add frontend/src/app/features/machines/models/vending-machine.ts frontend/src/app/features/machines/models/vending-machine-status.ts frontend/src/app/features/machines/models/vending-machine-status.spec.ts frontend/src/app/features/machines/extract-error-message.ts frontend/src/app/features/machines/machine-detail/machine-detail.ts
git commit -m "feat(frontend): add machine admin contracts

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

### Task 2: Dialog unique d'affichage des rapports machine

**Files:**
- Create: `frontend/src/app/features/machines/machine-report-dialog/machine-report-dialog.ts`
- Create: `frontend/src/app/features/machines/machine-report-dialog/machine-report-dialog.html`
- Create: `frontend/src/app/features/machines/machine-report-dialog/machine-report-dialog.spec.ts`

**Interfaces:**
- Consumes:
  - `VendingMachineStockReport`
  - `VendingMachineStatusReport`
  - `VendingMachineClientOrdersReport`
- Produces:
  - `export type MachineReportDialogData = { kind: 'stock'; report: VendingMachineStockReport } | { kind: 'status'; report: VendingMachineStatusReport } | { kind: 'orders'; report: VendingMachineClientOrdersReport }`
  - `export class MachineReportDialog`
  - Template close button `data-testid="machine-report-dialog-close"`

- [ ] **Step 1: Generate component shell**

Run:

```bash
cd frontend && npx ng generate component features/machines/machine-report-dialog --style=none
```

Expected: Angular creates the component files. Rename generated files to 2025 style if the CLI produces `.component.*`.

- [ ] **Step 2: Write the failing test**

Create `frontend/src/app/features/machines/machine-report-dialog/machine-report-dialog.spec.ts`:

```ts
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { MachineReportDialog, MachineReportDialogData } from './machine-report-dialog';

describe('MachineReportDialog', () => {
  let fixture: ComponentFixture<MachineReportDialog>;
  let dialogRef: { close: ReturnType<typeof vi.fn> };

  async function render(data: MachineReportDialogData) {
    dialogRef = { close: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [MachineReportDialog],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: data },
        { provide: MatDialogRef, useValue: dialogRef },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(MachineReportDialog);
    fixture.detectChanges();
    await fixture.whenStable();
  }

  beforeEach(() => {
    TestBed.resetTestingModule();
  });

  it('renders a stock report table', async () => {
    await render({
      kind: 'stock',
      report: {
        id: 'r-1',
        serialNumber: 'SN-1',
        stockEntries: [{ itemName: 'Water', quantity: 4 }],
      },
    });

    const text = fixture.nativeElement.textContent;

    expect(text).toContain('Stock report');
    expect(text).toContain('SN-1');
    expect(text).toContain('Water');
    expect(text).toContain('4');
  });

  it('renders an empty stock report state', async () => {
    await render({
      kind: 'stock',
      report: { id: 'r-1', serialNumber: 'SN-1', stockEntries: [] },
    });

    expect(fixture.nativeElement.textContent).toContain('No stock entries reported.');
  });

  it('renders a status report', async () => {
    await render({
      kind: 'status',
      report: {
        serialNumber: 'SN-1',
        lastIntervention: null,
        temperature: 4,
        powerStatus: 'POWER_ON',
        workingStatus: 'WORKING',
        rfidStatus: 'OK',
        smartCardStatus: 'OK',
        changeMoneyStatus: 'NORMAL',
        _links: { vendingMachine: { href: '/api/v1/vending-machines/m-1' } },
      },
    });

    const text = fixture.nativeElement.textContent;

    expect(text).toContain('Status report');
    expect(text).toContain('Power status');
    expect(text).toContain('POWER_ON');
    expect(text).toContain('Change money status');
    expect(text).toContain('NORMAL');
  });

  it('renders an orders report table and total', async () => {
    await render({
      kind: 'orders',
      report: {
        serialNumber: 'SN-1',
        clientOrders: [
          { itemName: 'Water', itemPrice: 1.5, orderedAt: new Date('2026-09-25T10:00:00Z') },
        ],
        totalAmount: 1.5,
        reportedAt: new Date('2026-09-25T11:00:00Z'),
      },
    });

    const text = fixture.nativeElement.textContent;

    expect(text).toContain('Client orders report');
    expect(text).toContain('Water');
    expect(text).toContain('1.5 €');
    expect(text).toContain('Total');
  });

  it('renders an empty orders report state', async () => {
    await render({
      kind: 'orders',
      report: {
        serialNumber: 'SN-1',
        clientOrders: [],
        totalAmount: 0,
        reportedAt: new Date('2026-09-25T11:00:00Z'),
      },
    });

    expect(fixture.nativeElement.textContent).toContain('No client orders reported.');
  });

  it('closes through the close button', async () => {
    await render({
      kind: 'stock',
      report: { id: 'r-1', serialNumber: 'SN-1', stockEntries: [] },
    });

    const closeButton: HTMLButtonElement = fixture.nativeElement.querySelector(
      '[data-testid="machine-report-dialog-close"]',
    );

    closeButton.click();

    expect(dialogRef.close).toHaveBeenCalled();
  });
});
```

- [ ] **Step 3: Run test to verify it fails**

Run:

```bash
cd frontend && npm test -- --include src/app/features/machines/machine-report-dialog/machine-report-dialog.spec.ts
```

Expected: FAIL because `MachineReportDialogData` and report rendering are not implemented.

- [ ] **Step 4: Write minimal implementation**

Replace `frontend/src/app/features/machines/machine-report-dialog/machine-report-dialog.ts` with:

```ts
import { DatePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { ValueOrEmptyPipe } from '../../../shared/value-or-empty-pipe';
import {
  VendingMachineClientOrdersReport,
  VendingMachineStatusReport,
  VendingMachineStockReport,
} from '../models/vending-machine';

export type MachineReportDialogData =
  | { kind: 'stock'; report: VendingMachineStockReport }
  | { kind: 'status'; report: VendingMachineStatusReport }
  | { kind: 'orders'; report: VendingMachineClientOrdersReport };

@Component({
  imports: [DatePipe, MatButtonModule, MatDialogModule, ValueOrEmptyPipe],
  selector: 'app-machine-report-dialog',
  templateUrl: './machine-report-dialog.html',
})
export class MachineReportDialog {
  readonly data = inject<MachineReportDialogData>(MAT_DIALOG_DATA);
  readonly dialogRef = inject(MatDialogRef<MachineReportDialog>);
}
```

Replace `frontend/src/app/features/machines/machine-report-dialog/machine-report-dialog.html` with:

```html
@switch (data.kind) {
  @case ('stock') {
    <h2 mat-dialog-title>Stock report</h2>

    <mat-dialog-content>
      <p class="mb-4">Serial number: {{ data.report.serialNumber | valueOrEmpty }}</p>

      @if (data.report.stockEntries.length === 0) {
        <p>No stock entries reported.</p>
      } @else {
        <table class="w-full text-left">
          <thead>
            <tr>
              <th class="font-medium">Item</th>
              <th class="font-medium">Quantity</th>
            </tr>
          </thead>
          <tbody>
            @for (entry of data.report.stockEntries; track entry.itemName) {
              <tr>
                <td>{{ entry.itemName }}</td>
                <td>{{ entry.quantity }}</td>
              </tr>
            }
          </tbody>
        </table>
      }
    </mat-dialog-content>
  }
  @case ('status') {
    <h2 mat-dialog-title>Status report</h2>

    <mat-dialog-content>
      <p class="mb-4">Serial number: {{ data.report.serialNumber | valueOrEmpty }}</p>
      <dl class="grid grid-cols-2 gap-x-4 gap-y-2">
        <dt class="font-medium">Last intervention</dt>
        <dd>{{ data.report.lastIntervention | date: 'short' | valueOrEmpty }}</dd>

        <dt class="font-medium">Temperature</dt>
        <dd>{{ data.report.temperature | valueOrEmpty }}</dd>

        <dt class="font-medium">Power status</dt>
        <dd>{{ data.report.powerStatus | valueOrEmpty }}</dd>

        <dt class="font-medium">Working status</dt>
        <dd>{{ data.report.workingStatus | valueOrEmpty }}</dd>

        <dt class="font-medium">RFID status</dt>
        <dd>{{ data.report.rfidStatus | valueOrEmpty }}</dd>

        <dt class="font-medium">Smart card status</dt>
        <dd>{{ data.report.smartCardStatus | valueOrEmpty }}</dd>

        <dt class="font-medium">Change money status</dt>
        <dd>{{ data.report.changeMoneyStatus | valueOrEmpty }}</dd>
      </dl>
    </mat-dialog-content>
  }
  @case ('orders') {
    <h2 mat-dialog-title>Client orders report</h2>

    <mat-dialog-content>
      <p class="mb-4">Serial number: {{ data.report.serialNumber | valueOrEmpty }}</p>

      @if (data.report.clientOrders.length === 0) {
        <p>No client orders reported.</p>
      } @else {
        <table class="w-full text-left">
          <thead>
            <tr>
              <th class="font-medium">Item</th>
              <th class="font-medium">Price</th>
              <th class="font-medium">Ordered at</th>
            </tr>
          </thead>
          <tbody>
            @for (order of data.report.clientOrders; track order.itemName + order.orderedAt) {
              <tr>
                <td>{{ order.itemName }}</td>
                <td>{{ order.itemPrice }} €</td>
                <td>{{ order.orderedAt | date: 'short' }}</td>
              </tr>
            }
          </tbody>
        </table>
      }

      <p class="mt-4 font-medium">Total: {{ data.report.totalAmount }} €</p>
      <p class="text-sm text-gray-600">Reported at: {{ data.report.reportedAt | date: 'short' }}</p>
    </mat-dialog-content>
  }
}

<mat-dialog-actions align="end">
  <button
    mat-button
    type="button"
    data-testid="machine-report-dialog-close"
    (click)="dialogRef.close()"
  >
    Close
  </button>
</mat-dialog-actions>
```

- [ ] **Step 5: Run test to verify it passes**

Run:

```bash
cd frontend && npm test -- --include src/app/features/machines/machine-report-dialog/machine-report-dialog.spec.ts
```

Expected: PASS.

- [ ] **Step 6: Commit**

Run:

```bash
build-brief ./gradlew clean build
git add frontend/src/app/features/machines/machine-report-dialog/machine-report-dialog.ts frontend/src/app/features/machines/machine-report-dialog/machine-report-dialog.html frontend/src/app/features/machines/machine-report-dialog/machine-report-dialog.spec.ts
git commit -m "feat(frontend): display machine reports in a dialog

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

### Task 3: Panneau admin pour approvisionner le stock

**Files:**
- Create: `frontend/src/app/features/machines/machine-admin-panel/machine-admin-panel.ts`
- Create: `frontend/src/app/features/machines/machine-admin-panel/machine-admin-panel.html`
- Create: `frontend/src/app/features/machines/machine-admin-panel/machine-admin-panel.spec.ts`

**Interfaces:**
- Consumes:
  - `linkHref(resource: HalResource | null | undefined, rel: string): string | undefined`
  - `itemsPageUrl(itemsHref: string | undefined, pageIndex: number, pageSize: number): string | undefined`
  - `ApiRootApi.link(rel: string): string | undefined`
  - `Item`
  - `ItemToProvision`
  - `VendingMachine`
  - `VendingMachineStock`
  - `extractErrorMessage(error: unknown): string | undefined`
- Produces:
  - `selector: 'app-machine-admin-panel'`
  - `readonly machine = input.required<VendingMachine>()`
  - `readonly stock = input<VendingMachineStock | undefined>()`
  - `readonly machineChanged = output<void>()`
  - `readonly stockChanged = output<void>()`

- [ ] **Step 1: Generate component shell**

Run:

```bash
cd frontend && npx ng generate component features/machines/machine-admin-panel --style=none
```

Expected: Angular creates the component files. Rename generated files to 2025 style if the CLI produces `.component.*`.

- [ ] **Step 2: Write the failing test**

Create `frontend/src/app/features/machines/machine-admin-panel/machine-admin-panel.spec.ts`:

```ts
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ApiRootApi } from '../../../core/http/api-root-api';
import { VendingMachine, VendingMachineStock } from '../models/vending-machine';
import { MachineAdminPanel } from './machine-admin-panel';

@Component({
  imports: [MachineAdminPanel],
  template: `
    <app-machine-admin-panel
      [machine]="machine()"
      [stock]="stock()"
      (stockChanged)="stockChanged()"
      (machineChanged)="machineChanged()"
    />
  `,
})
class HostComponent {
  readonly machine = signal<VendingMachine>({
    id: 'm-1',
    serialNumber: 'SN-1',
    address: null,
    lastIntervention: null,
    temperature: 4,
    itemType: 'SNACK',
    powerStatus: 'POWER_ON',
    workingStatus: 'WORKING',
    rfidStatus: 'OK',
    smartCardStatus: 'OK',
    changeMoneyStatus: 'NORMAL',
    _links: { 'status:reset': { href: '/api/v1/vending-machines/m-1/reset' } },
  });
  readonly stock = signal<VendingMachineStock | undefined>({
    itemQuantities: [],
    _links: {
      'stock:provision': { href: '/api/v1/vending-machines/m-1/stock' },
      'stock:report': { href: '/api/v1/vending-machines/m-1/stock/report' },
    },
  });
  readonly stockChanged = vi.fn();
  readonly machineChanged = vi.fn();
}

describe('MachineAdminPanel provisioning', () => {
  let fixture: ComponentFixture<HostComponent>;
  let backend: HttpTestingController;
  let snackBar: { open: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    snackBar = { open: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [HostComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        ApiRootApi,
        { provide: MatSnackBar, useValue: snackBar },
      ],
    }).compileComponents();

    backend = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(HostComponent);
    fixture.detectChanges();
  });

  function flushApiRootAndItems() {
    backend.expectOne('/api/v1').flush({
      _links: {
        items: { href: '/api/v1/items' },
        vendingMachines: { href: '/api/v1/vending-machines' },
      },
    });
    fixture.detectChanges();

    backend.expectOne('/api/v1/items?page=1&size=100').flush({
      _embedded: {
        elements: [
          { id: 'i-snack', name: 'Chips', type: 'SNACK', price: 2, _links: {} },
          { id: 'i-drink', name: 'Cola', type: 'COLD_BEVERAGE', price: 1.5, _links: {} },
        ],
      },
      page: { size: 100, totalElements: 2, totalPages: 1, number: 0 },
    });
    fixture.detectChanges();
  }

  afterEach(() => backend.verify());

  it('loads items from the API root and filters them to the machine item type', async () => {
    flushApiRootAndItems();
    await fixture.whenStable();

    const text = fixture.nativeElement.textContent;

    expect(text).toContain('Chips');
    expect(text).not.toContain('Cola');
  });

  it('posts the selected item and quantity to the stock provision link', async () => {
    flushApiRootAndItems();
    await fixture.whenStable();

    const select = fixture.nativeElement.querySelector('mat-select');
    select.click();
    fixture.detectChanges();
    await fixture.whenStable();

    const option = document.querySelector('mat-option') as HTMLElement;
    option.click();
    fixture.detectChanges();

    const quantityInput: HTMLInputElement = fixture.nativeElement.querySelector(
      '[data-testid="machine-admin-provision-quantity"]',
    );
    quantityInput.value = '7';
    quantityInput.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    const submitButton: HTMLButtonElement = fixture.nativeElement.querySelector(
      '[data-testid="machine-admin-provision-submit"]',
    );
    submitButton.click();

    const request = backend.expectOne('/api/v1/vending-machines/m-1/stock');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ itemId: 'i-snack', quantity: 7 });

    request.flush({
      itemQuantities: [{ itemId: 'i-snack', itemName: 'Chips', quantity: 7 }],
      _links: {},
    });
    await fixture.whenStable();

    expect(fixture.componentInstance.stockChanged).toHaveBeenCalled();
    expect(snackBar.open).toHaveBeenCalledWith('Stock provisioned.', 'Close', { duration: 5000 });
  });

  it('does not submit when the quantity is zero', async () => {
    flushApiRootAndItems();
    await fixture.whenStable();

    const submitButton: HTMLButtonElement = fixture.nativeElement.querySelector(
      '[data-testid="machine-admin-provision-submit"]',
    );

    expect(submitButton.disabled).toBe(true);
  });

  it('shows the backend business error message when provisioning fails', async () => {
    flushApiRootAndItems();
    await fixture.whenStable();

    const select = fixture.nativeElement.querySelector('mat-select');
    select.click();
    fixture.detectChanges();
    await fixture.whenStable();

    const option = document.querySelector('mat-option') as HTMLElement;
    option.click();
    fixture.detectChanges();

    const quantityInput: HTMLInputElement = fixture.nativeElement.querySelector(
      '[data-testid="machine-admin-provision-quantity"]',
    );
    quantityInput.value = '3';
    quantityInput.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    fixture.nativeElement
      .querySelector('[data-testid="machine-admin-provision-submit"]')
      .click();

    backend
      .expectOne('/api/v1/vending-machines/m-1/stock')
      .flush(
        { timestamp: '2026-09-25T10:00:00Z', message: 'Item type COLD_BEVERAGE is unsupported' },
        { status: 400, statusText: 'Bad Request' },
      );
    await fixture.whenStable();

    expect(snackBar.open).toHaveBeenCalledWith(
      'Item type COLD_BEVERAGE is unsupported',
      'Close',
      { duration: 5000 },
    );
  });
});
```

- [ ] **Step 3: Run test to verify it fails**

Run:

```bash
cd frontend && npm test -- --include src/app/features/machines/machine-admin-panel/machine-admin-panel.spec.ts
```

Expected: FAIL because `MachineAdminPanel` has no inputs/form/provision logic.

- [ ] **Step 4: Write minimal implementation**

Replace `frontend/src/app/features/machines/machine-admin-panel/machine-admin-panel.ts` with:

```ts
import { HttpClient, httpResource } from '@angular/common/http';
import { Component, computed, inject, input, output, signal } from '@angular/core';
import { form, FormField, required, submit } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { firstValueFrom } from 'rxjs';
import { ApiRootApi } from '../../../core/http/api-root-api';
import { itemsPageUrl } from '../../items/item-api';
import { Item } from '../../items/models/item';
import { linkHref, HalPage } from '../../../shared/models/hal';
import { Page } from '../../../shared/models/page';
import { extractErrorMessage } from '../extract-error-message';
import {
  ItemToProvision,
  VendingMachine,
  VendingMachineStock,
} from '../models/vending-machine';

interface ProvisionFormValue {
  itemId: string | null;
  quantity: number | null;
}

@Component({
  selector: 'app-machine-admin-panel',
  imports: [
    FormField,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
  ],
  templateUrl: './machine-admin-panel.html',
})
export class MachineAdminPanel {
  private readonly apiRoot = inject(ApiRootApi);
  private readonly http = inject(HttpClient);
  private readonly snackBar = inject(MatSnackBar);

  readonly machine = input.required<VendingMachine>();
  readonly stock = input<VendingMachineStock | undefined>();
  readonly machineChanged = output<void>();
  readonly stockChanged = output<void>();

  private readonly itemsResource = httpResource<HalPage<Item>>(() =>
    itemsPageUrl(this.apiRoot.link('items'), 0, 100),
  );

  readonly provision = signal<ProvisionFormValue>({ itemId: null, quantity: 1 });
  readonly provisionForm = form(this.provision, (path) => {
    required(path.itemId);
    required(path.quantity);
  });
  readonly provisioning = signal(false);
  readonly itemsLoading = this.itemsResource.isLoading;
  readonly provisionLink = computed(() => linkHref(this.stock(), 'stock:provision'));
  readonly items = computed(() =>
    this.itemsResource.hasValue() ? Page.fromHalPage(this.itemsResource.value()).elements : [],
  );
  readonly selectableItems = computed(() =>
    this.items().filter((item) => item.type === this.machine().itemType),
  );
  readonly canSubmitProvision = computed(() => {
    const value = this.provision();
    return (
      this.provisionLink() !== undefined &&
      !this.provisioning() &&
      value.itemId !== null &&
      typeof value.quantity === 'number' &&
      value.quantity > 0
    );
  });

  provisionStock(): void {
    if (!this.canSubmitProvision()) {
      return;
    }

    void submit(this.provisionForm, async () => {
      const href = this.provisionLink();
      const value = this.provision();

      if (!href || value.itemId === null || value.quantity === null || value.quantity <= 0) {
        return;
      }

      this.provisioning.set(true);

      try {
        await firstValueFrom(
          this.http.post<VendingMachineStock>(href, {
            itemId: value.itemId,
            quantity: value.quantity,
          } satisfies ItemToProvision),
        );
        this.snackBar.open('Stock provisioned.', 'Close', { duration: 5000 });
        this.stockChanged.emit();
      } catch (error) {
        this.snackBar.open(
          extractErrorMessage(error) ?? 'The stock could not be provisioned.',
          'Close',
          { duration: 5000 },
        );
      } finally {
        this.provisioning.set(false);
      }
    });
  }
}
```

Replace `frontend/src/app/features/machines/machine-admin-panel/machine-admin-panel.html` with:

```html
<section class="mt-6" aria-labelledby="machine-admin-title">
  <h2 id="machine-admin-title" class="mb-2 text-lg font-semibold">Administration</h2>

  <mat-card>
    <mat-card-content>
      <form class="flex flex-col gap-4" (submit)="$event.preventDefault(); provisionStock()">
        <h3 class="font-medium">Provision stock</h3>

        <mat-form-field>
          <mat-label>Item</mat-label>
          <mat-select [formField]="provisionForm.itemId">
            @for (item of selectableItems(); track item.id) {
              <mat-option [value]="item.id">{{ item.name }}</mat-option>
            }
          </mat-select>
        </mat-form-field>
        @if (itemsLoading()) {
          <p class="text-sm text-gray-600">Loading items…</p>
        }
        @if (!itemsLoading() && selectableItems().length === 0) {
          <p class="text-sm text-gray-600">No compatible item found.</p>
        }

        <mat-form-field>
          <mat-label>Quantity</mat-label>
          <input
            matInput
            type="number"
            min="1"
            data-testid="machine-admin-provision-quantity"
            [formField]="provisionForm.quantity"
          />
        </mat-form-field>
        @if (
          provisionForm.quantity().touched() &&
          (provision().quantity === null || provision().quantity! <= 0)
        ) {
          <p class="text-sm text-red-600" role="alert">Quantity must be greater than 0.</p>
        }

        <button
          mat-flat-button
          type="submit"
          color="primary"
          data-testid="machine-admin-provision-submit"
          [disabled]="!canSubmitProvision()"
        >
          {{ provisioning() ? 'Provisioning…' : 'Provision stock' }}
        </button>
      </form>
    </mat-card-content>
  </mat-card>
</section>
```

- [ ] **Step 5: Run test to verify it passes**

Run:

```bash
cd frontend && npm test -- --include src/app/features/machines/machine-admin-panel/machine-admin-panel.spec.ts
```

Expected: PASS.

- [ ] **Step 6: Commit**

Run:

```bash
build-brief ./gradlew clean build
git add frontend/src/app/features/machines/machine-admin-panel/machine-admin-panel.ts frontend/src/app/features/machines/machine-admin-panel/machine-admin-panel.html frontend/src/app/features/machines/machine-admin-panel/machine-admin-panel.spec.ts
git commit -m "feat(frontend): provision machine stock from admin panel

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

### Task 4: Reset conditionnel et génération des rapports dans le panneau admin

**Files:**
- Modify: `frontend/src/app/features/machines/machine-admin-panel/machine-admin-panel.ts`
- Modify: `frontend/src/app/features/machines/machine-admin-panel/machine-admin-panel.html`
- Modify: `frontend/src/app/features/machines/machine-admin-panel/machine-admin-panel.spec.ts`

**Interfaces:**
- Consumes:
  - `isAbnormalStatus(machine): boolean`
  - `linkHref(resource, 'status:reset' | 'status:report' | 'orders:report' | 'stock:report')`
  - `MachineReportDialog`
  - `MachineReportDialogData`
- Produces:
  - Reset visible iff `isAbnormalStatus(machine()) && linkHref(machine(), 'status:reset')`
  - `machineChanged.emit()` after successful reset
  - Dialog opens with `{ kind: 'stock' | 'status' | 'orders', report }`

- [ ] **Step 1: Replace the test with reset/report coverage**

Replace `frontend/src/app/features/machines/machine-admin-panel/machine-admin-panel.spec.ts` with:

```ts
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { of } from 'rxjs';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ApiRootApi } from '../../../core/http/api-root-api';
import { MachineReportDialog } from '../machine-report-dialog/machine-report-dialog';
import { VendingMachine, VendingMachineStock } from '../models/vending-machine';
import { MachineAdminPanel } from './machine-admin-panel';

@Component({
  imports: [MachineAdminPanel],
  template: `
    <app-machine-admin-panel
      [machine]="machine()"
      [stock]="stock()"
      (stockChanged)="stockChanged()"
      (machineChanged)="machineChanged()"
    />
  `,
})
class HostComponent {
  readonly machine = signal<VendingMachine>({
    id: 'm-1',
    serialNumber: 'SN-1',
    address: null,
    lastIntervention: null,
    temperature: 4,
    itemType: 'SNACK',
    powerStatus: 'POWER_ON',
    workingStatus: 'ERROR',
    rfidStatus: 'OK',
    smartCardStatus: 'OK',
    changeMoneyStatus: 'NORMAL',
    _links: {
      'status:reset': { href: '/api/v1/vending-machines/m-1/reset' },
      'status:report': { href: '/api/v1/vending-machines/m-1/status/report' },
      'orders:report': { href: '/api/v1/vending-machines/m-1/orders/report' },
    },
  });
  readonly stock = signal<VendingMachineStock | undefined>({
    itemQuantities: [],
    _links: {
      'stock:provision': { href: '/api/v1/vending-machines/m-1/stock' },
      'stock:report': { href: '/api/v1/vending-machines/m-1/stock/report' },
    },
  });
  readonly stockChanged = vi.fn();
  readonly machineChanged = vi.fn();
}

describe('MachineAdminPanel', () => {
  let fixture: ComponentFixture<HostComponent>;
  let backend: HttpTestingController;
  let snackBar: { open: ReturnType<typeof vi.fn> };
  let dialog: { open: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    snackBar = { open: vi.fn() };
    dialog = { open: vi.fn(() => ({ afterClosed: () => of(undefined) })) };

    await TestBed.configureTestingModule({
      imports: [HostComponent],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        ApiRootApi,
        { provide: MatSnackBar, useValue: snackBar },
        { provide: MatDialog, useValue: dialog },
      ],
    }).compileComponents();

    backend = TestBed.inject(HttpTestingController);
    fixture = TestBed.createComponent(HostComponent);
    fixture.detectChanges();
  });

  function flushApiRootAndItems() {
    backend.expectOne('/api/v1').flush({
      _links: {
        items: { href: '/api/v1/items' },
        vendingMachines: { href: '/api/v1/vending-machines' },
      },
    });
    fixture.detectChanges();

    backend.expectOne('/api/v1/items?page=1&size=100').flush({
      _embedded: {
        elements: [
          { id: 'i-snack', name: 'Chips', type: 'SNACK', price: 2, _links: {} },
          { id: 'i-drink', name: 'Cola', type: 'COLD_BEVERAGE', price: 1.5, _links: {} },
        ],
      },
      page: { size: 100, totalElements: 2, totalPages: 1, number: 0 },
    });
    fixture.detectChanges();
  }

  afterEach(() => backend.verify());

  it('loads items and filters them to the machine item type', async () => {
    flushApiRootAndItems();
    await fixture.whenStable();

    const text = fixture.nativeElement.textContent;

    expect(text).toContain('Chips');
    expect(text).not.toContain('Cola');
  });

  it('posts provision payloads to stock:provision and emits stockChanged', async () => {
    flushApiRootAndItems();
    await fixture.whenStable();

    fixture.nativeElement.querySelector('mat-select').click();
    fixture.detectChanges();
    await fixture.whenStable();
    (document.querySelector('mat-option') as HTMLElement).click();
    fixture.detectChanges();

    const quantityInput: HTMLInputElement = fixture.nativeElement.querySelector(
      '[data-testid="machine-admin-provision-quantity"]',
    );
    quantityInput.value = '7';
    quantityInput.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    fixture.nativeElement
      .querySelector('[data-testid="machine-admin-provision-submit"]')
      .click();

    const request = backend.expectOne('/api/v1/vending-machines/m-1/stock');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({ itemId: 'i-snack', quantity: 7 });
    request.flush({ itemQuantities: [], _links: {} });
    await fixture.whenStable();

    expect(fixture.componentInstance.stockChanged).toHaveBeenCalled();
  });

  it('shows the backend business error message when provisioning fails', async () => {
    flushApiRootAndItems();
    await fixture.whenStable();

    fixture.nativeElement.querySelector('mat-select').click();
    fixture.detectChanges();
    await fixture.whenStable();
    (document.querySelector('mat-option') as HTMLElement).click();
    fixture.detectChanges();

    const quantityInput: HTMLInputElement = fixture.nativeElement.querySelector(
      '[data-testid="machine-admin-provision-quantity"]',
    );
    quantityInput.value = '3';
    quantityInput.dispatchEvent(new Event('input'));
    fixture.detectChanges();

    fixture.nativeElement
      .querySelector('[data-testid="machine-admin-provision-submit"]')
      .click();

    backend
      .expectOne('/api/v1/vending-machines/m-1/stock')
      .flush(
        { timestamp: '2026-09-25T10:00:00Z', message: 'Item type COLD_BEVERAGE is unsupported' },
        { status: 400, statusText: 'Bad Request' },
      );
    await fixture.whenStable();

    expect(snackBar.open).toHaveBeenCalledWith(
      'Item type COLD_BEVERAGE is unsupported',
      'Close',
      { duration: 5000 },
    );
  });

  it('shows the reset button only for abnormal machines with a reset link', async () => {
    flushApiRootAndItems();
    await fixture.whenStable();

    expect(fixture.nativeElement.textContent).toContain('Reset status');

    fixture.componentInstance.machine.update((machine) => ({
      ...machine,
      workingStatus: 'WORKING',
    }));
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).not.toContain('Reset status');

    fixture.componentInstance.machine.update((machine) => ({
      ...machine,
      workingStatus: 'ERROR',
      _links: {},
    }));
    fixture.detectChanges();

    expect(fixture.nativeElement.textContent).not.toContain('Reset status');
  });

  it('posts reset and emits machineChanged', async () => {
    flushApiRootAndItems();
    await fixture.whenStable();

    fixture.nativeElement.querySelector('[data-testid="machine-admin-reset"]').click();

    const request = backend.expectOne('/api/v1/vending-machines/m-1/reset');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({});
    request.flush({
      ...fixture.componentInstance.machine(),
      workingStatus: 'WORKING',
      _links: {},
    });
    await fixture.whenStable();

    expect(snackBar.open).toHaveBeenCalledWith('Machine status reset.', 'Close', {
      duration: 5000,
    });
    expect(fixture.componentInstance.machineChanged).toHaveBeenCalled();
  });

  it('opens the stock report dialog from stock:report', async () => {
    flushApiRootAndItems();
    await fixture.whenStable();

    fixture.nativeElement.querySelector('[data-testid="machine-admin-stock-report"]').click();

    const request = backend.expectOne('/api/v1/vending-machines/m-1/stock/report');
    expect(request.request.method).toBe('POST');
    request.flush({
      id: 'r-stock',
      serialNumber: 'SN-1',
      stockEntries: [{ itemName: 'Chips', quantity: 7 }],
    });
    await fixture.whenStable();

    expect(dialog.open).toHaveBeenCalledWith(MachineReportDialog, {
      data: {
        kind: 'stock',
        report: {
          id: 'r-stock',
          serialNumber: 'SN-1',
          stockEntries: [{ itemName: 'Chips', quantity: 7 }],
        },
      },
    });
  });

  it('opens the status report dialog from status:report', async () => {
    flushApiRootAndItems();
    await fixture.whenStable();

    fixture.nativeElement.querySelector('[data-testid="machine-admin-status-report"]').click();

    const request = backend.expectOne('/api/v1/vending-machines/m-1/status/report');
    expect(request.request.method).toBe('POST');
    request.flush({
      serialNumber: 'SN-1',
      lastIntervention: null,
      temperature: 4,
      powerStatus: 'POWER_ON',
      workingStatus: 'ERROR',
      rfidStatus: 'OK',
      smartCardStatus: 'OK',
      changeMoneyStatus: 'NORMAL',
      _links: { vendingMachine: { href: '/api/v1/vending-machines/m-1' } },
    });
    await fixture.whenStable();

    expect(dialog.open).toHaveBeenCalledWith(MachineReportDialog, {
      data: {
        kind: 'status',
        report: {
          serialNumber: 'SN-1',
          lastIntervention: null,
          temperature: 4,
          powerStatus: 'POWER_ON',
          workingStatus: 'ERROR',
          rfidStatus: 'OK',
          smartCardStatus: 'OK',
          changeMoneyStatus: 'NORMAL',
          _links: { vendingMachine: { href: '/api/v1/vending-machines/m-1' } },
        },
      },
    });
  });

  it('opens the orders report dialog from orders:report', async () => {
    flushApiRootAndItems();
    await fixture.whenStable();

    fixture.nativeElement.querySelector('[data-testid="machine-admin-orders-report"]').click();

    const request = backend.expectOne('/api/v1/vending-machines/m-1/orders/report');
    expect(request.request.method).toBe('POST');
    request.flush({
      serialNumber: 'SN-1',
      clientOrders: [{ itemName: 'Chips', itemPrice: 2, orderedAt: '2026-09-25T10:00:00' }],
      totalAmount: 2,
      reportedAt: '2026-09-25T11:00:00',
    });
    await fixture.whenStable();

    expect(dialog.open).toHaveBeenCalledWith(MachineReportDialog, {
      data: {
        kind: 'orders',
        report: {
          serialNumber: 'SN-1',
          clientOrders: [{ itemName: 'Chips', itemPrice: 2, orderedAt: '2026-09-25T10:00:00' }],
          totalAmount: 2,
          reportedAt: '2026-09-25T11:00:00',
        },
      },
    });
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
cd frontend && npm test -- --include src/app/features/machines/machine-admin-panel/machine-admin-panel.spec.ts
```

Expected: FAIL because reset/report buttons and methods do not exist.

- [ ] **Step 3: Write minimal implementation**

Replace `frontend/src/app/features/machines/machine-admin-panel/machine-admin-panel.ts` with:

```ts
import { HttpClient, httpResource } from '@angular/common/http';
import { Component, computed, inject, input, output, signal } from '@angular/core';
import { form, FormField, required, submit } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar } from '@angular/material/snack-bar';
import { firstValueFrom } from 'rxjs';
import { ApiRootApi } from '../../../core/http/api-root-api';
import { HalPage, linkHref } from '../../../shared/models/hal';
import { Page } from '../../../shared/models/page';
import { itemsPageUrl } from '../../items/item-api';
import { Item } from '../../items/models/item';
import { extractErrorMessage } from '../extract-error-message';
import {
  ItemToProvision,
  VendingMachine,
  VendingMachineClientOrdersReport,
  VendingMachineStatusReport,
  VendingMachineStock,
  VendingMachineStockReport,
} from '../models/vending-machine';
import { isAbnormalStatus } from '../models/vending-machine-status';
import { MachineReportDialog } from '../machine-report-dialog/machine-report-dialog';

interface ProvisionFormValue {
  itemId: string | null;
  quantity: number | null;
}

@Component({
  selector: 'app-machine-admin-panel',
  imports: [
    FormField,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
  ],
  templateUrl: './machine-admin-panel.html',
})
export class MachineAdminPanel {
  private readonly apiRoot = inject(ApiRootApi);
  private readonly dialog = inject(MatDialog);
  private readonly http = inject(HttpClient);
  private readonly snackBar = inject(MatSnackBar);

  readonly machine = input.required<VendingMachine>();
  readonly stock = input<VendingMachineStock | undefined>();
  readonly machineChanged = output<void>();
  readonly stockChanged = output<void>();

  private readonly itemsResource = httpResource<HalPage<Item>>(() =>
    itemsPageUrl(this.apiRoot.link('items'), 0, 100),
  );

  readonly provision = signal<ProvisionFormValue>({ itemId: null, quantity: 1 });
  readonly provisionForm = form(this.provision, (path) => {
    required(path.itemId);
    required(path.quantity);
  });
  readonly provisioning = signal(false);
  readonly resetting = signal(false);
  readonly reporting = signal<'stock' | 'status' | 'orders' | null>(null);
  readonly itemsLoading = this.itemsResource.isLoading;

  readonly provisionLink = computed(() => linkHref(this.stock(), 'stock:provision'));
  readonly resetLink = computed(() => linkHref(this.machine(), 'status:reset'));
  readonly statusReportLink = computed(() => linkHref(this.machine(), 'status:report'));
  readonly stockReportLink = computed(() => linkHref(this.stock(), 'stock:report'));
  readonly ordersReportLink = computed(() => linkHref(this.machine(), 'orders:report'));
  readonly canReset = computed(() => isAbnormalStatus(this.machine()) && this.resetLink() !== undefined);

  readonly items = computed(() =>
    this.itemsResource.hasValue() ? Page.fromHalPage(this.itemsResource.value()).elements : [],
  );
  readonly selectableItems = computed(() =>
    this.items().filter((item) => item.type === this.machine().itemType),
  );
  readonly canSubmitProvision = computed(() => {
    const value = this.provision();
    return (
      this.provisionLink() !== undefined &&
      !this.provisioning() &&
      value.itemId !== null &&
      typeof value.quantity === 'number' &&
      value.quantity > 0
    );
  });

  provisionStock(): void {
    if (!this.canSubmitProvision()) {
      return;
    }

    void submit(this.provisionForm, async () => {
      const href = this.provisionLink();
      const value = this.provision();

      if (!href || value.itemId === null || value.quantity === null || value.quantity <= 0) {
        return;
      }

      this.provisioning.set(true);

      try {
        await firstValueFrom(
          this.http.post<VendingMachineStock>(href, {
            itemId: value.itemId,
            quantity: value.quantity,
          } satisfies ItemToProvision),
        );
        this.snackBar.open('Stock provisioned.', 'Close', { duration: 5000 });
        this.stockChanged.emit();
      } catch (error) {
        this.snackBar.open(
          extractErrorMessage(error) ?? 'The stock could not be provisioned.',
          'Close',
          { duration: 5000 },
        );
      } finally {
        this.provisioning.set(false);
      }
    });
  }

  resetStatus(): void {
    const href = this.resetLink();
    if (!href || !this.canReset()) {
      return;
    }

    this.resetting.set(true);
    this.http.post<VendingMachine>(href, {}).subscribe({
      next: () => {
        this.snackBar.open('Machine status reset.', 'Close', { duration: 5000 });
        this.machineChanged.emit();
      },
      error: (error) => {
        this.snackBar.open(
          extractErrorMessage(error) ?? 'The machine status could not be reset.',
          'Close',
          { duration: 5000 },
        );
      },
      complete: () => this.resetting.set(false),
    });
  }

  generateStockReport(): void {
    const href = this.stockReportLink();
    if (!href) {
      return;
    }

    this.reporting.set('stock');
    this.http.post<VendingMachineStockReport>(href, {}).subscribe({
      next: (report) => this.dialog.open(MachineReportDialog, { data: { kind: 'stock', report } }),
      error: (error) => this.reportError(error),
      complete: () => this.reporting.set(null),
    });
  }

  generateStatusReport(): void {
    const href = this.statusReportLink();
    if (!href) {
      return;
    }

    this.reporting.set('status');
    this.http.post<VendingMachineStatusReport>(href, {}).subscribe({
      next: (report) => this.dialog.open(MachineReportDialog, { data: { kind: 'status', report } }),
      error: (error) => this.reportError(error),
      complete: () => this.reporting.set(null),
    });
  }

  generateOrdersReport(): void {
    const href = this.ordersReportLink();
    if (!href) {
      return;
    }

    this.reporting.set('orders');
    this.http.post<VendingMachineClientOrdersReport>(href, {}).subscribe({
      next: (report) => this.dialog.open(MachineReportDialog, { data: { kind: 'orders', report } }),
      error: (error) => this.reportError(error),
      complete: () => this.reporting.set(null),
    });
  }

  private reportError(error: unknown): void {
    this.snackBar.open(
      extractErrorMessage(error) ?? 'The report could not be generated.',
      'Close',
      { duration: 5000 },
    );
  }
}
```

Replace `frontend/src/app/features/machines/machine-admin-panel/machine-admin-panel.html` with:

```html
<section class="mt-6" aria-labelledby="machine-admin-title">
  <h2 id="machine-admin-title" class="mb-2 text-lg font-semibold">Administration</h2>

  <div class="grid gap-4 lg:grid-cols-2">
    <mat-card>
      <mat-card-content>
        <form class="flex flex-col gap-4" (submit)="$event.preventDefault(); provisionStock()">
          <h3 class="font-medium">Provision stock</h3>

          <mat-form-field>
            <mat-label>Item</mat-label>
            <mat-select [formField]="provisionForm.itemId">
              @for (item of selectableItems(); track item.id) {
                <mat-option [value]="item.id">{{ item.name }}</mat-option>
              }
            </mat-select>
          </mat-form-field>
          @if (itemsLoading()) {
            <p class="text-sm text-gray-600">Loading items…</p>
          }
          @if (!itemsLoading() && selectableItems().length === 0) {
            <p class="text-sm text-gray-600">No compatible item found.</p>
          }

          <mat-form-field>
            <mat-label>Quantity</mat-label>
            <input
              matInput
              type="number"
              min="1"
              data-testid="machine-admin-provision-quantity"
              [formField]="provisionForm.quantity"
            />
          </mat-form-field>
          @if (
            provisionForm.quantity().touched() &&
            (provision().quantity === null || provision().quantity! <= 0)
          ) {
            <p class="text-sm text-red-600" role="alert">Quantity must be greater than 0.</p>
          }

          <button
            mat-flat-button
            type="submit"
            color="primary"
            data-testid="machine-admin-provision-submit"
            [disabled]="!canSubmitProvision()"
          >
            {{ provisioning() ? 'Provisioning…' : 'Provision stock' }}
          </button>
        </form>
      </mat-card-content>
    </mat-card>

    <mat-card>
      <mat-card-content>
        <h3 class="mb-4 font-medium">Status and reports</h3>

        <div class="flex flex-wrap gap-2">
          @if (canReset()) {
            <button
              mat-flat-button
              type="button"
              color="primary"
              data-testid="machine-admin-reset"
              [disabled]="resetting()"
              (click)="resetStatus()"
            >
              {{ resetting() ? 'Resetting…' : 'Reset status' }}
            </button>
          }

          @if (stockReportLink()) {
            <button
              mat-stroked-button
              type="button"
              data-testid="machine-admin-stock-report"
              [disabled]="reporting() === 'stock'"
              (click)="generateStockReport()"
            >
              Generate stock report
            </button>
          }

          @if (statusReportLink()) {
            <button
              mat-stroked-button
              type="button"
              data-testid="machine-admin-status-report"
              [disabled]="reporting() === 'status'"
              (click)="generateStatusReport()"
            >
              Generate status report
            </button>
          }

          @if (ordersReportLink()) {
            <button
              mat-stroked-button
              type="button"
              data-testid="machine-admin-orders-report"
              [disabled]="reporting() === 'orders'"
              (click)="generateOrdersReport()"
            >
              Generate client orders report
            </button>
          }
        </div>
      </mat-card-content>
    </mat-card>
  </div>
</section>
```

- [ ] **Step 4: Run test to verify it passes**

Run:

```bash
cd frontend && npm test -- --include src/app/features/machines/machine-admin-panel/machine-admin-panel.spec.ts
```

Expected: PASS.

- [ ] **Step 5: Commit**

Run:

```bash
build-brief ./gradlew clean build
git add frontend/src/app/features/machines/machine-admin-panel/machine-admin-panel.ts frontend/src/app/features/machines/machine-admin-panel/machine-admin-panel.html frontend/src/app/features/machines/machine-admin-panel/machine-admin-panel.spec.ts
git commit -m "feat(frontend): add machine admin reset and reports

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

### Task 5: Intégrer le panneau admin dans MachineDetail avec hooks de reload

**Files:**
- Modify: `frontend/src/app/features/machines/machine-detail/machine-detail.ts`
- Modify: `frontend/src/app/features/machines/machine-detail/machine-detail.html`
- Modify: `frontend/src/app/features/machines/machine-detail/machine-detail.spec.ts`

**Interfaces:**
- Consumes:
  - `MachineAdminPanel`
  - `machine = input.required<VendingMachine>()`
  - `stock = input<VendingMachineStock | undefined>()`
  - `machineChanged = output<void>()`
  - `stockChanged = output<void>()`
- Produces:
  - `onMachineChanged(): void` calls `this.resource.reload()`
  - `onStockChanged(): void` calls `this.stockResource.reload()`
  - Template renders `<app-machine-admin-panel>` only when `isAdmin()`

- [ ] **Step 1: Add failing MachineDetail tests**

Append these tests to `frontend/src/app/features/machines/machine-detail/machine-detail.spec.ts` inside the existing `describe('MachineDetail', ...)` block:

```ts
  it('renders the admin panel only for admins and passes the loaded machine and stock', async () => {
    TestBed.inject(TokenStore).setAccessToken(
      fakeJwt({ sub: 'admin@vending.me', roles: ['ROLE_ADMIN'], exp: 1 }),
    );

    await navigate();
    await flushMachineAndStock();
    await harness.fixture.whenStable();

    expect(harness.fixture.nativeElement.textContent).toContain('Administration');

    TestBed.inject(TokenStore).clear();
    harness.fixture.detectChanges();

    expect(harness.fixture.nativeElement.textContent).not.toContain('Administration');
  });

  it('reloads the machine resource when the admin panel emits machineChanged', async () => {
    TestBed.inject(TokenStore).setAccessToken(
      fakeJwt({ sub: 'admin@vending.me', roles: ['ROLE_ADMIN'], exp: 1 }),
    );

    const component = await navigate();
    await flushMachineAndStock();
    await harness.fixture.whenStable();

    component.onMachineChanged();
    harness.fixture.detectChanges();

    const reload = backend.expectOne('/api/v1/vending-machines/m-1');
    reload.flush({
      id: 'm-1',
      serialNumber: 'SN-1',
      address: null,
      lastIntervention: null,
      temperature: 4,
      itemType: 'SNACK',
      powerStatus: 'POWER_ON',
      workingStatus: 'WORKING',
      rfidStatus: 'OK',
      smartCardStatus: 'OK',
      changeMoneyStatus: 'NORMAL',
      _links: { stock: { href: 'https://api.example.test/vending-machines/m-1/stock' } },
    });
    await harness.fixture.whenStable();

    expect(component.machine()?.workingStatus).toBe('WORKING');
  });

  it('reloads the stock resource when the admin panel emits stockChanged', async () => {
    TestBed.inject(TokenStore).setAccessToken(
      fakeJwt({ sub: 'admin@vending.me', roles: ['ROLE_ADMIN'], exp: 1 }),
    );

    const component = await navigate();
    await flushMachineAndStock();
    await harness.fixture.whenStable();

    component.onStockChanged();
    harness.fixture.detectChanges();

    backend.expectOne('https://api.example.test/vending-machines/m-1/stock').flush({
      itemQuantities: [{ itemId: 'i-1', itemName: 'Water', quantity: 8 }],
      _links: {},
    });
    await harness.fixture.whenStable();

    expect(component.itemQuantities()).toEqual([{ itemId: 'i-1', itemName: 'Water', quantity: 8 }]);
  });
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
cd frontend && npm test -- --include src/app/features/machines/machine-detail/machine-detail.spec.ts
```

Expected: FAIL because `onMachineChanged()`/`onStockChanged()` and the panel integration do not exist.

- [ ] **Step 3: Write minimal implementation**

Replace `frontend/src/app/features/machines/machine-detail/machine-detail.ts` with:

```ts
import { DatePipe } from '@angular/common';
import { HttpClient, httpResource } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/auth/auth';
import { ValueOrEmptyPipe } from '../../../shared/value-or-empty-pipe';
import { extractErrorMessage } from '../extract-error-message';
import { MachineAdminPanel } from '../machine-admin-panel/machine-admin-panel';
import { ClientOrder } from '../models/client-order';
import { ItemQuantity, VendingMachine, VendingMachineStock } from '../models/vending-machine';
import {
  OrderConfirmDialog,
  OrderConfirmDialogData,
} from '../order-confirm-dialog/order-confirm-dialog';
import { machineUrl } from '../vending-machine-api';

interface DetailNavigationState {
  href?: string;
}

@Component({
  selector: 'app-machine-detail',
  imports: [
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    RouterLink,
    ValueOrEmptyPipe,
    DatePipe,
    MachineAdminPanel,
  ],
  templateUrl: './machine-detail.html',
})
export class MachineDetail {
  private readonly route = inject(ActivatedRoute);
  readonly auth = inject(AuthService);
  private readonly http = inject(HttpClient);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly isAdmin = computed(() => this.auth.roles().includes('ROLE_ADMIN'));
  readonly ordering = signal<string | null>(null);

  private readonly resourceUrl =
    (history.state as DetailNavigationState | null)?.href ??
    machineUrl(this.route.snapshot.paramMap.get('id')!);

  private readonly resource = httpResource<VendingMachine>(() => this.resourceUrl);

  private readonly stockUrl = computed(() => {
    const links = this.resource.value()?._links?.['stock'];
    return links && !Array.isArray(links) ? links.href : undefined;
  });
  private readonly stockResource = httpResource<VendingMachineStock>(() => this.stockUrl());

  readonly isLoading = this.resource.isLoading;
  readonly hasError = computed(() => this.resource.error() !== undefined);
  readonly machine = computed(() => this.resource.value());
  readonly stock = computed(() => this.stockResource.value());
  readonly stockLoading = this.stockResource.isLoading;
  readonly stockError = computed(() => this.stockResource.error() !== undefined);
  readonly itemQuantities = computed(() => this.stockResource.value()?.itemQuantities ?? []);

  itemLink(itemId: string): string | undefined {
    const links = this.stockResource.value()?._links?.['item'];
    const itemLinks = Array.isArray(links) ? links : links ? [links] : [];
    return itemLinks.find((link) => link.href.includes(itemId))?.href;
  }

  orderLink(itemId: string): string | undefined {
    const links = this.stockResource.value()?._links?.['order'];
    const orderLinks = Array.isArray(links) ? links : links ? [links] : [];
    return orderLinks.find((link) => link.href.includes(itemId))?.href;
  }

  canOrder(itemId: string): boolean {
    return this.auth.roles().includes('ROLE_USER') && this.orderLink(itemId) !== undefined;
  }

  orderItem(itemQuantity: ItemQuantity): void {
    const orderLink = this.orderLink(itemQuantity.itemId);
    if (!orderLink) {
      return;
    }

    this.dialog
      .open(OrderConfirmDialog, {
        data: {
          itemName: itemQuantity.itemName,
          quantity: itemQuantity.quantity,
        } satisfies OrderConfirmDialogData,
      })
      .afterClosed()
      .subscribe((confirmed) => {
        if (confirmed !== true) {
          return;
        }

        this.ordering.set(itemQuantity.itemId);

        this.http
          .post<ClientOrder>(orderLink, {})
          .pipe(finalize(() => this.ordering.set(null)))
          .subscribe({
            next: (order) => {
              this.snackBar.open(
                `Ordered ${itemQuantity.itemName} for ${order.amount} €`,
                'Close',
                { duration: 5000 },
              );
              this.stockResource.reload();
            },
            error: (error) => {
              this.snackBar.open(
                extractErrorMessage(error) ?? 'The item could not be ordered.',
                'Close',
                { duration: 5000 },
              );
              this.stockResource.reload();
            },
          });
      });
  }

  onMachineChanged(): void {
    this.resource.reload();
  }

  onStockChanged(): void {
    this.stockResource.reload();
  }

  readonly address = computed(() => {
    const address = this.machine()?.address;
    return address
      ? `${address.streetNumber} ${address.streetName}, ${address.postalCode} ${address.city}`
      : null;
  });
}
```

Replace `frontend/src/app/features/machines/machine-detail/machine-detail.html` with:

```html
<div class="mb-4 flex items-center justify-between">
  <h1 class="text-xl font-semibold">Vending machine details</h1>
  <a mat-button routerLink="/machines">Back to list</a>
</div>

@if (isLoading()) {
  <mat-progress-bar mode="indeterminate" />
}

@if (hasError()) {
  <p class="text-red-600" role="alert">The vending machine could not be loaded.</p>
} @else if (machine(); as machine) {
  <mat-card>
    <mat-card-content>
      <dl class="grid grid-cols-2 gap-x-4 gap-y-2">
        <dt class="font-medium">Serial number</dt>
        <dd>{{ machine.serialNumber | valueOrEmpty }}</dd>

        <dt class="font-medium">Address</dt>
        <dd>{{ address() | valueOrEmpty }}</dd>

        <dt class="font-medium">Item type</dt>
        <dd>{{ machine.itemType | valueOrEmpty }}</dd>

        <dt class="font-medium">Working status</dt>
        <dd>{{ machine.workingStatus | valueOrEmpty }}</dd>

        <dt class="font-medium">Power status</dt>
        <dd>{{ machine.powerStatus | valueOrEmpty }}</dd>

        <dt class="font-medium">Temperature</dt>
        <dd>{{ machine.temperature | valueOrEmpty }}</dd>

        <dt class="font-medium">RFID status</dt>
        <dd>{{ machine.rfidStatus | valueOrEmpty }}</dd>

        <dt class="font-medium">Smart card status</dt>
        <dd>{{ machine.smartCardStatus | valueOrEmpty }}</dd>

        <dt class="font-medium">Change money status</dt>
        <dd>{{ machine.changeMoneyStatus | valueOrEmpty }}</dd>

        <dt class="font-medium">Last intervention</dt>
        <dd>{{ machine.lastIntervention | date: 'short' | valueOrEmpty }}</dd>
      </dl>
    </mat-card-content>
  </mat-card>

  <h2 class="mt-6 mb-2 text-lg font-semibold">Stock</h2>

  @if (stockLoading()) {
    <mat-progress-bar mode="indeterminate" />
  }

  @if (stockError()) {
    <p class="text-red-600" role="alert">The stock could not be loaded.</p>
  } @else if (itemQuantities().length === 0) {
    <p>No stock for this vending machine.</p>
  } @else {
    <mat-card>
      <mat-card-content>
        <table class="w-full text-left">
          <thead>
            <tr>
              <th class="font-medium">Item</th>
              <th class="font-medium">Quantity</th>
              <th class="font-medium">Actions</th>
            </tr>
          </thead>
          <tbody>
            @for (itemQuantity of itemQuantities(); track itemQuantity.itemId) {
              <tr>
                <td>
                  {{ itemQuantity.itemName }}
                  @if (isAdmin() && itemLink(itemQuantity.itemId); as href) {
                    <a [href]="href" target="_blank" rel="noopener" aria-label="View item">
                      <mat-icon inline class="align-middle text-base">open_in_new</mat-icon>
                    </a>
                  }
                </td>
                <td>{{ itemQuantity.quantity }}</td>
                <td>
                  @if (canOrder(itemQuantity.itemId)) {
                    <button
                      mat-button
                      type="button"
                      [disabled]="ordering() === itemQuantity.itemId"
                      (click)="orderItem(itemQuantity)"
                    >
                      @if (ordering() === itemQuantity.itemId) {
                        <mat-progress-spinner
                          diameter="16"
                          mode="indeterminate"
                          class="mr-2 inline-block align-middle"
                        />
                      }
                      <span>Order</span>
                    </button>
                  }
                </td>
              </tr>
            }
          </tbody>
        </table>
      </mat-card-content>
    </mat-card>
  }

  @if (isAdmin()) {
    <app-machine-admin-panel
      [machine]="machine"
      [stock]="stock()"
      (machineChanged)="onMachineChanged()"
      (stockChanged)="onStockChanged()"
    />
  }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run:

```bash
cd frontend && npm test -- --include src/app/features/machines/machine-detail/machine-detail.spec.ts
```

Expected: PASS. If the admin panel triggers the API root/items request in `MachineDetail` tests, flush `/api/v1` and `/api/v1/items?page=1&size=100` in the new admin-only tests before `whenStable()`.

- [ ] **Step 5: Commit**

Run:

```bash
build-brief ./gradlew clean build
git add frontend/src/app/features/machines/machine-detail/machine-detail.ts frontend/src/app/features/machines/machine-detail/machine-detail.html frontend/src/app/features/machines/machine-detail/machine-detail.spec.ts
git commit -m "feat(frontend): wire machine admin panel into detail page

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

### Task 6: Verrouiller les helpers HAL si le prérequis existe sans test local

**Files:**
- Modify: `frontend/src/app/shared/models/hal.ts` only if `linkHref` is missing despite Task 0 being resolved on the branch.
- Modify: `frontend/src/app/features/machines/vending-machine-api.spec.ts` if no existing `linkHref` spec exists.

**Interfaces:**
- Consumes: `HalResource`, `HalLink`.
- Produces: `linkHref(resource, rel)` returns the first href for array rels, the href for scalar rels, and `undefined` for missing resources/rels.

- [ ] **Step 1: Write or verify the pinning tests**

If `linkHref` already has tests from `docs/plans/00-frontend-prerequis-partages.md`, do not duplicate them. Otherwise append to `frontend/src/app/features/machines/vending-machine-api.spec.ts`:

```ts
import { linkHref } from '../../shared/models/hal';

describe('linkHref', () => {
  it('returns undefined for missing resources and missing rels', () => {
    expect(linkHref(undefined, 'stock')).toBeUndefined();
    expect(linkHref(null, 'stock')).toBeUndefined();
    expect(linkHref({ _links: {} }, 'stock')).toBeUndefined();
  });

  it('returns the href for a scalar link relation', () => {
    expect(linkHref({ _links: { stock: { href: '/stock' } } }, 'stock')).toBe('/stock');
  });

  it('returns the first href for an array link relation', () => {
    expect(
      linkHref({ _links: { item: [{ href: '/items/1' }, { href: '/items/2' }] } }, 'item'),
    ).toBe('/items/1');
  });
});
```

- [ ] **Step 2: Run test to verify it fails only when the helper is absent**

Run:

```bash
cd frontend && npm test -- --include src/app/features/machines/vending-machine-api.spec.ts
```

Expected: PASS if the prerequisite exists; otherwise FAIL because `linkHref` is not exported.

- [ ] **Step 3: Implement only when missing**

If missing, add this exact export at the end of `frontend/src/app/shared/models/hal.ts`:

```ts
export function linkHref(resource: HalResource | null | undefined, rel: string): string | undefined {
  const link = resource?._links?.[rel];
  return Array.isArray(link) ? link[0]?.href : link?.href;
}
```

- [ ] **Step 4: Run test to verify it passes**

Run:

```bash
cd frontend && npm test -- --include src/app/features/machines/vending-machine-api.spec.ts
```

Expected: PASS.

- [ ] **Step 5: Commit if this task changed files**

Run only if files changed:

```bash
build-brief ./gradlew clean build
git add frontend/src/app/shared/models/hal.ts frontend/src/app/features/machines/vending-machine-api.spec.ts
git commit -m "feat(frontend): pin HAL link resolution

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

### Task 7: E2E admin complet pour provisioning, reset et rapport

**Files:**
- Create: `frontend/e2e/manage-machine-admin.spec.ts`

**Interfaces:**
- Consumes:
  - Login route `/login`
  - Admin fake JWT `roles: ['ROLE_ADMIN']`
  - `AuthService` behavior that skips `/me` for admins
  - Root `/api/v1` links `vendingMachines` and `items`
- Produces: Playwright scenario that proves the admin can provision stock, reset an abnormal machine, and open a report dialog.

- [ ] **Step 1: Write the failing e2e test**

Create `frontend/e2e/manage-machine-admin.spec.ts`:

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
const snackId = '22222222-2222-2222-2222-222222222222';
const drinkId = '33333333-3333-3333-3333-333333333333';
const machinePath = `/api/v1/vending-machines/${machineId}`;
const stockPath = `${machinePath}/stock`;
const resetPath = `${machinePath}/reset`;
const stockReportPath = `${stockPath}/report`;
const statusReportPath = `${machinePath}/status/report`;
const ordersReportPath = `${machinePath}/orders/report`;

const machinesPage = {
  _embedded: {
    elements: [
      {
        id: machineId,
        serialNumber: 'SN-0001',
        address: { city: 'Lyon', streetName: 'Rue de la Paix', postalCode: '69001' },
        itemType: 'SNACK',
        powerStatus: 'POWER_ON',
        workingStatus: 'ERROR',
        rfidStatus: 'OK',
        smartCardStatus: 'OK',
        changeMoneyStatus: 'NORMAL',
        _links: { self: { href: machinePath } },
      },
    ],
  },
  page: { size: 10, totalElements: 1, totalPages: 1, number: 0 },
};

function machineDetail(workingStatus: 'WORKING' | 'ERROR') {
  return {
    id: machineId,
    serialNumber: 'SN-0001',
    address: { streetNumber: 1, streetName: 'Rue de la Paix', postalCode: '69001', city: 'Lyon' },
    itemType: 'SNACK',
    powerStatus: 'POWER_ON',
    workingStatus,
    temperature: 4,
    lastIntervention: null,
    rfidStatus: 'OK',
    smartCardStatus: 'OK',
    changeMoneyStatus: 'NORMAL',
    _links: {
      stock: { href: stockPath },
      'status:reset': { href: resetPath },
      'status:report': { href: statusReportPath },
      'orders:report': { href: ordersReportPath },
    },
  };
}

function stockResponse(quantity: number) {
  return {
    itemQuantities: [{ itemId: snackId, itemName: 'Chips', quantity }],
    _links: {
      self: { href: stockPath },
      vendingMachine: { href: machinePath },
      'stock:provision': { href: stockPath },
      'stock:report': { href: stockReportPath },
      item: [{ href: `/api/v1/items/${snackId}` }],
      order: [],
    },
  };
}

const itemsPage = {
  _embedded: {
    elements: [
      { id: snackId, name: 'Chips', type: 'SNACK', price: 2, _links: {} },
      { id: drinkId, name: 'Cola', type: 'COLD_BEVERAGE', price: 1.5, _links: {} },
    ],
  },
  page: { size: 100, totalElements: 2, totalPages: 1, number: 0 },
};

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

async function signInAdmin(page: Page) {
  await page.goto('/login');

  await page.getByLabel('Email').fill('admin@vending.me');
  await page.getByLabel('Password').fill('S3cret!Passw0rd');
  await page.getByRole('button', { name: 'Sign in' }).click();

  await expect(page).toHaveURL(/\/machines$/);
  await expect(page.getByText('admin@vending.me')).toBeVisible();
  await expect(page.getByText('SN-0001')).toBeVisible();
}

async function goToMachineDetail(page: Page) {
  await page.getByRole('link', { name: 'View details' }).click();
  await expect(page).toHaveURL(new RegExp(`/machines/${machineId}$`));
}

test.beforeEach(async ({ page }) => {
  await page.route('**/api/v1', async (route) => {
    await fulfillJson(route, {
      _links: {
        vendingMachines: { href: '/api/v1/vending-machines' },
        items: { href: '/api/v1/items' },
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

  await page.route('**/api/v1/me', async () => {
    throw new Error('Admin login must not call /me');
  });

  await page.route('**/api/v1/authenticate/logout', async (route) => {
    await route.fulfill({ status: 204 });
  });
});

test('an admin can provision stock, reset status, and open machine reports', async ({ page }) => {
  let stockRequestCount = 0;
  let provisionPostCount = 0;
  let resetPostCount = 0;

  await page.route('**/api/v1/items**', async (route) => {
    await fulfillJson(route, itemsPage);
  });

  await page.route('**/api/v1/vending-machines**', async (route) => {
    const request = route.request();
    const pathname = new URL(request.url()).pathname;

    if (request.method() === 'GET' && pathname === '/api/v1/vending-machines') {
      await fulfillJson(route, machinesPage);
      return;
    }

    if (request.method() === 'GET' && pathname === machinePath) {
      await fulfillJson(route, machineDetail(resetPostCount === 0 ? 'ERROR' : 'WORKING'));
      return;
    }

    if (request.method() === 'GET' && pathname === stockPath) {
      stockRequestCount += 1;
      await fulfillJson(route, stockResponse(stockRequestCount === 1 ? 3 : 10));
      return;
    }

    if (request.method() === 'POST' && pathname === stockPath) {
      provisionPostCount += 1;
      expect(request.postDataJSON()).toEqual({ itemId: snackId, quantity: 7 });
      await fulfillJson(route, stockResponse(10));
      return;
    }

    if (request.method() === 'POST' && pathname === resetPath) {
      resetPostCount += 1;
      await fulfillJson(route, machineDetail('WORKING'));
      return;
    }

    if (request.method() === 'POST' && pathname === stockReportPath) {
      await fulfillJson(
        route,
        {
          id: 'report-1',
          serialNumber: 'SN-0001',
          stockEntries: [{ itemName: 'Chips', quantity: 10 }],
        },
        'application/json',
      );
      return;
    }

    if (request.method() === 'POST' && pathname === statusReportPath) {
      await fulfillJson(route, {
        serialNumber: 'SN-0001',
        lastIntervention: null,
        temperature: 4,
        powerStatus: 'POWER_ON',
        workingStatus: 'WORKING',
        rfidStatus: 'OK',
        smartCardStatus: 'OK',
        changeMoneyStatus: 'NORMAL',
        _links: { vendingMachine: { href: machinePath } },
      });
      return;
    }

    if (request.method() === 'POST' && pathname === ordersReportPath) {
      await fulfillJson(
        route,
        {
          serialNumber: 'SN-0001',
          clientOrders: [{ itemName: 'Chips', itemPrice: 2, orderedAt: '2026-09-25T10:00:00' }],
          totalAmount: 2,
          reportedAt: '2026-09-25T11:00:00',
        },
        'application/json',
      );
      return;
    }

    throw new Error(`Unexpected vending machines request: ${request.method()} ${pathname}`);
  });

  await signInAdmin(page);
  await goToMachineDetail(page);

  await expect(page.getByRole('heading', { name: 'Administration' })).toBeVisible();
  await expect(page.getByText('Cola')).toBeHidden();

  await page.getByLabel('Item').click();
  await page.getByRole('option', { name: 'Chips' }).click();
  await page.getByTestId('machine-admin-provision-quantity').fill('7');
  await page.getByTestId('machine-admin-provision-submit').click();

  await expect(page.getByText('Stock provisioned.')).toBeVisible();
  await expect(page.locator('tbody tr').filter({ hasText: 'Chips' })).toContainText('10');
  expect(provisionPostCount).toBe(1);

  await expect(page.getByTestId('machine-admin-reset')).toBeVisible();
  await page.getByTestId('machine-admin-reset').click();
  await expect(page.getByText('Machine status reset.')).toBeVisible();
  await expect(page.getByTestId('machine-admin-reset')).toBeHidden();
  expect(resetPostCount).toBe(1);

  await page.getByTestId('machine-admin-stock-report').click();
  await expect(page.getByRole('heading', { name: 'Stock report' })).toBeVisible();
  await expect(page.getByText('Chips')).toBeVisible();
  await page.getByTestId('machine-report-dialog-close').click();

  await page.getByTestId('machine-admin-status-report').click();
  await expect(page.getByRole('heading', { name: 'Status report' })).toBeVisible();
  await expect(page.getByText('POWER_ON')).toBeVisible();
  await page.getByTestId('machine-report-dialog-close').click();

  await page.getByTestId('machine-admin-orders-report').click();
  await expect(page.getByRole('heading', { name: 'Client orders report' })).toBeVisible();
  await expect(page.getByText('Total: 2 €')).toBeVisible();
  await page.getByTestId('machine-report-dialog-close').click();
});
```

- [ ] **Step 2: Run e2e to verify it fails before implementation is fully wired**

Run:

```bash
cd frontend && npm run e2e -- e2e/manage-machine-admin.spec.ts
```

Expected: FAIL until Tasks 1-5 are complete; after those tasks, failures should be limited to selector/copy mismatches that must be fixed in the implementation, not by weakening the test.

- [ ] **Step 3: Run e2e to verify it passes**

Run:

```bash
cd frontend && npm run e2e -- e2e/manage-machine-admin.spec.ts
```

Expected: PASS.

- [ ] **Step 4: Commit**

Run:

```bash
build-brief ./gradlew clean build
git add frontend/e2e/manage-machine-admin.spec.ts
git commit -m "feat(frontend): cover machine admin flow with e2e

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

### Task 8: Documentation frontend et backlog

**Files:**
- Modify: `frontend/README.md`
- Modify: `docs/features-front-a-implementer.md`

**Interfaces:**
- Consumes: changements déjà présents dans ces deux fichiers sur `main`; éditer sur `feat/frontend-machine-admin` sans les supprimer.
- Produces: documentation qui indique que le back-office machine est implémenté côté frontend et que l'item selector filtre `item.type === machine.itemType` pour éviter le 400 prévisible `UnsupportedItemToProvision`.

- [ ] **Step 1: Inspecter les modifications existantes avant édition**

Run:

```bash
git --no-pager diff -- frontend/README.md docs/features-front-a-implementer.md
```

Expected: voir les changements non liés. Les conserver.

- [ ] **Step 2: Update `frontend/README.md`**

Add this section near the machine feature documentation, without deleting existing unrelated text:

```md
### Machine administration

Admins can manage a vending machine from its detail page:

- provision stock through the `stock:provision` affordance;
- reset machine status only when the loaded status is abnormal;
- generate stock, status, and client orders reports from the HAL links returned by the backend.

The item selector loads one page of items from the API root `items` link and filters client-side to
`item.type === machine.itemType`. This keeps the UI aligned with the backend rule that rejects
unsupported item types with `UnsupportedItemToProvision`.
```

- [ ] **Step 3: Update `docs/features-front-a-implementer.md`**

Move or mark the stock/status/report machine feature as implemented. Use this wording in the relevant backlog area:

```md
- [x] Back-office machine: stock provisioning, abnormal-status reset, and stock/status/client-orders report generation are implemented in `MachineDetail` through `MachineAdminPanel`.
```

If the file uses a table instead of checkboxes, use:

```md
| Back-office machine | Done | Stock provisioning, abnormal-status reset, and stock/status/client-orders report generation are implemented in `MachineDetail` through `MachineAdminPanel`. |
```

- [ ] **Step 4: Run documentation-adjacent checks**

Run:

```bash
cd frontend && npm test -- --include src/app/features/machines/machine-detail/machine-detail.spec.ts --include src/app/features/machines/machine-admin-panel/machine-admin-panel.spec.ts
```

Expected: PASS.

- [ ] **Step 5: Commit**

Run:

```bash
build-brief ./gradlew clean build
git add frontend/README.md docs/features-front-a-implementer.md
git commit -m "feat(frontend): document machine admin frontend

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

### Task 9: Validation finale et self-review de branche

**Files:**
- Verify only: all files changed by Tasks 1-8.

**Interfaces:**
- Consumes: complete feature branch.
- Produces: branch ready for ff-only integration, with no backend source changes.

- [ ] **Step 1: Run all frontend unit tests**

Run:

```bash
cd frontend && npm test
```

Expected: PASS.

- [ ] **Step 2: Run frontend e2e**

Run:

```bash
cd frontend && npm run e2e
```

Expected: PASS.

- [ ] **Step 3: Run aggregate build**

Run:

```bash
build-brief ./gradlew clean build
```

Expected: PASS. Preserve the raw log path printed by `build-brief` if it fails.

- [ ] **Step 4: Verify no backend files changed**

Run:

```bash
git --no-pager diff --name-only main...HEAD | grep '^backend/' && exit 1 || true
```

Expected: no output and exit 0.

- [ ] **Step 5: Run plan self-review against the spec**

Check these points manually and fix inline before final commit if any fail:

```text
Spec coverage:
- Admin section appears only for ROLE_ADMIN.
- Provisioning follows stock:provision and sends { itemId, quantity }.
- Item selector filters to machine.itemType.
- UnsupportedItemToProvision {message} is surfaced in a snackbar.
- Reset button appears only when isAbnormalStatus(machine) and status:reset link exist.
- Reset emits parent reload for the machine.
- Stock provisioning emits parent reload for stock.
- Stock/status/orders reports POST their rels and open MachineReportDialog.
- MachineReportDialog uses @switch(data.kind).
- E2E uses admin fake JWT and no /me call.

Placeholder scan:
- No planning markers, deferred-work wording, copy-by-reference instructions, vague test instructions, or undefined refs in changed code.

Type consistency:
- MachineReportDialogData kinds match every call site.
- Report DTO field names match backend DTOs.
- output names are machineChanged and stockChanged.
```

- [ ] **Step 6: Final commit if self-review changed files**

Run only if Step 5 caused edits:

```bash
build-brief ./gradlew clean build
git add frontend/src/app/features/machines frontend/e2e/manage-machine-admin.spec.ts frontend/README.md docs/features-front-a-implementer.md
git commit -m "feat(frontend): finalize machine admin flow

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

- [ ] **Step 7: Prepare ff-only merge**

Run:

```bash
git switch main
git merge --ff-only feat/frontend-machine-admin
```

Expected: fast-forward merge. If `develop` exists by then and is the agreed integration branch, switch to `develop` and use `git merge --ff-only feat/frontend-machine-admin` there instead.
