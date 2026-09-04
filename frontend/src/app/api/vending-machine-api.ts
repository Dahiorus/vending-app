import { environment } from '../../environments/environment';

export function machinesPageUrl(pageIndex: number, pageSize: number): string {
  return `${environment.apiBaseUrl}/vending-machines?page=${pageIndex}&size=${pageSize}`;
}
