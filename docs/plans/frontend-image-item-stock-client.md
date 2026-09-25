# Image d'item dans la vue stock client — Plan d'implémentation

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** afficher une vignette publique d'item dans chaque ligne de stock de `MachineDetail`, avec repli visuel stable quand l'image est absente ou échoue.
**Architecture:** la vue `features/machines/machine-detail` consomme uniquement le helper pur `itemImageUrl(id: string): string` fourni par le plan 00, sans requête `HttpClient` supplémentaire. L'état local des images en échec est un `signal<ReadonlySet<string>>` indexé par `itemId`; il persiste pendant toute la durée de vie du composant pour éviter une boucle de retries sur les 404 après reload du stock.
**Tech Stack:** Angular 22 standalone zoneless, Signals, `httpResource()`, Angular Material, Tailwind CSS, Vitest via `npm test`, Playwright via `npm run e2e`.
**Spec:** `docs/specs/frontend-image-item-stock-client.md`
**Prérequis:** `docs/plans/00-frontend-prerequis-partages.md` exécuté (fournit `itemImageUrl`).

## Global Constraints

- Nommage Angular 2025 : fichiers comme `machine-detail.ts`, jamais `machine-detail.component.ts`.
- Tests unitaires uniquement avec `cd frontend && npm test` ; jamais `npx vitest run`.
- E2E avec `cd frontend && npm run e2e`.
- Gradle uniquement avec `build-brief ./gradlew ...`.
- Branche `feat/frontend-item-image-stock` depuis `develop` ; `develop` n'existe pas encore au 2026-09-25, seulement `main`, donc créer la branche depuis `main`.
- Commit style `feat(frontend): ...` avec trailer `Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>`.
- `build-brief ./gradlew clean build` passe avant chaque commit.
- Merge en fast-forward uniquement.
- Aucun changement backend.
- Ne jamais utiliser `innerHTML` ni `bypassSecurityTrust*`.

## Review Focus

- Même item dont l'image échoue puis reload du stock après commande : le fallback reste indexé par `itemId` et évite un retry réseau ; test ajouté dans Task 1.
- Plusieurs items dont un seul échoue : seul l'item concerné bascule sur l'icône, les autres gardent leur `<img>` ; test ajouté dans Task 1.
- Image pendant une consultation anonyme : l'image passe par un `<img src>` public, pas par `HttpClient`, donc aucun en-tête `Authorization` ni requête `HttpTestingController` ; test ajouté dans Task 1.
- Accessibilité : `alt` reprend exactement `itemQuantity.itemName`, l'icône de fallback est décorative avec `aria-hidden="true"` ; test ajouté dans Task 1.
- Images très grandes ou non carrées : la vignette reste contrainte à `32x32` avec `h-8 w-8 object-cover` ; test ajouté dans Task 1.

---

## File Structure

- `frontend/src/app/features/items/item-api.ts` — prérequis seulement : doit exporter `itemImageUrl(id: string): string`; ce plan ne le réimplémente pas.
- `frontend/src/app/features/machines/machine-detail/machine-detail.ts` — expose l'URL d'image au template et conserve les `itemId` dont l'image a échoué.
- `frontend/src/app/features/machines/machine-detail/machine-detail.html` — rend la vignette ou l'icône de fallback dans la cellule `Item`.
- `frontend/src/app/features/machines/machine-detail/machine-detail.spec.ts` — verrouille URL, accessibilité, fallback, isolation multi-items et persistance après reload.
- `frontend/e2e/order-item.spec.ts` — extension Playwright facultative : vérifie un vrai chargement navigateur 404 et PNG.
- `docs/features-front-a-implementer.md` — documentation backlog à mettre à jour sur la branche de feature, en conservant les changements non liés déjà présents dans le worktree.
- `frontend/README.md` — documentation du périmètre frontend à mettre à jour sur la branche de feature.

## Tasks

### Task 0: Vérifier les prérequis partagés

**Files:**
- Read: `docs/plans/00-frontend-prerequis-partages.md`
- Read: `frontend/src/app/features/items/item-api.ts`

**Interfaces:**
- Consumes: `export function itemImageUrl(id: string): string`
- Produces: aucune modification ; feu vert ou blocage explicite avant Task 1.

