# AGENTS.md — domain

Cœur métier de l'application. Règles spécifiques à ce module (en plus de
`AGENTS.md` à la racine) :

## Invariant : zéro dépendance externe

Ce module ne doit importer **aucune** classe Spring, JPA/Jakarta Persistence
ou Jackson. Seuls le JDK et le code de `me.dahiorus.project.vending.domain`
sont autorisés dans `src/main`. C'est vérifié à chaque revue d'architecture ;
toute nouvelle dépendance externe dans `domain` est un signal fort qu'un
concept a été mal placé (probablement un détail d'infrastructure qui doit
rester dans `infrastructure`, ou une annotation de validation Bean qui doit
vivre dans son propre mécanisme de `validation`).

## Structure par sous-domaine

Chaque sous-domaine (`item`, `machine`, `user`, `stock`, `reporting`, ...)
suit la même disposition :

```
<sous-domaine>/
  entity/     objets métier (records/classes immuables, value objects)
  port/       interfaces ApiPort (cas d'usage) + RepositoryPort (persistance)
  usecase/    logique métier non triviale, orchestrée par un ApplicationService
```

- `ApiPort` = port d'entrée (ce qu'expose le domaine à l'infrastructure via
  l'`ApplicationService` qui l'implémente).
- `RepositoryPort` = port de sortie (ce que le domaine attend de la
  persistance), implémenté par un `XxxRepositoryAdapter` dans
  `infrastructure`.
- Ne pas créer d'`ApiPort` qui ne fait que dupliquer un `RepositoryPort`
  1:1 sans intention métier propre (cf. `AGENTS.md` racine, section sur les
  services pass-through).

## Nommage des mixins CRUD

Les interfaces génériques réutilisables entre sous-domaines sont
adjectivales : `Creatable<T, R>`, `Findable<Id, R>`, `Updatable<...>`,
`Deletable<Id>`, `Searchable<...>`. Ne pas revenir à un suffixe `Spi`.

## Exceptions métier

Les exceptions du domaine (`domain/exception/`) représentent des règles
métier violées (`ResourceNotFound`, `InvalidBusinessObject`,
`ItemStockIsEmpty`, `NotWorkingVendingMachine`, ...). Elles ne doivent
jamais référencer un concept d'infrastructure (HTTP, SQL, JSON). La
traduction vers un code HTTP se fait exclusivement dans
`infrastructure/rest/exception/RestResponseExceptionHandler`.

## Test fixtures

Les fixtures partagées (`src/testFixtures`) sont gérées par le plugin
Gradle `java-test-fixtures` et exposées aux autres modules via
`testFixtures(project(":backend:domain"))`. Ne pas dupliquer une fixture déjà
présente ici dans `application` ou `infrastructure`.
