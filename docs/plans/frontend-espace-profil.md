# Espace profil — Plan d'implémentation

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Ajouter une page `/profile` permettant à un utilisateur `ROLE_USER` connecté de consulter et modifier son profil, sa photo et son mot de passe.
**Architecture:** La route `/profile` est protégée par un `userGuard` et charge un composant parent `Profile` qui possède le `httpResource<User>`. Le parent transmet le `User` et les liens HAL à trois composants enfants focalisés (`ProfileInfo`, `ProfilePicture`, `ProfilePassword`) ; les enfants effectuent les mutations via `HttpClient` et émettent `saved`/`uploaded` pour déclencher `reload()`.
**Tech Stack:** Angular 22 standalone/zoneless, Signal Forms, `httpResource()`, `HttpClient`, Angular Material, Vitest via `npm test`, Playwright via `npm run e2e`, Spring HAL/HAL-FORMS côté API.
**Spec:** `docs/specs/frontend-espace-profil.md`
**Prérequis:** `docs/plans/00-frontend-prerequis-partages.md` exécuté (vérifier la présence des symboles listés plus bas avant de commencer).

## Global Constraints

- Nommage 2025 : `profile.ts`, pas `profile.component.ts`.
- Services : `@Service()`, pas `@Injectable({ providedIn: 'root' })`.
- Lectures : `httpResource()` ; mutations : `HttpClient`.
- Formulaires : Signal Forms `@angular/forms/signals`.
- Génération : `ng generate` via `npx ng generate component <path> --style=none` (existing components have no css).
- Tests unitaires ONLY via `cd frontend && npm test` (never npx vitest).
- Tests e2e via `npm run e2e`.
- Gradle ONLY via `build-brief ./gradlew ...`.
- Branche `feat/frontend-profile` créée depuis `develop` ; note : `develop` does not exist yet on 2026-09-25, only `main`; create from `main` in that case.
- Messages de commit style `feat(frontend): ...` avec trailer `Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>`.
- `build-brief ./gradlew clean build` must pass before each commit.
- Fusion fast-forward uniquement.
- Aucun changement backend.
- UI strings en anglais comme l'application existante : le lien toolbar est `"My profile"` et non « Mon profil ».

## Review Focus

1. `PUT /me` envoyé en JSON : Spring binderait `firstname`/`lastname` à `null` car `SelfServiceRestController.update(...)` n'a pas `@RequestBody` ; test d'ancrage dans Task 1 (`HttpParams` + valeurs).
2. Accès direct `/profile` par un anonyme ou un admin : l'anonyme doit aller vers `/login`, l'admin vers `/machines` ; tests d'ancrage dans Task 2.
3. Erreurs mot de passe asymétriques : `400` sans `errors` cible `oldPassword`, `400` avec `errors[field=password]` cible `newPassword` ; tests d'ancrage dans Task 5.
4. Liens HAL absents ou sous forme de tableau : aucune requête vers `undefined`, message explicite et `linkHref(...)` consommé ; tests d'ancrage dans Tasks 3, 4, 5 et vérification prérequis Task 0.
5. Photo absente/cache navigateur : absence de photo affiche l'avatar partagé, upload réussi ajoute `?v=<n>` sans recréer de composant d'upload ; tests d'ancrage dans Task 4.

---

## File Structure

- Create `frontend/src/app/features/profile/models/user-profile.ts` — contrats frontend propres au profil (`UserToUpdate`, `EditPasswordRequest`, rels HAL).
- Create `frontend/src/app/features/profile/profile-api.ts` — URLs et mutations de profil ; `PUT /me` en `HttpParams` form-urlencoded, upload délégué à `uploadImageFile`.
- Create `frontend/src/app/features/profile/profile-api.spec.ts` — verrouille les corps HTTP, dont le `HttpParams` critique.
- Create `frontend/src/app/core/auth/user-guard.ts` — garde `authenticated && !isAdmin`, anonyme vers `/login`, admin vers `/machines`.
- Create `frontend/src/app/core/auth/user-guard.spec.ts` — tests du garde.
- Create `frontend/src/app/features/profile/profile-info/profile-info.ts|html|spec.ts` — formulaire informations personnelles, email read-only, `PUT self`.
- Create `frontend/src/app/features/profile/profile-picture/profile-picture.ts|html|spec.ts` — section photo, réutilise `app-image-upload`, upload multipart, cache-busting.
- Create `frontend/src/app/features/profile/profile-password/profile-password.ts|html|spec.ts` — changement mot de passe, confirmation, logout + navigation `/login`.
- Create `frontend/src/app/features/profile/profile/profile.ts|html|spec.ts` — page parent `/profile`, `httpResource<User>`, liens HAL, orchestration des enfants.
- Modify `frontend/src/app/app.routes.ts` — route lazy `/profile` avec `userGuard`.
- Modify `frontend/src/app/app.html` — lien toolbar `"My profile"` visible pour `isAuthenticated() && !isAdmin()`.
- Modify `frontend/src/app/app.spec.ts` — visibilité du lien toolbar.
- Create `frontend/e2e/manage-profile.spec.ts` — parcours profil complet avec API mockée.
- Modify `frontend/README.md` — retirer le profil de « Reste à faire », ajouter au périmètre actuel.
- Modify `docs/features-front-a-implementer.md` — déplacer l'espace profil dans « Déjà fait » sur la branche feature. Ces deux fichiers ont actuellement des changements non liés sur `main`; les éditer uniquement sur `feat/frontend-profile` sans écraser ces changements.

Décision de découpage : garder un parent `Profile` et trois enfants. C'est légèrement plus de fichiers qu'un composant unique, mais chaque section a des règles d'erreur et de succès différentes ; les enfants évitent un gros fichier difficile à tester sans ajouter d'abstraction inutile.

## Tasks

### Task 0: Vérifier la branche et les prérequis partagés

**Files:**
- Create: aucun
- Modify: aucun
- Test: aucun

**Interfaces:**
- Consumes:
  - `linkHref(resource: HalResource | null | undefined, rel: string): string | undefined` depuis `frontend/src/app/shared/models/hal.ts`
  - `uploadImageFile<T = unknown>(http: HttpClient, href: string, file: File): Observable<T>` depuis `frontend/src/app/shared/http/upload-image-file.ts`
  - `ImageUpload` standalone component selector `app-image-upload` depuis `frontend/src/app/shared/image-upload/image-upload.ts`
  - `withCacheBuster(href: string, version: number): string` depuis `frontend/src/app/shared/http/cache-buster.ts`
- Produces: une branche prête pour les tâches suivantes, aucun code produit.

- [ ] **Step 1: Créer la branche de travail**

```bash
git --no-pager branch --list develop
git switch develop || git switch main
git switch -c feat/frontend-profile
```

Expected: la branche `feat/frontend-profile` existe. Si `develop` est absent le 2026-09-25, la branche part de `main`.

- [ ] **Step 2: Vérifier les prérequis partagés sans les réimplémenter**

```bash
grep -R "export function linkHref" frontend/src/app/shared/models/hal.ts \
  && grep -R "export function uploadImageFile" frontend/src/app/shared/http/upload-image-file.ts \
  && grep -R "selector: 'app-image-upload'" frontend/src/app/shared/image-upload/image-upload.ts \
  && grep -R "export function withCacheBuster" frontend/src/app/shared/http/cache-buster.ts
```

Expected: les quatre symboles sont trouvés. Si ce n'est pas le cas, arrêter et exécuter `docs/plans/00-frontend-prerequis-partages.md`; ne pas les recréer dans ce plan.

- [ ] **Step 3: Vérifier que le ciblage des tests unitaires est supporté**

Lire `frontend/package.json` et `frontend/angular.json` : `npm test` délègue à `ng test`, et le builder est `@angular/build:unit-test`. Ce builder supporte `--include <glob>`, donc les tâches utilisent :

```bash
cd frontend && npm test -- --include src/app/path/to/file.spec.ts
```

Expected: aucune commande lancée à ce stade, seulement une vérification de configuration.

