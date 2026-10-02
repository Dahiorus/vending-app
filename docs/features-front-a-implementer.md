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
- **Gestion des items** (`ROLE_ADMIN`, `/items`) — liste paginée, détail,
  création, édition du prix, suppression et upload d'image
- **Espace profil utilisateur** (`/profile`, `ROLE_USER`) — consultation/
  édition des informations personnelles (`GET`/`PUT /me`), photo de profil
  (`GET`/`POST /me/picture`) et changement de mot de passe
  (`POST /me/password`)

## 🌐 Public / client (sans authentification)

1. **Détail/visuel d'un item** (`GET /items/{itemId}/**`, image publique) —
   pas encore d'affichage d'image d'item côté frontend

## 👤 Espace utilisateur connecté (`/api/v1/me/**`)

Tous les points identifiés pour l'espace profil sont implémentés côté frontend.

## 🔐 Back-office admin (`ROLE_ADMIN`, tout le reste)

2. **Gestion des machines** : modification / suppression (la création est
   faite, il manque édition et suppression pour un CRUD complet)
3. **Gestion du stock d'une machine** : ajouter du stock, rapport de stock
   (`/vending-machines/{id}/stock`, `/stock/report`) — la consultation du
   stock existe déjà côté client (point commande), pas côté admin ni le
   rapport
4. **Statut machine** : reset (`/reset`), rapport de statut
   (`/status/report`)
5. **Rapport des commandes** par machine (`/orders/report`)

## Priorité suggérée

1. Back-office admin (points 2-5, le plus gros lot)
2. Visuel item public (point 1)
