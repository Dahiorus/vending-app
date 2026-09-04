import { computed, inject, Injectable, signal } from '@angular/core';
import { map, Observable, switchMap, tap, throwError } from 'rxjs';
import { AuthApi } from '../../api/auth-api';
import { Credentials, JwtPayload } from '../../api/models/auth';
import { User } from '../../api/models/user';
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

@Injectable({ providedIn: 'root' })
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

  login(credentials: Credentials): Observable<User> {
    return this.api.login(credentials).pipe(
      tap((tokens) => this.tokens.setTokens(tokens)),
      switchMap(() => this.api.me()),
      tap((user) => this.user.set(user)),
    );
  }

  refreshAccessToken(): Observable<string> {
    const refreshToken = this.tokens.refreshToken();
    if (!refreshToken) {
      return throwError(() => new Error('No refresh token available'));
    }

    return this.api.refresh(refreshToken).pipe(
      tap((tokens) => this.tokens.setTokens(tokens)),
      map((tokens) => tokens.accessToken),
    );
  }

  logout(): void {
    this.tokens.clear();
    this.user.set(null);
  }
}
