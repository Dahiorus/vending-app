# Instructions Copilot — vending-app

Ce fichier synthétise les conventions du dépôt pour toute suggestion ou
génération de code par GitHub Copilot. Le détail complet et les décisions
argumentées vivent dans `AGENTS.md` (racine) et dans le `AGENTS.md` propre
à chaque module — s'y référer en cas de doute, et privilégier le `AGENTS.md`
le plus proche du code modifié en cas de conflit apparent.

## Architecture

Projet Gradle multi-module (Kotlin DSL) unique pour tout le dépôt :

```
backend/
  domain/            cœur métier, architecture hexagonale/DDD — ZÉRO dépendance
                     Spring/JPA/Jackson (JDK + code du package domain uniquement)
  application/       implémentations des cas d'usage (XxxApplicationService)
  infrastructure/    adaptateurs REST/JPA/sécurité, point d'entrée Spring Boot
frontend/            SPA Angular 22, wrappée en projet Gradle (délègue à npm,
                     aucun plugin Angular/Node Gradle)
```

Dépendances Java : `infrastructure` → `application` (**runtimeOnly**, pas
`implementation` : `infrastructure` ne compile jamais contre une classe
concrète d'`application`, seulement contre `domain`) → `domain`. `domain` ne
dépend d'aucun des deux autres.

## Commandes

```bash
./gradlew build                             # backend (compile+tests) + frontend (npm build+test)
./gradlew test                              # tests unitaires backend uniquement
./gradlew :backend:infrastructure:intTest   # tests d'intégration (*IT)
./gradlew clean build                       # build complet — critère de non-régression (140 tests)
./gradlew :backend:infrastructure:bootRun --args='--spring.profiles.active=dev'  # backend, :8080
cd frontend && npm start                    # frontend, :4200 (proxy /api -> :8080)
```

140 tests (68 `domain` + 72 `infrastructure`) = garde-fou de non-régression
backend à vérifier après toute modification. Le script `./dev.sh` à la
racine lance backend et frontend ensemble (PostgreSQL doit déjà tourner en
local, aucun docker-compose fourni ici).

## Conventions de nommage transverses

- Mixins CRUD génériques du domaine : adjectivaux — `Creatable`,
  `Findable`, `Updatable`, `Deletable`, `Searchable` (jamais de suffixe
  `Spi`).
- Adaptateurs hexagonaux implémentant un `RepositoryPort` :
  `XxxRepositoryAdapter`. Interfaces Spring Data brutes : `XxxJpaRepository`.
  Ne pas inverser.
- DTO : deux conventions distinctes, à ne pas unifier —
  - `XxxToCreateDto`/`XxxToUpdateDto` : miroir direct d'une commande CRUD du
    domaine (`XxxToCreate`/`XxxToUpdate`).
  - `XxxRequestDto`/`XxxResponseDto` : endpoints d'action sans équivalent
    CRUD direct (login, refresh, changement de mot de passe).
- Assemblers HATEOAS : suffixe `XxxDtoModelAssembler` (jamais `ModelAssembler`).

## Points d'architecture actés (ne pas ré-ouvrir sans nouvelle analyse)

- Un `XxxApplicationService` ne s'appelle ainsi que s'il implémente un
  `ApiPort` du domaine ET apporte une plus-value (logique métier,
  orchestration de plusieurs ports, ou frontière `@Transactional`
  explicite — le seul endroit possible pour garantir l'atomicité
  d'opérations JPA multiples, `domain` n'ayant aucune dépendance Spring).
  Un pur pass-through 1:1 vers un `RepositoryPort` ne doit pas être
  enveloppé artificiellement (`AuthenticationRestController`,
  `CreateDevEnvironmentAdmin` injectent volontairement leur `RepositoryPort`
  directement) — mais un service `@Transactional` existant n'est pas un
  pass-through à supprimer pour autant.
- `SimpleJpaRepository` instancié directement (sans interface
  `XxxJpaRepository` complète) est acceptable pour un adaptateur n'ayant
  besoin que d'une seule opération générique (typiquement `save()` pour un
  rapport en écriture seule) — ne pas signaler ce pattern comme de la
  sur-ingénierie.
- Un port du domaine à une seule implémentation `infrastructure` n'est pas
  une abstraction spéculative dès lors que cette implémentation dépend d'un
  mécanisme Spring que `domain` ne peut référencer (ex. `PasswordPolicy` /
  `PasswordPolicyProperties`) : c'est le patron port/adaptateur standard.
- Sécurité : serveur de ressources OAuth2 Spring Security
  (`oauth2ResourceServer(jwt(...))`), pas de filtre JWT maison. Le claim
  `roles` porte déjà les autorités complètes (préfixe vide) — ne pas
  rajouter `ROLE_`. `/oauth2/jwks` n'expose jamais la clé privée. Le
  refresh token n'est pas encore en rotation côté backend (le frontend ne
  doit pas le supposer).

## Backend (Java 21, Spring Boot 3.4.0)

- `domain` : voir `backend/domain/AGENTS.md` — structure par sous-domaine
  (`entity/`, `port/`, `usecase/`), exceptions métier sans concept
  d'infrastructure, fixtures partagées via `testFixtures`.
- `application` : voir `backend/application/AGENTS.md` — services rangés
  par sous-domaine sous `service/<sous-domaine>/`, jamais à plat.
- `infrastructure` : voir `backend/infrastructure/AGENTS.md` — JPA sous
  `jpa/repository/<sous-domaine>/`, REST sous `rest/{controller,assembler,
  entity,exception}/`.

## Frontend (Angular 22, standalone, zoneless, sans SSR)

Voir `frontend/AGENTS.md` pour le détail complet. Points clés :

- Utiliser le skill `angular-developer` et Angular CLI (`ng generate`) pour
  toute nouvelle feature plutôt que créer les fichiers à la main.
- Nommage 2025 (`login.ts`, pas `login.component.ts`).
- Services : `@Service()` (pas `@Injectable({ providedIn: 'root' })`).
- Lecture : `httpResource()`. Mutations (POST/PUT/DELETE) : `HttpClient`.
- Organisation par feature (pas de dossier `api/` générique) ; `core/` pour
  le transverse app-wide, `shared/` pour l'utilitaire réutilisable.
- Formulaires : Signal Forms (`@angular/forms/signals`).
- Access token en mémoire uniquement (jamais persisté), refresh token en
  `sessionStorage`. Jamais de jeton en URL, `innerHTML`, `bypassSecurityTrust*`.
- Tests : toujours `npm test` (jamais `npx vitest run` directement) ; avec
  `httpResource()`, flush la requête HTTP avant `whenStable()`.

## Workflow git

- Une branche dédiée par sujet, créée depuis `develop`.
- Un commit (ou suite de commits logiques) par branche, trailer
  `Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>` si
  assisté par l'agent.
- Vérifier `./gradlew clean build` (140 tests) avant chaque commit.
- Fusion en fast-forward uniquement (`git merge --ff-only`), jamais de
  commit de merge. Supprimer la branche locale après fusion.
