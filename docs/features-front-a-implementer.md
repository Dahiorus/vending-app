# Features front à implémenter

Analyse basée sur les contrôleurs REST du backend
(`backend/infrastructure/.../rest/controller/`) et les règles d'accès de
`WebSecurityConfig`, comparée à l'existant côté frontend.

## ✅ Déjà fait

- Login, inscription (avec auto-login), liste des machines (lecture seule,
  paginée)

## 🌐 Public / client (sans authentification)

1. **Détail d'une machine** (`GET /vending-machines/{id}`) — page dédiée
   (actuellement seule la liste existe)
2. **Détail/visuel d'un item** (`GET /items/{itemId}/**`, image publique)
3. **Passer une commande** (`POST /vending-machines/{id}/order/{itemId}`) —
   flow d'achat, cœur métier, public (pas besoin d'être connecté)

## 👤 Espace utilisateur connecté (`/api/v1/me/**`)

4. **Profil** : consulter/modifier ses infos (`GET`/`PUT /me`)
5. **Photo de profil** : afficher/uploader (`GET`/`POST /me/picture`)
6. **Changement de mot de passe** (`POST /me/password`)

## 🔐 Back-office admin (`ROLE_ADMIN`, tout le reste)

7. **Gestion des items** : CRUD complet + upload d'image (`/items`)
8. **Gestion des machines** : création / modification / suppression (le CRUD
   complet, au-delà de la liste)
9. **Gestion du stock d'une machine** : ajouter du stock, consulter le stock,
   rapport de stock (`/vending-machines/{id}/stock`, `/stock/report`)
10. **Statut machine** : reset (`/reset`), rapport de statut
    (`/status/report`)
11. **Rapport des commandes** par machine (`/orders/report`)

## Priorité suggérée

1. Détail machine + commande (point 3, cœur métier client)
2. Espace profil (points 4-6)
3. Back-office admin (points 7-11, le plus gros lot)