- [ ] **Step 1: Se placer sur la branche de feature**

Run:

```bash
git --no-pager status --short
git switch -c feat/frontend-item-image-stock main
```

Expected: la branche est créée depuis `main`, car `develop` n'existe pas au 2026-09-25. Si la branche existe déjà, utiliser `git switch feat/frontend-item-image-stock`. Les changements non liés déjà présents dans `docs/features-front-a-implementer.md` et `frontend/README.md` restent dans le worktree ; ne pas les réinitialiser.

- [ ] **Step 2: Vérifier que le plan prérequis existe**

Run:

```bash
test -f docs/plans/00-frontend-prerequis-partages.md
```

Expected: PASS. Si le fichier est absent, arrêter l'exécution de ce plan et exécuter d'abord le plan de prérequis partagé qui fournit `itemImageUrl`.

- [ ] **Step 3: Vérifier que le helper requis est exporté**

Run:

```bash
grep -n "export function itemImageUrl" frontend/src/app/features/items/item-api.ts
```

Expected: PASS avec une ligne ressemblant à :

```ts
export function itemImageUrl(id: string): string {
```

- [ ] **Step 4: Vérifier la signature et le contrat d'URL**

Confirmer que le helper présent dans `frontend/src/app/features/items/item-api.ts` a exactement cette signature et ce comportement :

```ts
export function itemImageUrl(id: string): string {
  return `${environment.apiBaseUrl}/items/${id}/image`;
}
```

Expected: le helper retourne `/api/v1/items/<id>/image` via `environment.apiBaseUrl`. Ne pas ajouter ni modifier ce helper dans ce plan ; c'est fourni par le prérequis.

- [ ] **Step 5: Commit**

Aucun commit pour Task 0 : cette tâche ne modifie aucun fichier.

### Task 1: Afficher la vignette d'item et son fallback dans `MachineDetail`

**Files:**
- Modify: `frontend/src/app/features/machines/machine-detail/machine-detail.ts`
- Modify: `frontend/src/app/features/machines/machine-detail/machine-detail.html`
- Test: `frontend/src/app/features/machines/machine-detail/machine-detail.spec.ts`

**Interfaces:**
- Consumes: `itemImageUrl(id: string): string` from `frontend/src/app/features/items/item-api.ts`
- Produces:
  - `itemImageHref(itemId: string): string`
  - `readonly failedImages: WritableSignal<ReadonlySet<string>>` inferred from `signal<ReadonlySet<string>>(new Set())`
  - `onItemImageError(itemId: string): void`

- [ ] **Step 1: Écrire les tests unitaires en échec**

Dans `frontend/src/app/features/machines/machine-detail/machine-detail.spec.ts`, modifier le helper `flushMachineAndStock` pour accepter plusieurs items :

```ts
  function flushMachineAndStock(
    options: {
      stockLink?: string;
      stockOrderLinks?: Array<{ href: string }>;
      itemQuantities?: Array<{ itemId: string; itemName: string; quantity: number }>;
    } = {},
  ) {
    const stockLink = options.stockLink ?? 'https://api.example.test/vending-machines/m-1/stock';
    const itemQuantities = options.itemQuantities ?? [
      { itemId: 'i-1', itemName: 'Water', quantity: 3 },
    ];

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
      _links: { stock: { href: stockLink } },
    });
    harness.fixture.detectChanges();

    return Promise.resolve().then(() => {
      harness.fixture.detectChanges();
      backend.expectOne(stockLink).flush({
        itemQuantities,
        _links: options.stockOrderLinks ? { order: options.stockOrderLinks } : undefined,
      });
    });
  }
```

Ajouter ces tests dans le même `describe('MachineDetail', ...)`, après le test `hides the order button when the stock item has no order link` :

