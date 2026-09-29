import { describe, expect, it } from 'vitest';
import { itemImageUrl, itemsPageUrl, itemUrl } from './item-api';

describe('itemsPageUrl', () => {
  it('builds the paged URL from the resolved items href, converting the 0-based pageIndex to the backend’s 1-based page param', () => {
    expect(itemsPageUrl('/api/v1/items', 2, 10)).toBe('/api/v1/items?page=3&size=10');
  });

  it('returns undefined while the items href is not resolved yet', () => {
    expect(itemsPageUrl(undefined, 0, 10)).toBeUndefined();
  });

  it('preserves query params already present on the href (e.g. advanced search filters)', () => {
    expect(itemsPageUrl('/api/v1/items?type=SNACK', 2, 10)).toBe(
      '/api/v1/items?type=SNACK&page=3&size=10',
    );
  });
});

describe('itemUrl', () => {
  it('builds the item resource URL from its id', () => {
    expect(itemUrl('i-1')).toBe('/api/v1/items/i-1');
  });
});

describe('itemImageUrl', () => {
  it('builds the public item image URL from its id', () => {
    expect(itemImageUrl('i-1')).toBe('/api/v1/items/i-1/image');
  });
});
