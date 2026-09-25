# Plan — Gestion des vending machines dans le frontend (édition et suppression)

## Problème

`frontend/src/app/features/machines/` couvre aujourd'hui la liste
(`MachineList`), la création (`MachineCreate`) et le détail
(`MachineDetail`, avec consultation du stock et commande d'un item pour
`ROLE_USER`). Il manque les deux opérations d'administration exposées en
affordances sur le lien `self` de chaque machine par
`VendingMachineDtoModelAssembler` : la **mise à jour** (`PUT`) et la
**suppression** (`DELETE`).

## Approche

Ajouter une page dédiée `/machines/:id/edit` et une suppression confirmée
par dialog, en suivant les patrons déjà établis dans le module :

- **Hypermédia d'abord.** `VendingMachineDtoModelAssembler` pose sur le
  lien `self` de chaque machine deux affordances — `update` et `delete` —
  pointant toutes deux sur `/api/v1/vending-machines/{id}`. L'URL de
  mutation est donc **le lien `self` lui-même** ; il est déjà transmis du
  listing au détail via l'état de navigation
  (`[state]="{ href: machine._links?.self?.href }"` dans
  `machine-list.html`, relu par `MachineDetail`). Poursuivre cette chaîne
  jusqu'à la page d'édition plutôt que de rebâtir une URL.
- `machineUrl(id)` (déjà documenté comme repli dans
  `vending-machine-api.ts`) reste le **seul** recours en entrée directe
  sur `/machines/:id/edit` (rafraîchissement, favori).
- Signal Forms + `parseValidationErrors` comme `MachineCreate` ;
  `HttpClient` pour les mutations, `httpResource()` pour la lecture ;
  dialog Material calqué sur `OrderConfirmDialog` pour la confirmation
  destructive.

## Contraintes backend relevées (vérifiées dans le code)

| Point | Détail |
| --- | --- |
| `PUT /api/v1/vending-machines/{id}` | `200` + `EntityModel<VendingMachineDto>` |
| `VendingMachineToUpdateDto` | **remplacement complet** : `address`, `temperature`, `itemType`, `powerStatus`, `workingStatus`, `rfidStatus`, `smartCardStatus`, `changeMoneyStatus` tous `@NotNull` ; seul `lastIntervention` est optionnel |
| `AddressDto` (update) | forme complète de l'adresse — **différente** de `AddressToCreateDto` utilisée par `MachineCreate` (à vérifier champ à champ lors de l'implémentation) |
| `serialNumber` | **absent** du DTO de mise à jour → non modifiable, à afficher en lecture seule |
| `DELETE /api/v1/vending-machines/{id}` | `204`, pas de corps |
| Autorisations | `WebSecurityConfig` : `GET /api/v1/vending-machines/**` est `permitAll`, mais `anyRequest().hasRole("ADMIN")` couvre `PUT`/`DELETE` → écritures réservées aux admins |
| Affordances | `self` porte `update` + `delete` ; la collection porte `create` (`VendingMachinePagedModelAssembler`) |
| Autres rels | `status:reset`, `status:report`, `orders:report` sur la machine ; `stock`, et sur la ressource stock `stock:provision` / `stock:report` — **hors périmètre** |

## Décisions actées avec l'utilisateur

- Périmètre : **édition (`PUT`) et suppression (`DELETE`) uniquement**.
- Le reset des statuts, l'approvisionnement du stock et les trois
  rapports sont **explicitement hors périmètre** de ce plan.
- Le formulaire d'édition expose **tous** les champs, y compris les
  quatre statuts et la température.
- UI d'édition : **page dédiée** `/machines/:id/edit`.

## Todos

1. **Persister les specs** — créer `docs/specs/`, y écrire ce plan sous
   `frontend-gestion-vending-machines.md` et y déplacer le plan CRUD des
   items sous `frontend-crud-items.md`.
2. **Modèles** — dans `features/machines/models/vending-machine.ts`,
   ajouter `VendingMachineToUpdate` (adresse complète + `temperature` +
   `itemType` + les 4 statuts + `lastIntervention`) et un helper
   `toUpdatePayload(machine: VendingMachine): VendingMachineToUpdate`
   isolant la conversion depuis la ressource lue. Vérifier au passage que
   `Address` (lecture) et la forme attendue par `AddressDto` (écriture)
   coïncident ; sinon prévoir un type d'écriture distinct.
3. **Énumérations** — `models/enums.ts` ne définit que des *types*
   TypeScript. Ajouter les tableaux de valeurs correspondants
   (`POWER_STATUSES`, `WORKING_STATUSES`, `CARD_SYSTEM_STATUSES`,
   `CHANGE_SYSTEM_STATUSES`) sur le modèle de `ITEM_TYPES` dans
   `shared/models/item-type.ts`, pour alimenter les `mat-select`.
4. **vending-machine-api** — ajouter les helpers de lecture des
   affordances `update`/`delete` portées par le lien `self`
   (`updateHref(machine)`, `deleteHref(machine)` lisant `_templates` /
   `_links.self.href`), en gardant `machineUrl(id)` comme repli documenté
   pour l'entrée directe.
