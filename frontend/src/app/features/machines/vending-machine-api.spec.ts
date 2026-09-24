import { describe, expect, it } from 'vitest';
import { machinesPageUrl } from './vending-machine-api';

describe('machinesPageUrl', () => {
  it('builds the paged URL from the resolved vending-machines href, converting the 0-based pageIndex to the backend’s 1-based page param', () => {
    expect(machinesPageUrl('/api/v1/vending-machines', 2, 10)).toBe(
      '/api/v1/vending-machines?page=3&size=10',
    );
  });

  it('returns undefined while the vending-machines href is not resolved yet', () => {
    expect(machinesPageUrl(undefined, 0, 10)).toBeUndefined();
  });

  it('preserves query params already present on the href (e.g. advanced search filters)', () => {
    expect(machinesPageUrl('/api/v1/vending-machines?city=Paris', 2, 10)).toBe(
      '/api/v1/vending-machines?city=Paris&page=3&size=10',
    );
  });
});
