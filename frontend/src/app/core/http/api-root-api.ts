import { httpResource } from '@angular/common/http';
import { Service } from '@angular/core';
import { environment } from '../../../environments/environment';
import { HalResource } from '../../shared/models/hal';

@Service()
export class ApiRootApi {
  private readonly resource = httpResource<HalResource>(() => environment.apiBaseUrl);

  link(rel: string): string | undefined {
    const links = this.resource.value()?._links;
    const link = links?.[rel];
    if (!link) return undefined;
    return Array.isArray(link) ? link[0]?.href : link.href;
  }
}
