# AGENTS.md — infrastructure

Adaptateurs (REST, JPA, sécurité) et point d'entrée Spring Boot. Règles
spécifiques à ce module (en plus de `AGENTS.md` à la racine) :

## Contrainte de compilation critique : `runtimeOnly(project(":backend:application"))`

`build.gradle.kts` déclare la dépendance vers `application` en
`runtimeOnly`, pas en `implementation`. Conséquence directe : le code de
`infrastructure` **ne peut pas compiler** contre une classe concrète
d'`application` (ex. `XxxApplicationService`) — seul le code de `domain`
(entités, `ApiPort`, `RepositoryPort`) est visible à la compilation. Les
implémentations concrètes ne sont résolues qu'au runtime via le
component-scan Spring.

C'est la raison structurelle pour laquelle **tout** contrôleur REST ou
toute commande dépend d'une interface `ApiPort` (ou, à défaut, directement
d'un `RepositoryPort` quand aucun `ApiPort` n'apporte de valeur — voir
`AGENTS.md` racine) plutôt que d'un `ApplicationService` concret. Ne pas
essayer de câbler une dépendance directe vers une classe concrète
d'`application` : ça ne compilera pas, et changer cette configuration en
`implementation` est un choix architectural à part entière, pas un simple
correctif ponctuel.

## Nommage de la couche JPA

```
jpa/repository/<sous-domaine>/
  XxxJpaRepository        interface Spring Data brute (JpaRepository<Entity, Id>)
  XxxRepositoryAdapter     implémente le RepositoryPort du domaine, utilise XxxJpaRepository
```

