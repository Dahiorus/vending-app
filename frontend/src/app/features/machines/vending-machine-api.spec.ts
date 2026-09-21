import { describe, expect, it } from 'vitest';
import { machinesPageUrl } from './vending-machine-api';

describe('machinesPageUrl', () => {
  it('builds the paged URL from the resolved vending-machines href', () => {
    expect(machinesPageUrl('/api/v1/vending-machines', 2, 10)).toBe(
      '/api/v1/vending-machines?page=2&size=10',
    );
  });

  it('returns undefined while the vending-machines href is not resolved yet', () => {
    expect(machinesPageUrl(undefined, 0, 10)).toBeUndefined();
  });
});
