# Bonnes pratiques Angular 22 — Signals, Signal Forms, Observables

Ce document complète `frontend/AGENTS.md` avec des exemples concrets pour les
trois piliers de la réactivité moderne d'Angular utilisés dans ce projet.
En cas de conflit, `frontend/AGENTS.md` fait référence.

## 1. Signals

### `signal()` — état modifiable

```ts
import { signal } from '@angular/core';

export class TokenStore {
  // Privé et modifiable en interne
  private readonly _accessToken = signal<string | null>(null);
  // Exposé en lecture seule pour empêcher toute mutation externe
  readonly accessToken = this._accessToken.asReadonly();

  setToken(token: string): void {
    this._accessToken.set(token);
  }
}
```

**Règle** : dans un service, toujours exposer un signal via `.asReadonly()`
plutôt que le signal modifiable brut.

### `computed()` — état dérivé, lecture seule

```ts
import { signal, computed } from '@angular/core';

const price = signal(100);
const quantity = signal(2);

// Recalculé automatiquement et uniquement si price ou quantity changent
const total = computed(() => price() * quantity());
```

**Règle** : utiliser `computed()` dès qu'une valeur est **strictement**
dérivée d'autres signaux, sans besoin de modification manuelle.

### `linkedSignal()` — état dérivé mais modifiable

```ts
import { signal, linkedSignal } from '@angular/core';

const shippingOptions = signal(['Ground', 'Air', 'Sea']);

// Par défaut = première option, mais l'utilisateur peut la changer
const selectedOption = linkedSignal(() => shippingOptions()[0]);

selectedOption.set('Air'); // Override manuel possible
```

**Règle** : utiliser `linkedSignal()` quand un état a une valeur par défaut
dérivée d'un autre signal, mais doit rester modifiable indépendamment.

### `effect()` — effets de bord uniquement

```ts
import { effect } from '@angular/core';

constructor() {
  effect((onCleanup) => {
    console.log(`Thème actuel : ${this.theme()}`);
    const timer = setTimeout(() => console.log('done'), 1000);
    onCleanup(() => clearTimeout(timer));
  });
}
```

**Interdit** : ne jamais appeler `.set()`/`.update()` sur un autre signal
depuis un `effect()` pour les synchroniser — utiliser `computed()` ou
`linkedSignal()`, sous peine d'erreurs
`ExpressionChangedAfterItHasBeenChecked` ou de boucles infinies.

**Piège asynchrone** : un signal lu après un `await` n'est pas tracké.

```ts
// ❌ theme() n'est pas suivi car lu après l'await
effect(async () => {
  const data = await fetchUserData();
  console.log(theme());
});

// ✅ Lire le signal avant la frontière asynchrone
effect(async () => {
  const currentTheme = theme();
  const data = await fetchUserData();
  console.log(currentTheme);
});
```

### `httpResource()` — lecture de données (convention du projet)

```ts
import { httpResource } from '@angular/common/http';

export class VendingMachineApi {
  private readonly machineId = signal('123');

  readonly machine = httpResource<VendingMachine>(
    () => `/api/vending-machines/${this.machineId()}`
  );
}
```

```html
@if (machine.isLoading()) {
  <p>Chargement...</p>
} @else if (machine.error()) {
  <p>Erreur de chargement</p>
} @else if (machine.hasValue()) {
  <p>{{ machine.value().serialNumber }}</p>
}
```

**Règle du projet** : `httpResource()` pour toute lecture (`GET`) ;
`HttpClient` direct pour les mutations (`POST`/`PUT`/`DELETE`).

## 2. Signal Forms

Remplacent totalement les Reactive Forms (`FormControl`, `FormGroup`,
`FormBuilder` — imports interdits) pour tout nouveau formulaire.

```ts
import { Component, signal } from '@angular/core';
import { form, FormField, required, email, min, submit } from '@angular/forms/signals';

@Component({
  imports: [FormField],
  template: `
    <input [formField]="userForm.name" />
    @if (userForm.name().touched() && userForm.name().invalid()) {
      <span class="error">{{ userForm.name().errors()[0]?.message }}</span>
    }

    <input [formField]="userForm.email" />
    <input type="number" [formField]="userForm.age" />

    <button (click)="onSubmit()">Valider</button>
  `,
})
export class UserFormComponent {
  // CRITIQUE : jamais null/undefined dans le modèle initial
  protected readonly model = signal({ name: '', email: '', age: 0 });

  protected readonly userForm = form(this.model, (schemaPath) => {
    required(schemaPath.name, { message: 'Le nom est requis' });
    email(schemaPath.email, { message: 'Email invalide' });
    min(schemaPath.age, 18);
  });

  onSubmit(): void {
    submit(this.userForm, async () => {
      // Ne s'exécute que si le formulaire est valide
      await this.apiService.save(this.model());
    });
  }
}
```

**Règles clés** :
- Modèle initial : `''`, `0`, `[]` — jamais `null`/`undefined`.
- Un champ doit être **appelé** pour accéder à son état :
  `userForm.name().touched()`, pas `userForm.name.touched()`.
- Avec `[formField]`, ne jamais fixer en parallèle `value`, `min`, `max`,
  `disabled`, `readonly` (déjà gérés par la directive), sauf `value`
  statique sur `radio`/`checkbox`.
- Le callback de `submit()` doit être `async` et retourner une `Promise`.

## 3. Observables (RxJS)

Angular 22 privilégie les signaux pour l'état de composant et la lecture de
données, mais RxJS reste pertinent pour :

- les flux à émissions multiples (WebSocket, `router.events`, `fromEvent`) ;
- la composition d'opérateurs complexes (`debounceTime`, `switchMap`,
  `combineLatest`) ;
- les mutations HTTP via `HttpClient` nécessitant interceptors/annulation
  fine (convention du projet : `HttpClient` direct pour `POST`/`PUT`/`DELETE`).

### Interopérabilité signal ↔ observable

```ts
import { toSignal, toObservable } from '@angular/core/rxjs-interop';
import { debounceTime, switchMap } from 'rxjs';

// Observable -> Signal
const searchResults = toSignal(
  toObservable(this.searchTerm).pipe(
    debounceTime(300),
    switchMap((term) => this.searchApi.search(term)),
  ),
  { initialValue: [] },
);
```

```ts
// Mutation avec HttpClient (convention du projet)
saveOrder(order: OrderToCreate): void {
  this.http.post<OrderResponseDto>('/api/orders', order).subscribe({
    next: (response) => this.router.navigate(['/orders', response.id]),
    error: (err) => this.notify.error('Échec de la commande'),
  });
}
```

**Règle** : par défaut, signaux (`httpResource`) pour l'état et la lecture ;
RxJS uniquement quand un vrai flux d'événements ou un opérateur de
composition l'exige.

## Références

- `frontend/AGENTS.md` — conventions du module (priment en cas de conflit).
- Skill `angular-developer` (`references/signals-overview.md`,
  `references/linked-signal.md`, `references/resource.md`,
  `references/effects.md`, `references/signal-forms.md`).
