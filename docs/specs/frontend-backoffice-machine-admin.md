# Plan — Back-office machine dans le frontend (stock, statuts, rapports)

## Problème

`MachineDetail` affiche aujourd'hui le stock en lecture seule et permet
la commande d'un item pour `ROLE_USER`, mais aucune des actions
d'administration exposées par le backend et rattachées à une machine
n'a de traduction frontend :

- **approvisionnement du stock** (`POST /vending-machines/{id}/stock`,
  rel `stock:provision` sur la ressource stock) ;
- **reset des statuts d'erreur** (`POST /vending-machines/{id}/reset`,
  rel `status:reset` sur la machine) ;
- **trois rapports en écriture seule** (générés à la demande, non
  consultables a posteriori) : stock (`stock:report`), statut
  (`status:report`), commandes clients (`orders:report`).

Ces quatre opérations sont `ROLE_ADMIN` (`anyRequest().hasRole("ADMIN")`
dans `WebSecurityConfig`, la seule exception étant le `GET` du stock et
de la machine elle-même, déjà `permitAll`).

## Approche

Étendre `MachineDetail` (pas de nouvelle route) avec une section
**« Administration »**, visible uniquement pour `isAdmin()`, en dessous
de la section stock déjà existante :

- un formulaire d'approvisionnement (sélection d'un item + quantité) ;
- un bouton de reset des statuts ;
- trois boutons « Générer le rapport... » (stock / statut / commandes),
  chacun ouvrant un dialog affichant le contenu retourné par le `POST`
  correspondant.

Les trois rapports ne sont **ni paginés ni relisibles** : c'est le corps
de la réponse `POST` qui porte l'unique donnée disponible, cohérent avec
la persistance en écriture seule des entités de rapport côté backend
(`VendingMachineStockReportRepositoryAdapter`/
`VendingMachineStatusReportRepositoryAdapter`, `SimpleJpaRepository`
sans `GET` associé). Un dialog éphémère suffit donc, pas de page dédiée.

Hypermédia d'abord : tous les liens (`status:reset`, `status:report`,
`stock` → `stock:provision`/`stock:report`, `orders:report`) sont déjà
présents sur les ressources machine/stock chargées par `MachineDetail`
(`VendingMachineDtoModelAssembler`, `VendingMachineStockDtoModelAssembler`),
aucune URL à construire à la main.

## Contraintes backend relevées (vérifiées dans le code)

| Point | Détail |
| --- | --- |
| `POST /vending-machines/{id}/stock` | corps `ItemToProvisionDto` (`itemId`, `quantity: int`) ; `200` + `EntityModel<VendingMachineStockDto>` (le stock complet à jour, mêmes rels `item`/`order` que le `GET`) |
| `POST /vending-machines/{id}/reset` | pas de corps ; `200` + `EntityModel<VendingMachineDto>` (statuts remis à zéro) |
| `POST /vending-machines/{id}/stock/report` | pas de corps ; `200` + `VendingMachineStockReportDto` **brut** (pas d'`EntityModel`, aucun lien HAL) : `{id, serialNumber, stockEntries: [{itemName, quantity}]}` |
| `POST /vending-machines/{id}/status/report` | pas de corps ; `200` + `EntityModel<VendingMachineStatusReportDto>` (seul rapport enveloppé, porte un rel `vendingMachine` inutile ici) : `{serialNumber, lastIntervention, temperature, powerStatus, workingStatus, rfidStatus, smartCardStatus, changeMoneyStatus}` |
| `POST /vending-machines/{id}/orders/report` | pas de corps ; `200` + `VendingMachineClientOrdersReportDto` **brut** : `{serialNumber, clientOrders: [{itemName, itemPrice, orderedAt}], totalAmount, reportedAt}` |
| Sélection d'item à approvisionner | pas d'endpoint de recherche/autocomplete côté items ; `GET /items` est paginé (`ROLE_ADMIN`) mais sans filtre texte — se contenter d'un chargement en une page suffisamment grande (le catalogue d'une machine à sodas reste réduit) plutôt que d'ajouter une pagination dans le `mat-select` |
| Autorisations | les quatre endpoints ci-dessus sont couverts par `anyRequest().hasRole("ADMIN")` (aucun matcher spécifique dans `WebSecurityConfig`) |
| Relation HAL | `status:reset`/`status:report`/`stock`/`orders:report` portés par `VendingMachineDtoModelAssembler` sur le `self` de la machine ; `stock:provision`/`stock:report` portés par `VendingMachineStockDtoModelAssembler` sur le `self` du stock |