```ts
  it('renders a public item image with accessible text and fixed thumbnail styling', async () => {
    await navigate();
    await flushMachineAndStock();
    await harness.fixture.whenStable();

    const image = harness.fixture.nativeElement.querySelector(
      'img[data-testid="item-image"]',
    ) as HTMLImageElement | null;

    expect(image).not.toBeNull();
    expect(image!.getAttribute('src')).toBe('/api/v1/items/i-1/image');
    expect(image!.getAttribute('alt')).toBe('Water');
    expect(image!.getAttribute('width')).toBe('32');
    expect(image!.getAttribute('height')).toBe('32');
    expect(image!.getAttribute('loading')).toBe('lazy');
    expect(image!.className).toContain('h-8');
    expect(image!.className).toContain('w-8');
    expect(image!.className).toContain('object-cover');
    backend.expectNone('/api/v1/items/i-1/image');
  });

  it('replaces only the failed item image with a decorative fallback icon', async () => {
    await navigate();
    await flushMachineAndStock({
      itemQuantities: [
        { itemId: 'i-1', itemName: 'Water', quantity: 3 },
        { itemId: 'i-2', itemName: 'Juice', quantity: 5 },
      ],
    });
    await harness.fixture.whenStable();

    const images = Array.from(
      harness.fixture.nativeElement.querySelectorAll('img[data-testid="item-image"]'),
    ) as HTMLImageElement[];

    expect(images).toHaveLength(2);

    images[0].dispatchEvent(new Event('error'));
    harness.fixture.detectChanges();

    const fallbackIcons = Array.from(
      harness.fixture.nativeElement.querySelectorAll('[data-testid="item-image-fallback"]'),
    ) as HTMLElement[];
    const remainingImages = Array.from(
      harness.fixture.nativeElement.querySelectorAll('img[data-testid="item-image"]'),
    ) as HTMLImageElement[];

    expect(fallbackIcons).toHaveLength(1);
    expect(fallbackIcons[0].getAttribute('aria-hidden')).toBe('true');
    expect(fallbackIcons[0].textContent?.trim()).toBe('image_not_supported');
    expect(remainingImages).toHaveLength(1);
    expect(remainingImages[0].getAttribute('alt')).toBe('Juice');
  });

  it('keeps the image fallback for the same item after an order reloads stock', async () => {
    authenticate();
    dialog.open.mockReturnValue({ afterClosed: () => of(true) });

    await navigate();
    await flushMachineAndStock({
      stockOrderLinks: [{ href: 'https://api.example.test/vending-machines/m-1/items/i-1/order' }],
    });
    await harness.fixture.whenStable();

    const image = harness.fixture.nativeElement.querySelector(
      'img[data-testid="item-image"]',
    ) as HTMLImageElement;
    image.dispatchEvent(new Event('error'));
    harness.fixture.detectChanges();

    expect(
      harness.fixture.nativeElement.querySelector('[data-testid="item-image-fallback"]'),
    ).not.toBeNull();

    queryOrderButton()!.click();

    backend
      .expectOne('https://api.example.test/vending-machines/m-1/items/i-1/order')
      .flush({ amount: 1.5, createdAt: '2026-09-24T12:00:00Z', _links: {} });
    harness.fixture.detectChanges();
    await Promise.resolve();
    harness.fixture.detectChanges();

    backend.expectOne('https://api.example.test/vending-machines/m-1/stock').flush({
      itemQuantities: [{ itemId: 'i-1', itemName: 'Water', quantity: 2 }],
      _links: {
        order: [{ href: 'https://api.example.test/vending-machines/m-1/items/i-1/order' }],
      },
    });
    await harness.fixture.whenStable();

    expect(
      harness.fixture.nativeElement.querySelector('[data-testid="item-image-fallback"]'),
    ).not.toBeNull();
    expect(harness.fixture.nativeElement.querySelector('img[data-testid="item-image"]')).toBeNull();
    expect(harness.fixture.nativeElement.textContent).toContain('2');
  });
```

Ces tests explicitent que jsdom ne charge pas réellement les images : aucune requête image ne passe par `HttpClient`, donc `backend.verify()` reste vert et aucun en-tête `Authorization` Angular n'est impliqué.

- [ ] **Step 2: Lancer le test ciblé et vérifier l'échec**

Run:

```bash
cd frontend && npm test -- --include src/app/features/machines/machine-detail/machine-detail.spec.ts
```

Expected: FAIL. Les erreurs attendues mentionnent que `itemImageHref` ou `onItemImageError` n'existent pas encore, ou que `img[data-testid="item-image"]` est introuvable.

- [ ] **Step 3: Implémenter le minimum dans `machine-detail.ts`**