### Task 1: Ajouter les contrats et mutations HTTP du profil

**Files:**
- Create: `frontend/src/app/features/profile/models/user-profile.ts`
- Create: `frontend/src/app/features/profile/profile-api.ts`
- Test: `frontend/src/app/features/profile/profile-api.spec.ts`

**Interfaces:**
- Consumes:
  - `User` depuis `frontend/src/app/core/auth/models/user.ts`
  - `uploadImageFile<T>(http: HttpClient, href: string, file: File): Observable<T>`
  - `environment.apiBaseUrl`
- Produces:
  - `export interface UserToUpdate { firstname: string; lastname: string; }`
  - `export interface EditPasswordRequest { oldPassword: string; newPassword: string; }`
  - `export const PROFILE_PASSWORD_REL = 'me:password'`
  - `export const PROFILE_PICTURE_REL = 'me:picture'`
  - `export function profileUrl(): string`
  - `export function updateProfile(http: HttpClient, href: string, payload: UserToUpdate): Observable<User>`
  - `export function updatePassword(http: HttpClient, href: string, payload: EditPasswordRequest): Observable<void>`
  - `export function uploadProfilePicture(http: HttpClient, href: string, file: File): Observable<User>`

- [ ] **Step 1: Write the failing test**

Create `frontend/src/app/features/profile/profile-api.spec.ts`:

```ts
import { HttpClient, HttpParams } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import {
  profileUrl,
  updatePassword,
  updateProfile,
  uploadProfilePicture,
} from './profile-api';

describe('profile-api', () => {
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

  it('builds the singleton profile URL from the API base URL', () => {
    expect(profileUrl()).toBe('/api/v1/me');
  });

  it('updates the profile with PUT form parameters because the backend update method has no @RequestBody', () => {
    updateProfile(http, '/api/v1/me', { firstname: 'Ada', lastname: 'Lovelace' }).subscribe();

    const request = backend.expectOne('/api/v1/me');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toBeInstanceOf(HttpParams);
    expect((request.request.body as HttpParams).get('firstname')).toBe('Ada');
    expect((request.request.body as HttpParams).get('lastname')).toBe('Lovelace');
    request.flush({
      id: 'u-1',
      email: 'ada@vending.me',
      firstname: 'Ada',
      lastname: 'Lovelace',
    });
  });

  it('updates the password with the JSON contract expected by POST /me/password', () => {
    updatePassword(http, '/api/v1/me/password', {
      oldPassword: 'OldPassw0rd!',
      newPassword: 'NewPassw0rd!',
    }).subscribe();

    const request = backend.expectOne('/api/v1/me/password');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      oldPassword: 'OldPassw0rd!',
      newPassword: 'NewPassw0rd!',
    });
    request.flush(null, { status: 204, statusText: 'No Content' });
  });

  it('uploads the profile picture as multipart FormData with field name file', () => {
    const file = new File(['avatar'], 'avatar.png', { type: 'image/png' });

    uploadProfilePicture(http, '/api/v1/me/picture', file).subscribe();

    const request = backend.expectOne('/api/v1/me/picture');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toBeInstanceOf(FormData);
    expect((request.request.body as FormData).get('file')).toBe(file);
    expect(request.request.headers.has('Content-Type')).toBe(false);
    request.flush({
      id: 'u-1',
      email: 'ada@vending.me',
      firstname: 'Ada',
      lastname: 'Lovelace',
    });
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
cd frontend && npm test -- --include src/app/features/profile/profile-api.spec.ts
```

Expected: FAIL with module resolution errors for `./profile-api`.

- [ ] **Step 3: Write minimal implementation**

Create `frontend/src/app/features/profile/models/user-profile.ts`:

```ts
export const PROFILE_PASSWORD_REL = 'me:password';
export const PROFILE_PICTURE_REL = 'me:picture';

export interface UserToUpdate {
  firstname: string;
  lastname: string;
}

export interface EditPasswordRequest {
  oldPassword: string;
  newPassword: string;
}
```

Create `frontend/src/app/features/profile/profile-api.ts`:

```ts
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User } from '../../core/auth/models/user';
import { uploadImageFile } from '../../shared/http/upload-image-file';
import { EditPasswordRequest, UserToUpdate } from './models/user-profile';

export function profileUrl(): string {
  return `${environment.apiBaseUrl}/me`;
}

export function updateProfile(
  http: HttpClient,
  href: string,
  payload: UserToUpdate,
): Observable<User> {
  const body = new HttpParams()
    .set('firstname', payload.firstname)
    .set('lastname', payload.lastname);

  return http.put<User>(href, body);
}

export function updatePassword(
  http: HttpClient,
  href: string,
  payload: EditPasswordRequest,
): Observable<void> {
  return http.post<void>(href, payload);
}

export function uploadProfilePicture(
  http: HttpClient,
  href: string,
  file: File,
): Observable<User> {
  return uploadImageFile<User>(http, href, file);
}
```

- [ ] **Step 4: Optional manual backend verification**

Si un backend local tourne et qu'un token utilisateur est disponible, vérifier le binding réel :

```bash
curl -i -X PUT \
  -H 'Authorization: Bearer <token>' \
  --data-urlencode firstname=Ada \
  --data-urlencode lastname=Lovelace \
  http://localhost:8080/api/v1/me
```

Expected: `200` et les champs `firstname`/`lastname` sont mis à jour. Note : ajouter `@RequestBody` au backend serait une amélioration séparée ; elle imposerait ensuite de repasser le frontend en JSON.

- [ ] **Step 5: Run test to verify it passes**

Run:

```bash
cd frontend && npm test -- --include src/app/features/profile/profile-api.spec.ts
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
build-brief ./gradlew clean build
git add frontend/src/app/features/profile/models/user-profile.ts frontend/src/app/features/profile/profile-api.ts frontend/src/app/features/profile/profile-api.spec.ts
git commit -m "feat(frontend): add profile API helpers" -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

### Task 2: Ajouter le garde utilisateur non-admin

**Files:**
- Create: `frontend/src/app/core/auth/user-guard.ts`
- Test: `frontend/src/app/core/auth/user-guard.spec.ts`

**Interfaces:**
- Consumes:
  - `AuthService.isAuthenticated(): boolean`
  - `AuthService.isAdmin(): boolean`
- Produces:
  - `export const userGuard: CanActivateFn`

- [ ] **Step 1: Write the failing test**

Create `frontend/src/app/core/auth/user-guard.spec.ts`:

```ts
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { CanActivateFn, provideRouter, UrlTree } from '@angular/router';
import { beforeEach, describe, expect, it } from 'vitest';
import { TokenStore } from './token-store';
import { userGuard } from './user-guard';

