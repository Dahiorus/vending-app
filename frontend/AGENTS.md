# AGENTS.md — frontend

SPA Angular consommant l'API REST du backend `vending-app`.

## Commandes

```bash
npm start        # serveur de dev sur http://localhost:4200 (proxy /api -> localhost:8080)
npm run build    # build de production
npm test         # tests unitaires Vitest
npm run e2e      # tests end-to-end Playwright (API mockée, aucun backend requis)
```

Node est épinglé par Volta (`volta.node` dans `package.json`) : Angular 22 exige
Node >= 22.22.3.

`frontend/` est aussi intégré au build Gradle englobant (racine du dépôt,
projet `:frontend`, voir `AGENTS.md` racine) : `./gradlew build` y exécute
`npm ci`/`npm run build`/`npm test` via des tâches `Exec` (pas de plugin
Angular/Node Gradle). Ce wrapping ne remplace pas le workflow quotidien
ci-dessus : continuer à utiliser `npm start`/`npm test` directement pendant
le développement, le Gradle wrapping n'existe que pour le build agrégé.

## Workflow de développement

- Utiliser le skill `angular-developer` pour toute nouvelle feature Angular
  (composants, services, guides d'architecture, bonnes pratiques signaux/
  formulaires/routage).
- Générer les composants et services avec **Angular CLI** (`ng generate
  component ...`, `ng generate service ...`) plutôt que de créer les fichiers
  à la main, puis adapter le squelette généré au besoin (voir les conventions
  ci-dessous : nommage 2025, Signal Forms, `httpResource`, etc.).

## Conventions

- Angular standalone et **zoneless** ; pas de SSR.
- Nommage de fichiers style guide **2025** : `login.ts`, pas `login.component.ts`.
- **Services** : décorateur **`@Service()`** (Angular 22, `@angular/core`), pas
  `@Injectable({ providedIn: 'root' })` — c'est le nouveau défaut de
  `ng generate service` et la recommandation actuelle pour un singleton racine
  standard (voir `TokenStore`, `AuthApi`, `AuthService`). Garder `@Injectable`
  uniquement pour un besoin de provider avancé (`useClass`, `useFactory`, scope
  non racine, etc.) que `@Service()` ne couvre pas.
- **Lecture de données** : `httpResource()` (signaux). **Mutations** (`POST`/`PUT`/
  `DELETE`) : `HttpClient` directement — c'est la recommandation Angular.
- **Organisation par feature** (guide de style Angular 2025) : pas de dossier
  générique `api/`. Le code d'accès aux données vit à côté de la feature qui
  l'utilise (ex. `features/machines/vending-machine-api.ts` +
  `features/machines/models/`). Seul le code réellement transverse (utilisé par
  plusieurs features) va dans `core/` (singletons app-wide, ex. `core/auth/`)
  ou `shared/` (utilitaires/modèles réutilisables, ex. `shared/models/hal.ts`).
- Les modèles (ex. `features/machines/models/`, `core/auth/models/`) sont
  **écrits à la main** et sont le miroir des DTO du module `infrastructure`.
  Toute évolution d'un DTO backend doit être répercutée ici manuellement.
- Le contrat HAL n'est déballé qu'au seul endroit prévu : `toPage()` dans
  `shared/models/hal.ts`. `_embedded` est absent des pages vides.
- Les nouveaux formulaires utilisent **Signal Forms** (`@angular/forms/signals`).
- Angular Material fournit les composants, Tailwind la mise en page.
- **Tests unitaires impliquant `httpResource()`** : ne jamais `await
  fixture.whenStable()` avant d'avoir flush une requête HTTP en attente —
  `httpResource` garde le fixture instable tant que sa requête n'est pas résolue,
  ce qui bloque `whenStable()` indéfiniment (timeout du hook/test). Déclencher la
  détection de changements avec `fixture.detectChanges()` (synchrone) pour laisser
  la requête partir, la flush via `HttpTestingController`, puis attendre
  `whenStable()` seulement après.
- Toujours lancer les tests via `npm test` (passe par `ng test` /
  `@angular/build:unit-test`), jamais `npx vitest run` directement : ce dernier
  contourne le pipeline de build Angular et ne résout pas `templateUrl`/
  `styleUrl`, ce qui provoque des échecs de résolution de composant sans rapport
  avec le code testé.

## Sécurité des jetons (décision actée)

- L'access token vit **uniquement en mémoire** (signal privé de `TokenStore`) et
  ne doit jamais être persisté — un test unitaire verrouille cette propriété.
- Le refresh token est en `sessionStorage`, faute de cookie `httpOnly` côté
  backend.
- Le backend **ne fait pas de rotation** du refresh token : `/authenticate/refresh`
  renvoie le même jeton. Ne pas écrire de code qui suppose l'inverse.
- Évolution prévue (chantier backend) : refresh token en cookie `httpOnly` +
  `SameSite` et rotation à chaque renouvellement.
- Interdits : jeton dans une URL, `innerHTML`, `bypassSecurityTrust*`.
