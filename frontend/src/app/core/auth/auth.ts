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
   * Loads the current user's profile, except for admin accounts: `/me` is a
   * self-service endpoint backed by `AppUserRepositoryPort`, scoped to
   * `ROLE_USER` accounts only (admins have no matching profile there yet, see
   * backend `AppUserRepositoryAdapter`/`AGENTS.md`).
   */
  private loadCurrentUserIfNeeded(accessToken: string): Observable<User | null> {
    const isAdmin = (decodePayload(accessToken)?.roles ?? []).includes('ROLE_ADMIN');
    return isAdmin ? of(null) : this.api.me();
  }

  login(credentials: Credentials): Observable<User | null> {
    return this.api.login(credentials).pipe(
      tap((session) => this.tokens.setAccessToken(session.accessToken)),
      switchMap((session) => this.loadCurrentUserIfNeeded(session.accessToken)),
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

  /**
   * Used once at app startup (see `restoreSessionOnStartup`) to silently
   * resume a session from the refresh-token cookie and reload the current
   * user's profile, so the toolbar shows the actual user (not the "no
   * profile loaded" admin fallback) after a full page reload.
   */
  restoreSession(): Observable<string> {
    return this.refreshAccessToken().pipe(
      switchMap((accessToken) =>
        this.loadCurrentUserIfNeeded(accessToken).pipe(
          tap((user) => this.user.set(user)),
          map(() => accessToken),
        ),
      ),
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