function fakeJwt(payload: Record<string, unknown>): string {
  const encode = (value: unknown) =>
    btoa(JSON.stringify(value)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${encode({ alg: 'none' })}.${encode(payload)}.signature`;
}

describe('userGuard', () => {
  const executeGuard: CanActivateFn = (...guardParameters) =>
    TestBed.runInInjectionContext(() => userGuard(...guardParameters));

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([
          { path: 'login', children: [] },
          { path: 'machines', children: [] },
        ]),
      ],
    });
  });

  it('redirects anonymous users to /login', () => {
    const result = executeGuard({} as never, { url: '/profile' } as never);

    expect(result).toBeInstanceOf(UrlTree);
    expect((result as UrlTree).toString()).toBe('/login');
  });

  it('redirects admins to /machines because admins have no /me profile', () => {
    TestBed.inject(TokenStore).setAccessToken(
      fakeJwt({ sub: 'admin@vending.me', roles: ['ROLE_ADMIN'], exp: 1 }),
    );

    const result = executeGuard({} as never, { url: '/profile' } as never);

    expect(result).toBeInstanceOf(UrlTree);
    expect((result as UrlTree).toString()).toBe('/machines');
  });

  it('allows an authenticated non-admin user', () => {
    TestBed.inject(TokenStore).setAccessToken(
      fakeJwt({ sub: 'user@vending.me', roles: ['ROLE_USER'], exp: 1 }),
    );

    const result = executeGuard({} as never, { url: '/profile' } as never);

    expect(result).toBe(true);
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
cd frontend && npm test -- --include src/app/core/auth/user-guard.spec.ts
```

Expected: FAIL with module resolution error for `./user-guard`.

- [ ] **Step 3: Write minimal implementation**

Create `frontend/src/app/core/auth/user-guard.ts`:

```ts
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth';

export const userGuard: CanActivateFn = () => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!auth.isAuthenticated()) {
    return router.parseUrl('/login');
  }

  return !auth.isAdmin() || router.parseUrl('/machines');
};
```

- [ ] **Step 4: Run test to verify it passes**

Run:

```bash
cd frontend && npm test -- --include src/app/core/auth/user-guard.spec.ts
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
build-brief ./gradlew clean build
git add frontend/src/app/core/auth/user-guard.ts frontend/src/app/core/auth/user-guard.spec.ts
git commit -m "feat(frontend): guard user profile routes" -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

### Task 3: Ajouter la section informations personnelles

**Files:**
- Create: `frontend/src/app/features/profile/profile-info/profile-info.ts`
- Create: `frontend/src/app/features/profile/profile-info/profile-info.html`
- Test: `frontend/src/app/features/profile/profile-info/profile-info.spec.ts`

**Interfaces:**
- Consumes:
  - `User`
  - `linkHref(resource, 'self')`
  - `updateProfile(http, href, payload): Observable<User>`
  - `parseValidationErrors(error, ['firstname', 'lastname'])`
- Produces:
  - `ProfileInfo` standalone component, selector `app-profile-info`
  - input `user = input.required<User>()`
  - output `saved = output<User>()`

- [ ] **Step 1: Scaffold with Angular CLI**

```bash
cd frontend && npx ng generate component features/profile/profile-info --style=none
```

Expected: Angular crée les fichiers du composant sans fichier CSS.

- [ ] **Step 2: Write the failing test**

Replace `frontend/src/app/features/profile/profile-info/profile-info.spec.ts` with:

```ts
import { HttpParams } from '@angular/common/http';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { User } from '../../../core/auth/models/user';
import { ProfileInfo } from './profile-info';

const user: User = {
  id: 'u-1',
  email: 'ada@vending.me',
  firstname: 'Ada',
  lastname: 'Lovelace',
  _links: { self: { href: '/api/v1/me' } },
};

describe('ProfileInfo', () => {
  let fixture: ComponentFixture<ProfileInfo>;
  let component: ProfileInfo;
  let backend: HttpTestingController;
  let snackBar: { open: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    snackBar = { open: vi.fn() };
    await TestBed.configureTestingModule({
      imports: [ProfileInfo],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: MatSnackBar, useValue: snackBar },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ProfileInfo);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('user', user);
    fixture.detectChanges();
    await fixture.whenStable();
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('shows email as read-only and initializes firstname and lastname', () => {
    const emailInput = fixture.nativeElement.querySelector('input[type="email"]') as HTMLInputElement;

    expect(emailInput.value).toBe('ada@vending.me');
    expect(emailInput.readOnly).toBe(true);
    expect(component.infoForm.firstname().value()).toBe('Ada');
    expect(component.infoForm.lastname().value()).toBe('Lovelace');
  });

  it('saves firstname and lastname with form-url-encoded HttpParams and emits the updated user', async () => {
    const emitted: User[] = [];
    component.saved.subscribe((value) => emitted.push(value));
    component.infoForm.firstname().value.set('Grace');
    component.infoForm.lastname().value.set('Hopper');

    component.submit();

    const request = backend.expectOne('/api/v1/me');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toBeInstanceOf(HttpParams);
    expect((request.request.body as HttpParams).get('firstname')).toBe('Grace');
    expect((request.request.body as HttpParams).get('lastname')).toBe('Hopper');
    request.flush({ ...user, firstname: 'Grace', lastname: 'Hopper' });
    await fixture.whenStable();

    expect(emitted[0].firstname).toBe('Grace');
    expect(snackBar.open).toHaveBeenCalledWith('Profile updated.', 'Close', { duration: 5000 });
    expect(component.submitting()).toBe(false);
  });

  it('does not send a request when the self link is missing', async () => {
    fixture.componentRef.setInput('user', { ...user, _links: {} });
    fixture.detectChanges();
    await fixture.whenStable();

    component.submit();

    backend.expectNone('/api/v1/me');
    expect(component.errorMessage()).toBe('Profile update link is unavailable.');
  });

  it('shows field validation errors returned by the backend', async () => {
    component.infoForm.firstname().value.set('');
    component.infoForm.lastname().value.set('Hopper');

    component.submit();

    backend.expectOne('/api/v1/me').flush(
      {
        message: 'Validation failed',
        errors: [{ field: 'firstname', code: 'not_blank', defaultMessage: 'must not be blank' }],
      },
      { status: 400, statusText: 'Bad Request' },
    );
    await fixture.whenStable();

    expect(component.fieldErrors()).toEqual({ firstname: 'must not be blank' });
    expect(component.errorMessage()).toBeNull();
  });
});
```

- [ ] **Step 3: Run test to verify it fails**

Run:

```bash
cd frontend && npm test -- --include src/app/features/profile/profile-info/profile-info.spec.ts
```

Expected: FAIL because `ProfileInfo` does not expose the required input/output/form behavior yet.

- [ ] **Step 4: Write minimal implementation**

Replace `frontend/src/app/features/profile/profile-info/profile-info.ts` with:

```ts
import { HttpClient } from '@angular/common/http';
import { Component, effect, inject, input, output, signal } from '@angular/core';
import { form, FormField, required, submit } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { firstValueFrom } from 'rxjs';
import { User } from '../../../core/auth/models/user';
import { linkHref } from '../../../shared/models/hal';
import { parseValidationErrors } from '../../../shared/models/validation-error';
import { UserToUpdate } from '../models/user-profile';
import { updateProfile } from '../profile-api';

const FORM_FIELDS = ['firstname', 'lastname'];

@Component({
  selector: 'app-profile-info',
  imports: [FormField, MatButtonModule, MatCardModule, MatFormFieldModule, MatInputModule],
  templateUrl: './profile-info.html',
})
export class ProfileInfo {
  private readonly http = inject(HttpClient);
  private readonly snackBar = inject(MatSnackBar);

  readonly user = input.required<User>();
  readonly saved = output<User>();

  readonly profile = signal<UserToUpdate>({ firstname: '', lastname: '' });
  readonly infoForm = form(this.profile, (path) => {
    required(path.firstname);
    required(path.lastname);
  });
  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly fieldErrors = signal<Record<string, string>>({});

  constructor() {
    effect(() => {
      const user = this.user();
      this.profile.set({ firstname: user.firstname, lastname: user.lastname });
      this.fieldErrors.set({});
      this.errorMessage.set(null);
    });
  }

  submit(): void {
    void submit(this.infoForm, async () => {
      const href = linkHref(this.user(), 'self');
      if (!href) {
        this.errorMessage.set('Profile update link is unavailable.');
        return;
      }

      this.submitting.set(true);
      this.errorMessage.set(null);
      this.fieldErrors.set({});

      try {
        const updatedUser = await firstValueFrom(updateProfile(this.http, href, this.profile()));
        this.submitting.set(false);
        this.snackBar.open('Profile updated.', 'Close', { duration: 5000 });
        this.saved.emit(updatedUser);
      } catch (error) {
        this.submitting.set(false);

        const parsed = parseValidationErrors(error, FORM_FIELDS);
        if (parsed) {
          this.fieldErrors.set(parsed.fieldErrors);
          this.errorMessage.set(
            parsed.hasObjectLevelError ? 'Profile could not be updated.' : null,
          );
          return;
        }

        this.errorMessage.set('Profile could not be updated.');
      }
    });
  }
}
```

Replace `frontend/src/app/features/profile/profile-info/profile-info.html` with:

```html
<mat-card>
  <mat-card-header>
    <mat-card-title>Profile information</mat-card-title>
  </mat-card-header>
  <mat-card-content>
    <form class="flex flex-col gap-4 pt-4" (submit)="$event.preventDefault(); submit()">
      <mat-form-field>
        <mat-label>Email</mat-label>
        <input matInput type="email" [value]="user().email" readonly />
      </mat-form-field>

      <mat-form-field>
        <mat-label>First name</mat-label>
        <input matInput type="text" autocomplete="given-name" [formField]="infoForm.firstname" />
      </mat-form-field>
      @if (infoForm.firstname().touched() && infoForm.firstname().errors().length) {
        <p class="text-sm text-red-600" role="alert">First name is required.</p>
      }
      @if (fieldErrors()['firstname']; as message) {
        <p class="text-sm text-red-600" role="alert">{{ message }}</p>
      }

      <mat-form-field>
        <mat-label>Last name</mat-label>
        <input matInput type="text" autocomplete="family-name" [formField]="infoForm.lastname" />
      </mat-form-field>
      @if (infoForm.lastname().touched() && infoForm.lastname().errors().length) {
        <p class="text-sm text-red-600" role="alert">Last name is required.</p>
      }
      @if (fieldErrors()['lastname']; as message) {
        <p class="text-sm text-red-600" role="alert">{{ message }}</p>
      }

      @if (errorMessage(); as message) {
        <p class="text-sm text-red-600" role="alert">{{ message }}</p>
      }

      <button mat-flat-button type="submit" [disabled]="submitting() || !infoForm().valid()">
        {{ submitting() ? 'Saving…' : 'Save profile' }}
      </button>
    </form>
  </mat-card-content>
</mat-card>
```

- [ ] **Step 5: Run test to verify it passes**

Run:

```bash
cd frontend && npm test -- --include src/app/features/profile/profile-info/profile-info.spec.ts
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
build-brief ./gradlew clean build
git add frontend/src/app/features/profile/profile-info/profile-info.ts frontend/src/app/features/profile/profile-info/profile-info.html frontend/src/app/features/profile/profile-info/profile-info.spec.ts
git commit -m "feat(frontend): edit profile information" -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

### Task 4: Ajouter la section photo de profil

**Files:**
- Create: `frontend/src/app/features/profile/profile-picture/profile-picture.ts`
- Create: `frontend/src/app/features/profile/profile-picture/profile-picture.html`
- Test: `frontend/src/app/features/profile/profile-picture/profile-picture.spec.ts`

**Interfaces:**
- Consumes:
  - `ImageUpload` selector `app-image-upload`
  - `uploadProfilePicture(http, href, file): Observable<User>`
  - `withCacheBuster(href, version)`
- Produces:
  - `ProfilePicture` standalone component, selector `app-profile-picture`
  - input `pictureHref = input<string | undefined>(undefined)`
  - output `uploaded = output<void>()`

- [ ] **Step 1: Scaffold with Angular CLI**

```bash
cd frontend && npx ng generate component features/profile/profile-picture --style=none
```

- [ ] **Step 2: Write the failing test**

Replace `frontend/src/app/features/profile/profile-picture/profile-picture.spec.ts` with:

```ts
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { ProfilePicture } from './profile-picture';

describe('ProfilePicture', () => {
  let fixture: ComponentFixture<ProfilePicture>;
  let component: ProfilePicture;
  let backend: HttpTestingController;
  let snackBar: { open: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    snackBar = { open: vi.fn() };
    await TestBed.configureTestingModule({
      imports: [ProfilePicture],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: MatSnackBar, useValue: snackBar },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ProfilePicture);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('pictureHref', '/api/v1/me/picture');
    fixture.detectChanges();
    await fixture.whenStable();
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('passes the original picture URL to the shared upload component before the first upload', () => {
    expect(component.currentImageUrl()).toBe('/api/v1/me/picture');
  });

  it('preserves an absolute picture URL origin when adding the cache-busting version', async () => {
    fixture.componentRef.setInput('pictureHref', 'https://api.example.test/api/v1/me/picture?size=small');
    fixture.detectChanges();
    const file = new File(['avatar'], 'avatar.png', { type: 'image/png' });

    component.onFileSelected(file);

    backend.expectOne('https://api.example.test/api/v1/me/picture?size=small').flush({ id: 'u-1' });
    await fixture.whenStable();

    expect(component.currentImageUrl()).toBe(
      'https://api.example.test/api/v1/me/picture?size=small&v=1',
    );
  });

  it('uploads a selected image, increments the cache-busting version, and emits uploaded', async () => {
    const emitted: void[] = [];
    component.uploaded.subscribe(() => emitted.push(undefined));
    const file = new File(['avatar'], 'avatar.png', { type: 'image/png' });

    component.onFileSelected(file);

    const request = backend.expectOne('/api/v1/me/picture');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toBeInstanceOf(FormData);
    expect((request.request.body as FormData).get('file')).toBe(file);
    request.flush({ id: 'u-1' });
    await fixture.whenStable();

    expect(component.currentImageUrl()).toBe('/api/v1/me/picture?v=1');
    expect(snackBar.open).toHaveBeenCalledWith('Profile picture updated.', 'Close', {
      duration: 5000,
    });
    expect(emitted.length).toBe(1);
  });

  it('does not send a request when the shared upload component rejects a file and emits null', () => {
    component.onFileSelected(null);

    backend.expectNone('/api/v1/me/picture');
  });

  it('shows a clear error when the picture link is unavailable', () => {
    fixture.componentRef.setInput('pictureHref', undefined);
    fixture.detectChanges();

    component.onFileSelected(new File(['avatar'], 'avatar.png', { type: 'image/png' }));

    backend.expectNone('/api/v1/me/picture');
    expect(component.errorMessage()).toBe('Profile picture upload link is unavailable.');
  });
});
```

- [ ] **Step 3: Run test to verify it fails**

Run:

```bash
cd frontend && npm test -- --include src/app/features/profile/profile-picture/profile-picture.spec.ts
```

Expected: FAIL because the component API and implementation are absent.

- [ ] **Step 4: Write minimal implementation**

Replace `frontend/src/app/features/profile/profile-picture/profile-picture.ts` with:

```ts
import { HttpClient } from '@angular/common/http';
import { Component, computed, inject, input, output, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar } from '@angular/material/snack-bar';
import { firstValueFrom } from 'rxjs';
import { withCacheBuster } from '../../../shared/http/cache-buster';
import { ImageUpload } from '../../../shared/image-upload/image-upload';
import { uploadProfilePicture } from '../profile-api';

@Component({
  selector: 'app-profile-picture',
  imports: [ImageUpload, MatCardModule],
  templateUrl: './profile-picture.html',
})
export class ProfilePicture {
  private readonly http = inject(HttpClient);
  private readonly snackBar = inject(MatSnackBar);

  readonly pictureHref = input<string | undefined>(undefined);
  readonly uploaded = output<void>();

  readonly version = signal(0);
  readonly uploading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly currentImageUrl = computed(() => {
    const href = this.pictureHref();
    return href ? withCacheBuster(href, this.version()) : null;
  });

  onFileSelected(file: File | null): void {
    if (!file) {
      return;
    }

    const href = this.pictureHref();
    if (!href) {
      this.errorMessage.set('Profile picture upload link is unavailable.');
      return;
    }

    this.uploading.set(true);
    this.errorMessage.set(null);

    void firstValueFrom(uploadProfilePicture(this.http, href, file))
      .then(() => {
        this.uploading.set(false);
        this.version.update((value) => value + 1);
        this.snackBar.open('Profile picture updated.', 'Close', { duration: 5000 });
        this.uploaded.emit();
      })
      .catch(() => {
        this.uploading.set(false);
        this.errorMessage.set('Profile picture could not be updated.');
      });
  }
}
```

Replace `frontend/src/app/features/profile/profile-picture/profile-picture.html` with:

```html
<mat-card>
  <mat-card-header>
    <mat-card-title>Profile picture</mat-card-title>
  </mat-card-header>
  <mat-card-content class="pt-4">
    <app-image-upload
      [currentImageUrl]="currentImageUrl()"
      label="Profile picture"
      fallbackIcon="account_circle"
      [error]="errorMessage()"
      (fileSelected)="onFileSelected($event)"
    />
    @if (uploading()) {
      <p class="text-sm text-slate-600">Uploading…</p>
    }
  </mat-card-content>
</mat-card>
```

- [ ] **Step 5: Run test to verify it passes**

Run:

```bash
cd frontend && npm test -- --include src/app/features/profile/profile-picture/profile-picture.spec.ts
```

Expected: PASS. Le fallback 404/avatar est couvert par le composant partagé `ImageUpload` du plan 00 ; cette tâche vérifie que `fallbackIcon="account_circle"` et l'URL versionnée lui sont transmis.

- [ ] **Step 6: Commit**

```bash
build-brief ./gradlew clean build
git add frontend/src/app/features/profile/profile-picture/profile-picture.ts frontend/src/app/features/profile/profile-picture/profile-picture.html frontend/src/app/features/profile/profile-picture/profile-picture.spec.ts
git commit -m "feat(frontend): upload profile picture" -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

### Task 5: Ajouter la section changement de mot de passe

**Files:**
- Create: `frontend/src/app/features/profile/profile-password/profile-password.ts`
- Create: `frontend/src/app/features/profile/profile-password/profile-password.html`
- Test: `frontend/src/app/features/profile/profile-password/profile-password.spec.ts`

**Interfaces:**
- Consumes:
  - `AuthService.logout(): void`
  - `updatePassword(http, href, payload): Observable<void>`
  - `parseValidationErrors(error, ['password'])`
- Produces:
  - `ProfilePassword` standalone component, selector `app-profile-password`
  - input `passwordHref = input<string | undefined>(undefined)`

- [ ] **Step 1: Scaffold with Angular CLI**

```bash
cd frontend && npx ng generate component features/profile/profile-password --style=none
```

- [ ] **Step 2: Write the failing test**

Replace `frontend/src/app/features/profile/profile-password/profile-password.spec.ts` with:

```ts
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router, provideRouter } from '@angular/router';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthService } from '../../../core/auth/auth';
import { ProfilePassword } from './profile-password';

describe('ProfilePassword', () => {
  let fixture: ComponentFixture<ProfilePassword>;
  let component: ProfilePassword;
  let backend: HttpTestingController;
  let router: Router;
  let auth: { logout: ReturnType<typeof vi.fn> };
  let snackBar: { open: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    auth = { logout: vi.fn() };
    snackBar = { open: vi.fn() };
    await TestBed.configureTestingModule({
      imports: [ProfilePassword],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'login', children: [] }]),
        { provide: AuthService, useValue: auth },
        { provide: MatSnackBar, useValue: snackBar },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ProfilePassword);
    component = fixture.componentInstance;
    fixture.componentRef.setInput('passwordHref', '/api/v1/me/password');
    fixture.detectChanges();
    await fixture.whenStable();
    backend = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
  });

  afterEach(() => backend.verify());

  function fillPasswords(confirm = 'NewPassw0rd!'): void {
    component.passwordForm.oldPassword().value.set('OldPassw0rd!');
    component.passwordForm.newPassword().value.set('NewPassw0rd!');
    component.passwordForm.confirmNewPassword().value.set(confirm);
  }

  it('rejects mismatched passwords without calling the API', () => {
    fillPasswords('DifferentPassw0rd!');

    component.submit();

    backend.expectNone('/api/v1/me/password');
    expect(component.passwordsMatch()).toBe(false);
    expect(component.passwordForm().valid()).toBe(false);
  });

  it('posts the password change, logs out, navigates to login, and shows the success snackbar', async () => {
    fillPasswords();

    component.submit();

    const request = backend.expectOne('/api/v1/me/password');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      oldPassword: 'OldPassw0rd!',
      newPassword: 'NewPassw0rd!',
    });
    request.flush(null, { status: 204, statusText: 'No Content' });
    await fixture.whenStable();

    expect(auth.logout).toHaveBeenCalledOnce();
    expect(router.url).toBe('/login');
    expect(snackBar.open).toHaveBeenCalledWith('Password changed. Please sign in again.', 'Close', {
      duration: 5000,
    });
  });

  it('maps a weak password validation error returned as field "password" to newPassword', async () => {
    fillPasswords();

    component.submit();

    backend.expectOne('/api/v1/me/password').flush(
      {
        timestamp: '2026-09-25T12:00:00Z',
        message: 'Validation failed',
        errors: [
          {
            field: 'password',
            code: 'validation.constraints.password.min-length',
            defaultMessage: 'A password must contain at least 8 character(s)',
          },
        ],
      },
      { status: 400, statusText: 'Bad Request' },
    );
    await fixture.whenStable();

    expect(component.fieldErrors()).toEqual({
      newPassword: 'A password must contain at least 8 character(s)',
    });
    expect(component.errorMessage()).toBeNull();
  });

  it('maps a 400 without validation errors to the current password field', async () => {
    fillPasswords();

    component.submit();

    backend.expectOne('/api/v1/me/password').flush(
      { timestamp: '2026-09-25T12:00:00Z', message: 'Old password does not match' },
      { status: 400, statusText: 'Bad Request' },
    );
    await fixture.whenStable();

    expect(component.fieldErrors()).toEqual({
      oldPassword: 'Current password is incorrect.',
    });
    expect(component.errorMessage()).toBeNull();
  });
});
```

- [ ] **Step 3: Run test to verify it fails**

Run:

```bash
cd frontend && npm test -- --include src/app/features/profile/profile-password/profile-password.spec.ts
```

Expected: FAIL because the component API and error mapping are absent.

- [ ] **Step 4: Write minimal implementation**

Replace `frontend/src/app/features/profile/profile-password/profile-password.ts` with:

```ts
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, signal } from '@angular/core';
import { form, FormField, required, submit, validate } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../../../core/auth/auth';
import { parseValidationErrors } from '../../../shared/models/validation-error';
import { updatePassword } from '../profile-api';

