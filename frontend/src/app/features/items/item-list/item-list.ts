import { httpResource } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { ApiRootApi } from '../../../core/http/api-root-api';
import { HalPage, toPage } from '../../../shared/models/hal';
import { itemsPageUrl } from '../item-api';
import { Item } from '../models/item';

const DEFAULT_PAGE_SIZE = 10;

@Component({
  selector: 'app-item-list',
  imports: [MatPaginatorModule, MatProgressBarModule, MatTableModule],
  templateUrl: './item-list.html',
})
export class ItemList {
  private readonly apiRoot = inject(ApiRootApi);

  protected readonly displayedColumns = ['name', 'type', 'price'];

  readonly pageIndex = signal(0);
  readonly pageSize = signal(DEFAULT_PAGE_SIZE);

  private readonly resource = httpResource<HalPage<Item>>(() =>
    itemsPageUrl(this.apiRoot.link('items'), this.pageIndex(), this.pageSize()),
  );

  readonly isLoading = this.resource.isLoading;
  readonly hasError = computed(() => this.resource.error() !== undefined);
  readonly items = computed(() =>
    this.resource.hasValue() ? toPage(this.resource.value()).elements : [],
  );
  readonly totalElements = computed(() =>
    this.resource.hasValue() ? toPage(this.resource.value()).totalElements : 0,
  );

  onPageChange(event: Pick<PageEvent, 'pageIndex' | 'pageSize' | 'length'>): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
  }
}
