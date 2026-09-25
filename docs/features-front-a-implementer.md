# Features front à implémenter

Analyse basée sur les contrôleurs REST du backend
(`backend/infrastructure/.../rest/controller/`) et les règles d'accès de
`WebSecurityConfig`, comparée à l'existant côté frontend.

## ✅ Déjà fait

- Login, inscription (sans auto-login — l'utilisateur doit se connecter
  ensuite), liste des machines (lecture seule, paginée)
- **Détail d'une machine** (`GET /vending-machines/{id}`) — infos machine,
  **stock consulté et commande d'un item** (`POST
  /vending-machines/{id}/order/{itemId}`) pour un utilisateur `ROLE_USER`
- **Création d'une machine** (`ROLE_ADMIN`, `POST /vending-machines`) —
  formulaire Signal Forms dédié (`machines/new`)
- **Liste des items** (`ROLE_ADMIN`, `GET /items`) — lecture seule, paginée
  (`items`), sans création/édition/suppression ni image

## 🌐 Public / client (sans authentification)

1. **Détail/visuel d'un item** (`GET /items/{itemId}/**`, image publique) —
   pas encore d'affichage d'image d'item côté frontend

## 👤 Espace utilisateur connecté (`/api/v1/me/**`)

2. **Profil** : consulter/modifier ses infos (`GET`/`PUT /me`)
3. **Photo de profil** : afficher/uploader (`GET`/`POST /me/picture`)
4. **Changement de mot de passe** (`POST /me/password`)

## 🔐 Back-office admin (`ROLE_ADMIN`, tout le reste)

5. **Gestion des items** : création/édition/suppression + upload d'image
   (`/items`) — seule la liste en lecture seule existe
6. **Gestion des machines** : modification / suppression (la création est
   faite, il manque édition et suppression pour un CRUD complet)
7. **Gestion du stock d'une machine** : ajouter du stock, rapport de stock
   (`/vending-machines/{id}/stock`, `/stock/report`) — la consultation du
   stock existe déjà côté client (point commande), pas côté admin ni le
   rapport
8. **Statut machine** : reset (`/reset`), rapport de statut
   (`/status/report`)
9. **Rapport des commandes** par machine (`/orders/report`)

## Priorité suggérée

1. Espace profil (points 2-4)
2. Back-office admin (points 5-9, le plus gros lot)
3. Visuel item public (point 1)