@Component({
  selector: 'app-profile-password',
  imports: [FormField, MatButtonModule, MatCardModule, MatFormFieldModule, MatInputModule],
  templateUrl: './profile-password.html',
})
export class ProfilePassword {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly passwordHref = input<string | undefined>(undefined);
  readonly passwords = signal({ oldPassword: '', newPassword: '', confirmNewPassword: '' });
  readonly passwordForm = form(this.passwords, (path) => {
    required(path.oldPassword);
    required(path.newPassword);
    validate(path.confirmNewPassword, ({ value, valueOf }) => {
      if (value() === valueOf(path.newPassword)) {
        return undefined;
      }
      return { kind: 'mismatch', message: 'Passwords do not match.' };
    });
  });
  readonly passwordsMatch = computed(
    () => this.passwords().newPassword === this.passwords().confirmNewPassword,
  );
  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly fieldErrors = signal<Record<string, string>>({});

  submit(): void {
    if (!this.passwordsMatch()) {
      return;
    }

    void submit(this.passwordForm, async () => {
      const href = this.passwordHref();
      if (!href) {
        this.errorMessage.set('Password update link is unavailable.');
        return;
      }

      this.submitting.set(true);
      this.errorMessage.set(null);
      this.fieldErrors.set({});

      try {
        const { oldPassword, newPassword } = this.passwords();
        await firstValueFrom(updatePassword(this.http, href, { oldPassword, newPassword }));
        this.submitting.set(false);
        this.auth.logout();
        await this.router.navigate(['/login']);
        this.snackBar.open('Password changed. Please sign in again.', 'Close', { duration: 5000 });
      } catch (error) {
        this.submitting.set(false);

        const parsed = parseValidationErrors(error, ['password']);
        if (parsed?.fieldErrors['password']) {
          this.fieldErrors.set({ newPassword: parsed.fieldErrors['password'] });
          return;
        }

        if (error instanceof HttpErrorResponse && error.status === 400) {
          this.fieldErrors.set({ oldPassword: 'Current password is incorrect.' });
          return;
        }

        this.errorMessage.set('Password could not be changed.');
      }
    });
  }
}
```

Replace `frontend/src/app/features/profile/profile-password/profile-password.html` with:

```html
<mat-card>
  <mat-card-header>
    <mat-card-title>Change password</mat-card-title>
  </mat-card-header>
  <mat-card-content>
    <form class="flex flex-col gap-4 pt-4" (submit)="$event.preventDefault(); submit()">
      <mat-form-field>
        <mat-label>Current password</mat-label>
        <input
          matInput
          type="password"
          autocomplete="current-password"
          [formField]="passwordForm.oldPassword"
        />
      </mat-form-field>
      @if (fieldErrors()['oldPassword']; as message) {
        <p class="text-sm text-red-600" role="alert">{{ message }}</p>
      }

      <mat-form-field>
        <mat-label>New password</mat-label>
        <input
          matInput
          type="password"
          autocomplete="new-password"
          [formField]="passwordForm.newPassword"
        />
      </mat-form-field>
      @if (fieldErrors()['newPassword']; as message) {
        <p class="text-sm text-red-600" role="alert">{{ message }}</p>
      }

      <mat-form-field>
        <mat-label>Confirm new password</mat-label>
        <input
          matInput
          type="password"
          autocomplete="new-password"
          [formField]="passwordForm.confirmNewPassword"
        />
      </mat-form-field>
      @if (!passwordsMatch()) {
        <p class="text-sm text-red-600" role="alert">Passwords do not match.</p>
      }

      @if (errorMessage(); as message) {
        <p class="text-sm text-red-600" role="alert">{{ message }}</p>
      }

      <button
        mat-flat-button
        type="submit"
        [disabled]="submitting() || !passwordForm().valid() || !passwordsMatch()"
      >
        {{ submitting() ? 'Changing…' : 'Change password' }}
      </button>
    </form>
  </mat-card-content>
