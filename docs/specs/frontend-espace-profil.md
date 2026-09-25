# Plan — Espace profil utilisateur dans le frontend

## Problème

`/api/v1/me/**` (`SelfServiceRestController`) n'a aucune traduction
frontend : pas de page pour consulter/modifier son nom, pas d'upload/
affichage de photo de profil, pas de changement de mot de passe. Rien
n'existe aujourd'hui sous `features/` pour ce sous-domaine.

**Restriction importante, vérifiée dans le code** : `/api/v1/me` est
scopé aux comptes `AppUser` (`AppUserRepositoryAdapter`), distincts des
comptes admin (`AdminUserRepositoryAdapter`). `AuthService.login` saute
déjà explicitement l'appel `GET /me` pour un compte `ROLE_ADMIN` (cf.
commentaire dans `core/auth/auth.ts`) car aucun profil `AppUser` n'existe
pour lui. Cette feature est donc **réservée aux comptes `ROLE_USER`**,
pas aux admins.

## Approche

Nouvelle feature `features/profile/`, page unique `/profile` en sections
(infos + photo + mot de passe), suivant les patrons déjà établis :
Signal Forms + `parseValidationErrors` pour les formulaires,
`httpResource()` pour la lecture du profil, `HttpClient` pour les
mutations, réutilisation du composant d'upload d'image prévu par
`frontend-crud-items.md` (todo 3, `shared/image-upload/`) plutôt que
d'en recréer un second.

Un lien « Mon profil » est ajouté dans la barre d'outils (`app.html`),
visible uniquement pour un utilisateur authentifié **non admin**.

## Contraintes backend relevées (vérifiées dans le code)

