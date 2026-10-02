import { HttpClient, httpResource } from '@angular/common/http';
import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth';
import { ApiRootApi } from '../../../core/http/api-root-api';
import { intQueryParam } from '../../../shared/http/query-params';
import { HalPage } from '../../../shared/models/hal';
import { Page } from '../../../shared/models/page';
import { PricePipe } from '../../../shared/price/price';
import { ItemDeleteDialog, ItemDeleteDialogData } from '../item-delete-dialog/item-delete-dialog';
import {
  createItemHref,
  deleteErrorMessage,
  itemSelfHref,
  itemUrl,
  itemsPageUrl,
} from '../item-api';
import { Item } from '../models/item';

const DEFAULT_PAGE_SIZE = 20;

@Component({
  selector: 'app-item-list',
  imports: [
    PricePipe,
    MatButtonModule,
    MatIconModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatTableModule,
    RouterLink,
  ],
  templateUrl: './item-list.html',
})
export class ItemList {
  private readonly auth = inject(AuthService);
  private readonly apiRoot = inject(ApiRootApi);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly http = inject(HttpClient);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly isAdmin = this.auth.isAdmin;
  readonly displayedColumns = ['name', 'type', 'price', 'actions'];

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
  readonly createHref = computed(() =>
    this.resource.hasValue() ? createItemHref(this.resource.value()) : undefined,
  );

  onPageChange(event: Pick<PageEvent, 'pageIndex' | 'pageSize' | 'length'>): void {
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { page: event.pageIndex, size: event.pageSize },
      queryParamsHandling: 'merge',
    });
  }

  deleteItem(item: Item, event?: Event): void {
    event?.stopPropagation();

    this.dialog
      .open(ItemDeleteDialog, {
        data: { itemName: item.name ?? 'this item' } satisfies ItemDeleteDialogData,
      })
      .afterClosed()
      .subscribe((confirmed) => {
        if (confirmed !== true) {
          return;
        }

        this.http.delete(itemSelfHref(item) ?? itemUrl(item.id)).subscribe({
          next: () => {
            this.snackBar.open(`Deleted ${item.name ?? 'item'}`, 'Close', { duration: 5000 });
            if (this.pageIndex() > 0 && this.items().elements.length <= 1) {
              this.onPageChange({
                pageIndex: this.pageIndex() - 1,
                pageSize: this.pageSize(),
                length: this.totalElements() - 1,
              });
            } else {
              this.resource.reload();
            }
          },
          error: (error: unknown) => {
            this.snackBar.open(deleteErrorMessage(error), 'Close', { duration: 5000 });
          },
        });
      });
  }
}