</mat-card>
```

- [ ] **Step 5: Run test to verify it passes**

Run:

```bash
cd frontend && npm test -- --include src/app/features/profile/profile-password/profile-password.spec.ts
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
build-brief ./gradlew clean build
git add frontend/src/app/features/profile/profile-password/profile-password.ts frontend/src/app/features/profile/profile-password/profile-password.html frontend/src/app/features/profile/profile-password/profile-password.spec.ts
git commit -m "feat(frontend): change profile password" -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

### Task 6: Composer la page `/profile` et la route

**Files:**
- Create: `frontend/src/app/features/profile/profile/profile.ts`
- Create: `frontend/src/app/features/profile/profile/profile.html`
- Test: `frontend/src/app/features/profile/profile/profile.spec.ts`
- Modify: `frontend/src/app/app.routes.ts`

**Interfaces:**
- Consumes:
  - `profileUrl(): string`
  - `linkHref(user, 'me:picture' | 'me:password')`
  - `ProfileInfo`, `ProfilePicture`, `ProfilePassword`
  - `userGuard`
- Produces:
  - `Profile` standalone component, selector `app-profile`
  - Route `{ path: 'profile', loadComponent: ..., canActivate: [userGuard], title: 'My profile' }`

- [ ] **Step 1: Scaffold with Angular CLI**

