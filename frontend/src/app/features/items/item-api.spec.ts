import { describe, expect, it } from 'vitest';
import { createItemHref, itemImageHref, itemsPageUrl, itemSelfHref } from './item-api';
import { Item } from './models/item';

describe('itemsPageUrl', () => {
  it("builds the paged URL from the resolved items href, converting the 0-based pageIndex to the backend's 1-based page param", () => {
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

describe('item HAL helpers', () => {
  it('reads the create target from the HAL-FORMS default template when Spring exposes one', () => {
    expect(
      createItemHref({
        _links: { self: { href: '/api/v1/items?page=1&size=20' } },
        _templates: {
          default: {
            method: 'post',
            target: '/api/v1/items',
          },
        },
        page: { size: 20, totalElements: 0, totalPages: 0, number: 0 },
      }),
    ).toBe('/api/v1/items');
  });

  it('falls back to the page self link when Spring omits the HAL-FORMS target because it equals self', () => {
    expect(
      createItemHref({
        _links: { self: { href: '/api/v1/items' } },
        _templates: {
          default: {
            method: 'post',
          },
        },
        page: { size: 20, totalElements: 0, totalPages: 0, number: 0 },
      }),
    ).toBe('/api/v1/items');
  });

  it('returns undefined when the page has no create template and no self link yet', () => {
    expect(
      createItemHref({
        page: { size: 20, totalElements: 0, totalPages: 0, number: 0 },
      }),
    ).toBeUndefined();
  });

  it('reads self and image links from an item resource', () => {
    const item: Item = {
      id: 'i-1',
      name: 'Cola',
      type: 'COLD_BEVERAGE',
      price: 1.5,
      _links: {
        self: { href: '/api/v1/items/i-1' },
        'item:image': { href: '/api/v1/items/i-1/image' },
      },
    };

    expect(itemSelfHref(item)).toBe('/api/v1/items/i-1');
    expect(itemImageHref(item)).toBe('/api/v1/items/i-1/image');
  });
});
