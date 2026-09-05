import { environment } from '../../../environments/environment';

export function machinesUrl(): string {
  return `${environment.apiBaseUrl}/vending-machines`;
}

export function machinesPageUrl(pageIndex: number, pageSize: number): string {
  return `${machinesUrl()}?page=${pageIndex}&size=${pageSize}`;
}
