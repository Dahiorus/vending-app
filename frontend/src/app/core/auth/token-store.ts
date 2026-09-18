import { Service, signal } from '@angular/core';

@Service()
export class TokenStore {
  private readonly access = signal<string | null>(null);

  readonly accessToken = this.access.asReadonly();

  setAccessToken(token: string): void {
    this.access.set(token);
  }

  clear(): void {
    this.access.set(null);
  }
}
