# Features front à implémenter

Analyse basée sur les contrôleurs REST du backend
(`backend/infrastructure/.../rest/controller/`) et les règles d'accès de
`WebSecurityConfig`, comparée à l'existant côté frontend.

## ✅ Déjà fait

- Login, inscription (avec auto-login), liste des machines (lecture seule,
  paginée)
- **Détail d'une machine** (`GET /vending-machines/{id}`) — page en lecture
  seule (infos machine, statuts) ; ne liste pas encore les items ni ne
  propose de commande
- **Création d'une machine** (`ROLE_ADMIN`, `POST /vending-machines`) —
  formulaire Signal Forms dédié (`machines/new`)

## 🌐 Public / client (sans authentification)

1. **Items d'une machine + commande** — la page détail machine n'affiche pas
   encore les items disponibles ni un flow d'achat
   (`POST /vending-machines/{id}/order/{itemId}`), cœur métier, public
2. **Détail/visuel d'un item** (`GET /items/{itemId}/**`, image publique)

## 👤 Espace utilisateur connecté (`/api/v1/me/**`)

3. **Profil** : consulter/modifier ses infos (`GET`/`PUT /me`)
4. **Photo de profil** : afficher/uploader (`GET`/`POST /me/picture`)
5. **Changement de mot de passe** (`POST /me/password`)

## 🔐 Back-office admin (`ROLE_ADMIN`, tout le reste)

6. **Gestion des items** : CRUD complet + upload d'image (`/items`)
7. **Gestion des machines** : modification / suppression (la création est
   faite, il manque édition et suppression pour un CRUD complet)
8. **Gestion du stock d'une machine** : ajouter du stock, consulter le stock,
   rapport de stock (`/vending-machines/{id}/stock`, `/stock/report`)
9. **Statut machine** : reset (`/reset`), rapport de statut
   (`/status/report`)
10. **Rapport des commandes** par machine (`/orders/report`)

## Priorité suggérée

1. Items d'une machine + commande (point 1, cœur métier client)
2. Espace profil (points 3-5)
3. Back-office admin (points 6-10, le plus gros lot)