## Décisions actées avec l'utilisateur

- Périmètre : **approvisionnement du stock, reset des statuts, et les
  trois rapports**. Pas de nouvelle route : tout se passe dans une
  section « Administration » de `MachineDetail`, visible uniquement pour
  `isAdmin()`.
- Les rapports sont **affichés dans un dialog** à la génération, pas
  persistés/consultables ensuite côté frontend (cohérent avec l'absence
  de `GET` côté backend).
- Le bouton de reset des statuts n'est affiché que si la machine est
  dans un **état anormal** (règle frontend uniquement : le backend
  accepte toujours le reset, `VendingMachineStatusApplicationService
  .resetStatus` n'a aucune précondition). « Anormal » est défini comme
  l'inverse de `VendingMachineStatus.isAllSystemClear()` côté domaine :
  `workingStatus != WORKING` (ou machine éteinte), `rfidStatus == FAILED`,
  `smartCardStatus == FAILED`, ou `changeMoneyStatus != NORMAL`.
- Le formulaire d'approvisionnement utilise un `mat-select` alimenté par
  un chargement simple (une page) de `GET /items`, sans recherche.

## Todos

1. **Modèles** — `features/machines/models/vending-machine.ts` : ajouter
   `ItemToProvision` (`itemId`, `quantity`), `VendingMachineStockReport`,
   `VendingMachineStatusReport` (`extends HalResource` pour le rel
   `vendingMachine`, même si non utilisé), `VendingMachineClientOrdersReport`
   — types miroir des DTO backend, à écrire à la main
   (`frontend/AGENTS.md`).
2. **vending-machine-api** — ajouter les helpers de lecture des rels
   `status:reset`, `status:report`, `stock:provision` (porté par la
   ressource stock), `stock:report`, `orders:report` depuis les
   `_links`/`_templates` des ressources déjà chargées par `MachineDetail`.
3. **items pour le sélecteur** — dans `features/items/item-api.ts`,
   ajouter un helper de lecture d'une page unique d'items (réutiliser
   `itemsPageUrl` avec une taille de page fixe, ex. 100), consommé par le
   nouveau formulaire de provisionnement.
4. **ProvisionStockForm** — sous-composant ou section intégrée à
   `MachineDetail` (à trancher à l'implémentation selon la taille du
   template ; générer via `ng generate component` si extrait). Signal
   Form `itemId`/`quantity` (`required`, quantité positive), `mat-select`
   des items. Soumission : `POST` sur `stock:provision`, snackbar, puis
   `stockResource.reload()` (le corps de la réponse suffirait aussi, mais
   `reload()` reste cohérent avec le pattern déjà en place pour
   `orderItem`).
5. **Reset des statuts** — bouton « Réinitialiser les statuts » dans la
   section administration, affiché **uniquement si la machine est en
   état anormal** (helper `isAbnormalStatus(machine): boolean` dans
   `machine-detail.ts`, miroir de `VendingMachineStatus.isAllSystemClear()`
   — cf. décision ci-dessus ; recalculé à chaque `reload()` puisque
   dérivé des champs déjà chargés, pas de nouvel appel réseau).
   Soumission : `POST` sur `status:reset`, snackbar de succès, puis
   `resource.reload()` de la machine (les statuts affichés dans la
   section infos et la visibilité du bouton doivent se rafraîchir — le
   bouton disparaît une fois les statuts redevenus normaux).
6. **MachineReportDialog** — `features/machines/machine-report-dialog/`
   (généré via `ng generate component`), un seul composant de dialog
   paramétré par une donnée discriminée
   `{ kind: 'stock' | 'status' | 'orders'; report: ... }`, calqué sur
   `OrderConfirmDialog` pour la structure. Un template par branche
   (`@switch` sur `kind`) : tableau `itemName`/`quantity` pour le stock,
   `dl` des statuts pour le statut, tableau `itemName`/`itemPrice`/
   `orderedAt` + `totalAmount` pour les commandes.
7. **Boutons de rapport** — trois boutons dans la section
   administration, chacun : `POST` sur le rel correspondant, ouverture
   de `MachineReportDialog` avec les données reçues (pas de rechargement
   d'autre ressource, ces appels sont sans effet de bord sur le stock/la
   machine).
8. **`machine-detail.html`** — nouvelle section « Administration »
   conditionnée à `isAdmin()`, sous la section stock existante.
9. **Tests unitaires** — étendre `machine-detail.spec.ts` (ou extraire
   des specs dédiées si des sous-composants sont créés) avec
   `HttpTestingController` : provisionnement (succès + validation
   quantité), reset de statut (bouton visible sur machine en état
   anormal, absent sur machine à l'état normal, disparition après un
   reset réussi), les trois générations de rapport (contenu affiché dans
   le dialog), masquage total de la section pour un non-admin.
10. **Test e2e** — `frontend/e2e/manage-machine-admin.spec.ts` (API
    mockée) : approvisionnement, reset, génération d'un rapport.
11. **Validation** — `npm test` puis `npm run e2e` depuis `frontend/`,
    puis **`build-brief ./gradlew clean build`** à la racine.
12. **Documentation** — mettre à jour `frontend/README.md` et
    `docs/features-front-a-implementer.md` pour sortir stock/statut/
    rapports du backlog, en renvoyant vers ce fichier.

## Notes et points de vigilance

- **Le reset conditionnel est une règle frontend, pas backend.** Le
  endpoint `POST /reset` reste appelable sans précondition côté serveur ;
  masquer le bouton en état normal est un choix d'UX (éviter une action
  inutile), pas une contrainte de sécurité — un appel direct à l'API
  resterait accepté même hors de ce cas. Ne pas ajouter de validation
  bloquante côté backend dans le cadre de cette spec.
