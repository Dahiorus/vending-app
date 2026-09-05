import { environment } from '../../../environments/environment';

export function machinesUrl(): string {
  return `${environment.apiBaseUrl}/vending-machines`;
}

export function machinesPageUrl(pageIndex: number, pageSize: number): string {
  return `${machinesUrl()}?page=${pageIndex}&size=${pageSize}`;
}

/**
 * Fallback used only when no HATEOAS `self` link is available (e.g. direct
 * navigation/page refresh on the detail page). Prefer following the resource's
 * own `_links.self.href` when it is known.
 */
export function machineUrl(id: string): string {
  return `${machinesUrl()}/${id}`;
}
