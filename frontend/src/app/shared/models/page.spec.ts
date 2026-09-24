import { HalPage } from './hal';
import { Page } from './page';

interface Sample {
  id: string;
}

describe('Page.fromHalPage', () => {
  it('unwraps the _embedded.elements collection and the page metadata', () => {
    const hal: HalPage<Sample> = {
      _embedded: { elements: [{ id: 'a' }, { id: 'b' }] },
      page: { size: 20, totalElements: 42, totalPages: 3, number: 1 },
    };

    expect(Page.fromHalPage(hal)).toEqual({
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

    expect(Page.fromHalPage(hal).elements).toEqual([]);
    expect(Page.fromHalPage(hal).totalElements).toBe(0);
  });
});

describe('Page.isEmpty', () => {
  it('should be empty given empty elements', () => {
    const page = Page.empty();

    expect(page.isEmpty).toBeTruthy();
  });

  it('should not be empty given non empty elements', () => {
    const page = new Page<Sample>([{ id: 'a' }, { id: 'b' }], 20, 3, 1, 5);

    expect(page.isEmpty).toBeFalsy();
  });
});
