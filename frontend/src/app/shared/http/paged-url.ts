/**
 * Builds a URL by merging `params` into `href`'s existing query string,
 * preserving any query params already present (e.g. filters from an
 * advanced search). A `param` set to `undefined` removes the corresponding
 * key instead of setting it, so callers can omit optional criteria.
 *
 * `href` is expected to be relative (e.g. `/api/v1/items`), as returned by
 * the HAL root links; the result is returned as `pathname + search`.
 */
export function pagedUrl(
  href: string | undefined,
  params: Record<string, string | number | undefined>,
): string | undefined {
  if (!href) return undefined;

  const url = new URL(href, window.location.origin);
  for (const [key, value] of Object.entries(params)) {
    if (value === undefined) {
      url.searchParams.delete(key);
    } else {
      url.searchParams.set(key, String(value));
    }
  }
  return `${url.pathname}${url.search}`;
}
