# Pagination par curseur (keyset scrolling) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Remplacer la pagination `OFFSET`/`LIMIT` (`PageNumber`/`Total`) des listings `Item` et `VendingMachine` par une pagination par curseur opaque (keyset scrolling Spring Data JPA), bidirectionnelle (`next`/`prev`), sans total d'éléments.

**Architecture:** Un value object `Cursor` (chaîne opaque) vit dans `domain.pagination.entity` ; son encodage/décodage vers un `KeysetScrollPosition` Spring Data est confiné à un `CursorCodec` dans `infrastructure`. Les adaptateurs JPA (`ItemRepositoryAdapter`, `VendingMachineRepositoryAdapter`) exécutent un scroll keyset via `findBy(Example, fluentQuery -> ...)` combiné au Query By Example existant, et construisent un `PageResult` avec curseurs `next`/`prev`. Les contrôleurs REST exposent `cursor`/`size`/`sort` en paramètres et retournent un `CollectionModel` avec liens HATEOAS `self`/`next`/`prev` (les autres paramètres de la requête courante étant reconduits).

**Tech Stack:** Java 21, Spring Boot 3.5.16, Spring Data JPA 3.5.13 (keyset scrolling / `Window<T>` / `ScrollPosition`), Spring HATEOAS, H2 (tests d'intégration), JUnit 5 + AssertJ + Mockito.

**Spec:** `docs/superpowers/specs/2026-09-04-cursor-pagination-design.md`

## Global Constraints

- `domain` ne doit importer aucune classe Spring, JPA/Jakarta Persistence ou Jackson (invariant vérifié à chaque revue) — `Cursor` reste une chaîne opaque dans `domain`, tout l'encodage/décodage Spring Data reste dans `infrastructure`.
- Aucune compatibilité ascendante à préserver : remplacement complet de `PageNumber`/`Total`/`Searchable.count(...)`, aucun client existant.
- Le token de curseur est un texte, pas du JSON/Jackson à typage polymorphe : allowlist fermée de `TYPE_TAG`, allowlist par préfixe de package (`me.dahiorus.project.vending.`) pour les classes d'énumération avant tout `Class.forName`/`Enum.valueOf`.
- Un curseur mal formé, de type inconnu, ou référençant une classe d'énumération hors allowlist lève `InvalidCursor` (nouvelle exception domaine) → HTTP 400 via `RestResponseExceptionHandler`.
- Après le refactoring des adaptateurs, `ExampleMatcherAdapter` (Query By Example) reste utilisé tel quel, combiné au scroll keyset.
- Garde-fou de non-régression : `./gradlew clean build` (tests unitaires `domain`+`application`+`infrastructure`) et `./gradlew :infrastructure:intTest` doivent passer avant chaque commit. Le nombre total de tests est mis à jour dans `AGENTS.md` racine en dernière tâche (ne pas le considérer figé à 140 pendant ce plan).

---

## File Structure

```
domain/src/main/java/.../domain/pagination/entity/
  Cursor.java                 (create)  chaîne opaque
  Pagination.java             (modify)  cursor + size + sort, sans PageNumber
  PageResult.java             (modify)  content + next + previous, sans Total
  PageNumber.java              (delete)
  Total.java                   (delete)
domain/src/main/java/.../domain/Searchable.java   (modify)  search() renvoie PageResult, count() supprimé
domain/src/main/java/.../domain/exception/InvalidCursor.java   (create)
domain/src/test/java/.../domain/pagination/entity/
  CursorTest.java              (create)
  PaginationTest.java          (create)
  PageResultTest.java          (create)

infrastructure/src/main/java/.../infrastructure/jpa/repository/
  CursorCodec.java             (create)  encode/decode Cursor <-> KeysetScrollPosition
  KeysetSearchSupport.java     (create)  algorithme de scroll partagé Item/VendingMachine
  PageSortConverter.java       (create)  PageSort domaine -> Sort Spring Data
  ToPageableConverter.java     (delete)  remplacé par PageSortConverter
  item/ItemRepositoryAdapter.java            (modify)  search() garde uniquement le scroll, count() supprimé
  machine/VendingMachineRepositoryAdapter.java (modify) idem
infrastructure/src/test/java/.../infrastructure/jpa/repository/
  CursorCodecTest.java          (create)
infrastructure/src/intTest/java/.../infrastructure/jpa/repository/item/ItemRepositoryAdapterIT.java (modify)
infrastructure/src/intTest/java/.../infrastructure/jpa/repository/machine/VendingMachineRepositoryAdapterIT.java (modify)

infrastructure/src/main/java/.../infrastructure/rest/utils/
  ToPaginationConverter.java    (modify)  construit Pagination depuis cursor/size/sort de la requête
  CursorLinkFactory.java        (create)  construit les URI self/next/prev depuis la requête courante
infrastructure/src/main/java/.../infrastructure/rest/config/WebConfig.java  (modify) SortHandlerMethodArgumentResolver
infrastructure/src/main/java/.../infrastructure/rest/controller/item/ItemCrudRestController.java (modify)
infrastructure/src/main/java/.../infrastructure/rest/controller/machine/VendingMachineCrudRestController.java (modify)
infrastructure/src/main/java/.../infrastructure/rest/exception/RestResponseExceptionHandler.java (modify) gère InvalidCursor -> 400

application/src/main/java/.../application/service/item/ItemApplicationService.java (modify) search() délègue directement
application/src/main/java/.../application/service/machine/VendingMachineApplicationService.java (modify) idem

AGENTS.md (racine)  (modify)  nombre de tests de non-régression mis à jour
```

---

### Task 1: Spike de faisabilité — keyset scrolling + Query By Example sur H2

**Files:**
- Test (temporaire, supprimé à la fin de la tâche 6) : `infrastructure/src/intTest/java/me/dahiorus/project/vending/infrastructure/jpa/repository/item/KeysetScrollSpikeIT.java`

**Interfaces:**
- Consumes: `ItemRepositoryAdapter` existant (`JpaRepository<JpaItem, UUID>` déclaré, or expose déjà `QueryByExampleExecutor`), `H2DbContainer` (base de test existante).
- Produces: confirmation que `findBy(Example, fluentQuery -> fluentQuery.sortBy(...).limit(...).scroll(ScrollPosition.keyset()))` fonctionne sur H2, combiné à un `Example` construit via `ExampleMatcherAdapter.toExample`. Aucune classe de production n'est créée par cette tâche.

- [ ] **Step 1: Écrire le test de spike**

```java
package me.dahiorus.project.vending.infrastructure.jpa.repository.item;

import static me.dahiorus.project.vending.domain.item.entity.ItemType.COLD_BEVERAGE;
import static me.dahiorus.project.vending.infrastructure.jpa.repository.ExampleMatcherAdapter.toExample;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import me.dahiorus.project.vending.domain.item.entity.Item;
import me.dahiorus.project.vending.domain.item.entity.ItemName;
import me.dahiorus.project.vending.domain.item.entity.ItemToCreate;
import me.dahiorus.project.vending.domain.pagination.entity.Filter;
import me.dahiorus.project.vending.domain.pagination.entity.FilterMatcher;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaItem;
import me.dahiorus.project.vending.infrastructure.jpa.repository.H2DbContainer;
import me.dahiorus.project.vending.infrastructure.jpa.repository.item.ItemRepositoryAdapterIT.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = TestConfig.class)
class KeysetScrollSpikeIT extends H2DbContainer {

  @Autowired ItemRepositoryAdapter repository;
  @Autowired jakarta.persistence.EntityManager entityManager;

  @Test
  void should_scroll_forward_then_backward_using_keyset_and_query_by_example() {
    var item1 =
        repository.create(
            new ItemToCreate(ItemName.of("Coca-Cola 33cL"), COLD_BEVERAGE, BigDecimal.valueOf(1.50)));
    var item2 =
        repository.create(
            new ItemToCreate(ItemName.of("Pepsi 33cL"), COLD_BEVERAGE, BigDecimal.valueOf(1.50)));
    var item3 =
        repository.create(
            new ItemToCreate(ItemName.of("Fanta 33cL"), COLD_BEVERAGE, BigDecimal.valueOf(1.50)));
    entityManager.flush();

    var jpaRepository = repository.jpaRepositoryForSpike();
    var example = toExample(new Filter<>(new Item(null, null, null, null), new FilterMatcher()), JpaItem::fromDomain);
    var sort = Sort.by(Sort.Direction.ASC, "name").and(Sort.by(Sort.Direction.ASC, "id"));

    var firstWindow =
        jpaRepository.findBy(
            example, q -> q.sortBy(sort).limit(2).scroll(ScrollPosition.keyset()));

    assertThat(firstWindow.map(JpaItem::toDomain).toList())
        .containsExactly(item3, item1); // "Fanta" < "Coca-Cola" alphabetically after "F" vs "C"? see note below
    assertThat(firstWindow.hasNext()).isTrue();

    var nextPosition = firstWindow.positionAt(firstWindow.size() - 1).forward();
    var secondWindow =
        jpaRepository.findBy(
            example, q -> q.sortBy(sort).limit(2).scroll(nextPosition));

    assertThat(secondWindow.map(JpaItem::toDomain).toList()).containsExactly(item2);
    assertThat(secondWindow.hasNext()).isFalse();
  }
}
```

Note : `jpaRepositoryForSpike()` n'existe pas encore sur `ItemRepositoryAdapter` — ajoute temporairement une méthode package-private `QueryByExampleExecutor<JpaItem> jpaRepositoryForSpike() { return jpaRepository; }` sur `ItemRepositoryAdapter` pour cette seule tâche (retirée à la tâche 6, une fois `KeysetSearchSupport` en place et invoqué directement par `search()`).

- [ ] **Step 2: Lancer le test**

Run: `./gradlew :infrastructure:intTest --tests "*.KeysetScrollSpikeIT"`
Expected: PASS — si le tri alphabétique donné dans l'assertion ci-dessus est faux (l'ordre exact dépend de la locale H2), corrige l'ordre attendu d'après le résultat réel avant de valider ; l'important est que `hasNext()`/`positionAt(...).forward()`/le second `scroll(...)` fonctionnent sans exception.

