import { environment } from '../../../environments/environment';
import { pagedUrl } from '../../shared/http/paged-url';
import { HalPage, linkHref } from '../../shared/models/hal';
import { Item } from './models/item';

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

/**
 * Fallback used only when no HATEOAS `self` link is available (e.g. direct
 * navigation/page refresh on an item page). Prefer following the resource's own
 * `_links.self.href` when it is known.
 */
export function itemUrl(id: string): string {
  return `${environment.apiBaseUrl}/items/${id}`;
}

/**
 * Public (`permitAll`) item image URL. Prefer the `item:image` link of an `Item`
 * when it is loaded; this fallback serves places that only know the item id
 * (e.g. the stock of a vending machine, which exposes no `item:image` rel).
 */
export function itemImageUrl(id: string): string {
  return `${itemUrl(id)}/image`;
}

/**
 * Spring HAL-FORMS omits `_templates.default.target` when the affordance target
 * equals the page `self` link, so callers must accept both shapes.
 */
export function createItemHref(page: HalPage<Item>): string | undefined {
  return page._templates?.['default']?.target ?? linkHref(page, 'self');
}

export function itemSelfHref(item: Item | null | undefined): string | undefined {
  return linkHref(item, 'self');
}

export function itemImageHref(item: Item | null | undefined): string | undefined {
  return linkHref(item, 'item:image');
}