Dans `frontend/src/app/features/machines/machine-detail/machine-detail.ts`, ajouter l'import :

```ts
import { AuthService } from '../../../core/auth/auth';
import { ValueOrEmptyPipe } from '../../../shared/value-or-empty-pipe';
import { itemImageUrl } from '../../items/item-api';
import { ClientOrder } from '../models/client-order';
```

Ajouter les membres suivants dans la classe `MachineDetail`, juste après `readonly ordering = signal<string | null>(null);` :

```ts
  readonly failedImages = signal<ReadonlySet<string>>(new Set());

  itemImageHref(itemId: string): string {
    return itemImageUrl(itemId);
  }

  onItemImageError(itemId: string): void {
    this.failedImages.update((failedImages) => new Set(failedImages).add(itemId));
  }
```

Décision à conserver en revue : l'état d'échec persiste pour toute la durée de vie du composant. Un reload du stock ne supprime donc pas l'entrée `itemId`, ce qui évite un retry loop sur une image durablement absente.

- [ ] **Step 4: Implémenter le minimum dans `machine-detail.html`**

Dans `frontend/src/app/features/machines/machine-detail/machine-detail.html`, remplacer la cellule item de la table par ce bloc, avec le contexte inclus pour placer l'édition sans ambiguïté :

```html
          <tbody>
            @for (itemQuantity of itemQuantities(); track itemQuantity.itemId) {
              <tr>
                <td>
                  @if (failedImages().has(itemQuantity.itemId)) {
                    <mat-icon
                      data-testid="item-image-fallback"
                      aria-hidden="true"
                      class="mr-2 align-middle"
                    >
                      image_not_supported
                    </mat-icon>
                  } @else {
                    <img
                      [src]="itemImageHref(itemQuantity.itemId)"
                      [alt]="itemQuantity.itemName"
                      width="32"
                      height="32"
                      class="mr-2 inline-block h-8 w-8 rounded object-cover align-middle"
                      data-testid="item-image"
                      loading="lazy"
                      (error)="onItemImageError(itemQuantity.itemId)"
                    />
                  }
                  {{ itemQuantity.itemName }}
                  @if (isAdmin() && itemLink(itemQuantity.itemId); as href) {
                    <a [href]="href" target="_blank" rel="noopener" aria-label="View item">
                      <mat-icon inline class="align-middle text-base">open_in_new</mat-icon>
                    </a>
                  }
                </td>
                <td>{{ itemQuantity.quantity }}</td>
```

- [ ] **Step 5: Lancer le test ciblé et vérifier le passage**

Run:

```bash
cd frontend && npm test -- --include src/app/features/machines/machine-detail/machine-detail.spec.ts
```

Expected: PASS.

- [ ] **Step 6: Commit**

Run:

```bash
build-brief ./gradlew clean build
git add frontend/src/app/features/machines/machine-detail/machine-detail.ts frontend/src/app/features/machines/machine-detail/machine-detail.html frontend/src/app/features/machines/machine-detail/machine-detail.spec.ts
git commit -m "feat(frontend): show item image in client stock

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

Expected: le build complet passe avant le commit.

### Task 2: Ajouter l'extension E2E facultative du chargement d'image navigateur

**Files:**
- Modify: `frontend/e2e/order-item.spec.ts`

**Interfaces:**
- Consumes: `data-testid="item-image"` and `data-testid="item-image-fallback"` from Task 1.
- Produces: couverture Playwright facultative des réponses image 404 et PNG.

- [ ] **Step 1: Écrire les tests E2E en échec**

Dans `frontend/e2e/order-item.spec.ts`, ajouter la constante PNG après `const itemName = 'Sparkling Water';` :

```ts
const tinyPngBase64 =
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==';
```

Ajouter ces tests après le test `a signed-in user can cancel an order without sending the POST request` :

```ts
test('shows the item image fallback when the public image endpoint returns 404', async ({ page }) => {
  await page.route('**/api/v1/items/*/image', async (route) => {
    await route.fulfill({ status: 404 });
  });

  await page.route('**/api/v1/vending-machines**', async (route) => {
    const request = route.request();
    const pathname = new URL(request.url()).pathname;

    if (request.method() === 'GET' && pathname === '/api/v1/vending-machines') {
      await fulfillJson(route, machinesPage);
      return;
    }

    if (request.method() === 'GET' && pathname === machinePath) {
      await fulfillJson(route, machineDetail);
      return;
    }

    if (request.method() === 'GET' && pathname === stockPath) {
      await fulfillJson(route, stockResponse(3));
      return;
    }

    throw new Error(`Unexpected vending machines request: ${request.method()} ${pathname}`);
  });

  await signIn(page);
  await goToMachineDetail(page);

  const itemRow = page.locator('tbody tr').filter({ hasText: itemName });
  await expect(itemRow.getByTestId('item-image-fallback')).toBeVisible();
  await expect(itemRow.getByTestId('item-image')).toBeHidden();
});