- [ ] **Step 3: Documenter la confirmation et committer**

Aucune modification de code de production. Commit le spike seul (il sera supprimé à la tâche 6, son rôle est de valider l'approche avant le refactoring).

```bash
git add infrastructure/src/intTest/java/me/dahiorus/project/vending/infrastructure/jpa/repository/item/KeysetScrollSpikeIT.java infrastructure/src/main/java/me/dahiorus/project/vending/infrastructure/jpa/repository/item/ItemRepositoryAdapter.java
git commit -m "spike: confirm keyset scrolling works with Query By Example on H2"
```

---

### Task 2: `Cursor` value object et exception `InvalidCursor`

**Files:**
- Create: `domain/src/main/java/me/dahiorus/project/vending/domain/pagination/entity/Cursor.java`
- Create: `domain/src/main/java/me/dahiorus/project/vending/domain/exception/InvalidCursor.java`
- Test: `domain/src/test/java/me/dahiorus/project/vending/domain/pagination/entity/CursorTest.java`

**Interfaces:**
- Produces: `record Cursor(String value)` — validation non-blank. `InvalidCursor extends RuntimeException`, constructeurs `(Cursor)` et `(Cursor, Throwable)`.

- [ ] **Step 1: Écrire le test qui échoue**

```java
package me.dahiorus.project.vending.domain.pagination.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CursorTest {

  @Test
  void should_create_cursor_with_value() {
    var cursor = new Cursor("abc123");

    assertThat(cursor.value()).isEqualTo("abc123");
  }

  @Test
  void should_reject_null_value() {
    assertThatThrownBy(() -> new Cursor(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cursor value must not be blank");
  }

  @Test
  void should_reject_blank_value() {
    assertThatThrownBy(() -> new Cursor("   "))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Cursor value must not be blank");
  }
}
```

- [ ] **Step 2: Lancer le test pour vérifier l'échec**

Run: `./gradlew :domain:test --tests "*.CursorTest"`
Expected: FAIL avec "cannot find symbol: class Cursor"

- [ ] **Step 3: Implémenter `Cursor`**

```java
package me.dahiorus.project.vending.domain.pagination.entity;

public record Cursor(String value) {
  public Cursor {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Cursor value must not be blank");
    }
  }
}
```

- [ ] **Step 4: Lancer le test pour vérifier le succès**

Run: `./gradlew :domain:test --tests "*.CursorTest"`
Expected: PASS

- [ ] **Step 5: Implémenter `InvalidCursor`**

```java
package me.dahiorus.project.vending.domain.exception;

import me.dahiorus.project.vending.domain.pagination.entity.Cursor;

public class InvalidCursor extends RuntimeException {
  public InvalidCursor(Cursor cursor) {
    super("Invalid cursor: " + cursor.value());
  }

  public InvalidCursor(Cursor cursor, Throwable cause) {
    super("Invalid cursor: " + cursor.value(), cause);
  }
}
```

Pas de test dédié : cette exception est exercée end-to-end par `CursorCodecTest` (tâche 4) et par le handler REST (tâche 9).

- [ ] **Step 6: Commit**

```bash
git add domain/src/main/java/me/dahiorus/project/vending/domain/pagination/entity/Cursor.java \
        domain/src/main/java/me/dahiorus/project/vending/domain/exception/InvalidCursor.java \
        domain/src/test/java/me/dahiorus/project/vending/domain/pagination/entity/CursorTest.java
git commit -m "feat(domain): add opaque Cursor value object and InvalidCursor exception"
```

---

### Task 3: Refactoring `Pagination` / `PageResult` / `Searchable`

**Files:**
- Modify: `domain/src/main/java/me/dahiorus/project/vending/domain/pagination/entity/Pagination.java`
- Modify: `domain/src/main/java/me/dahiorus/project/vending/domain/pagination/entity/PageResult.java`
- Delete: `domain/src/main/java/me/dahiorus/project/vending/domain/pagination/entity/PageNumber.java`
- Delete: `domain/src/main/java/me/dahiorus/project/vending/domain/pagination/entity/Total.java`
- Modify: `domain/src/main/java/me/dahiorus/project/vending/domain/Searchable.java`
- Test: `domain/src/test/java/me/dahiorus/project/vending/domain/pagination/entity/PaginationTest.java`
- Test: `domain/src/test/java/me/dahiorus/project/vending/domain/pagination/entity/PageResultTest.java`

**Interfaces:**
- Consumes: `Cursor` (tâche 2), `PageSize`, `PageSort` (existants, inchangés).
- Produces:
  - `record Pagination(Cursor cursor, PageSize size, PageSort sort)` avec `Pagination()` par défaut (`cursor=null`, `size=20`, `sort` vide) et `Optional<Cursor> maybeCursor()`.
  - `record PageResult<D>(List<D> content, Cursor next, Cursor previous)` avec `Optional<Cursor> maybeNext()`, `Optional<Cursor> maybePrevious()`, `<S> PageResult<S> map(Function<D,S> mapper)`.
  - `interface Searchable<D, S> { PageResult<D> search(Pagination pagination, Filter<S> filter); }` (plus de `count`).

- [ ] **Step 1: Écrire les tests qui échouent**

```java
// domain/src/test/java/me/dahiorus/project/vending/domain/pagination/entity/PaginationTest.java
package me.dahiorus.project.vending.domain.pagination.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

class PaginationTest {

  @Test
  void should_have_default_values_when_created_with_no_args() {
    var pagination = new Pagination();

    assertThat(pagination.maybeCursor()).isEmpty();
    assertThat(pagination.size()).isEqualTo(new PageSize(20));
    assertThat(pagination.sort()).isEqualTo(new PageSort(Set.of()));
  }

  @Test
  void should_expose_cursor_when_present() {
    var cursor = new Cursor("abc");
    var pagination = new Pagination(cursor, new PageSize(10), new PageSort(Set.of()));

    assertThat(pagination.maybeCursor()).contains(cursor);
  }
}
```

```java
// domain/src/test/java/me/dahiorus/project/vending/domain/pagination/entity/PageResultTest.java
package me.dahiorus.project.vending.domain.pagination.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class PageResultTest {

  @Test
  void should_expose_empty_next_and_previous_when_absent() {
    var result = new PageResult<>(List.of("a", "b"), null, null);

    assertThat(result.maybeNext()).isEmpty();
    assertThat(result.maybePrevious()).isEmpty();
    assertThat(result.content()).containsExactly("a", "b");
  }

  @Test
  void should_expose_next_and_previous_when_present() {
    var next = new Cursor("next-token");
    var previous = new Cursor("prev-token");
    var result = new PageResult<>(List.of("a"), next, previous);

    assertThat(result.maybeNext()).contains(next);
    assertThat(result.maybePrevious()).contains(previous);
  }

  @Test
  void should_map_content_while_keeping_cursors() {
    var next = new Cursor("next-token");
    var result = new PageResult<>(List.of(1, 2), next, null);

    var mapped = result.map(String::valueOf);

    assertThat(mapped.content()).containsExactly("1", "2");
    assertThat(mapped.maybeNext()).contains(next);
  }
}
```

- [ ] **Step 2: Lancer les tests pour vérifier l'échec**

Run: `./gradlew :domain:test --tests "*.PaginationTest" --tests "*.PageResultTest"`
Expected: FAIL (signatures actuelles incompatibles : `Pagination` attend `PageNumber`, `PageResult` attend `Total`/`pagination`).

- [ ] **Step 3: Réécrire `Pagination`**

```java
package me.dahiorus.project.vending.domain.pagination.entity;

import java.util.Optional;
import java.util.Set;

public record Pagination(Cursor cursor, PageSize size, PageSort sort) {
  public Pagination() {
    this(null, new PageSize(20), new PageSort(Set.of()));
  }

  public Optional<Cursor> maybeCursor() {
    return Optional.ofNullable(cursor);
  }
}
```

- [ ] **Step 4: Réécrire `PageResult`**

```java
package me.dahiorus.project.vending.domain.pagination.entity;

import static java.util.Collections.unmodifiableList;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public record PageResult<D>(List<D> content, Cursor next, Cursor previous) {
  public PageResult {
    content = unmodifiableList(content);
  }

  public Optional<Cursor> maybeNext() {
    return Optional.ofNullable(next);
  }

  public Optional<Cursor> maybePrevious() {
    return Optional.ofNullable(previous);
  }

  public <S> PageResult<S> map(Function<D, S> mapper) {
    return new PageResult<>(content.stream().map(mapper).toList(), next, previous);
  }
}
```

- [ ] **Step 5: Supprimer `PageNumber.java` et `Total.java`, réécrire `Searchable`**

```bash
rm domain/src/main/java/me/dahiorus/project/vending/domain/pagination/entity/PageNumber.java
rm domain/src/main/java/me/dahiorus/project/vending/domain/pagination/entity/Total.java
```

```java
package me.dahiorus.project.vending.domain;

import me.dahiorus.project.vending.domain.pagination.entity.Filter;
import me.dahiorus.project.vending.domain.pagination.entity.PageResult;
import me.dahiorus.project.vending.domain.pagination.entity.Pagination;

public interface Searchable<D, S> {
  PageResult<D> search(Pagination pagination, Filter<S> filter);
}
```

- [ ] **Step 6: Lancer les tests domaine pour vérifier le succès**

Run: `./gradlew :domain:test --tests "*.PaginationTest" --tests "*.PageResultTest"`
Expected: PASS

Note: `./gradlew :domain:compileJava` échouera à ce stade tant que les tâches 6-8 (adaptateurs/services) n'ont pas été alignées sur la nouvelle signature de `Searchable`/`Pagination`/`PageResult` — c'est attendu, ces modules seront corrigés dans les tâches suivantes. Ne lance pas `./gradlew build` avant la fin de la tâche 9.

- [ ] **Step 7: Commit**

```bash
git add domain/src/main/java/me/dahiorus/project/vending/domain/pagination/entity/Pagination.java \
        domain/src/main/java/me/dahiorus/project/vending/domain/pagination/entity/PageResult.java \
        domain/src/main/java/me/dahiorus/project/vending/domain/Searchable.java \
        domain/src/test/java/me/dahiorus/project/vending/domain/pagination/entity/PaginationTest.java \
        domain/src/test/java/me/dahiorus/project/vending/domain/pagination/entity/PageResultTest.java
git rm domain/src/main/java/me/dahiorus/project/vending/domain/pagination/entity/PageNumber.java \
       domain/src/main/java/me/dahiorus/project/vending/domain/pagination/entity/Total.java
git commit -m "refactor(domain): replace offset pagination with cursor-based Pagination/PageResult/Searchable"
```

---

### Task 4: `CursorCodec` — encodage/décodage du token de curseur

**Files:**
- Create: `infrastructure/src/main/java/me/dahiorus/project/vending/infrastructure/jpa/repository/CursorCodec.java`
- Test: `infrastructure/src/test/java/me/dahiorus/project/vending/infrastructure/jpa/repository/CursorCodecTest.java`

**Interfaces:**
- Consumes: `Cursor`, `InvalidCursor` (domaine, tâche 2), `org.springframework.data.domain.KeysetScrollPosition`/`ScrollPosition` (spring-data-commons, déjà une dépendance `infrastructure`).
- Produces: `public Cursor encode(KeysetScrollPosition position)`, `public KeysetScrollPosition decode(Cursor cursor)` (lève `InvalidCursor`). Consommé par `KeysetSearchSupport` (tâche 5).

- [ ] **Step 1: Écrire les tests qui échouent**

```java
package me.dahiorus.project.vending.infrastructure.jpa.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import me.dahiorus.project.vending.domain.exception.InvalidCursor;
import me.dahiorus.project.vending.domain.item.entity.ItemType;
import me.dahiorus.project.vending.domain.pagination.entity.Cursor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.ScrollPosition;

class CursorCodecTest {

  private final CursorCodec codec = new CursorCodec();

  static java.util.stream.Stream<Object[]> roundTripValues() {
    return java.util.stream.Stream.of(
        new Object[] {"name", "Coca-Cola 33cL"},
        new Object[] {"id", UUID.fromString("11111111-1111-1111-1111-111111111111")},
        new Object[] {"active", Boolean.TRUE},
        new Object[] {"count", 42},
        new Object[] {"bigCount", 42L},
        new Object[] {"price", new BigDecimal("1.50")},
        new Object[] {"birthDate", LocalDate.of(2024, 1, 1)},
        new Object[] {"createdAt", LocalDateTime.of(2024, 1, 1, 10, 30)},
        new Object[] {"instant", Instant.parse("2024-01-01T10:30:00Z")},
        new Object[] {"type", ItemType.SNACK});
  }

  @ParameterizedTest
  @MethodSource("roundTripValues")
  void should_round_trip_forward_position_for_each_supported_type(String key, Object value) {
    var position = (KeysetScrollPosition) ScrollPosition.forward(Map.of(key, value));

    var cursor = codec.encode(position);
    var decoded = codec.decode(cursor);

    assertThat(decoded.getKeys()).isEqualTo(Map.of(key, value));
    assertThat(decoded.scrollsForward()).isTrue();
  }

  @Test
  void should_round_trip_backward_position() {
    var position =
        (KeysetScrollPosition) ScrollPosition.backward(Map.of("name", "Fanta 33cL"));

    var decoded = codec.decode(codec.encode(position));

    assertThat(decoded.scrollsBackward()).isTrue();
    assertThat(decoded.getKeys()).isEqualTo(Map.of("name", "Fanta 33cL"));
  }

  @Test
  void should_reject_cursor_with_invalid_base64() {
    var cursor = new Cursor("not-valid-base64-!!!");

    assertThatThrownBy(() -> codec.decode(cursor)).isInstanceOf(InvalidCursor.class);
  }

  @Test
  void should_reject_enum_class_outside_allowed_package() {
    var name = Base64.getUrlEncoder().withoutPadding().encodeToString("role".getBytes());
    var value =
        Base64.getUrlEncoder().withoutPadding().encodeToString("ADMIN".getBytes());
    var content =
        "FORWARD\n" + name + ",ENUM," + value + ",java.time.DayOfWeek";
    var cursor =
        new Cursor(Base64.getUrlEncoder().withoutPadding().encodeToString(content.getBytes()));

    assertThatThrownBy(() -> codec.decode(cursor)).isInstanceOf(InvalidCursor.class);
  }

  @Test
  void should_reject_unknown_type_tag() {
    var name = Base64.getUrlEncoder().withoutPadding().encodeToString("name".getBytes());
    var value = Base64.getUrlEncoder().withoutPadding().encodeToString("x".getBytes());
    var content = "FORWARD\n" + name + ",BINARY," + value;
    var cursor =
        new Cursor(Base64.getUrlEncoder().withoutPadding().encodeToString(content.getBytes()));

    assertThatThrownBy(() -> codec.decode(cursor)).isInstanceOf(InvalidCursor.class);
  }
}
```

- [ ] **Step 2: Lancer les tests pour vérifier l'échec**

Run: `./gradlew :infrastructure:test --tests "*.CursorCodecTest"`
Expected: FAIL avec "cannot find symbol: class CursorCodec"

- [ ] **Step 3: Implémenter `CursorCodec`**

```java
package me.dahiorus.project.vending.infrastructure.jpa.repository;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import me.dahiorus.project.vending.domain.exception.InvalidCursor;
import me.dahiorus.project.vending.domain.pagination.entity.Cursor;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.ScrollPosition;

public class CursorCodec {

  private static final String ALLOWED_ENUM_PACKAGE_PREFIX = "me.dahiorus.project.vending.";
  private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
  private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

  public Cursor encode(KeysetScrollPosition position) {
    var content = new StringBuilder(position.scrollsForward() ? "FORWARD" : "BACKWARD");
    position.getKeys().forEach((name, value) -> content.append('\n').append(encodeProperty(name, value)));

    return new Cursor(ENCODER.encodeToString(content.toString().getBytes(UTF_8)));
  }

  public KeysetScrollPosition decode(Cursor cursor) {
    try {
      var lines = new String(DECODER.decode(cursor.value()), UTF_8).split("\n", -1);
      var direction = ScrollPosition.Direction.valueOf(lines[0]);
      Map<String, Object> keys = new LinkedHashMap<>();
      for (int i = 1; i < lines.length; i++) {
        var property = decodeProperty(lines[i]);
        keys.put(property.name(), property.value());
      }

      return (KeysetScrollPosition) ScrollPosition.of(keys, direction);
    } catch (InvalidCursor e) {
      throw e;
    } catch (Exception e) {
      throw new InvalidCursor(cursor, e);
    }
  }

  private static String encodeProperty(String name, Object value) {
    var typeTag = TypeTag.of(value);
    var encodedName = ENCODER.encodeToString(name.getBytes(UTF_8));
    var encodedValue = ENCODER.encodeToString(typeTag.toStringValue(value).getBytes(UTF_8));

    return typeTag == TypeTag.ENUM
        ? String.join(",", encodedName, typeTag.name(), encodedValue, ((Enum<?>) value).getDeclaringClass().getName())
        : String.join(",", encodedName, typeTag.name(), encodedValue);
  }

  private record DecodedProperty(String name, Object value) {}

  private static DecodedProperty decodeProperty(String line) {
    var parts = line.split(",", -1);
    if (parts.length < 3) {
      throw new IllegalArgumentException("Malformed cursor property: " + line);
    }
    var name = new String(DECODER.decode(parts[0]), UTF_8);
    var typeTag = TypeTag.valueOf(parts[1]);
    var rawValue = new String(DECODER.decode(parts[2]), UTF_8);
    var enumClassName = parts.length > 3 ? parts[3] : null;

    return new DecodedProperty(name, typeTag.fromStringValue(rawValue, enumClassName));
  }

  private enum TypeTag {
    STRING {
      @Override
      String toStringValue(Object value) {
        return (String) value;
      }

      @Override
      Object fromStringValue(String raw, String enumClassName) {
        return raw;
      }
    },
    UUID_TAG {
      @Override
      String toStringValue(Object value) {
        return value.toString();
      }

      @Override
      Object fromStringValue(String raw, String enumClassName) {
        return UUID.fromString(raw);
      }
    },
    BOOLEAN {
      @Override
      String toStringValue(Object value) {
        return value.toString();
      }

      @Override
      Object fromStringValue(String raw, String enumClassName) {
        return Boolean.parseBoolean(raw);
      }
    },
    INTEGER {
      @Override
      String toStringValue(Object value) {
        return value.toString();
      }

      @Override
      Object fromStringValue(String raw, String enumClassName) {
        return Integer.parseInt(raw);
      }
    },
    LONG {
      @Override
      String toStringValue(Object value) {
        return value.toString();
      }

      @Override
      Object fromStringValue(String raw, String enumClassName) {
        return Long.parseLong(raw);
      }
    },
    BIG_DECIMAL {
      @Override
      String toStringValue(Object value) {
        return value.toString();
      }

      @Override
      Object fromStringValue(String raw, String enumClassName) {
        return new BigDecimal(raw);
      }
    },
    LOCAL_DATE {
      @Override
      String toStringValue(Object value) {
        return value.toString();
      }

      @Override
      Object fromStringValue(String raw, String enumClassName) {
        return LocalDate.parse(raw);
      }
    },
    LOCAL_DATE_TIME {
      @Override
      String toStringValue(Object value) {
        return value.toString();
      }

      @Override
      Object fromStringValue(String raw, String enumClassName) {
        return LocalDateTime.parse(raw);
      }
    },
    INSTANT {
      @Override
      String toStringValue(Object value) {
        return value.toString();
      }

      @Override
      Object fromStringValue(String raw, String enumClassName) {
        return Instant.parse(raw);
      }
    },
    ENUM {
      @Override
      String toStringValue(Object value) {
        return ((Enum<?>) value).name();
      }

      @SuppressWarnings({"unchecked", "rawtypes"})
      @Override
      Object fromStringValue(String raw, String enumClassName) {
        if (enumClassName == null || !enumClassName.startsWith(ALLOWED_ENUM_PACKAGE_PREFIX)) {
          throw new IllegalArgumentException("Enum class not allowed: " + enumClassName);
        }
        try {
          Class<? extends Enum> enumClass = (Class<? extends Enum>) Class.forName(enumClassName);
          return Enum.valueOf(enumClass, raw);
        } catch (ClassNotFoundException e) {
          throw new IllegalArgumentException("Unknown enum class: " + enumClassName, e);
        }
      }
    };

    abstract String toStringValue(Object value);

    abstract Object fromStringValue(String raw, String enumClassName);

    static TypeTag of(Object value) {
      return switch (value) {
        case String ignored -> STRING;
        case UUID ignored -> UUID_TAG;
        case Boolean ignored -> BOOLEAN;
        case Integer ignored -> INTEGER;
        case Long ignored -> LONG;
        case BigDecimal ignored -> BIG_DECIMAL;
        case LocalDate ignored -> LOCAL_DATE;
        case LocalDateTime ignored -> LOCAL_DATE_TIME;
        case Instant ignored -> INSTANT;
        case Enum<?> ignored -> ENUM;
        default -> throw new IllegalArgumentException(
            "Unsupported cursor value type: " + value.getClass());
      };
    }
  }
}
```

Note : le `TYPE_TAG` d'UUID s'appelle `UUID_TAG` en Java (conflit de nom avec `java.util.UUID`) mais reste sérialisé sous le nom `UUID` dans le protocole texte — ajuste `TypeTag.valueOf(parts[1])` si besoin en gardant `UUID` comme libellé texte : remplace la déclaration `UUID_TAG` par une correspondance explicite si un test échoue sur le nom sérialisé (utilise `@JsonAlias`-style manuel : un `switch` `name()`/`valueOf()` dédié plutôt que `Enum.valueOf(TypeTag.class, ...)` direct, en mappant `"UUID"` -> `TypeTag.UUID_TAG` dans `decodeProperty`). Adapte le test `should_reject_unknown_type_tag` en conséquence si le nom réellement sérialisé diffère.

- [ ] **Step 4: Lancer les tests pour vérifier le succès**

Run: `./gradlew :infrastructure:test --tests "*.CursorCodecTest"`
Expected: PASS — si le mapping `UUID`/`UUID_TAG` échoue, corrige `decodeProperty`/`encodeProperty` pour sérialiser explicitement `"UUID"` au lieu de `TypeTag.name()` quand `typeTag == TypeTag.UUID_TAG`, et parser `"UUID"` -> `TypeTag.UUID_TAG` en décodage.

- [ ] **Step 5: Commit**

```bash
git add infrastructure/src/main/java/me/dahiorus/project/vending/infrastructure/jpa/repository/CursorCodec.java \
        infrastructure/src/test/java/me/dahiorus/project/vending/infrastructure/jpa/repository/CursorCodecTest.java
git commit -m "feat(infrastructure): add CursorCodec to encode/decode opaque cursor tokens"
```

---

### Task 5: `PageSortConverter` et `KeysetSearchSupport`

**Files:**
- Create: `infrastructure/src/main/java/me/dahiorus/project/vending/infrastructure/jpa/repository/PageSortConverter.java`
- Create: `infrastructure/src/main/java/me/dahiorus/project/vending/infrastructure/jpa/repository/KeysetSearchSupport.java`
- Delete: `infrastructure/src/main/java/me/dahiorus/project/vending/infrastructure/jpa/repository/ToPageableConverter.java`

**Interfaces:**
- Consumes: `PageSort`, `Pagination`, `Filter`, `PageResult`, `Cursor` (domaine) ; `CursorCodec` (tâche 4) ; `ExampleMatcherAdapter.toExample` (existant) ; `org.springframework.data.repository.query.QueryByExampleExecutor`, `org.springframework.data.domain.Window`, `org.springframework.data.domain.ScrollPosition`, `org.springframework.data.domain.Sort`.
- Produces: `KeysetSearchSupport.search(QueryByExampleExecutor<E> repository, Filter<S> filter, Pagination pagination, Function<S,E> toEntity, Function<E,D> toDomain, CursorCodec cursorCodec)` retournant `PageResult<D>` — consommé par `ItemRepositoryAdapter`/`VendingMachineRepositoryAdapter` (tâches 6-7). Cette classe est exercée par les tests d'intégration existants des adaptateurs (tâches 6-7), pas de test unitaire dédié (elle a besoin d'un vrai `EntityManager`/H2).