Chaque sous-domaine (`item`, `machine`, `order`, `user`) a son propre
sous-package. Ne pas revenir à l'ancien nommage inversé
(`JpaXxxDao` / `XxxJpaRepository` pour l'adaptateur).

## Structure REST

```
rest/
  controller/<sous-domaine>/   contrôleurs REST
  assembler/                   XxxDtoModelAssembler (HATEOAS), un par ressource exposée
  entity/<sous-domaine>/       DTOs (Request/Response ou ToCreate/ToUpdate, cf. AGENTS.md racine)
  exception/                   RestResponseExceptionHandler + exceptions HTTP dédiées
```

Les assemblers portent tous le suffixe `DtoModelAssembler` (et non
`ModelAssembler`) pour lever toute ambiguïté sur ce qu'ils assemblent.

## Sécurité

L'application est un serveur de ressources OAuth2 Spring Security
(`config/WebSecurityConfig`), et non un filtre JWT maison : pas de
`UsernamePasswordAuthenticationFilter`/`OncePerRequestFilter` personnalisé,
la vérification du bearer token est déléguée à
`oauth2ResourceServer(jwt(...))`.

```
security/jwt/
  JwtProperties        @ConfigurationProperties(prefix = "jwt") : issuer-uri (obligatoire),
                       durées d'accès/rafraîchissement, clé RSA publique/privée (PEM, optionnelles)
  JwtKeysConfig        RSAKey (configurée ou générée de façon éphémère avec un WARN au démarrage),
                       beans JwtEncoder/JwtDecoder/JwtAuthenticationConverter
  JwtTokenIssuer        émission des access/refresh tokens (claim "token_type" pour les distinguer)
```

Points à respecter en cas d'évolution :

- Le claim `roles` porte déjà les autorités complètes (`ROLE_ADMIN`,
  produites par `Role#asRole`) : `JwtGrantedAuthoritiesConverter` est
  configuré avec un préfixe vide (`setAuthorityPrefix("")`). Ne pas
  ajouter `ROLE_` une deuxième fois.
- Le endpoint `/oauth2/jwks` (`rest/controller/JwksRestController`) expose
  uniquement la clé **publique** (`rsaJwk.toPublicJWK()`) ; ne jamais y
  exposer la clé privée.
- Le refresh token vit dans un cookie `httpOnly` `refresh_token` déposé au
  login ; `AuthenticationRestController.refreshToken` le lit exclusivement
  depuis ce cookie (pas de `RefreshTokenRequestDto` ni de token de
  rafraîchissement transmis dans le corps JSON).
- `AuthenticationRestController.refreshToken` doit toujours vérifier le
  claim `token_type == "refresh"` avant de faire confiance au cookie
  présenté sur `/api/v1/authenticate/refresh` — un access token ne doit
  jamais pouvoir servir de refresh token.
- Le logout est configuré nativement dans `WebSecurityConfig` via
  `HttpSecurity#logout(...)` (pas de logique de logout dans
  `AuthenticationRestController`, qui n'expose plus qu'une méthode
  marqueur `logout()` pour Swagger) : `RefreshTokenLogoutHandler`
  (bean `refreshTokenLogoutHandler`) révoque le refresh token porté par le
  cookie (best-effort, ne doit jamais faire échouer un logout sur un
  cookie absent/invalide — idempotence requise) puis
  `HttpStatusReturningLogoutSuccessHandler` répond `204`. Toute évolution
  du logout doit rester dans ce câblage Spring Security, pas revenir à un
  endpoint applicatif custom.
- La rotation/révocation serveur passe par `RefreshTokenApiPort` et la
  table `refresh_token` (JTI persisté, remplacement atomique à chaque
  refresh, suppression à la déconnexion) ; ne jamais revenir à un modèle
  purement stateless où n'importe quel refresh token signé resterait valide
  jusqu'à expiration.
- La protection CSRF est volontairement **limitée** aux POST
  `/api/v1/authenticate/refresh` et `/api/v1/authenticate/logout` via
  `CookieCsrfTokenRepository` : le backend dépose `XSRF-TOKEN`, le client
  renvoie `X-XSRF-TOKEN`, et le login reste exclu pour permettre
  l'amorçage initial du cookie CSRF.
- `CsrfAuthenticationStrategy` (protection anti session-fixation) est
  explicitement désactivée (`csrf.sessionAuthenticationStrategy(new
  NullAuthenticatedSessionStrategy())`) : elle est câblée par défaut avec
  Spring Security et supprime le cookie `XSRF-TOKEN` à chaque
  authentification réussie ; or en mode `STATELESS` + JWT, **chaque**
  requête authentifiée ré-authentifie (pas de session persistée), donc
  sans ce correctif le cookie disparaissait dès le premier appel
  authentifié suivant le login. Ne pas retirer cette désactivation : la
  protection session-fixation n'a pas de sens sans session.
- La configuration CORS doit conserver `allowCredentials=true`, des
  origines explicites (jamais `*`) et l'en-tête `X-XSRF-TOKEN`, sinon les
  cookies de refresh/XSRF ne circulent plus correctement depuis le frontend.
- Sans clé RSA configurée (`jwt.public-key`/`jwt.private-key`), une paire
  éphémère est générée à chaque démarrage : tous les tokens émis avant un
  redémarrage deviennent invalides. Acceptable en développement,
  inacceptable pour un environnement long-lived — y configurer une vraie
  paire de clés.
- Tests de la chaîne de sécurité (login, refresh via cookie, logout,
  rotation/révocation, CSRF 403, 401/403, tokens expirés/altérés/signés
  par une clé étrangère, endpoints publics) dans
  `infrastructure/src/intTest/.../security/SecurityChainIT.java`, seul
  test `@SpringBootTest` du dépôt à ce jour ; le task Gradle `intTest`
  déclare `-XX:MaxDirectMemorySize` car ce test charge le contexte complet
  et donc tous les caches Ehcache off-heap.

## Runners de commande

`command/CreateDevEnvironmentAdmin` (profil `dev` uniquement) doit rester
best-effort et ne jamais faire échouer le démarrage de l'application ; en
cas d'ajustement du bloc `catch`, conserver la cause complète dans le log
(`logger.error("...", e)`), pas seulement `e.getMessage()`.
