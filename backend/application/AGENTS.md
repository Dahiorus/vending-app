# AGENTS.md — application

Implémentations des cas d'usage du domaine. Règles spécifiques à ce module
(en plus de `AGENTS.md` à la racine) :

## Structure

```
service/
  <sous-domaine>/   ex. service/item, service/machine, service/user
```

Les services sont rangés par sous-domaine métier, jamais à plat au niveau
racine de `service/` (regroupement effectué lors de la revue d'architecture
— ne pas régresser en ajoutant un nouveau service directement sous
`service/`).

## Contrat d'un `XxxApplicationService`

- Implémente toujours une interface `XxxApiPort` définie dans `domain`.
- Porte systématiquement `@Transactional` au niveau classe (cohérence
  vérifiée sur l'ensemble des services existants) ; envisager
  `@Transactional(readOnly = true)` au niveau méthode pour les opérations de
  pure lecture (`read`, `find*`, `report*`) si l'optimisation est utile —
  point identifié mais non appliqué par défaut, à traiter au cas par cas.
- Ne doit exister que s'il ajoute quelque chose par rapport à un simple
  appel direct au `RepositoryPort` : de la logique métier (validation,
  orchestration de plusieurs ports) ou, au minimum, une frontière
  transactionnelle explicite. Un pur miroir 1:1 sans plus-value ne doit pas
  être créé (cf. décision sur `AuthenticationRestController` /
  `CreateDevEnvironmentAdmin`, qui gardent volontairement un accès direct au
  `RepositoryPort`).

## Dépendance vers `domain`

`application` dépend de `domain` en `api` (et non `implementation`) car les
types du domaine (entités, ports) font partie de la signature publique des
`ApplicationService`. Ne pas changer cette configuration sans revérifier
l'ensemble du classpath de compilation d'`infrastructure`.
