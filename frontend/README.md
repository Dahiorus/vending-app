# Frontend — vending-app

SPA Angular 22 (standalone, zoneless, sans SSR) consommant l'API REST HAL du
backend `vending-app` (module `infrastructure`). Voir aussi
[`AGENTS.md`](./AGENTS.md) pour les conventions détaillées.

## Prérequis

- **Node ≥ 22.22.3** (le projet épingle **Node 24** via [Volta](https://volta.sh/) :
  `volta.node` dans `package.json`). Si Volta est installé, `cd frontend` suffit
  à utiliser la bonne version.
- Le backend Spring Boot (`./gradlew :backend:infrastructure:bootRun` depuis
  la racine du dépôt) et PostgreSQL pour un usage complet en développement —
  non requis pour les tests unitaires ni pour l'E2E Playwright, qui mockent
  l'API.

## Démarrage en développement

Lancer le backend puis le frontend dans deux terminaux :

```bash
# terminal 1 — backend, port 8080
cd /home/bung@france.groupe.intra/IdeaProjects/vending-app
./gradlew :backend:infrastructure:bootRun --args='--spring.profiles.active=dev'

# terminal 2 — frontend, port 4200
cd frontend
npm start
```

`npm start` lance `ng serve` avec `proxy.conf.json`, qui relaie toutes les
requêtes `/api/**` vers `http://localhost:8080` (évite le CORS en dev). En
production, le frontend est servi derrière le même hôte que l'API : les deux
partagent la même base de chemin `/api/v1`, définie dans
`src/environments/environment.ts`.

## Commandes

```bash
npm start        # serveur de dev, http://localhost:4200
npm run build    # build de production dans dist/
npm test         # tests unitaires Vitest
npm run e2e      # tests end-to-end Playwright (API mockée, aucun backend requis)
```

## Périmètre actuel

- **Authentification** : login, inscription, jeton d'accès en mémoire
  uniquement (jamais persisté), refresh token en cookie `httpOnly` géré par le
  backend (rotation + révocation côté serveur), renouvellement automatique sur
  401, protection CSRF (`XSRF-TOKEN`/`X-XSRF-TOKEN`), déconnexion. Garde de
  route `adminGuard` sur les pages réservées aux admins.
- **Distributeurs** : liste paginée (table Material + paginator), création
  (admin), détail avec consultation du stock et commande d'un article
  (utilisateur connecté avec le rôle `ROLE_USER`).
- **Articles** : liste paginée en lecture seule (admin).

**Reste à faire** (backend déjà exposé, frontend absent) :
- CRUD complet articles (création/édition/suppression — seule la liste
  existe) et gestion des images d'article
- Édition/suppression d'un distributeur (seules la liste et la création
  existent)
- Rapports admin : statut des distributeurs, stock, commandes clients
- Profil utilisateur (consultation/édition, changement de mot de passe)
- Layouts séparés public/admin, navigation admin dédiée
- i18n

## Tests

- **Unitaires (Vitest)** : un test par unité non triviale
  (`src/**/*.spec.ts`). Toujours lancer via `npm test` (jamais
  `npx vitest run` directement — voir `AGENTS.md`).
- **End-to-end (Playwright)** : `e2e/login-and-browse-machines.spec.ts` mocke
  l'API via `page.route()` et ne nécessite ni PostgreSQL ni backend démarré.
