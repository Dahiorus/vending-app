import { TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it } from 'vitest';
import { TokenStore } from './token-store';

describe('TokenStore', () => {
  let store: TokenStore;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({});
    store = TestBed.inject(TokenStore);
  });

  it('starts empty', () => {
    expect(store.accessToken()).toBeNull();
    expect(store.refreshToken()).toBeNull();
  });

  it('keeps the access token in memory only', () => {
    store.setTokens({ accessToken: 'access-1', refreshToken: 'refresh-1' });

    expect(store.accessToken()).toBe('access-1');
    expect(sessionStorage.getItem('vending.refreshToken')).toBe('refresh-1');
    expect(JSON.stringify(sessionStorage)).not.toContain('access-1');
  });

  it('reads a refresh token persisted by a previous page load', () => {
    sessionStorage.setItem('vending.refreshToken', 'refresh-from-reload');
    const reloaded = TestBed.inject(TokenStore);

    expect(reloaded.refreshToken()).toBe('refresh-from-reload');
  });

  it('clears both tokens', () => {
    store.setTokens({ accessToken: 'access-1', refreshToken: 'refresh-1' });

    store.clear();

    expect(store.accessToken()).toBeNull();
    expect(store.refreshToken()).toBeNull();
    expect(sessionStorage.getItem('vending.refreshToken')).toBeNull();
  });
});
