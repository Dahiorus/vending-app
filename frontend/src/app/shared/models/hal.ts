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

export class Page<T> {
  constructor(
    readonly elements: T[],
    readonly totalElements: number,
    readonly totalPages: number,
    readonly pageIndex: number,
    readonly pageSize: number,
  ) {}

  static empty<T>(): Page<T> {
    return new Page<T>([], 0, 0, 0, 0);
  }

  static fromHalPage<T>(hal: HalPage<T>): Page<T> {
    return new Page(
      hal._embedded?.elements ?? [],
      hal.page.totalElements,
      hal.page.totalPages,
      hal.page.number,
      hal.page.size,
    );
  }

  get isEmpty(): boolean {
    return this.elements.length === 0;
  }
}