- **Ne pas construire de page de « rapports »/historique.** Le backend
  ne stocke ces rapports que pour un usage interne
  (`SimpleJpaRepository` en écriture seule, cf. `AGENTS.md` racine) ; il
  n'existe aucun `GET` pour les relire. Ne pas ajouter de liste/
  historique côté frontend qui laisserait croire à une persistance
  consultable.
- **Rapport de statut vs rapport de stock/commandes : forme différente.**
  Seul le rapport de statut est enveloppé dans un `EntityModel` (avec un
  rel `vendingMachine` sans utilité ici) ; les deux autres sont des DTO
  bruts sans `_links`. Le composant de dialog doit lire directement le
  corps de la réponse dans les trois cas (ne pas tenter de désembaler un
  `_embedded`/`_links` inexistant pour stock/commandes).
- **Cohérence avec les autres plans.** Cette spec suppose que
  `frontend-gestion-vending-machines.md` (édition/suppression) est déjà
  implémentée ou en cours : la section administration s'ajoute à la même
  page `MachineDetail` sans la modifier structurellement.
- **Conventions impératives** (`frontend/AGENTS.md`) : nommage 2025,
  `@Service()`, `httpResource()` en lecture / `HttpClient` en mutation,
  Signal Forms, organisation par feature, génération via Angular CLI +
  skill `angular-developer`, tests lancés uniquement par `npm test`.
- Le backend n'est pas modifié : aucun test Java à ajouter, le
  `build-brief ./gradlew clean build` ne sert qu'à confirmer l'absence de
  régression et à exécuter l'agrégation frontend. Conserver le chemin du
  log brut affiché par `build-brief` en cas d'échec.