5. **MachineEdit** — `features/machines/machine-edit/`, route
   `/machines/:id/edit` protégée par `adminGuard`. Générer via
   `ng generate component`. Charger la machine par `httpResource()` en
   suivant le `self` transmis par l'état de navigation depuis le détail,
   préremplir le Signal Form, afficher `serialNumber` en lecture seule,
   `required` sur tous les champs obligatoires, `mat-select` pour
   `itemType` et les 4 statuts. Soumission : `PUT` sur le lien `self`
   puis navigation vers `/machines/:id`. Erreurs traitées par
   `parseValidationErrors` avec la liste plate des chemins de champs
   (`FORM_FIELDS`), comme dans `MachineCreate`.
6. **`lastIntervention`** — champ non modifiable dans ce plan : l'afficher
   en lecture seule (`| date: 'short'`) et le **renvoyer tel quel** dans
   le payload pour ne pas l'effacer par un `PUT` de remplacement complet.
7. **MachineDeleteDialog** — `features/machines/machine-delete-dialog/`,
   calqué sur `OrderConfirmDialog` (rappel du numéro de série dans le
   texte de confirmation).
8. **Suppression** — câbler le dialog depuis la liste **et** depuis le
   détail : `DELETE` sur le lien `self`, puis snackbar + `reload()` de la
   ressource de liste, ou navigation vers `/machines` depuis le détail.
9. **Détail** — ajouter dans `machine-detail.html` les actions
   « Éditer » / « Supprimer », rendues uniquement si `isAdmin()` **et** si
   l'affordance correspondante est présente. Propager le `self` à la page
   d'édition via `[state]`.
10. **Liste** — enrichir la colonne `actions` de `machine-list.html` avec
    les boutons éditer/supprimer réservés à `isAdmin()`, en passant le
    `self` de la ligne.
11. **Routes** — déclarer `/machines/:id/edit` dans `app.routes.ts`
    (`loadComponent`, `title`, `adminGuard`), **après** `machines/new` et
    de façon à ne pas être capté par `machines/:id`.
12. **Tests unitaires** — un `.spec.ts` par fichier ajouté, avec
    `HttpTestingController`. Rappel du module : avec `httpResource()`,
    déclencher le rendu puis **flush la requête avant** `whenStable()`.
    Couvrir : préremplissage du formulaire depuis la ressource, payload
    `PUT` complet (statuts et `lastIntervention` inclus), suppression
    confirmée vs annulée, masquage des actions pour un non-admin.
13. **Non-régression des specs existantes** — `machine-list.spec.ts` et
    `machine-detail.spec.ts` seront impactés par les nouvelles actions
    (assertions sur `displayedColumns`, nouveaux boutons) ; les mettre à
    jour plutôt que de les contourner.
14. **Test e2e** — `frontend/e2e/manage-machines.spec.ts` (API mockée,
    dans la lignée de `order-item.spec.ts`) : édition d'une machine puis
    suppression depuis la liste.
15. **Validation** — `npm test` puis `npm run e2e` depuis `frontend/`,
    puis **`build-brief ./gradlew clean build`** à la racine (toute
    commande Gradle passe par `build-brief`, cf. `AGENTS.md`).
16. **Documentation** — mettre à jour `frontend/README.md` (sections
    « Périmètre actuel » / « Reste à faire ») et
    `docs/features-front-a-implementer.md` pour retirer
    l'édition/suppression de machine du backlog, en y renvoyant vers le
    nouveau fichier de `docs/specs/`.

## Notes et points de vigilance

- **`PUT` = remplacement complet.** Tous les champs étant `@NotNull`, un
  formulaire partiel écraserait des données par `null` et produirait un
  `400`. Le préremplissage exhaustif depuis la ressource lue est donc une
  exigence fonctionnelle, pas un confort.
- **Ordre des routes.** `machines/:id` capterait `machines/:id/edit` si la
  route d'édition n'est pas déclarée correctement — vérifier par un test
  de routage.
- **Cohérence avec le plan items.** Les deux plans introduisent un dialog
  de confirmation de suppression très proche. Ne pas factoriser
  prématurément : si le second arrive après le premier et que les deux
  sont identiques au libellé près, alors seulement extraire un
  `shared/confirm-dialog/`.
- **Conventions impératives** (`frontend/AGENTS.md`) : nommage 2025
  (`machine-edit.ts`), `@Service()` et non
  `@Injectable({ providedIn: 'root' })`, `httpResource()` en lecture /
  `HttpClient` en mutation, Signal Forms (`@angular/forms/signals`),
  organisation par feature, génération via Angular CLI + skill
  `angular-developer`, tests lancés uniquement par `npm test`.
- Le backend n'est pas modifié : aucun test Java à ajouter, le
  `build-brief ./gradlew clean build` ne sert qu'à confirmer l'absence de
  régression et à exécuter l'agrégation frontend. Conserver le chemin du
  log brut affiché par `build-brief` en cas d'échec.
