import { Service, signal } from '@angular/core';
import { AuthTokens } from '../../api/models/auth';

const REFRESH_TOKEN_KEY = 'vending.refreshToken';

@Service()
export class TokenStore {
  private readonly access = signal<string | null>(null);

  readonly accessToken = this.access.asReadonly();

  refreshToken(): string | null {
    return sessionStorage.getItem(REFRESH_TOKEN_KEY);
  }

  setTokens(tokens: AuthTokens): void {
    this.access.set(tokens.accessToken);
    sessionStorage.setItem(REFRESH_TOKEN_KEY, tokens.refreshToken);
  }

  clear(): void {
    this.access.set(null);
    sessionStorage.removeItem(REFRESH_TOKEN_KEY);
  }
}
