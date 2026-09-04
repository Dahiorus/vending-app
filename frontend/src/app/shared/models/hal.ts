export interface HalLink {
  href: string;
  templated?: boolean;
}

export interface HalResource {
  _links?: Record<string, HalLink | HalLink[]>;
}

export interface HalPageMetadata {
  size: number;
  totalElements: number;
  totalPages: number;
  number: number;
}

/** Shape returned by Spring HATEOAS PagedModel. `_embedded` is omitted for empty pages. */
export interface HalPage<T> extends HalResource {
  _embedded?: { elements: T[] };
  page: HalPageMetadata;
}

export interface Page<T> {
  elements: T[];
  totalElements: number;
  totalPages: number;
  pageIndex: number;
  pageSize: number;
}

export function toPage<T>(hal: HalPage<T>): Page<T> {
  return {
    elements: hal._embedded?.elements ?? [],
    totalElements: hal.page.totalElements,
    totalPages: hal.page.totalPages,
    pageIndex: hal.page.number,
    pageSize: hal.page.size,
  };
}