```bash
cd frontend && npx ng generate component features/profile/profile --style=none
```

- [ ] **Step 2: Write the failing test**

Replace `frontend/src/app/features/profile/profile/profile.spec.ts` with:

```ts
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { provideRouter } from '@angular/router';
import { RouterTestingHarness } from '@angular/router/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { TokenStore } from '../../../core/auth/token-store';
import { Profile } from './profile';

function fakeJwt(payload: Record<string, unknown>): string {
  const encode = (value: unknown) =>
    btoa(JSON.stringify(value)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${encode({ alg: 'none' })}.${encode(payload)}.signature`;
}

describe('Profile', () => {
  let harness: RouterTestingHarness;
  let backend: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        provideRouter([{ path: 'profile', component: Profile }]),
        { provide: MatSnackBar, useValue: { open: vi.fn() } },
      ],
    }).compileComponents();

    TestBed.inject(TokenStore).setAccessToken(
      fakeJwt({ sub: 'user@vending.me', roles: ['ROLE_USER'], exp: 1 }),
    );
    backend = TestBed.inject(HttpTestingController);
    harness = await RouterTestingHarness.create();
  });

  afterEach(() => backend.verify());

  it('loads the profile with httpResource and renders all three sections after the GET is flushed', async () => {
    const component = await harness.navigateByUrl('/profile', Profile);
    harness.fixture.detectChanges();

    backend.expectOne('/api/v1/me').flush({
      id: 'u-1',
      email: 'ada@vending.me',
      firstname: 'Ada',
      lastname: 'Lovelace',
      _links: {
        self: { href: '/api/v1/me' },
        'me:picture': { href: '/api/v1/me/picture' },
        'me:password': { href: '/api/v1/me/password' },
      },
    });
    await harness.fixture.whenStable();
    harness.fixture.detectChanges();

    const page = harness.fixture.nativeElement as HTMLElement;
    expect(component.profile()?.email).toBe('ada@vending.me');
    expect(page.textContent).toContain('My profile');
    expect(page.textContent).toContain('Profile information');
    expect(page.textContent).toContain('Profile picture');
    expect(page.textContent).toContain('Change password');
  });

  it('reloads the profile resource when a child emits saved', async () => {
    const component = await harness.navigateByUrl('/profile', Profile);
    harness.fixture.detectChanges();

    backend.expectOne('/api/v1/me').flush({
      id: 'u-1',
      email: 'ada@vending.me',
      firstname: 'Ada',
      lastname: 'Lovelace',
      _links: { self: { href: '/api/v1/me' } },
    });
    await harness.fixture.whenStable();

    component.reload();
    harness.fixture.detectChanges();

    backend.expectOne('/api/v1/me').flush({
      id: 'u-1',
      email: 'ada@vending.me',
      firstname: 'Grace',
      lastname: 'Hopper',
      _links: { self: { href: '/api/v1/me' } },
    });
    await harness.fixture.whenStable();

    expect(component.profile()?.firstname).toBe('Grace');
  });
});
```

- [ ] **Step 3: Run test to verify it fails**

Run:

```bash
cd frontend && npm test -- --include src/app/features/profile/profile/profile.spec.ts
```

Expected: FAIL because the parent component and/or imports are incomplete.

- [ ] **Step 4: Write minimal implementation**

Replace `frontend/src/app/features/profile/profile/profile.ts` with:

```ts
import { httpResource } from '@angular/common/http';
import { Component, computed } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { linkHref } from '../../../shared/models/hal';
import { PROFILE_PASSWORD_REL, PROFILE_PICTURE_REL } from '../models/user-profile';
import { ProfileInfo } from '../profile-info/profile-info';
import { ProfilePassword } from '../profile-password/profile-password';
import { ProfilePicture } from '../profile-picture/profile-picture';
import { profileUrl } from '../profile-api';
import { User } from '../../../core/auth/models/user';

@Component({
  selector: 'app-profile',
  imports: [MatProgressSpinnerModule, ProfileInfo, ProfilePassword, ProfilePicture],
  templateUrl: './profile.html',
})
export class Profile {
  private readonly resource = httpResource<User>(() => profileUrl());

  readonly isLoading = this.resource.isLoading;
  readonly hasError = computed(() => this.resource.error() !== undefined);
  readonly profile = computed(() => this.resource.value());
  readonly pictureHref = computed(() => linkHref(this.profile(), PROFILE_PICTURE_REL));
  readonly passwordHref = computed(() => linkHref(this.profile(), PROFILE_PASSWORD_REL));

