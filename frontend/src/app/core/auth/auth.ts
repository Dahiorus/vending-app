import { computed, inject, Service, signal } from '@angular/core';
import { catchError, map, Observable, of, switchMap, tap } from 'rxjs';
import { AuthApi } from './auth-api';
import { Credentials, JwtPayload } from './models/auth';
import { User, UserToRegister } from './models/user';
import { TokenStore } from './token-store';

/** Decodes the payload of a JWT without verifying its signature (the backend does that). */
function decodePayload(token: string): JwtPayload | null {
  const [, payload] = token.split('.');
  if (!payload) {
    return null;
  }
  try {
    const normalized = payload.replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(atob(normalized)) as JwtPayload;
  } catch {
    return null;
  }
}

@Service()
export class AuthService {
  private readonly api = inject(AuthApi);
  private readonly tokens = inject(TokenStore);
  private readonly user = signal<User | null>(null);

  readonly currentUser = this.user.asReadonly();
  readonly isAuthenticated = computed(() => this.tokens.accessToken() !== null);
  readonly roles = computed(() => {
    const token = this.tokens.accessToken();
    return token ? (decodePayload(token)?.roles ?? []) : [];
  });
  readonly isAdmin = computed(() => this.roles().includes('ROLE_ADMIN'));

  /**
   * Loads the current user's profile after login, except for admin accounts:
   * `/me` is a self-service endpoint backed by `AppUserRepositoryPort`, scoped to
   * `ROLE_USER` accounts only (admins have no matching profile there yet, see backend
   * `AppUserRepositoryAdapter`/`AGENTS.md`).
   */
  login(credentials: Credentials): Observable<User | null> {
    return this.api.login(credentials).pipe(
      tap((session) => this.tokens.setAccessToken(session.accessToken)),
      switchMap((session) => {
        const isAdmin = (decodePayload(session.accessToken)?.roles ?? []).includes('ROLE_ADMIN');
        return isAdmin ? of(null) : this.api.me();
      }),
      tap((user) => this.user.set(user)),
    );
  }

  register(payload: UserToRegister): Observable<User | null> {
    return this.api
      .register(payload)
      .pipe(switchMap(() => this.login({ username: payload.email, password: payload.password })));
  }

  refreshAccessToken(): Observable<string> {
    return this.api.refresh().pipe(
      tap((session) => this.tokens.setAccessToken(session.accessToken)),
      map((session) => session.accessToken),
    );
  }

  logout(): void {
    this.api
      .logout()
      .pipe(catchError(() => of(undefined)))
      .subscribe(() => {
        this.tokens.clear();
        this.user.set(null);
      });
  }
}
