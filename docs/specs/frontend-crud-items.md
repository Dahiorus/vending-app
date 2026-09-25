# Plan — CRUD des items dans le frontend (avec upload d'image)

## Problème

`frontend/src/app/features/items/` est aujourd'hui strictement en lecture
seule : `ItemList` affiche une table paginée (`name`/`type`/`price`) via
`httpResource<HalPage<Item>>`. Aucune création, édition, suppression, ni
aucune gestion d'image (le mot `picture`/`image` n'apparaît nulle part dans
`frontend/src/app`), alors que le backend expose déjà tout le nécessaire.

## Approche

Compléter la feature `items` en réutilisant à l'identique les patrons déjà
en place côté machines : Signal Forms + `parseValidationErrors` pour les
formulaires (cf. `MachineCreate`), `httpResource()` pour la lecture,
`HttpClient` pour les mutations, liens HATEOAS pour la navigation, dialog
Material pour la confirmation destructive (cf. `OrderConfirmDialog`).

L'édition soumet **deux appels** : `PUT /api/v1/items/{id}` (prix) puis, si
un fichier a été sélectionné, `POST /api/v1/items/{id}/image`
(`multipart/form-data`, champ `file`). La création suit la même logique :
`POST /api/v1/items` → récupération de l'`id` dans la réponse → upload
éventuel de l'image.

## Contraintes backend relevées (vérifiées dans le code)

