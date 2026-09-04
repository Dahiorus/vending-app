# Pagination par curseur (keyset scrolling) — Design

## Contexte

Les listings REST (`GET /api/v1/items`, `GET /api/v1/vending-machines`)
utilisent aujourd'hui une pagination `OFFSET`/`LIMIT` (`Pagination`
`PageNumber`+`PageSize`). Un `OFFSET` profond oblige le moteur SQL à
parcourir/trier toutes les lignes précédentes avant de renvoyer la page
demandée : problème de performance en "deep pagination".

## Décisions

| Sujet | Décision |
|---|---|
| Périmètre | Tous les listings paginés (`Item`, `VendingMachine`) |
| Compatibilité | Remplacement complet — aucun client existant à ce jour |
| Total d'éléments | Supprimé (`Total`, `count()`, `totalElements()`) |
| Navigation | Bidirectionnelle : `next` et `prev` |
| Mécanisme | `ScrollPosition`/`Window` natifs de Spring Data JPA 3.5.13 (keyset scrolling), combiné au Query By Example déjà utilisé par les adaptateurs |

## Faits techniques vérifiés (sources Spring Data JPA 3.5.13)

- `SimpleJpaRepository.findBy(Example, queryFunction)` délègue à
  `FetchableFluentQueryBySpecification`, qui expose
  `.sortBy(Sort)`, `.limit(int)`, `.scroll(ScrollPosition)` retournant un
  `Window<T>`. Le Query By Example existant (`ExampleMatcherAdapter`) reste
  utilisable tel quel.
- `KeysetScrollSpecification.createSort` ajoute automatiquement les
  attributs d'identifiant de l'entité comme tie-breaker si le tri demandé
  ne les contient pas déjà (`domain/pagination` n'a pas à s'en soucier).
- `ScrollDelegate.scroll` exécute la requête avec `limit + 1` lignes et
  retourne un `Window<T>` : `hasNext()` (repositionnement dans le sens de
  la requête) et `positionAt(index)` (curseur vers un élément donné).
- `KeysetScrollPosition` expose `getKeys()` (`Map<String,Object>`),
  `getDirection()`, `forward()`, `backward()` : la construction du lien
  `prev` réutilise le même mécanisme que `next`, en position inverse.
- `KeysetScrollDelegate.createPredicate` lie les valeurs de curseur comme
  paramètres de requête typés selon l'attribut d'entité : **les valeurs
  décodées doivent conserver leur type Java d'origine** (un enum doit
  redevenir une instance d'enum, pas une chaîne), sous peine d'erreur de
  liaison Hibernate.
- `JpaEntity` (racine commune `JpaItem`/`JpaVendingMachine`) porte un `id`
  `UUID` et un `createdAt` `LocalDateTime` : le tri de repli
  (`createdAt DESC`) reste valide comme aujourd'hui.

## Conception

### Curseur opaque (`domain.pagination.entity.Cursor`)

```java
public record Cursor(String value) {
  public Cursor {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Cursor value must not be blank");
    }
  }
}
```

`domain` ne connaît que cette chaîne opaque. L'encodage/décodage vers un
`KeysetScrollPosition` Spring Data est **strictement confiné à
`infrastructure`**, préservant l'invariant zéro-dépendance de `domain`.

### Format du token (`infrastructure.jpa.repository.CursorCodec`)

Le token est un texte `Base64URL` (sans padding) d'un contenu texte
construit ainsi (pas de JSON, pas de Jackson à typage polymorphe — évite
toute faille de désérialisation) :

```
<DIRECTION>\n
<base64url(propName1)>,<TYPE_TAG1>,<base64url(valueAsString1)>[,<enumClassName1>]\n
<base64url(propName2)>,<TYPE_TAG2>,<base64url(valueAsString2)>[,<enumClassName2>]\n
...
```

- `DIRECTION` ∈ `{FORWARD, BACKWARD}`.
- `TYPE_TAG` ∈ allowlist fermée : `STRING`, `UUID`, `BOOLEAN`, `INTEGER`,
  `LONG`, `BIG_DECIMAL`, `LOCAL_DATE`, `LOCAL_DATE_TIME`, `INSTANT`, `ENUM`.