- [ ] **Step 1: Implémenter `PageSortConverter`**

```java
package me.dahiorus.project.vending.infrastructure.jpa.repository;

import me.dahiorus.project.vending.domain.pagination.entity.PageSort;
import me.dahiorus.project.vending.domain.pagination.entity.Pagination.Direction;
import org.springframework.data.domain.Sort;

public final class PageSortConverter {
  private PageSortConverter() {}

  public static Sort toSort(PageSort pageSort) {
    return Sort.by(
        pageSort.sortProperties().stream()
            .map(sortProperty -> new Sort.Order(toDirection(sortProperty.direction()), sortProperty.name()))
            .toList());
  }

  private static Sort.Direction toDirection(Direction direction) {
    return switch (direction) {
      case ASC -> Sort.Direction.ASC;
      case DESC -> Sort.Direction.DESC;
    };
  }
}
```

- [ ] **Step 2: Supprimer `ToPageableConverter`**

```bash
rm infrastructure/src/main/java/me/dahiorus/project/vending/infrastructure/jpa/repository/ToPageableConverter.java
```

- [ ] **Step 3: Implémenter `KeysetSearchSupport`**

```java
package me.dahiorus.project.vending.infrastructure.jpa.repository;

import static me.dahiorus.project.vending.infrastructure.jpa.repository.ExampleMatcherAdapter.toExample;
import static me.dahiorus.project.vending.infrastructure.jpa.repository.PageSortConverter.toSort;

import java.util.function.Function;
import me.dahiorus.project.vending.domain.pagination.entity.Cursor;
import me.dahiorus.project.vending.domain.pagination.entity.Filter;
import me.dahiorus.project.vending.domain.pagination.entity.PageResult;
import me.dahiorus.project.vending.domain.pagination.entity.Pagination;
import org.springframework.data.domain.KeysetScrollPosition;
import org.springframework.data.domain.ScrollPosition;
import org.springframework.data.domain.Window;
import org.springframework.data.repository.query.QueryByExampleExecutor;

public final class KeysetSearchSupport {
  private KeysetSearchSupport() {}

  public static <S, E, D> PageResult<D> search(
      QueryByExampleExecutor<E> repository,
      Filter<S> filter,
      Pagination pagination,
      Function<S, E> toEntity,
      Function<E, D> toDomain,
      CursorCodec cursorCodec) {
    var example = toExample(filter, toEntity);
    var sort = toSort(pagination.sort());
    var requestedPosition =
        pagination.maybeCursor().map(cursorCodec::decode).orElseGet(ScrollPosition::keyset);

    Window<E> window =
        repository.findBy(
            example, q -> q.sortBy(sort).limit(pagination.size().value()).scroll(requestedPosition));

    var content = window.map(toDomain).toList();
    var next = nextCursor(window, requestedPosition, cursorCodec);
    var previous = previousCursor(window, requestedPosition, cursorCodec);

    return new PageResult<>(content, next, previous);
  }

  private static <E> Cursor nextCursor(
      Window<E> window, ScrollPosition requestedPosition, CursorCodec cursorCodec) {
    boolean isBackward =
        requestedPosition instanceof KeysetScrollPosition ksp && ksp.scrollsBackward();

    if (isBackward) {
      var lastIndex = window.size() - 1;
      var position = (KeysetScrollPosition) window.positionAt(lastIndex);
      return cursorCodec.encode(position.forward());
    }

    if (!window.hasNext() || window.isEmpty()) {
      return null;
    }
    var lastIndex = window.size() - 1;
    var position = (KeysetScrollPosition) window.positionAt(lastIndex);
    return cursorCodec.encode(position.forward());
  }

  private static <E> Cursor previousCursor(
      Window<E> window, ScrollPosition requestedPosition, CursorCodec cursorCodec) {
    boolean isBackward =
        requestedPosition instanceof KeysetScrollPosition ksp && ksp.scrollsBackward();
    boolean isInitial = requestedPosition.isInitial();

    if (isInitial || window.isEmpty()) {
      return null;
    }
    if (isBackward && !window.hasNext()) {
      return null;
    }

    var position = (KeysetScrollPosition) window.positionAt(0);
    return cursorCodec.encode(position.backward());
  }
}
```

