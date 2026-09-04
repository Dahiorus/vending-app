# AGENTS.md — vending-app

Ce fichier documente les conventions du projet pour tout agent (ou humain)
qui contribue au code. Chaque module a en plus son propre `AGENTS.md` qui
précise les règles locales ; en cas de conflit, le fichier du module le
plus proche du code modifié prévaut.

## Vue d'ensemble

Deux projets côte à côte à la racine du dépôt, chacun avec son propre
`AGENTS.md` détaillant ses règles locales :

```
backend/            projet multi-module Gradle (Kotlin DSL), architecture
                     hexagonale / DDD, Spring Boot 3.4.0, Java 21
  domain/            cœur métier, aucune dépendance externe
  application/       implémentations des cas d'usage (ApplicationService)
  infrastructure/    adaptateurs (REST, JPA, sécurité), point d'entrée
                     Spring Boot
frontend/            SPA Angular (Node/npm, hors build Gradle)
```

Dépendances entre modules Gradle : `infrastructure` → `application` (au
runtime seulement, voir `backend/infrastructure/AGENTS.md`) → `domain`.
`domain` ne dépend d'aucun des deux autres.

Le module `frontend/` a son propre `AGENTS.md` et son propre workflow : ses
commandes passent par `npm` (voir `frontend/README.md`), pas par Gradle. Le
compte de 140 tests Gradle mentionné plus bas reste le garde-fou de
non-régression du backend uniquement ; les tests du frontend (`npm test`,
`npm run e2e`) s'exécutent et se vérifient séparément.

## Commandes

Toutes les commandes Gradle s'exécutent depuis `backend/` :

```bash
cd backend
./gradlew build            # compile + tests unitaires (domain, application, infrastructure)
./gradlew test              # tests unitaires uniquement
./gradlew :infrastructure:intTest   # tests d'intégration (*IT)
./gradlew clean build       # build complet, critère de non-régression (140 tests)
```

Le nombre de tests exécutés (140 = 68 `domain` + 72 `infrastructure`) est le
garde-fou de non-régression à vérifier après toute modification.

## Conventions de nommage

- Les mixins CRUD génériques du domaine sont adjectivaux :
  `Creatable`, `Findable`, `Updatable`, `Deletable`, `Searchable`.
- Les adaptateurs hexagonaux implémentant un `RepositoryPort` s'appellent
  `XxxRepositoryAdapter` ; les interfaces Spring Data brutes s'appellent
  `XxxJpaRepository`. (Historiquement inversé, corrigé — ne pas régresser.)
- Quand un `XxxRepositoryAdapter` n'a besoin que d'une seule opération JPA
  générique (typiquement `save()` pour un rapport en écriture seule), il
  est acceptable d'instancier directement `SimpleJpaRepository` avec l'
  `EntityManager` plutôt que de déclarer une interface `XxxJpaRepository`
  complète qui exposerait inutilement tout le CRUD Spring Data (décision
  actée : `VendingMachineStatusReportRepositoryAdapter` et
  `VendingMachineStockReportRepositoryAdapter`). Ne pas signaler ce
  pattern comme de la sur-ingénierie.
- Les DTO ont deux conventions distinctes et volontaires, à ne pas unifier :
  - `XxxToCreateDto` / `XxxToUpdateDto` : miroir direct d'une commande CRUD
    du domaine (`XxxToCreate`, `XxxToUpdate`).
  - `XxxRequestDto` / `XxxResponseDto` : endpoints d'action sans équivalent
    CRUD direct (login, refresh de token, changement de mot de passe).
- Un service applicatif ne s'appelle `XxxApplicationService` que s'il
  implémente un `ApiPort` du domaine et orchestre/valide plusieurs ports
  ou porte une frontière `@Transactional` explicite. Un pass-through pur
  1:1 vers un `RepositoryPort`, sans logique ni frontière transactionnelle
  à apporter, ne doit pas être enveloppé artificiellement (décision actée :
  `AuthenticationRestController` et `CreateDevEnvironmentAdmin` injectent
  volontairement leur `RepositoryPort` directement).
- À l'inverse, un `XxxApplicationService` qui délègue à un seul use case ou
  port mais porte l'annotation `@Transactional` n'est PAS un pass-through à
  supprimer : le module `domain` n'a aucune dépendance Spring et ne peut
  donc pas porter lui-même cette frontière transactionnelle. Dès qu'un use
  case ou un adaptateur enchaîne plusieurs opérations JPA nécessitant une
  atomicité (lecture(s) + écriture(s) sur plusieurs `RepositoryPort` ou
  plusieurs appels JPA dans un même adaptateur), l'`ApplicationService`
  `@Transactional` est le seul endroit possible pour garantir cette
  atomicité (décision actée : `OrderItemApplicationService`,
  `VendingMachineStockReportApplicationService`,
  `VendingMachineClientOrdersReportApplicationService`,
  `ItemImageApplicationService`). Ne pas signaler ces classes comme de la
  sur-ingénierie lors d'une revue ou d'un audit.
- Un port du domaine (ex. `PasswordPolicy`) à une seule implémentation dans
  `infrastructure` n'est pas non plus une abstraction spéculative à
  supprimer dès lors que cette implémentation dépend d'un mécanisme Spring
  (ex. `PasswordPolicyProperties` avec `@ConfigurationProperties`) que
  `domain` ne peut pas référencer lui-même. Le port est ici la seule façon
  de garder `domain` exempt de toute dépendance Spring/JPA/Jackson tout en
  permettant l'injection de configuration côté infrastructure — ce n'est
  pas un YAGNI, c'est le patron port/adaptateur hexagonal standard.

## Workflow git

- Une branche dédiée par sujet de correction/évolution, créée depuis
  `develop`.
- Un commit (ou une suite de commits logiques) par branche, avec le
  trailer `Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>`
  si le travail a été assisté par l'agent.
- Vérification du build (`cd backend && ./gradlew clean build`, 140 tests)
  avant chaque commit.
- Fusion dans `develop` en fast-forward (`git merge --ff-only`, rebase au
  besoin) — jamais de commit de merge.
- Suppression de la branche locale après fusion.

## Points d'architecture actés (ne pas ré-ouvrir sans nouvelle analyse)

- Le module `domain` ne doit avoir aucune dépendance vers Spring, JPA ou
  Jackson — c'est un invariant vérifié à chaque revue.
- L'accès direct à un `RepositoryPort` depuis `infrastructure` (au lieu de
  passer par un `ApplicationService`) est acceptable quand il n'y a ni
  logique métier ni frontière transactionnelle à encapsuler.