- Pour `ENUM`, un 4ᵉ champ porte le nom qualifié complet de la classe
  d'énumération ; le décodage n'autorise que les classes dont le nom
  commence par `me.dahiorus.project.vending.` (allowlist par préfixe de
  package) avant tout `Class.forName` + `Enum.valueOf`, ce qui exclut toute
  classe arbitraire.
- Chaque propriété/valeur individuelle est elle-même Base64URL-encodée
  pour éviter tout problème d'échappement de séparateur.
- Un token dont le format, le type, ou la classe d'énumération ne
  correspond pas à l'allowlist lève une `InvalidCursor` (nouvelle exception
  du domaine) → traduite en HTTP 400.

### `domain.pagination.entity.Pagination` / `PageResult` / `Searchable`

```java
public record Pagination(Cursor cursor, PageSize size, PageSort sort) {
  public Pagination() { this(null, new PageSize(20), new PageSort(Set.of())); }
  public Optional<Cursor> maybeCursor() { return Optional.ofNullable(cursor); }
}

public record PageResult<D>(List<D> content, Cursor next, Cursor previous) {
  public PageResult { content = List.copyOf(content); }
  public Optional<Cursor> maybeNext() { return Optional.ofNullable(next); }
  public Optional<Cursor> maybePrevious() { return Optional.ofNullable(previous); }
  public <S> PageResult<S> map(Function<D, S> mapper) {
    return new PageResult<>(content.stream().map(mapper).toList(), next, previous);
  }
}

public interface Searchable<D, S> {
  PageResult<D> search(Pagination pagination, Filter<S> filter);
}
```

`PageNumber` et `Total` sont supprimés. `Searchable.count(...)` est
supprimé.

### Règle de calcul `next` / `previous`

`Window<T>.hasNext()` signifie « il reste des éléments dans le sens de la
requête ». La traduction en curseurs `next`/`previous` est asymétrique :

| Requête | `next` | `previous` |
|---|---|---|
| Sans curseur (1ʳᵉ page) ou `FORWARD` | `positionAt(dernier élément).forward()` si `window.hasNext()`, sinon absent | absent si sans curseur ; sinon `positionAt(premier élément).backward()` |
| `BACKWARD` | `positionAt(dernier élément).forward()` (toujours présent : on vient nécessairement de l'avant) | `positionAt(premier élément).backward()` si `window.hasNext()`, sinon absent |

Spring Data réordonne déjà les résultats d'un scroll `BACKWARD` dans le
sens de tri demandé (`KeysetScrollDelegate` réversible) : le contenu
retourné est toujours dans l'ordre logique, quel que soit le sens de
navigation du client.

### Contrat REST

- Paramètres : `cursor` (opaque, optionnel), `size`, `sort` (syntaxe
  Spring inchangée, ex. `sort=name,asc`). Le paramètre `page` disparaît.
- Réponse : `CollectionModel<EntityModel<XxxDto>>` avec liens `self`,
  `next`, `prev` (paramètres de filtre/tri/taille de la requête courante
  reconduits, seul `cursor` change).
- `WebConfig` : `PageableHandlerMethodArgumentResolver` remplacé par
  `SortHandlerMethodArgumentResolver` (repli `createdAt DESC`).
- `PagedResourcesAssembler`/`PagedModel` ne sont plus utilisés dans les
  contrôleurs de listing.
- Curseur invalide (`InvalidCursor`) → `400 Bad Request` via
  `RestResponseExceptionHandler`.

## Fichiers impactés

Voir le plan d'implémentation associé :
`docs/superpowers/plans/2026-09-04-cursor-pagination.md`.

## Risques

| Risque | Mitigation |
|---|---|
| Keyset incompatible avec Query By Example sur H2 | Spike de faisabilité en première tâche du plan, avant tout refactoring |
| Valeur de curseur mal reconstruite (type erroné) | Allowlist de types fermée + tests d'aller-retour par type, y compris `ENUM` |
| Erreur de sens en navigation arrière | Tableau de règles explicite + test d'intégration « aller puis retour = mêmes pages » |
| Le garde-fou "140 tests" de `AGENTS.md` racine devient obsolète | Mise à jour explicite en dernière tâche du plan |
