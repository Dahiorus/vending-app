# AGENTS.md — infrastructure

Adaptateurs (REST, JPA, sécurité) et point d'entrée Spring Boot. Règles
spécifiques à ce module (en plus de `AGENTS.md` à la racine) :

## Contrainte de compilation critique : `runtimeOnly(project(":application"))`

`build.gradle.kts` déclare la dépendance vers `application` en
`runtimeOnly`, pas en `implementation`. Conséquence directe : le code de
`infrastructure` **ne peut pas compiler** contre une classe concrète
d'`application` (ex. `XxxApplicationService`) — seul le code de `domain`
(entités, `ApiPort`, `RepositoryPort`) est visible à la compilation. Les
implémentations concrètes ne sont résolues qu'au runtime via le
component-scan Spring.

C'est la raison structurelle pour laquelle **tout** contrôleur REST ou
toute commande dépend d'une interface `ApiPort` (ou, à défaut, directement
d'un `RepositoryPort` quand aucun `ApiPort` n'apporte de valeur — voir
`AGENTS.md` racine) plutôt que d'un `ApplicationService` concret. Ne pas
essayer de câbler une dépendance directe vers une classe concrète
d'`application` : ça ne compilera pas, et changer cette configuration en
`implementation` est un choix architectural à part entière, pas un simple
correctif ponctuel.

## Nommage de la couche JPA

```
jpa/repository/<sous-domaine>/
  XxxJpaRepository        interface Spring Data brute (JpaRepository<Entity, Id>)
  XxxRepositoryAdapter     implémente le RepositoryPort du domaine, utilise XxxJpaRepository
```

Chaque sous-domaine (`item`, `machine`, `order`, `user`) a son propre
sous-package. Ne pas revenir à l'ancien nommage inversé
(`JpaXxxDao` / `XxxJpaRepository` pour l'adaptateur).

## Structure REST

```
rest/
  controller/<sous-domaine>/   contrôleurs REST
  assembler/                   XxxDtoModelAssembler (HATEOAS), un par ressource exposée
  entity/<sous-domaine>/       DTOs (Request/Response ou ToCreate/ToUpdate, cf. AGENTS.md racine)
  exception/                   RestResponseExceptionHandler + exceptions HTTP dédiées
```

Les assemblers portent tous le suffixe `DtoModelAssembler` (et non
`ModelAssembler`) pour lever toute ambiguïté sur ce qu'ils assemblent.

## Sécurité

`security/filter/JwtAuthorizationFilter` valide le token bearer sur les
requêtes déjà authentifiées ; ne pas le confondre avec
`JwtAuthenticationFilter` qui gère le login. Respecter cette distinction de
responsabilité si un nouveau filtre est ajouté.

## Runners de commande

`command/CreateDevEnvironmentAdmin` (profil `dev` uniquement) doit rester
best-effort et ne jamais faire échouer le démarrage de l'application ; en
cas d'ajustement du bloc `catch`, conserver la cause complète dans le log
(`logger.error("...", e)`), pas seulement `e.getMessage()`.
