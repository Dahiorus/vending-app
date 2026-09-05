# vending-app

Application de gestion de distributeurs automatiques (vending machines) :
gestion des articles, du stock, des commandes clients et de
l'administration, exposée via une API REST Spring Boot et consommée par
une SPA Angular.

## Architecture

Un seul projet Gradle (Kotlin DSL) multi-module englobe le dépôt entier :

```
backend/
  domain/            cœur métier (architecture hexagonale / DDD), aucune
                      dépendance vers Spring, JPA ou Jackson
  application/        implémentations des cas d'usage (ApplicationService)
  infrastructure/     adaptateurs (REST, JPA, sécurité), point d'entrée
                       Spring Boot
frontend/              SPA Angular, wrappée en projet Gradle qui délègue
                      à npm (aucun plugin Angular/Node Gradle)
```

Dépendances entre modules Java : `infrastructure` → `application` (au
runtime seulement) → `domain`. `domain` ne dépend d'aucun des deux autres.

Voir `AGENTS.md` (racine et par module) pour le détail des conventions.

## Stack technique

- **Backend** : Java 21, Spring Boot 3.5, Spring Data JPA, Spring Security
  (OAuth2 resource server / JWT), Spring HATEOAS, springdoc-openapi,
  Flyway, PostgreSQL, Ehcache.
- **Frontend** : Angular 22, Angular Material, TailwindCSS, RxJS,
  Playwright (tests e2e), Vitest.
- **Build** : Gradle (wrapper `./gradlew`), npm côté frontend.

## Prérequis

- JDK 21
- Node.js / npm (voir `frontend/package.json`, champ `volta` : Node 24,
  npm 11)
- Versions outillées via [mise](https://mise.jdx.dev/) (`mise.toml` à la
  racine) : lancer `mise install` pour obtenir les versions exactes de
  Java, Node et npm utilisées par le projet

  ```bash
  mise install && mise trust
  ```
- Une instance PostgreSQL locale (le dépôt ne fournit pas de
  docker-compose) : base `vending-app`, accessible sur
  `jdbc:postgresql://localhost:5432/vending-app` (voir
  `backend/infrastructure/src/main/resources/application-dev.properties`)

## Lancer le projet en développement

```bash
./dev.sh          # backend (port 8080, profil dev) + frontend (port 4200)
./dev.sh -d       # idem, avec le debug distant du backend sur le port 5005
```

Le frontend proxie `/api` vers le backend. Le backend nécessite que
PostgreSQL tourne déjà en local.

## Commandes de build

Toutes les commandes Gradle s'exécutent depuis la racine du dépôt :

```bash
./gradlew build                              # backend (compile + tests) + frontend (npm build + npm test)
./gradlew test                               # tests unitaires backend uniquement
./gradlew :backend:infrastructure:intTest    # tests d'intégration backend (*IT)
./gradlew clean build                        # build complet, critère de non-régression (140 tests backend)
```

Côté frontend, le workflow quotidien passe par `npm` directement (voir
`frontend/README.md`) :

```bash
cd frontend
npm start   # serveur de dev (ng serve)
npm run build
npm test    # tests unitaires (Vitest)
npm run e2e # tests e2e (Playwright)
```

## Documentation complémentaire

- `AGENTS.md` (racine et par module) : conventions de code, de nommage et
  d'architecture à respecter.
- `docs/` : documentation fonctionnelle complémentaire (ex.
  `features-front-a-implementer.md`).
- Documentation de l'API : `springdoc-openapi` / Swagger UI, exposée par
  le module `backend/infrastructure` une fois l'application démarrée.
