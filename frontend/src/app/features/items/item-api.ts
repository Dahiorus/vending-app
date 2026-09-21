/**
 * `undefined` until the `items` root link is resolved (see `ApiRootApi`).
 */
export function itemsPageUrl(
  itemsHref: string | undefined,
  pageIndex: number,
  pageSize: number,
): string | undefined {
  if (!itemsHref) return undefined;
  return `${itemsHref}?page=${pageIndex}&size=${pageSize}`;
}
