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
  });

  it('keeps the access token in memory only, never in sessionStorage', () => {
    store.setAccessToken('access-1');

    expect(store.accessToken()).toBe('access-1');
    expect(sessionStorage.length).toBe(0);
  });

  it('clears the access token', () => {
    store.setAccessToken('access-1');

    store.clear();

    expect(store.accessToken()).toBeNull();
    expect(sessionStorage.length).toBe(0);
  });
});
