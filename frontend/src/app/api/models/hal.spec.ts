import { describe, expect, it } from 'vitest';
import { HalPage, toPage } from './hal';

interface Sample {
  id: string;
}

describe('toPage', () => {
  it('unwraps the _embedded.elements collection and the page metadata', () => {
    const hal: HalPage<Sample> = {
      _embedded: { elements: [{ id: 'a' }, { id: 'b' }] },
      page: { size: 20, totalElements: 42, totalPages: 3, number: 1 },
    };

    expect(toPage(hal)).toEqual({
      elements: [{ id: 'a' }, { id: 'b' }],
      totalElements: 42,
      totalPages: 3,
      pageIndex: 1,
      pageSize: 20,
    });
  });

  it('returns an empty collection when the backend omits _embedded', () => {
    const hal: HalPage<Sample> = {
      page: { size: 20, totalElements: 0, totalPages: 0, number: 0 },
    };

    expect(toPage(hal).elements).toEqual([]);
    expect(toPage(hal).totalElements).toBe(0);
  });
});
