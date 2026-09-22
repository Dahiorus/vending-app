import { describe, expect, it } from 'vitest';
import { pagedUrl } from './paged-url';

describe('pagedUrl', () => {
  it('appends the given params to a href without a query string', () => {
    expect(pagedUrl('/api/v1/items', { page: 2, size: 10 })).toBe('/api/v1/items?page=2&size=10');
  });

  it('preserves existing query params already present on the href', () => {
    expect(pagedUrl('/api/v1/items?city=Paris', { page: 0, size: 20 })).toBe(
      '/api/v1/items?city=Paris&page=0&size=20',
    );
  });

  it('overwrites a param that already exists on the href', () => {
    expect(pagedUrl('/api/v1/items?page=1&size=20', { page: 3, size: 20 })).toBe(
      '/api/v1/items?page=3&size=20',
    );
  });

  it('removes a param whose value is undefined', () => {
    expect(pagedUrl('/api/v1/items?city=Paris', { city: undefined, page: 0 })).toBe(
      '/api/v1/items?page=0',
    );
  });

  it('returns undefined while the href is not resolved yet', () => {
    expect(pagedUrl(undefined, { page: 0, size: 10 })).toBeUndefined();
  });
});
