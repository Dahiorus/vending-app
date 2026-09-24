import { describe, expect, it } from 'vitest';
import { intQueryParam } from './query-params';

describe('intQueryParam', () => {
  it('parses a valid integer query param', () => {
    expect(intQueryParam('2', 0)).toBe(2);
  });

  it('falls back to the default when the param is absent', () => {
    expect(intQueryParam(null, 10)).toBe(10);
  });

  it('falls back to the default when the param is not a valid non-negative integer', () => {
    expect(intQueryParam('-1', 10)).toBe(10);
    expect(intQueryParam('abc', 10)).toBe(10);
    expect(intQueryParam('1.5', 10)).toBe(10);
  });
});
