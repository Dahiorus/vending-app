import { environment } from '../../../environments/environment';
import { pagedUrl } from '../../shared/http/paged-url';

/** Used for the create mutation, out of scope for the root-link discovery below. */
export function machinesUrl(): string {
  return `${environment.apiBaseUrl}/vending-machines`;
}

/** `undefined` until the `vendingMachines` root link is resolved (see `ApiRootApi`). */
export function machinesPageUrl(
  vendingMachinesHref: string | undefined,
  pageIndex: number,
  pageSize: number,
): string | undefined {
  return pagedUrl(vendingMachinesHref, { page: pageIndex + 1, size: pageSize });
}

/**
 * Fallback used only when no HATEOAS `self` link is available (e.g. direct
 * navigation/page refresh on the detail page). Prefer following the resource's
 * own `_links.self.href` when it is known.
 */
export function machineUrl(id: string): string {
  return `${machinesUrl()}/${id}`;
}