  reload(): void {
    this.resource.reload();
  }
}
```

Replace `frontend/src/app/features/profile/profile/profile.html` with:

```html
<section class="mx-auto flex max-w-3xl flex-col gap-6">
  <header>
    <h1 class="text-2xl font-semibold">My profile</h1>
  </header>

  @if (isLoading()) {
    <div class="flex justify-center p-8">
      <mat-spinner aria-label="Loading profile" />
    </div>
  } @else if (hasError()) {
    <p class="text-sm text-red-600" role="alert">Profile could not be loaded.</p>
  } @else if (profile(); as user) {
    <app-profile-info [user]="user" (saved)="reload()" />
    <app-profile-picture [pictureHref]="pictureHref()" (uploaded)="reload()" />
    <app-profile-password [passwordHref]="passwordHref()" />
  }
</section>
```

Modify `frontend/src/app/app.routes.ts`:

```ts
import { Routes } from '@angular/router';
import { adminGuard } from './core/auth/admin-guard';
import { userGuard } from './core/auth/user-guard';

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
    path: 'profile',
    loadComponent: () => import('./features/profile/profile/profile').then((m) => m.Profile),
    canActivate: [userGuard],
    title: 'My profile',
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

- [ ] **Step 5: Run test to verify it passes**

Run:

```bash
cd frontend && npm test -- --include src/app/features/profile/profile/profile.spec.ts
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
build-brief ./gradlew clean build
git add frontend/src/app/features/profile/profile/profile.ts frontend/src/app/features/profile/profile/profile.html frontend/src/app/features/profile/profile/profile.spec.ts frontend/src/app/app.routes.ts
git commit -m "feat(frontend): add profile route" -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

### Task 7: Ajouter le lien toolbar `"My profile"`

**Files:**
- Modify: `frontend/src/app/app.html`
- Modify: `frontend/src/app/app.spec.ts`

**Interfaces:**
- Consumes:
  - `App.isAuthenticated()`
  - `App.isAdmin()`
- Produces: lien `<a mat-button routerLink="/profile">My profile</a>` visible seulement pour un utilisateur connecté non admin.

- [ ] **Step 1: Write the failing test**

Replace `frontend/src/app/app.spec.ts` with:

```ts
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import { beforeEach, describe, expect, it } from 'vitest';
import { App } from './app';
import { AuthService } from './core/auth/auth';