test('shows the public item image when the endpoint returns a PNG', async ({ page }) => {
  await page.route('**/api/v1/items/*/image', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'image/png',
      body: Buffer.from(tinyPngBase64, 'base64'),
    });
  });

  await page.route('**/api/v1/vending-machines**', async (route) => {
    const request = route.request();
    const pathname = new URL(request.url()).pathname;

    if (request.method() === 'GET' && pathname === '/api/v1/vending-machines') {
      await fulfillJson(route, machinesPage);
      return;
    }

    if (request.method() === 'GET' && pathname === machinePath) {
      await fulfillJson(route, machineDetail);
      return;
    }

    if (request.method() === 'GET' && pathname === stockPath) {
      await fulfillJson(route, stockResponse(3));
      return;
    }

    throw new Error(`Unexpected vending machines request: ${request.method()} ${pathname}`);
  });

  await signIn(page);
  await goToMachineDetail(page);

  const itemRow = page.locator('tbody tr').filter({ hasText: itemName });
  const image = itemRow.getByTestId('item-image');
  await expect(image).toBeVisible();
  await expect(image).toHaveAttribute('alt', itemName);
});
```

- [ ] **Step 2: Lancer l'E2E ciblé et vérifier l'échec avant Task 1**

Run:

```bash
cd frontend && npm run e2e -- e2e/order-item.spec.ts
```

Expected avant Task 1: FAIL car les `data-testid` image/fallback n'existent pas. Si Task 1 est déjà passée, ces tests peuvent déjà PASS ; dans ce cas, vérifier que les deux nouveaux tests apparaissent bien dans la sortie Playwright.

- [ ] **Step 3: Lancer l'E2E ciblé après Task 1**

Run:

```bash
cd frontend && npm run e2e -- e2e/order-item.spec.ts
```

Expected: PASS.

- [ ] **Step 4: Commit**

Run:

```bash
build-brief ./gradlew clean build
git add frontend/e2e/order-item.spec.ts
git commit -m "feat(frontend): cover stock item image loading in e2e

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

Expected: le build complet passe avant le commit. Cette tâche est facultative ; si elle est omise, documenter l'omission dans la PR et conserver Task 1 comme couverture obligatoire.

### Task 3: Mettre à jour la documentation frontend

**Files:**
- Modify: `docs/features-front-a-implementer.md`
- Modify: `frontend/README.md`

**Interfaces:**
- Consumes: comportement livré par Task 1.
- Produces: documentation alignée avec le backlog et le README frontend.

- [ ] **Step 1: Préserver le worktree et confirmer la branche de feature**

Run:

```bash
git --no-pager status --short
git --no-pager branch --show-current
```

Expected: la branche courante est `feat/frontend-item-image-stock`, créée en Task 0 depuis `main` car `develop` n'existe pas au 2026-09-25. `docs/features-front-a-implementer.md` et `frontend/README.md` ont des changements non liés hérités du worktree initial ; les éditer uniquement sur cette branche de feature et ne pas écraser ces modifications.

- [ ] **Step 2: Modifier `docs/features-front-a-implementer.md`**

Dans `## ✅ Déjà fait`, remplacer le bloc initial par :

