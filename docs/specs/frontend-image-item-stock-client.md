# Plan — Image d'item dans la vue stock client (`MachineDetail`)

## Problème

`GET /api/v1/items/{itemId}/**` (image d'item) est `permitAll` dans
`WebSecurityConfig` : n'importe qui peut afficher l'image d'un item sans
authentification. Pourtant, la vue stock de `MachineDetail` (déjà
implémentée, visible par un `ROLE_USER` avant commande) n'affiche que le
nom de l'item, jamais son image — c'est le seul point du backlog
`docs/features-front-a-implementer.md` marqué « public / sans
authentification ».

Portée volontairement réduite : il ne s'agit pas d'une nouvelle page ni
d'un nouveau flux, seulement d'enrichir un affichage existant.

## Approche

Ajouter une vignette `<img>` par ligne de la table de stock dans
`machine-detail.html`, à côté du nom de l'item.

**Absence de rel HATEOAS dédié.** `VendingMachineStockDtoModelAssembler`
n'expose que les rels `item` (vers `ItemCrudRestController.read`) et
`order` par entrée de stock — pas de rel `item:image` (celui-ci n'existe
que sur `ItemDto`, chargé uniquement par la page de détail item du
back-office). Suivre ici le même repli assumé que pour `machineUrl()`/
`itemUrl()` : construire l'URL d'image directement à partir de l'`itemId`
déjà connu (`GET /items/{itemId}/image`), plutôt que d'ajouter un aller-
retour réseau supplémentaire (`GET` de l'item complet) juste pour
récupérer son lien d'image.

## Contraintes backend relevées (vérifiées dans le code)

| Point | Détail |
| --- | --- |
| `GET /items/{itemId}/image` | `permitAll` (matcher `GET /api/v1/items/{itemId}/**`) → `<img src>` simple, sans jeton ; `404` si l'item n'a pas d'image |
| Forme de l'URL | déterministe, indépendante d'un lien HAL : `${apiBaseUrl}/items/{itemId}/image` |

## Décisions actées avec l'utilisateur

- Traitement en tant qu'ajout ciblé sur `MachineDetail`, pas de nouvelle
  route ni de nouveau composant de page.
- Image absente (`404`) : même traitement que pour la spec profil —
  repli visuel (icône Material), pas de requête répétée ni de message
  d'erreur intrusif.

## Todos

1. **`itemImageUrl(id)`** — dans `features/items/item-api.ts` (prévu par
   `frontend-crud-items.md`), s'assurer que ce helper existe et
   l'exporter ; sinon l'ajouter ici en premier s'il n'a pas encore été
   implémenté par le plan items.
2. **`machine-detail.ts`** — importer `itemImageUrl` depuis
   `features/items/item-api.ts` et exposer un helper
   `itemImageHref(itemId: string): string` pour le template.
3. **`machine-detail.html`** — dans la table de stock, ajouter une
   `<img>` (taille fixe, ex. 32×32) avant/à côté du nom de l'item,
   `[src]="itemImageHref(itemQuantity.itemId)"`, avec un gestionnaire
   `(error)` basculant sur une icône Material de repli (pas de fetch
   préalable pour vérifier l'existence : laisser l'`<img>` échouer et
   intercepter l'évènement `error`, plus simple qu'un aller-retour HTTP
   dédié).
4. **Tests unitaires** — étendre `machine-detail.spec.ts` : image
   affichée avec la bonne URL, repli sur l'icône quand l'image échoue à
   charger.
5. **Validation** — `npm test` depuis `frontend/`, puis
   **`build-brief ./gradlew clean build`** à la racine.
6. **Documentation** — mettre à jour `docs/features-front-a-implementer.md`
   pour sortir ce point du backlog, en renvoyant vers ce fichier.

## Notes et points de vigilance

- **Dépendance inter-feature assumée.** `machines` importe un helper de
  `items` (`itemImageUrl`) : acceptable, il s'agit d'une simple fonction
  pure de construction d'URL, pas d'un couplage de composants ou de
  modèles métier.
- **Ordre d'implémentation.** Cette spec suppose que `itemImageUrl` existe
  déjà (issue de `frontend-crud-items.md`) ; si ce plan n'a pas encore été
  implémenté, traiter le todo 1 avant de continuer.
- Le backend n'est pas modifié : le `build-brief ./gradlew clean build`
  ne sert qu'à confirmer l'absence de régression et à exécuter
  l'agrégation frontend.