| Point | Détail |
| --- | --- |
| `GET /me` | `200` + `EntityModel<UserDto>` (`id`, `email`, `firstname`, `lastname`) ; lien `self` porte l'affordance `update`, plus les rels `me:password` (`SELF_PASSWORD`) et `me:picture` (`SELF_PICTURE`) avec leurs affordances respectives |
| `PUT /me` | `UserToUpdateDto` = **`firstname` + `lastname` uniquement** (`email` non modifiable, absent du DTO d'update) ; `200` + `EntityModel<UserDto>` |
| `POST /me/picture` | `multipart/form-data`, param **`file`**, `200` ; même validateur que les items (`MultipartFileValidator`, seuls `image/jpeg`/`image/png` acceptés, sinon 500 sans payload de validation — même limitation que documentée dans `frontend-crud-items.md`) |
| `GET /me/picture` | `200` + image, ou **`404`** si aucune photo n'a été uploadée (pas de corps JSON exploitable) |
| `POST /me/password` | corps `{oldPassword, newPassword}` (`EditPasswordRequestDto`) ; `204` si succès |
| Erreur mot de passe actuel incorrect | `OldPasswordNotMatch` → **`400`** avec seulement `{timestamp, message}` (**pas** de tableau `errors`) → `parseValidationErrors` renverra `null` : traiter comme un message générique, pas un champ de formulaire structuré |
| Erreur nouveau mot de passe invalide | `UserPasswordValidator` → `InvalidBusinessObject` → `400` avec `errors: [{field: "password", code, defaultMessage}]` — **le champ renvoyé s'appelle littéralement `"password"`**, pas `"newPassword"` ; à mapper manuellement vers le champ `newPassword` du formulaire (`parseValidationErrors(error, ['password'])` puis afficher `fieldErrors['password']` sous le champ nouveau mot de passe) |
| Politique de mot de passe | `PasswordPolicyProperties` (`app.password-policy.*`) : longueur min/max, min minuscules/majuscules/chiffres/caractères spéciaux — tous **optionnels** (`Optional<Integer>`), donc pas de règle client-side à coder en dur ; laisser le backend être la seule source de vérité et n'afficher que les erreurs qu'il renvoie |
| Autorisations | `/api/v1/me/**` → `.authenticated()` dans `WebSecurityConfig` (pas de restriction de rôle particulière, mais fonctionnellement réservé aux `ROLE_USER`, cf. ci-dessus) |
| Relation HAL | `self` (+ affordance `update`), `me:password` (+ affordance `updatePassword`), `me:picture` (+ affordance `uploadProfilePicture`), tous portés par `UserDtoModelAssembler` |

## Décisions actées avec l'utilisateur

- Page unique `/profile` avec sections (infos, photo, mot de passe), pas
  de pages séparées.
- Point d'entrée : lien « Mon profil » dans la barre d'outils
  (`app.html`), visible pour un utilisateur authentifié non admin.
- Photo absente (`404`) : afficher une icône/avatar Material par défaut,
  pas de message texte.
- Après un changement de mot de passe réussi : **déconnexion et
  redirection vers `/login`** par précaution (mesure de sécurité, même si
  le backend ne révoque pas explicitement les tokens en cours).

## Todos

1. **profile-api** — `features/profile/profile-api.ts`. Lecture via le
   lien `self` du `GET /me` (pas d'URL construite à la main, contrairement
   à `machineUrl()`/`itemUrl()` : `/me` est un singleton par utilisateur,
   toujours atteignable via l'environnement (`${apiBaseUrl}/me`) comme
   repli documenté, mais préférer les liens `_links` de la ressource lue
   pour les mutations (`update`, `me:password`, `me:picture`). Exposer un
   helper `uploadProfilePicture(http, href, file)` analogue à
   `uploadItemImage`.
2. **modèles** — `features/profile/models/user-profile.ts` (ou étendre
   `core/auth/models/user.ts` si le type `User` y suffit) : ajouter
   `UserToUpdate` (`firstname`, `lastname`) et `EditPasswordRequest`
   (`oldPassword`, `newPassword`), documenter les rels `me:password` /
   `me:picture`.
3. **userGuard** — `core/auth/user-guard.ts`, calqué sur `admin-guard.ts` :
   autorise si authentifié et **non** admin, sinon redirige vers
   `/machines`.
4. **Profile** — `features/profile/profile/`, route `/profile`
   (`userGuard`). Généré via `ng generate component`. `httpResource<User>`
   sur `GET /me` pour préremplir la section infos (Signal Form
   `firstname`/`lastname`, `required`) et fournir le lien `me:picture`/
   `me:password` aux sous-sections. Soumission infos : `PUT` sur le lien
   `self`, snackbar de succès, pas de rechargement de page.
5. **Section photo** — réutiliser le composant partagé
   `shared/image-upload/` (prévu par `frontend-crud-items.md`, todo 3 ; le
   créer à cette occasion si le plan items n'a pas encore été implémenté,
   pour ne pas le dupliquer). Afficher la photo actuelle
   (`<img src="{me:picture href}">`) avec repli sur une icône Material
   (`mat-icon` type avatar) en cas de `404`. Upload : `POST` sur
   `me:picture`, cache-busting après upload (même remarque que pour les
   items : l'URL ne change pas), snackbar de succès/échec.
6. **Section mot de passe** — Signal Form `oldPassword`/`newPassword`/
   `confirmNewPassword` (`required`, validation de correspondance calquée
   sur `Register`). Soumission : `POST /me/password`. En cas de
   `400` **sans** `errors` (voir contrainte ci-dessus) → message générique
   sous le champ « mot de passe actuel » (« Mot de passe actuel
   incorrect. »). En cas de `400` **avec** `errors` → mapper
   `fieldErrors['password']` sous le champ « nouveau mot de passe ». En
   cas de succès : `auth.logout()` puis navigation vers `/login` avec un
   message (query param ou snackbar avant redirection) invitant à se
   reconnecter.
7. **Toolbar** — dans `app.html`/le composant racine, ajouter le lien
   « Mon profil » (`routerLink="/profile"`), affiché quand
   `isAuthenticated() && !isAdmin()`.
8. **Routes** — déclarer `/profile` dans `app.routes.ts`
   (`loadComponent`, `title`, `userGuard`).
9. **Tests unitaires** — un `.spec.ts` par fichier ajouté
   (`user-guard.spec.ts` calqué sur `admin-guard.spec.ts`), avec
   `HttpTestingController`. Couvrir en particulier : préremplissage du
   formulaire infos, échec d'upload photo, les deux formes d'erreur du
   changement de mot de passe (avec/sans `errors`), déconnexion +
   redirection après succès, masquage du lien toolbar pour un admin.
10. **Test e2e** — `frontend/e2e/manage-profile.spec.ts` (API mockée,
    dans la lignée de `order-item.spec.ts`) : modification des infos,
    upload de photo, changement de mot de passe réussi (vérifier la
    redirection vers `/login`).
11. **Validation** — `npm test` puis `npm run e2e` depuis `frontend/`,
    puis **`build-brief ./gradlew clean build`** à la racine (toute
    commande Gradle passe par `build-brief`, cf. `AGENTS.md`).
12. **Documentation** — mettre à jour `frontend/README.md` et
    `docs/features-front-a-implementer.md` pour sortir l'espace profil du
    backlog, en renvoyant vers ce fichier.

## Notes et points de vigilance

- **Ne pas confondre avec le compte admin.** Un admin connecté ne doit
  jamais voir le lien « Mon profil » ni pouvoir naviguer sur `/profile`
  (`userGuard` bloque l'accès direct par URL) : `GET /me` échouerait
  (pas de `AppUser` associé) et produirait une expérience incohérente.
- **`email` non modifiable.** Contrairement à un formulaire de profil
  généraliste, `UserToUpdateDto` ne porte pas `email` : l'afficher en
  lecture seule dans la section infos.
- **Asymétrie des erreurs de mot de passe.** Les deux cas d'échec de
  `POST /me/password` (mot de passe actuel incorrect vs nouveau mot de
  passe invalide) ne renvoient pas la même forme de corps `400` ; ne pas
  supposer que `parseValidationErrors` couvre les deux cas, cf. table
  ci-dessus.
- **Conventions impératives** (`frontend/AGENTS.md`) : nommage 2025
  (`profile.ts`, pas `profile.component.ts`), `@Service()` et non
  `@Injectable({ providedIn: 'root' })`, `httpResource()` en lecture /
  `HttpClient` en mutation, Signal Forms (`@angular/forms/signals`),
  organisation par feature, génération via Angular CLI + skill
  `angular-developer`, tests lancés uniquement par `npm test`.
- Le backend n'est pas modifié : aucun test Java à ajouter, le
  `build-brief ./gradlew clean build` ne sert qu'à confirmer l'absence de
  régression et à exécuter l'agrégation frontend. Conserver le chemin du
  log brut affiché par `build-brief` en cas d'échec.