```md
## ✅ Déjà fait

- Login, inscription (sans auto-login — l'utilisateur doit se connecter
  ensuite), liste des machines (lecture seule, paginée)
- **Détail d'une machine** (`GET /vending-machines/{id}`) — infos machine,
  **stock consulté, image publique de l'item affichée avec fallback, et
  commande d'un item** (`POST /vending-machines/{id}/order/{itemId}`) pour
  un utilisateur `ROLE_USER`
- **Création d'une machine** (`ROLE_ADMIN`, `POST /vending-machines`) —
  formulaire Signal Forms dédié (`machines/new`)
- **Liste des items** (`ROLE_ADMIN`, `GET /items`) — lecture seule, paginée
  (`items`), sans création/édition/suppression ni gestion admin d'image
```

Supprimer entièrement la section devenue vide :

```md
## 🌐 Public / client (sans authentification)

1. **Détail/visuel d'un item** (`GET /items/{itemId}/**`, image publique) —
   pas encore d'affichage d'image d'item côté frontend
```

Puis renuméroter les sections restantes ainsi :

```md
## 👤 Espace utilisateur connecté (`/api/v1/me/**`)

1. **Profil** : consulter/modifier ses infos (`GET`/`PUT /me`)
2. **Photo de profil** : afficher/uploader (`GET`/`POST /me/picture`)
3. **Changement de mot de passe** (`POST /me/password`)

## 🔐 Back-office admin (`ROLE_ADMIN`, tout le reste)

4. **Gestion des items** : création/édition/suppression + upload d'image
   (`/items`) — seule la liste en lecture seule existe
5. **Gestion des machines** : modification / suppression (la création est
   faite, il manque édition et suppression pour un CRUD complet)
6. **Gestion du stock d'une machine** : ajouter du stock, rapport de stock
   (`/vending-machines/{id}/stock`, `/stock/report`) — la consultation du
   stock existe déjà côté client (point commande), pas côté admin ni le
   rapport
7. **Statut machine** : reset (`/reset`), rapport de statut
   (`/status/report`)
8. **Rapport des commandes** par machine (`/orders/report`)
```

Remplacer `## Priorité suggérée` par :

```md
## Priorité suggérée

1. Espace profil (points 1-3)
2. Back-office admin (points 4-8, le plus gros lot)
```

- [ ] **Step 3: Modifier `frontend/README.md`**

Dans `## Périmètre actuel`, remplacer la ligne distributeurs par :

```md
- **Distributeurs** : liste paginée (table Material + paginator), création
  (admin), détail avec consultation du stock, image publique de l'item avec
  fallback, et commande d'un article (utilisateur connecté avec le rôle
  `ROLE_USER`).
```

Dans `**Reste à faire**`, remplacer la ligne articles par :

```md
- CRUD complet articles (création/édition/suppression) et upload/gestion
  admin des images d'article
```

- [ ] **Step 4: Vérifier que la documentation ne contient plus l'ancien backlog public**

Run:

```bash
grep -n "pas encore d'affichage d'image d'item côté frontend\\|Visuel item public\\|Public / client" docs/features-front-a-implementer.md frontend/README.md
```

Expected: FAIL avec aucun match, car ces formulations doivent avoir disparu.

- [ ] **Step 5: Valider la documentation avec le build complet**

Run:

```bash
build-brief ./gradlew clean build
```

Expected: PASS.

- [ ] **Step 6: Commit**

Run:

```bash
git add docs/features-front-a-implementer.md frontend/README.md
git commit -m "feat(frontend): document stock item images

Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

Expected: le commit ne contient que les changements documentaires de cette tâche et les changements non liés déjà présents ont été préservés, pas réécrits.

## Self-Review

- Spec coverage: le plan couvre le helper prérequis `itemImageUrl`, l'affichage `<img>`, le fallback Material, la persistance par `itemId`, les tests unitaires, l'extension E2E facultative et les mises à jour docs.
- Placeholder scan: aucun marqueur indéfini à compléter par l'implémenteur ; les commandes, chemins, signatures et snippets sont explicites.
- Type consistency: `itemImageHref(itemId: string): string`, `failedImages` inféré en `WritableSignal<ReadonlySet<string>>` via `signal<ReadonlySet<string>>(new Set())`, et `onItemImageError(itemId: string): void` sont utilisés de façon cohérente en TypeScript et template.
- Review Focus: les cinq modes d'entrée listés sont attachés à des assertions de Task 1 ; Task 2 ajoute une couverture navigateur optionnelle pour les réponses image réelles.