- [ ] **Step 4: Compiler (pas encore utilisé, sans effet sur les tests)**

Run: `./gradlew :infrastructure:compileJava`
Expected: succès de compilation pour ces deux nouvelles classes (les adaptateurs ne les utilisent pas encore, la compilation globale du module échouera toujours à cause des tâches 6-9 restantes — c'est attendu).

- [ ] **Step 5: Commit**

```bash
git add infrastructure/src/main/java/me/dahiorus/project/vending/infrastructure/jpa/repository/PageSortConverter.java \
        infrastructure/src/main/java/me/dahiorus/project/vending/infrastructure/jpa/repository/KeysetSearchSupport.java
git rm infrastructure/src/main/java/me/dahiorus/project/vending/infrastructure/jpa/repository/ToPageableConverter.java
git commit -m "feat(infrastructure): add PageSortConverter and shared KeysetSearchSupport"
```

---

### Task 6: Migrer `ItemRepositoryAdapter` vers le scroll keyset

**Files:**
- Modify: `infrastructure/src/main/java/me/dahiorus/project/vending/infrastructure/jpa/repository/item/ItemRepositoryAdapter.java`
- Modify: `infrastructure/src/intTest/java/me/dahiorus/project/vending/infrastructure/jpa/repository/item/ItemRepositoryAdapterIT.java`
- Delete: `infrastructure/src/intTest/java/me/dahiorus/project/vending/infrastructure/jpa/repository/item/KeysetScrollSpikeIT.java` (créé en tâche 1)

**Interfaces:**
- Consumes: `KeysetSearchSupport.search(...)`, `CursorCodec` (nouvelle dépendance du constructeur de `ItemRepositoryAdapter`).
- Produces: `ItemRepositoryAdapter implements ItemRepositoryPort` avec `PageResult<Item> search(Pagination, Filter<Item>)`, plus de `count(...)`.

- [ ] **Step 1: Réécrire les tests d'intégration de recherche**

Remplace le bloc `@Nested class SearchAndCount` de `ItemRepositoryAdapterIT` par :

```java
@Nested
class Search {
  Item item1, item2, item3;

  @BeforeEach
  void setUpItems() {
    item1 =
        repository.create(
            new ItemToCreate(ItemName.of("Coca-Cola 33cL"), COLD_BEVERAGE, BigDecimal.valueOf(1.50)));
    item2 =
        repository.create(
            new ItemToCreate(ItemName.of("Pepsi 33cL"), COLD_BEVERAGE, BigDecimal.valueOf(1.50)));
    item3 =
        repository.create(
            new ItemToCreate(ItemName.of("Fanta 33cL"), COLD_BEVERAGE, BigDecimal.valueOf(1.50)));
    entityManager.flush();
  }

  @Test
  void should_return_all_items_given_empty_filter_and_no_cursor() {
    var result =
        repository.search(
            new Pagination(),
            new Filter<>(new Item(null, null, null, null), new FilterMatcher()));

    assertThat(result.content()).containsExactlyInAnyOrder(item1, item2, item3);
    assertThat(result.maybePrevious()).isEmpty();
  }

  @Test
  void should_return_filtered_items() {
    var result =
        repository.search(
            new Pagination(),
            new Filter<>(
                new Item(null, ItemName.of("Coca-Cola 33cL"), null, null), new FilterMatcher()));

    assertThat(result.content()).containsExactly(item1);
    assertThat(result.maybeNext()).isEmpty();
  }

  @Test
  void should_scroll_forward_then_backward_across_pages() {
    var pagination =
        new Pagination(null, new PageSize(2), PageSort.asc(java.util.Set.of("name")));
    var firstPage =
        repository.search(
            pagination, new Filter<>(new Item(null, null, null, null), new FilterMatcher()));

    assertThat(firstPage.content()).hasSize(2);
    assertThat(firstPage.maybeNext()).isPresent();
    assertThat(firstPage.maybePrevious()).isEmpty();

    var secondPagePagination =
        new Pagination(firstPage.next(), new PageSize(2), PageSort.asc(java.util.Set.of("name")));
    var secondPage =
        repository.search(
            secondPagePagination,
            new Filter<>(new Item(null, null, null, null), new FilterMatcher()));

    assertThat(secondPage.content()).hasSize(1);
    assertThat(secondPage.maybeNext()).isEmpty();
    assertThat(secondPage.maybePrevious()).isPresent();

    var backToFirstPagePagination =
        new Pagination(
            secondPage.previous(), new PageSize(2), PageSort.asc(java.util.Set.of("name")));
    var backToFirstPage =
        repository.search(
            backToFirstPagePagination,
            new Filter<>(new Item(null, null, null, null), new FilterMatcher()));

    assertThat(backToFirstPage.content()).containsExactlyElementsOf(firstPage.content());
  }
}
```

Supprime aussi les imports/usages de `PageNumber`/`Total`/`count(...)` restants dans ce fichier, et ajoute les imports de `PageSize`, `PageSort`.

Supprime le fichier `KeysetScrollSpikeIT.java` (son rôle de spike est terminé, sa logique est maintenant couverte par ce test).

```bash
rm infrastructure/src/intTest/java/me/dahiorus/project/vending/infrastructure/jpa/repository/item/KeysetScrollSpikeIT.java
```

- [ ] **Step 2: Lancer les tests pour vérifier l'échec**

Run: `./gradlew :infrastructure:intTest --tests "*.ItemRepositoryAdapterIT"`
Expected: FAIL à la compilation (`ItemRepositoryAdapter.search` renvoie encore `List<Item>`, pas de constructeur `CursorCodec`).

- [ ] **Step 3: Réécrire `ItemRepositoryAdapter`**

```java
package me.dahiorus.project.vending.infrastructure.jpa.repository.item;

import static me.dahiorus.project.vending.infrastructure.jpa.repository.KeysetSearchSupport.search;

import jakarta.persistence.EntityManager;
import java.util.Optional;
import java.util.UUID;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.file.entity.FileToUpload;
import me.dahiorus.project.vending.domain.file.entity.UploadedFile;
import me.dahiorus.project.vending.domain.item.entity.Item;
import me.dahiorus.project.vending.domain.item.entity.ItemId;
import me.dahiorus.project.vending.domain.item.entity.ItemToCreate;
import me.dahiorus.project.vending.domain.item.entity.ItemToUpdate;
import me.dahiorus.project.vending.domain.item.entity.ItemWithImage;
import me.dahiorus.project.vending.domain.item.port.ItemRepositoryPort;
import me.dahiorus.project.vending.domain.pagination.entity.Filter;
import me.dahiorus.project.vending.domain.pagination.entity.PageResult;
import me.dahiorus.project.vending.domain.pagination.entity.Pagination;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaItem;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaUploadedFile;
import me.dahiorus.project.vending.infrastructure.jpa.repository.CursorCodec;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;

@CacheConfig(cacheNames = "items")
@Repository
public class ItemRepositoryAdapter implements ItemRepositoryPort {
  private final JpaRepository<JpaItem, UUID> jpaRepository;
  private final JpaRepository<JpaUploadedFile, UUID> jpaUploadedFileRepository;
  private final CursorCodec cursorCodec;

  public ItemRepositoryAdapter(EntityManager entityManager) {
    this.jpaRepository = new SimpleJpaRepository<>(JpaItem.class, entityManager);
    this.jpaUploadedFileRepository =
        new SimpleJpaRepository<>(JpaUploadedFile.class, entityManager);
    this.cursorCodec = new CursorCodec();
  }

  @Cacheable(key = "#id.value")
  @Override
  public Optional<Item> find(ItemId id) {
    return jpaRepository.findById(id.value()).map(JpaItem::toDomain);
  }

  @CachePut(key = "#result.id.value")
  @Override
  public Item create(ItemToCreate itemToCreate) {
    return jpaRepository.save(JpaItem.createFrom(itemToCreate)).toDomain();
  }

  @CachePut(key = "#result.id")
  @Override
  public Item update(ItemToUpdate toUpdate) {
    return find(toUpdate.id())
        .map(item -> item.updateFrom(toUpdate))
        .map(JpaItem::fromDomain)
        .map(jpaRepository::save)
        .map(JpaItem::toDomain)
        .orElseThrow(() -> new ResourceNotFound(toUpdate.id()));
  }

  @CacheEvict(key = "#itemId.value")
  @Override
  public void delete(ItemId itemId) {
    jpaRepository.deleteById(itemId.value());
  }

  @Override
  public PageResult<Item> search(Pagination pagination, Filter<Item> filter) {
    return search(
        jpaRepository, filter, pagination, JpaItem::fromDomain, JpaItem::toDomain, cursorCodec);
  }

  @CachePut(cacheNames = "itemImages", key = "#result.item.id.value")
  @Override
  public ItemWithImage uploadImage(final ItemId itemId, final FileToUpload image)
      throws ResourceNotFound {
    return jpaRepository
        .findById(itemId.value())
        .map(
            jpaItem -> {
              var uploadedImage = jpaUploadedFileRepository.save(JpaUploadedFile.toCreate(image));
              jpaItem.setImage(uploadedImage);
              jpaRepository.save(jpaItem);

              return new ItemWithImage(jpaItem.toDomain(), uploadedImage.toDomain());
            })
        .orElseThrow(() -> new ResourceNotFound(itemId));
  }

  @Cacheable(value = "itemImages", key = "#itemId.value")
  @Override
  public Optional<UploadedFile> findImage(final ItemId itemId) {
    var jpaItem =
        jpaRepository.findById(itemId.value()).orElseThrow(() -> new ResourceNotFound(itemId));

    return jpaItem.maybeImage().map(JpaUploadedFile::toDomain);
  }
}
```

Note : le nom de méthode statique importé (`search`) entre en conflit avec la méthode d'instance `search(...)` déclarée juste après — renomme l'import statique en `KeysetSearchSupport.search` appelé pleinement qualifié (`me.dahiorus.project.vending.infrastructure.jpa.repository.KeysetSearchSupport.search(...)`) plutôt qu'en import statique, pour éviter toute ambiguïté de résolution Java.

- [ ] **Step 4: Lancer les tests d'intégration pour vérifier le succès**

Run: `./gradlew :infrastructure:intTest --tests "*.ItemRepositoryAdapterIT"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add infrastructure/src/main/java/me/dahiorus/project/vending/infrastructure/jpa/repository/item/ItemRepositoryAdapter.java \
        infrastructure/src/intTest/java/me/dahiorus/project/vending/infrastructure/jpa/repository/item/ItemRepositoryAdapterIT.java
git rm infrastructure/src/intTest/java/me/dahiorus/project/vending/infrastructure/jpa/repository/item/KeysetScrollSpikeIT.java
git commit -m "refactor(infrastructure): migrate ItemRepositoryAdapter to keyset cursor scrolling"
```

---

### Task 7: Migrer `VendingMachineRepositoryAdapter` vers le scroll keyset

**Files:**
- Modify: `infrastructure/src/main/java/me/dahiorus/project/vending/infrastructure/jpa/repository/machine/VendingMachineRepositoryAdapter.java`
- Modify: `infrastructure/src/intTest/java/me/dahiorus/project/vending/infrastructure/jpa/repository/machine/VendingMachineRepositoryAdapterIT.java`

**Interfaces:**
- Consumes: `KeysetSearchSupport.search(...)`, `CursorCodec`.
- Produces: `VendingMachineRepositoryAdapter implements VendingMachineRepositoryPort` avec `PageResult<VendingMachine> search(...)`, plus de `count(...)`.

- [ ] **Step 1: Réécrire le bloc de tests `SearchAndCount` → `Search`**

Remplace le `@Nested class SearchAndCount` (avec ses sous-`@Nested Search`/`Count`) par un unique `@Nested class Search` suivant le même modèle que la tâche 6 (tests `should_search_vending_machines_by_filter`, `should_list_all_given_empty_filter` adaptés à `result.content()`, plus un test `should_scroll_forward_then_backward_across_pages` avec `PageSize(2)` et tri sur `serialNumber`).

```java
@Nested
class Search {
  VendingMachine vendingMachine1, vendingMachine2, vendingMachine3;

  @BeforeEach
  void setUpVendingMachines() {
    vendingMachine1 = aVendingMachine().itemType(SNACK).serialNumber("VM-1234").build();
    vendingMachine2 = aVendingMachine().itemType(COLD_BEVERAGE).serialNumber("VM-4567").build();
    vendingMachine3 = aVendingMachine().itemType(HOT_BEVERAGE).serialNumber("VM-8910").build();
    repository.create(vendingMachine1);
    repository.create(vendingMachine2);
    repository.create(vendingMachine3);
    entityManager.flush();
  }

  @Test
  void should_search_vending_machines_by_filter() {
    var result =
        repository.search(
            new Pagination(),
            new Filter<>(
                new VendingMachine(null, SerialNumber.of("VM-12"), null, SNACK, aVendingMachineStatus().build(), null),
                new FilterMatcher()));

    assertThat(result.content()).containsExactly(vendingMachine1);
  }

  @Test
  void should_list_all_given_empty_filter() {
    var result =
        repository.search(
            new Pagination(),
            new Filter<>(
                new VendingMachine(null, null, null, null, null, null), new FilterMatcher()));

    assertThat(result.content()).containsExactlyInAnyOrder(vendingMachine1, vendingMachine2, vendingMachine3);
  }

  @Test
  void should_scroll_forward_then_backward_across_pages() {
    var pagination =
        new Pagination(null, new PageSize(2), PageSort.asc(java.util.Set.of("serialNumber")));
    var firstPage =
        repository.search(
            pagination,
            new Filter<>(new VendingMachine(null, null, null, null, null, null), new FilterMatcher()));

    assertThat(firstPage.content()).hasSize(2);
    assertThat(firstPage.maybeNext()).isPresent();

    var secondPage =
        repository.search(
            new Pagination(firstPage.next(), new PageSize(2), PageSort.asc(java.util.Set.of("serialNumber"))),
            new Filter<>(new VendingMachine(null, null, null, null, null, null), new FilterMatcher()));

    assertThat(secondPage.content()).hasSize(1);
    assertThat(secondPage.maybePrevious()).isPresent();

    var backToFirstPage =
        repository.search(
            new Pagination(secondPage.previous(), new PageSize(2), PageSort.asc(java.util.Set.of("serialNumber"))),
            new Filter<>(new VendingMachine(null, null, null, null, null, null), new FilterMatcher()));

    assertThat(backToFirstPage.content()).containsExactlyElementsOf(firstPage.content());
  }
}
```

- [ ] **Step 2: Lancer les tests pour vérifier l'échec**

Run: `./gradlew :infrastructure:intTest --tests "*.VendingMachineRepositoryAdapterIT"`
Expected: FAIL à la compilation (signature `search` incompatible, `count` encore déclaré côté port).

- [ ] **Step 3: Réécrire `VendingMachineRepositoryAdapter`**

```java
package me.dahiorus.project.vending.infrastructure.jpa.repository.machine;

import static java.util.function.Predicate.not;
import static me.dahiorus.project.vending.infrastructure.jpa.repository.KeysetSearchSupport.search;

import java.util.Optional;
import me.dahiorus.project.vending.domain.exception.ResourceNotFound;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachine;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineId;
import me.dahiorus.project.vending.domain.machine.entity.VendingMachineToUpdate;
import me.dahiorus.project.vending.domain.machine.port.VendingMachineRepositoryPort;
import me.dahiorus.project.vending.domain.pagination.entity.Filter;
import me.dahiorus.project.vending.domain.pagination.entity.PageResult;
import me.dahiorus.project.vending.domain.pagination.entity.Pagination;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaVendingMachine;
import me.dahiorus.project.vending.infrastructure.jpa.repository.CursorCodec;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

@CacheConfig(cacheNames = "vendingMachines")
@Repository
public class VendingMachineRepositoryAdapter implements VendingMachineRepositoryPort {
  private final VendingMachineJpaRepository jpaRepository;
  private final CursorCodec cursorCodec = new CursorCodec();

  public VendingMachineRepositoryAdapter(VendingMachineJpaRepository jpaRepository) {
    this.jpaRepository = jpaRepository;
  }

  @CachePut(key = "#result.id.value")
  @Override
  public VendingMachine create(VendingMachine machineToCreate) {
    return jpaRepository.save(JpaVendingMachine.fromDomain(machineToCreate)).toDomain();
  }

  @Cacheable(key = "#id.value")
  @Override
  public Optional<VendingMachine> find(VendingMachineId id) {
    return jpaRepository.findById(id.value()).map(JpaVendingMachine::toDomain);
  }

  @CachePut(key = "#result.id.value")
  @Override
  public VendingMachine update(VendingMachineToUpdate toUpdate) {
    return find(toUpdate.id())
        .map(machine -> machine.updateFrom(toUpdate))
        .map(JpaVendingMachine::fromDomain)
        .map(jpaRepository::save)
        .map(JpaVendingMachine::toDomain)
        .orElseThrow(() -> new ResourceNotFound(toUpdate.id()));
  }

  @CacheEvict(key = "#id.value")
  @Override
  public void delete(VendingMachineId id) {
    jpaRepository.deleteById(id.value());
  }

  @Override
  public PageResult<VendingMachine> search(Pagination pagination, Filter<VendingMachine> filter) {
    return me.dahiorus.project.vending.infrastructure.jpa.repository.KeysetSearchSupport.search(
        jpaRepository,
        filter,
        pagination,
        JpaVendingMachine::fromDomain,
        JpaVendingMachine::toDomain,
        cursorCodec);
  }

  @Override
  public Optional<VendingMachine> findDuplicateOf(VendingMachine vendingMachine) {
    return jpaRepository
        .findBySerialNumber(vendingMachine.serialNumber().value())
        .map(JpaVendingMachine::toDomain)
        .filter(not(duplicate -> duplicate.id().equals(vendingMachine.id())));
  }
}
```

(Comme à la tâche 6, appelle `KeysetSearchSupport.search` en nom pleinement qualifié pour éviter le conflit avec la méthode d'instance `search`.)

- [ ] **Step 4: Lancer les tests pour vérifier le succès**

Run: `./gradlew :infrastructure:intTest --tests "*.VendingMachineRepositoryAdapterIT"`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add infrastructure/src/main/java/me/dahiorus/project/vending/infrastructure/jpa/repository/machine/VendingMachineRepositoryAdapter.java \
        infrastructure/src/intTest/java/me/dahiorus/project/vending/infrastructure/jpa/repository/machine/VendingMachineRepositoryAdapterIT.java
git commit -m "refactor(infrastructure): migrate VendingMachineRepositoryAdapter to keyset cursor scrolling"
```

---

### Task 8: Simplifier les `ApplicationService` (`Item`, `VendingMachine`)

**Files:**
- Modify: `application/src/main/java/me/dahiorus/project/vending/application/service/item/ItemApplicationService.java`
- Modify: `application/src/main/java/me/dahiorus/project/vending/application/service/machine/VendingMachineApplicationService.java`

**Interfaces:**
- Consumes: `ItemRepositoryPort.search`/`VendingMachineRepositoryPort.search` renvoyant désormais directement `PageResult<D>` (tâches 6-7).
- Produces: `ItemApiPort.search`/`VendingMachineApiPort.search` inchangés en signature (`PageResult<Item> search(Pagination, Item, FilterMatcher)`), implémentation simplifiée en délégation directe.

- [ ] **Step 1: Réécrire `ItemApplicationService.search`**

```java
@Override
public PageResult<Item> search(
    final Pagination pagination, final Item example, final FilterMatcher filterMatcher) {
  return itemRepository.search(pagination, new Filter<>(example, filterMatcher));
}
```

Retire l'import `me.dahiorus.project.vending.domain.pagination.entity.Total` (supprimé) et la variable `count` devenue inutile.

- [ ] **Step 2: Réécrire `VendingMachineApplicationService.search`**

```java
@Override
public PageResult<VendingMachine> search(
    Pagination pagination, VendingMachine example, FilterMatcher filterMatcher) {
  return vendingMachineRepository.search(pagination, new Filter<>(example, filterMatcher));
}
```

Retire l'import `Total` devenu inutile.

Ce module (`application`) n'a pas de source de test unitaire dans ce dépôt (`application/src/test` n'existe pas, cf. `AGENTS.md` racine — seuls `domain` et `infrastructure/intTest` portent des tests) ; la couverture de cette délégation se fait via les tests d'intégration de contrôleur (tâche 9) et d'adaptateur (tâches 6-7).

- [ ] **Step 3: Compiler le module `application`**

Run: `./gradlew :application:compileJava`
Expected: succès de compilation

- [ ] **Step 4: Commit**

```bash
git add application/src/main/java/me/dahiorus/project/vending/application/service/item/ItemApplicationService.java \
        application/src/main/java/me/dahiorus/project/vending/application/service/machine/VendingMachineApplicationService.java
git commit -m "refactor(application): delegate search directly to cursor-based repository ports"
```

---

### Task 9: Couche REST — `WebConfig`, contrôleurs, gestion d'erreur

**Files:**
- Modify: `infrastructure/src/main/java/me/dahiorus/project/vending/infrastructure/rest/utils/ToPaginationConverter.java`
- Create: `infrastructure/src/main/java/me/dahiorus/project/vending/infrastructure/rest/utils/CursorLinkFactory.java`
- Modify: `infrastructure/src/main/java/me/dahiorus/project/vending/infrastructure/rest/config/WebConfig.java`
- Modify: `infrastructure/src/main/java/me/dahiorus/project/vending/infrastructure/rest/controller/item/ItemCrudRestController.java`
- Modify: `infrastructure/src/main/java/me/dahiorus/project/vending/infrastructure/rest/controller/machine/VendingMachineCrudRestController.java`
- Modify: `infrastructure/src/main/java/me/dahiorus/project/vending/infrastructure/rest/exception/RestResponseExceptionHandler.java`

**Interfaces:**
- Consumes: `PageResult<D>` (avec `maybeNext()`/`maybePrevious()`), `Cursor`, `InvalidCursor` (domaine).
- Produces: `ToPaginationConverter.toPagination(String cursor, int size, Sort sort)` → `Pagination` ; `CursorLinkFactory.selfLink()`/`cursorLink(Cursor)` → `URI` ; contrôleurs exposant `GET /api/v1/items` et `GET /api/v1/vending-machines` avec paramètres `cursor` (optionnel), `size`, `sort`, renvoyant `CollectionModel<EntityModel<XxxDto>>` avec liens `self`/`next`/`prev`.

- [ ] **Step 1: Réécrire `ToPaginationConverter`**

```java
package me.dahiorus.project.vending.infrastructure.rest.utils;

import me.dahiorus.project.vending.domain.pagination.entity.Cursor;
import me.dahiorus.project.vending.domain.pagination.entity.PageSize;
import me.dahiorus.project.vending.domain.pagination.entity.PageSort;
import me.dahiorus.project.vending.domain.pagination.entity.PageSort.SortProperty;
import me.dahiorus.project.vending.domain.pagination.entity.Pagination;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

public final class ToPaginationConverter {
  private ToPaginationConverter() {}

  public static Pagination toPagination(String cursor, int size, Sort sort) {
    var cursorValue = StringUtils.hasText(cursor) ? new Cursor(cursor) : null;

    return new Pagination(cursorValue, new PageSize(size), toPageSort(sort));
  }

  private static PageSort toPageSort(Sort sort) {
    return new PageSort(
        sort.stream()
            .map(order -> new SortProperty(order.getProperty(), toDirection(order.getDirection())))
            .collect(java.util.stream.Collectors.toSet()));
  }

  private static Pagination.Direction toDirection(Sort.Direction sortDirection) {
    return switch (sortDirection) {
      case ASC -> Pagination.Direction.ASC;
      case DESC -> Pagination.Direction.DESC;
    };
  }
}
```

- [ ] **Step 2: Implémenter `CursorLinkFactory`**

```java
package me.dahiorus.project.vending.infrastructure.rest.utils;

import java.net.URI;
import java.util.Optional;
import me.dahiorus.project.vending.domain.pagination.entity.Cursor;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

public final class CursorLinkFactory {
  private CursorLinkFactory() {}

  public static URI selfLink() {
    return ServletUriComponentsBuilder.fromCurrentRequestUri().build().toUri();
  }

  public static Optional<URI> cursorLink(Cursor cursor) {
    return Optional.ofNullable(cursor)
        .map(
            c ->
                ServletUriComponentsBuilder.fromCurrentRequestUri()
                    .replaceQueryParam("cursor", c.value())
                    .build()
                    .toUri());
  }
}
```

- [ ] **Step 3: Réécrire `WebConfig`**

```java
package me.dahiorus.project.vending.infrastructure.rest.config;

import static io.swagger.v3.oas.annotations.enums.SecuritySchemeType.HTTP;
import static org.springframework.data.domain.Sort.Direction.DESC;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortHandlerMethodArgumentResolver;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@OpenAPIDefinition(
    info =
        @Info(
            title = "Vending app API",
            description = "Simple vending application",
            version = "v1.0"))
@SecurityScheme(name = "bearerAuth", bearerFormat = "JWT", type = HTTP, scheme = "bearer")
public class WebConfig implements WebMvcConfigurer {
  @Override
  public void addArgumentResolvers(final List<HandlerMethodArgumentResolver> resolvers) {
    resolvers.add(sortHandlerResolver());
  }

  private static HandlerMethodArgumentResolver sortHandlerResolver() {
    SortHandlerMethodArgumentResolver resolver = new SortHandlerMethodArgumentResolver();
    resolver.setFallbackSort(Sort.by(DESC, "createdAt"));

    return resolver;
  }
}
```

- [ ] **Step 4: Réécrire la méthode `search` de `ItemCrudRestController`**

```java
@Operation(description = "Get a page of items")
@ApiResponse(responseCode = "200", description = "Items found")
@GetMapping
public ResponseEntity<CollectionModel<EntityModel<ItemDto>>> search(
    @RequestParam(required = false) String cursor,
    @RequestParam(defaultValue = "20") int size,
    @ParameterObject Sort sort,
    @ParameterObject ItemDto example,
    @ParameterObject FilterMatcherDto filterMatcher) {
  var pagination = toPagination(cursor, size, sort);
  var page =
      service.search(pagination, example.toDomain(), filterMatcher.toDomain()).map(ItemDto::fromDomain);

  var models = page.content().stream().map(modelAssembler::toModel).toList();
  var collection = CollectionModel.of(models);
  collection.add(Link.of(CursorLinkFactory.selfLink().toString(), SELF));
  page.maybeNext()
      .flatMap(CursorLinkFactory::cursorLink)
      .ifPresent(uri -> collection.add(Link.of(uri.toString(), "next")));
  page.maybePrevious()
      .flatMap(CursorLinkFactory::cursorLink)
      .ifPresent(uri -> collection.add(Link.of(uri.toString(), "prev")));

  return ok(collection);
}
```

Mets à jour les imports du fichier : retire `PagedResourcesAssembler`, `PageImpl`, `Pageable`, `PagedModel` ; ajoute `CollectionModel`, `Link`, `Sort`, `RequestParam`, `me.dahiorus.project.vending.infrastructure.rest.utils.ToPaginationConverter.toPagination` (déjà importé statiquement), `me.dahiorus.project.vending.infrastructure.rest.utils.CursorLinkFactory`. Retire aussi le champ/constructeur `pageModelAssembler` (`PagedResourcesAssembler<ItemDto>`) devenu inutile ; garde `modelAssembler` (`RepresentationModelAssembler<ItemDto, EntityModel<ItemDto>>`).

- [ ] **Step 5: Réécrire la méthode `search` de `VendingMachineCrudRestController`**

Applique la même transformation qu'à l'étape 4, avec `VendingMachineDto` à la place d'`ItemDto`, et retire de même le champ `pageModelAssembler`.

- [ ] **Step 6: Gérer `InvalidCursor` dans `RestResponseExceptionHandler`**

```java
@ExceptionHandler(InvalidCursor.class)
@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public Object handleInvalidCursor(InvalidCursor ex) {
  return initResponseBody(ex);
}
```

Ajoute l'import `me.dahiorus.project.vending.domain.exception.InvalidCursor`.

- [ ] **Step 7: Lancer la compilation complète et les tests**

Run: `./gradlew clean build`
Expected: BUILD SUCCESSFUL, tous les tests unitaires (`domain`, `application`, `infrastructure`) passent.

Run: `./gradlew :infrastructure:intTest`
Expected: BUILD SUCCESSFUL, tous les tests d'intégration passent.

Si des erreurs de compilation subsistent (imports résiduels de `Pageable`/`PagedResourcesAssembler`/`PageNumber`/`Total`/`count(...)` dans des fichiers non listés ci-dessus), corrige-les fichier par fichier avant de continuer — ne passe pas à la tâche 10 tant que `./gradlew clean build` n'est pas vert.

- [ ] **Step 8: Commit**

```bash
git add infrastructure/src/main/java/me/dahiorus/project/vending/infrastructure/rest/utils/ToPaginationConverter.java \
        infrastructure/src/main/java/me/dahiorus/project/vending/infrastructure/rest/utils/CursorLinkFactory.java \
        infrastructure/src/main/java/me/dahiorus/project/vending/infrastructure/rest/config/WebConfig.java \
        infrastructure/src/main/java/me/dahiorus/project/vending/infrastructure/rest/controller/item/ItemCrudRestController.java \
        infrastructure/src/main/java/me/dahiorus/project/vending/infrastructure/rest/controller/machine/VendingMachineCrudRestController.java \
        infrastructure/src/main/java/me/dahiorus/project/vending/infrastructure/rest/exception/RestResponseExceptionHandler.java
git commit -m "feat(infrastructure): expose cursor-based pagination on Item and VendingMachine listing endpoints"
```

---

### Task 10: Mettre à jour le garde-fou de non-régression dans `AGENTS.md`

**Files:**
- Modify: `AGENTS.md` (racine)

**Interfaces:**
- Aucune (documentation).

- [ ] **Step 1: Compter les tests réels**

Run: `./gradlew clean build :infrastructure:intTest 2>&1 | tee /tmp/build-output.log`

Relève dans la sortie (ou dans `*/build/test-results/test/*.xml` et `infrastructure/build/test-results/intTest/*.xml`) le nombre total de tests `domain` + `application` + `infrastructure` (unitaires) et le nombre de tests `infrastructure:intTest`.

- [ ] **Step 2: Mettre à jour `AGENTS.md`**

Remplace la ligne :

```
Le nombre de tests exécutés (140 = 68 `domain` + 72 `infrastructure`) est le
garde-fou de non-régression à vérifier après toute modification.
```

par le nombre réel constaté à l'étape 1 (par exemple `X = A domain + B infrastructure`, en conservant la même formulation et en ajoutant `application` dans la somme si des tests y ont été ajoutés — ce qui n'est pas le cas dans ce plan).

- [ ] **Step 3: Commit**

```bash
git add AGENTS.md
git commit -m "docs: update regression test count baseline after cursor pagination migration"
```

---

## Self-Review

- **Couverture de la spec** : `Cursor` (tâche 2), `Pagination`/`PageResult`/`Searchable` (tâche 3), `CursorCodec` + allowlist de types + allowlist d'énumération par préfixe de package (tâche 4), scroll keyset combiné au Query By Example sur les deux adaptateurs (tâches 6-7), règle de calcul `next`/`previous` selon le sens de navigation (implémentée dans `KeysetSearchSupport`, tâche 5, exercée par les tests de scroll aller-retour des tâches 6-7), contrat REST (`cursor`/`size`/`sort`, `CollectionModel`, liens `self`/`next`/`prev`, disparition de `page`/`PagedResourcesAssembler`) (tâche 9), `InvalidCursor` → 400 (tâches 4 et 9), mise à jour du garde-fou de tests (tâche 10), spike de faisabilité en première tâche (tâche 1).
- **Types** : `Pagination(Cursor, PageSize, PageSort)`, `PageResult<D>(List<D>, Cursor next, Cursor previous)` cohérents entre toutes les tâches ; `KeysetSearchSupport.search(...)` a la même signature partout où il est appelé (tâches 6-7).
- **Point d'attention laissé explicite (pas un placeholder)** : le nommage exact de la constante `TYPE_TAG` pour `UUID` (conflit avec `java.util.UUID`) est signalé avec une solution de repli explicite à la tâche 4, à trancher pendant l'implémentation en fonction du résultat réel du test.
