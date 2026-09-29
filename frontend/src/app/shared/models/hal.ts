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

/**
 * Reads the href of a HAL relation, returning the first href when the relation
 * holds an array of links (Spring HATEOAS serialises repeated rels as arrays).
 */
export function linkHref(
  resource: HalResource | null | undefined,
  rel: string,
): string | undefined {
  const link = resource?._links?.[rel];
  if (!link) return undefined;
  return Array.isArray(link) ? link[0]?.href : link.href;
}
