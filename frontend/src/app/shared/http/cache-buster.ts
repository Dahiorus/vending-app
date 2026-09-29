const ABSOLUTE_URL = /^[a-z][a-z\d+\-.]*:/i;

/**
 * Forces the browser to refetch a resource whose URL does not change after an
 * update (e.g. an image re-uploaded at the same `/image` URL) by setting a `v`
 * query param. Unlike `pagedUrl`, absolute hrefs keep their origin.
 */
export function withCacheBuster(href: string, version: number): string {
  if (version === 0) return href;

  const url = new URL(href, window.location.origin);
  url.searchParams.set('v', String(version));

  return ABSOLUTE_URL.test(href) ? url.toString() : `${url.pathname}${url.search}${url.hash}`;
}