| Point | Détail |
| --- | --- |
| `ItemToCreateDto` | `name` (`@NotBlank`), `type` (`@NotNull`), `price` (`@Positive @NotNull`) |
| `ItemToUpdateDto` | **`price` uniquement** (`@Positive`) — nom et type non modifiables |
| `PUT /items/{id}` | renvoie `200` + `EntityModel<ItemDto>` |
| `POST /items` | renvoie `201` + `Location` + `EntityModel<ItemDto>` (l'`id` est dans le corps) |
| `DELETE /items/{id}` | renvoie `204` |
| `POST /items/{id}/image` | `multipart/form-data`, param **`file`**, `200` ; `MultipartFileValidator` n'accepte que `image/jpeg` et `image/png` (sinon `IllegalArgumentException` → `400`) |
| `GET /items/{id}/image` | **`permitAll`** dans `WebSecurityConfig` (matcher `GET /api/v1/items/{itemId}/**`) → un `<img src>` simple suffit, pas besoin de blob/`Authorization` |
| Écritures | `anyRequest().hasRole("ADMIN")` → création/édition/suppression réservées aux admins |
| Relation HAL | l'image porte le rel **`item:image`** (`Relation.ITEM_IMAGE`), posé par `ItemDtoModelAssembler` sur chaque `ItemDto` |

## Décisions actées avec l'utilisateur

- Formulaire d'édition limité à **prix + image** ; nom et type affichés en
  lecture seule. Aucune modification du backend.
- Périmètre : **création, édition, suppression, page de détail**.
- UI d'édition : **page dédiée** `/items/:id/edit` (pas de dialog).
- L'upload d'image est proposé **aussi à la création**.
- Échec partiel (mutation OK mais upload KO) : on **reste sur le
  formulaire** avec un message d'erreur ciblé sur le champ image.

## Todos

1. **item-api** — étendre `features/items/item-api.ts` en s'appuyant
   d'abord sur l'hypermédia, pas sur des URL construites à la main :
   - **création** : `ItemPagedModelAssembler` pose une *affordance* de
     `ItemCrudRestController#create` sur le lien `self` de la page d'items.
     L'URL de création est donc déjà connue du listing — l'extraire du
     `self`/`_templates` de la réponse de `ItemList` (helper
     `createItemHref(page)` lisant `_templates.default.target ?? _links.self.href`)
     et la transmettre au formulaire de création, plutôt que de réintroduire
     un `itemsUrl()` bâti depuis `environment.apiBaseUrl`.
   - **détail / édition / suppression / image** : utiliser les liens
     `self` et `item:image` portés par chaque `ItemDto`
     (`ItemDtoModelAssembler`).
   - ne conserver un `itemUrl(id)`/`itemImageUrl(id)` bâti depuis
     `environment.apiBaseUrl` que comme **repli** pour l'entrée directe sur
     `/items/:id` (rafraîchissement de page, aucun lien HATEOAS en mémoire),
     exactement comme le commentaire de `machineUrl()` dans
     `vending-machine-api.ts` le documente.
2. **modèles** — dans `features/items/models/item.ts`, ajouter
   `ItemToCreate` (`name`, `type`, `price`) et `ItemToUpdate` (`price`),
   et documenter le rel `item:image` porté par `Item extends HalResource`.
3. **shared: upload d'image** — petit composant réutilisable
   `shared/image-upload/` (sélection de fichier, validation côté client du
   type `image/jpeg`|`image/png`, aperçu via `URL.createObjectURL` avec
   révocation dans un `effect`/`DestroyRef`, `output` du `File` choisi).
   Générer via `ng generate component`.
4. **helper de mutation image** — fonction partagée
   `uploadItemImage(http, id, file)` dans `item-api.ts` construisant le
   `FormData` avec la clé `file` (ne pas fixer manuellement l'en-tête
   `Content-Type`, le navigateur doit poser le `boundary`).
5. **ItemCreate** — `features/items/item-create/`, route `/items/new`
   (`adminGuard`). Signal Form `name`/`type`/`price` (`required`, prix
   positif), `mat-select` alimenté par `ITEM_TYPES`, plus le composant
   d'upload. Soumission : `POST /items` → lecture de l'`id` de la réponse
   → upload si fichier → navigation vers `/items`. En cas d'échec de
   l'upload seul : rester sur la page, message ciblé sur l'image, ne pas
   rejouer le POST (item déjà créé → basculer le formulaire vers un état
   « item créé, image à réessayer »).
6. **ItemDetail** — `features/items/item-detail/`, route `/items/:id`.
   Lecture via `httpResource<Item>`, affichage nom/type/prix + image
   (`<img>` sur le lien `item:image`, avec repli visuel si `404`), boutons
   « Éditer » / « Supprimer » conditionnés à `auth.roles().includes('ROLE_ADMIN')`.
7. **ItemEdit** — `features/items/item-edit/`, route `/items/:id/edit`
   (`adminGuard`). Nom et type en lecture seule, Signal Form sur `price`,
   composant d'upload pré-rempli avec l'image courante. Soumission :
   `PUT /items/{id}` puis upload conditionnel ; en cas de succès complet,
   navigation vers `/items/:id` ; en cas d'échec de l'upload, rester sur
   le formulaire avec l'erreur sur le champ image.
8. **Suppression** — `ItemDeleteDialog` (calqué sur `OrderConfirmDialog`),
   déclenché depuis la liste et depuis le détail. `DELETE` puis snackbar
   et `reload()` de la ressource (liste) ou navigation vers `/items`
   (détail).
9. **ItemList** — ajouter un bouton « Nouvel item », une colonne
   `actions` (éditer / supprimer) et rendre les lignes cliquables vers le
   détail. Les actions d'écriture ne sont rendues que pour `ROLE_ADMIN`.
10. **Routes** — déclarer `/items/new`, `/items/:id`, `/items/:id/edit`
    dans `app.routes.ts` avec `loadComponent`, `title`, et `adminGuard`
    sur `new`/`edit` (l'ordre importe : `items/new` avant `items/:id`).
11. **Tests unitaires** — un `.spec.ts` par composant/fichier ajouté,
    avec `HttpTestingController`. Respecter la règle du module : avec
    `httpResource()`, déclencher le rendu, **flush la requête avant**
    `whenStable()`. Couvrir en particulier : l'enchaînement des deux
    appels à la soumission, le cas « fichier absent → un seul appel », et
    le cas d'échec partiel upload.
12. **Test e2e** — ajouter `frontend/e2e/manage-items.spec.ts` (API
    mockée, dans la lignée de `order-item.spec.ts`) couvrant le parcours
    création → édition avec image → suppression.
13. **Validation** — `npm test` puis `npm run e2e` depuis `frontend/`,
    et **`build-brief ./gradlew clean build`** à la racine (toute commande
    Gradle passe par `build-brief`, cf. `AGENTS.md`) pour vérifier la
    non-régression globale (251 tests backend + agrégation frontend).
14. **Documentation** — mettre à jour `frontend/README.md` (section
    « Périmètre actuel » / « Reste à faire ») et
    `docs/features-front-a-implementer.md` pour sortir le CRUD items du
    backlog.

## Notes et points de vigilance

- **Conventions impératives** (`frontend/AGENTS.md`) : nommage 2025
  (`item-edit.ts`, pas `item-edit.component.ts`), `@Service()` et non
  `@Injectable({ providedIn: 'root' })`, `httpResource()` en lecture /
  `HttpClient` en mutation, Signal Forms (`@angular/forms/signals`),
  organisation par feature, génération via Angular CLI + skill
  `angular-developer`, tests lancés uniquement par `npm test`.
- **Cache de l'image** : après un upload, l'URL `GET /items/{id}/image`
  est inchangée ; ajouter un paramètre de cache-busting (`?v=<timestamp>`
  issu d'un signal incrémenté après upload) pour que le détail affiche
  bien la nouvelle image.
- **Erreur d'upload** : `MultipartFileValidator` lève une
  `IllegalArgumentException`, et `RestResponseExceptionHandler` ne la gère
  **pas** (il ne couvre que `MethodArgumentNotValidException`,
  `InvalidBusinessObject`, `ResourceNotFound`, `ItemStockIsEmpty`,
  `UserNotAuthenticated`). Un type de fichier non supporté remonte donc en
  **500 sans payload de validation** : `parseValidationErrors` ne
  s'applique pas. Conséquences pour le frontend :
  - la validation du type (`image/jpeg`, `image/png`) doit être faite
    **côté client**, dans le composant d'upload, pour que ce 500 ne soit
    jamais atteint en usage normal (`accept` sur l'input **et** contrôle
    explicite de `file.type`, l'attribut `accept` étant contournable) ;
  - l'échec d'upload côté serveur se traduit par un **message générique**
    sur le champ image (« L'image n'a pas pu être envoyée. »), sans
    tentative de lecture d'un corps de validation ;
  - ne pas élargir le controller advice dans le cadre de ce plan (aucune
    modification backend actée) — le noter comme amélioration backend
    possible et distincte.
- **Non-régression `item-list.spec.ts`** : l'ajout de la colonne
  `actions` et du routage sur les lignes impacte les assertions
  existantes sur `displayedColumns`.
- Le backend n'étant pas modifié, aucun test Java n'est à ajouter ; le
  `build-brief ./gradlew clean build` ne sert ici qu'à confirmer l'absence
  de régression et à exécuter l'agrégation frontend. Ne pas invoquer
  `./gradlew` directement : passer systématiquement par `build-brief`, et
  conserver le chemin du log brut qu'il affiche en cas d'échec.
