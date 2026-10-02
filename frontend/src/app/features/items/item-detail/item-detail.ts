import { HttpClient, httpResource } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth';
import { withCacheBuster } from '../../../shared/http/cache-buster';
import { ValueOrEmptyPipe } from '../../../shared/value-or-empty-pipe';
import {
  deleteErrorMessage,
  itemImageHref,
  itemImageUrl,
  itemSelfHref,
  itemUrl,
} from '../item-api';
import { PricePipe } from '../../../shared/price/price';
import { ItemDeleteDialog, ItemDeleteDialogData } from '../item-delete-dialog/item-delete-dialog';
import { Item } from '../models/item';

interface DetailNavigationState {
  href?: string;
  imageVersion?: number;
}

@Component({
  selector: 'app-item-detail',
  imports: [
    PricePipe,
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressBarModule,
    RouterLink,
    ValueOrEmptyPipe,
  ],
  templateUrl: './item-detail.html',
})
export class ItemDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly http = inject(HttpClient);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);
  private readonly auth = inject(AuthService);

  readonly isAdmin = this.auth.isAdmin;
  readonly showImageFallback = signal(false);
  private readonly navigationState = history.state as DetailNavigationState | null;
  readonly imageVersion = signal(
    typeof this.navigationState?.imageVersion === 'number' ? this.navigationState.imageVersion : 0,
  );

  private readonly paramMap = toSignal(this.route.paramMap, { requireSync: true });
  private readonly itemId = computed(() => this.paramMap().get('id')!);

  // The navigation state only describes the item the page was opened for: another id falls back to the built URL.
  private readonly resourceUrl = computed(() => {
    const href = this.navigationState?.href;
    return href?.endsWith(`/${this.itemId()}`) ? href : itemUrl(this.itemId());
  });

  private readonly resource = httpResource<Item>(() => this.resourceUrl());

  readonly isLoading = this.resource.isLoading;
  readonly hasError = computed(() => this.resource.error() !== undefined);
  readonly item = computed(() => this.resource.value());
  readonly selfHref = computed(() => itemSelfHref(this.item()));
  readonly imageUrl = computed(() => {
    const href = itemImageHref(this.item()) ?? itemImageUrl(this.itemId());
    return withCacheBuster(href, this.imageVersion());
  });

  imageFailed(): void {
    this.showImageFallback.set(true);
  }

  deleteItem(): void {
    const item = this.item();
    if (!item) {
      return;
    }

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
            void this.router.navigate(['/items']);
          },
          error: (error: unknown) => {
            this.snackBar.open(deleteErrorMessage(error), 'Close', { duration: 5000 });
          },
        });
      });
  }
}
