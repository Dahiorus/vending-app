import { httpResource } from '@angular/common/http';
import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiRootApi } from '../../../core/http/api-root-api';
import { intQueryParam } from '../../../shared/http/query-params';
import { HalPage } from '../../../shared/models/hal';
import { itemsPageUrl } from '../item-api';
import { Item } from '../models/item';
import { Page } from '../../../shared/models/page';

const DEFAULT_PAGE_SIZE = 20;

@Component({
  selector: 'app-item-list',
  imports: [MatPaginatorModule, MatProgressBarModule, MatTableModule],
  templateUrl: './item-list.html',
})
export class ItemList {
  private readonly apiRoot = inject(ApiRootApi);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  protected readonly displayedColumns = ['name', 'type', 'price'];

  private readonly queryParamMap = toSignal(this.route.queryParamMap, { requireSync: true });
  readonly pageIndex = computed(() => intQueryParam(this.queryParamMap().get('page'), 0));
  readonly pageSize = computed(() =>
    intQueryParam(this.queryParamMap().get('size'), DEFAULT_PAGE_SIZE),
  );

  private readonly resource = httpResource<HalPage<Item>>(() =>
    itemsPageUrl(this.apiRoot.link('items'), this.pageIndex(), this.pageSize()),
  );

  readonly isLoading = this.resource.isLoading;
  readonly hasError = computed(() => this.resource.error() !== undefined);
  readonly items = computed(() =>
    this.resource.hasValue() ? Page.fromHalPage(this.resource.value()) : Page.empty<Item>(),
  );
  readonly totalElements = computed(() => this.items().totalElements);

  onPageChange(event: Pick<PageEvent, 'pageIndex' | 'pageSize' | 'length'>): void {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { page: event.pageIndex, size: event.pageSize },
      queryParamsHandling: 'merge',
    });
  }
}
