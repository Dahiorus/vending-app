import { pagedUrl } from '../../shared/http/paged-url';

/**
 * `undefined` until the `items` root link is resolved (see `ApiRootApi`).
 */
export function itemsPageUrl(
  itemsHref: string | undefined,
  pageIndex: number,
  pageSize: number,
): string | undefined {
  return pagedUrl(itemsHref, { page: pageIndex + 1, size: pageSize });
}
