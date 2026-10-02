import { describe, expect, it } from 'vitest';
import { withCacheBuster } from './cache-buster';

describe('withCacheBuster', () => {
  it('returns the href unchanged for the initial version 0', () => {
    expect(withCacheBuster('/api/v1/me/picture', 0)).toBe('/api/v1/me/picture');
  });

  it('appends the version as a `v` query param on a relative href', () => {
    expect(withCacheBuster('/api/v1/me/picture', 2)).toBe('/api/v1/me/picture?v=2');
  });

  it('keeps an absolute href absolute, preserving its origin', () => {
    expect(withCacheBuster('https://api.example.test/api/v1/items/i-1/image', 1)).toBe(
      'https://api.example.test/api/v1/items/i-1/image?v=1',
    );
  });

  it('preserves existing query params and overwrites a previous version', () => {
    expect(withCacheBuster('/api/v1/items/i-1/image?size=small&v=1', 3)).toBe(
      '/api/v1/items/i-1/image?size=small&v=3',
    );
  });
});