function fakeJwt(payload: Record<string, unknown>): string {
  const encode = (value: unknown) =>
    btoa(JSON.stringify(value)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${encode({ alg: 'none' })}.${encode(payload)}.signature`;
}

describe('App', () => {
  let http: HttpTestingController;

  beforeEach(async () => {
    sessionStorage.clear();
    await TestBed.configureTestingModule({
      imports: [App],
      providers: [provideHttpClient(), provideHttpClientTesting(), provideRouter([])],
    }).compileComponents();
    http = TestBed.inject(HttpTestingController);
  });

  it('offers a sign-in link to anonymous visitors', async () => {
    const fixture = TestBed.createComponent(App);
    await fixture.whenStable();

    const toolbar = fixture.nativeElement as HTMLElement;
    expect(toolbar.textContent).toContain('Sign in');
    expect(toolbar.textContent).not.toContain('My profile');
  });

  it('offers a profile link and sign-out button to a logged-in user', async () => {
    const auth = TestBed.inject(AuthService);
    auth.login({ username: 'ada@vending.me', password: 'secret' }).subscribe();
    http.expectOne('/api/v1/authenticate').flush({
      accessToken: fakeJwt({ sub: 'ada@vending.me', roles: ['ROLE_USER'], exp: 1 }),
    });
    http.expectOne('/api/v1/me').flush({
      id: 'u-1',
      email: 'ada@vending.me',
      firstname: 'Ada',
      lastname: 'Lovelace',
    });

    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    await fixture.whenStable();

    const toolbar = fixture.nativeElement as HTMLElement;
    expect(toolbar.textContent).toContain('My profile');
    expect(toolbar.querySelector('a[routerLink="/profile"]')?.textContent).toContain('My profile');
    expect(toolbar.textContent).toContain('Log out');
  });

  it('offers a sign-out button to a logged-in admin without a /me profile', async () => {
    const auth = TestBed.inject(AuthService);
    auth.login({ username: 'admin@vending.me', password: 'secret' }).subscribe();
    http.expectOne('/api/v1/authenticate').flush({
      accessToken: fakeJwt({ sub: 'admin@vending.me', roles: ['ROLE_ADMIN'], exp: 1 }),
    });

    const fixture = TestBed.createComponent(App);
    fixture.detectChanges();
    await fixture.whenStable();

    const toolbar = fixture.nativeElement as HTMLElement;
    expect(toolbar.textContent).toContain('Log out');
    expect(toolbar.textContent).toContain('Admin');
    expect(toolbar.textContent).not.toContain('My profile');
  });
});
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```bash
cd frontend && npm test -- --include src/app/app.spec.ts
```

Expected: FAIL because `"My profile"` is not rendered yet.

- [ ] **Step 3: Write minimal implementation**

Replace `frontend/src/app/app.html` with:

```html
<mat-toolbar class="flex items-center gap-4">
  <span class="font-semibold">VendingApp</span>
  <a mat-button routerLink="/machines">Machines</a>
  @if (isAdmin()) {
    <a mat-button routerLink="/items">Items</a>
  }
  @if (isAuthenticated() && !isAdmin()) {
    <a mat-button routerLink="/profile">My profile</a>
  }
  <span class="flex-1"></span>
  @if (isAuthenticated()) {
    <span class="text-sm">{{ currentUser()?.email ?? 'Admin' }}</span>
    <button mat-button type="button" (click)="logout()">Log out</button>
  } @else {
    <a mat-button routerLink="/login">Sign in</a>
  }
</mat-toolbar>

<main class="p-4">
  <router-outlet />
</main>
```

- [ ] **Step 4: Run test to verify it passes**

Run:

```bash
cd frontend && npm test -- --include src/app/app.spec.ts
```

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
build-brief ./gradlew clean build
git add frontend/src/app/app.html frontend/src/app/app.spec.ts
git commit -m "feat(frontend): link to user profile" -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

### Task 8: Ajouter le test e2e du profil

**Files:**
- Create: `frontend/e2e/manage-profile.spec.ts`

**Interfaces:**
- Consumes:
  - UI labels `"My profile"`, `"Save profile"`, `"Change password"`
  - API contracts `/api/v1/me`, `/api/v1/me/picture`, `/api/v1/me/password`, `/api/v1/authenticate/logout`
- Produces: couverture e2e du profil sans backend réel.

- [ ] **Step 1: Write the failing e2e test**

Create `frontend/e2e/manage-profile.spec.ts`:

```ts
import { expect, type Page, type Route, test } from '@playwright/test';

function fakeAccessToken(): string {
  const encode = (value: unknown) =>
    btoa(JSON.stringify(value)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
  return `${encode({ alg: 'none' })}.${encode({
    sub: 'user@vending.me',
    roles: ['ROLE_USER'],
    exp: 4102444800,
    token_type: 'access',
  })}.signature`;
}

const user = {
  id: 'u-1',
  email: 'user@vending.me',
  firstname: 'Ada',
  lastname: 'Lovelace',
  _links: {
    self: { href: '/api/v1/me' },
    'me:picture': { href: '/api/v1/me/picture' },
    'me:password': { href: '/api/v1/me/password' },
  },
};

async function fulfillJson(route: Route, body: unknown, status = 200) {
  await route.fulfill({
    status,
    contentType: 'application/hal+json',
    body: JSON.stringify(body),
  });
}

async function signIn(page: Page) {
  await page.goto('/login');
  await page.getByLabel('Email').fill('user@vending.me');
  await page.getByLabel('Password').fill('S3cret!Passw0rd');
  await page.getByRole('button', { name: 'Sign in' }).click();
  await expect(page).toHaveURL(/\/machines$/);
  await expect(page.getByText('user@vending.me')).toBeVisible();
}

test.beforeEach(async ({ page }) => {
  await page.route('**/api/v1', async (route) => {
    await fulfillJson(route, { _links: { vendingMachines: { href: '/api/v1/vending-machines' } } });
  });

  await page.route('**/api/v1/authenticate', async (route) => {
    await route.fulfill({
      status: 200,
      contentType: 'application/json',
      body: JSON.stringify({ accessToken: fakeAccessToken() }),
    });
  });

  await page.route('**/api/v1/authenticate/refresh', async (route) => {
    await route.fulfill({ status: 401 });
  });

  await page.route('**/api/v1/vending-machines**', async (route) => {
    await fulfillJson(route, {
      _embedded: { elements: [] },
      page: { size: 10, totalElements: 0, totalPages: 0, number: 0 },
    });
  });
});

test('a signed-in user can manage profile information, picture, and password', async ({ page }) => {
  let firstname = user.firstname;
  let lastname = user.lastname;
  let updateBody = '';
  let pictureUploadCount = 0;
  let passwordChanged = false;

  await page.route('**/api/v1/me', async (route) => {
    const request = route.request();

    if (request.method() === 'GET') {
      await fulfillJson(route, { ...user, firstname, lastname });
      return;
    }

    if (request.method() === 'PUT') {
      updateBody = request.postData() ?? '';
      const params = new URLSearchParams(updateBody);
      firstname = params.get('firstname') ?? firstname;
      lastname = params.get('lastname') ?? lastname;
      await fulfillJson(route, { ...user, firstname, lastname });
      return;
    }

    throw new Error(`Unexpected /me request: ${request.method()}`);
  });

  await page.route('**/api/v1/me/picture**', async (route) => {
    const request = route.request();

    if (request.method() === 'GET') {
      await route.fulfill({ status: 404 });
      return;
    }

    if (request.method() === 'POST') {
      pictureUploadCount += 1;
      await fulfillJson(route, { ...user, firstname, lastname });
      return;
    }

    throw new Error(`Unexpected /me/picture request: ${request.method()}`);
  });

  await page.route('**/api/v1/me/password', async (route) => {
    const request = route.request();
    expect(request.method()).toBe('POST');
    expect(request.postDataJSON()).toEqual({
      oldPassword: 'OldPassw0rd!',
      newPassword: 'NewPassw0rd!',
    });
    passwordChanged = true;
    await route.fulfill({ status: 204 });
  });

  await page.route('**/api/v1/authenticate/logout', async (route) => {
    await route.fulfill({ status: 204 });
  });

  await signIn(page);
  await page.getByRole('link', { name: 'My profile' }).click();
  await expect(page).toHaveURL(/\/profile$/);
  await expect(page.getByRole('heading', { name: 'My profile' })).toBeVisible();

  await page.getByLabel('First name').fill('Grace');
  await page.getByLabel('Last name').fill('Hopper');
  await page.getByRole('button', { name: 'Save profile' }).click();
  await expect(page.getByText('Profile updated.')).toBeVisible();
  expect(updateBody).toContain('firstname=Grace');
  expect(updateBody).toContain('lastname=Hopper');

  await page.getByTestId('image-upload-input').setInputFiles({
    name: 'avatar.png',
    mimeType: 'image/png',
    buffer: Buffer.from('avatar'),
  });
  await expect(page.getByText('Profile picture updated.')).toBeVisible();
  expect(pictureUploadCount).toBe(1);

  await page.getByLabel('Current password').fill('OldPassw0rd!');
  await page.getByLabel('New password').fill('NewPassw0rd!');
  await page.getByLabel('Confirm new password').fill('NewPassw0rd!');
  await page.getByRole('button', { name: 'Change password' }).click();
  await expect(page).toHaveURL(/\/login$/);
  expect(passwordChanged).toBe(true);
});
```

- [ ] **Step 2: Run e2e test to verify it fails before the feature exists**

Run:

```bash
cd frontend && npm run e2e -- e2e/manage-profile.spec.ts
```

Expected: FAIL before Tasks 1-7 are implemented; after Tasks 1-7, it should pass.

- [ ] **Step 3: Run e2e test to verify it passes after implementation**

Run:

```bash
cd frontend && npm run e2e -- e2e/manage-profile.spec.ts
```

Expected: PASS.

- [ ] **Step 4: Commit**

```bash
build-brief ./gradlew clean build
git add frontend/e2e/manage-profile.spec.ts
git commit -m "feat(frontend): cover profile management e2e" -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

### Task 9: Mettre à jour la documentation frontend

**Files:**
- Modify: `frontend/README.md`
- Modify: `docs/features-front-a-implementer.md`

**Interfaces:**
- Consumes: fonctionnalité livrée par Tasks 1-8.
- Produces: documentation alignée, sans changer les modifications non liées déjà présentes sur `main`.

- [ ] **Step 1: Write the documentation diff**

Dans `frontend/README.md`, mettre à jour `## Périmètre actuel` pour inclure :

```md
- **Profil utilisateur** : page `/profile` réservée aux utilisateurs connectés
  non admin, consultation/édition du prénom et du nom, email en lecture seule,
  upload de photo de profil et changement de mot de passe avec déconnexion.
```

Dans la section `**Reste à faire**`, retirer uniquement la ligne :

```md
- Profil utilisateur (consultation/édition, changement de mot de passe)
```

Dans `docs/features-front-a-implementer.md`, ajouter sous `## ✅ Déjà fait` :

```md
- **Espace profil utilisateur** (`/profile`, `ROLE_USER`) — consultation/
  édition des informations personnelles (`GET`/`PUT /me`), photo de profil
  (`GET`/`POST /me/picture`) et changement de mot de passe
  (`POST /me/password`)
```

Et remplacer la section `## 👤 Espace utilisateur connecté (`/api/v1/me/**`)` par :

```md
## 👤 Espace utilisateur connecté (`/api/v1/me/**`)

Tous les points identifiés pour l'espace profil sont implémentés côté frontend.
```

- [ ] **Step 2: Run documentation check**

Run:

```bash
git --no-pager diff -- frontend/README.md docs/features-front-a-implementer.md
```

Expected: le diff ne touche que les sections indiquées. Il ne doit pas écraser les changements non liés déjà présents dans ces fichiers.

- [ ] **Step 3: Run full validation before commit**

Run:

```bash
cd frontend && npm test
cd frontend && npm run e2e
build-brief ./gradlew clean build
```

Expected: PASS pour les trois commandes.

- [ ] **Step 4: Commit**

```bash
git add frontend/README.md docs/features-front-a-implementer.md
git commit -m "feat(frontend): document profile management" -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
```

## Notes

- `SelfServiceRestController.update(Authentication authentication, UserToUpdateDto userDto)` n'a pas `@RequestBody`; tant que le backend reste ainsi, le frontend doit envoyer `PUT /api/v1/me` en `application/x-www-form-urlencoded` via `HttpParams`. Ajouter `@RequestBody` côté backend serait une amélioration séparée et imposerait de repasser cette mutation frontend en JSON.
- `/api/v1/me/**` est authentifié mais fonctionnellement réservé aux comptes `AppUser` ; `userGuard` documente la décision : anonyme vers `/login`, admin vers `/machines`.
- Ne pas ajouter de règle client-side de complexité de mot de passe : la politique backend est configurable et reste la source de vérité.
- Ne pas créer un second composant d'upload : `ImageUpload` du plan 00 couvre le fallback avatar, le rejet JPEG/PNG et la preview locale.

## Self-Review

- **Spec coverage:** `/profile`, toolbar `"My profile"`, infos, photo, mot de passe, garde non-admin, form-urlencoded `PUT /me`, e2e et documentation sont couverts par Tasks 1-9.
- **Placeholder scan:** aucun marqueur de travail incomplet ni fonction non définie dans les étapes.
- **Type consistency:** les interfaces produites en Task 1 (`UserToUpdate`, `EditPasswordRequest`, helpers API) sont consommées avec les mêmes signatures dans Tasks 3-5 ; `userGuard` est importé en Task 6.
- **Review Focus:** les cinq modes d'échec listés sont chacun ancrés par au moins un test dans la tâche propriétaire.
