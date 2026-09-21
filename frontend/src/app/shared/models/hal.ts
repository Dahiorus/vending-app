export interface HalLink {
  href: string;
  templated?: boolean;
}

export interface HalTemplateProperty {
  name: string;
  required?: boolean;
  readOnly?: boolean;
  type?: string;
}

/** A HAL-FORMS `_templates` entry describing an action available on the resource. */
export interface HalTemplate {
  method: string;
  contentType?: string;
  properties?: HalTemplateProperty[];
  target?: string;
}

export interface HalResource {
  _links?: Record<string, HalLink | HalLink[]>;
  _templates?: Record<string, HalTemplate>;
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
