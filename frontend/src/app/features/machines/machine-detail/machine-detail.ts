import { DatePipe } from '@angular/common';
import { HttpClient, HttpErrorResponse, httpResource } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { AuthService } from '../../../core/auth/auth';
import { ValueOrEmptyPipe } from '../../../shared/value-or-empty-pipe';
import { ClientOrder } from '../models/client-order';
import { ItemQuantity, VendingMachine, VendingMachineStock } from '../models/vending-machine';
import {
  OrderConfirmDialog,
  OrderConfirmDialogData,
} from '../order-confirm-dialog/order-confirm-dialog';
import { machineUrl } from '../vending-machine-api';

interface DetailNavigationState {
  href?: string;
}

function extractErrorMessage(error: unknown): string | undefined {
  if (
    error instanceof HttpErrorResponse &&
    typeof error.error === 'object' &&
    error.error !== null &&
    'message' in error.error &&
    typeof error.error.message === 'string'
  ) {
    return error.error.message;
  }

  return undefined;
}

@Component({
  selector: 'app-machine-detail',
  imports: [
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    RouterLink,
    ValueOrEmptyPipe,
    DatePipe,
  ],
  templateUrl: './machine-detail.html',
})
export class MachineDetail {
  private readonly route = inject(ActivatedRoute);
  readonly auth = inject(AuthService);
  private readonly http = inject(HttpClient);
  private readonly dialog = inject(MatDialog);
  private readonly snackBar = inject(MatSnackBar);

  readonly isAdmin = computed(() => this.auth.roles().includes('ROLE_ADMIN'));
  readonly ordering = signal<string | null>(null);

  // Follow the HATEOAS `self` link carried over from the listing via router
  // navigation state rather than reconstructing the resource URL. Fall back to
  // `machineUrl(id)` only when navigating directly (page refresh, bookmark),
  // where no navigation state is available.
  private readonly resourceUrl =
    (history.state as DetailNavigationState | null)?.href ??
    machineUrl(this.route.snapshot.paramMap.get('id')!);

  private readonly resource = httpResource<VendingMachine>(() => this.resourceUrl);

  // Follows the `stock` HATEOAS link exposed on the machine resource itself,
  // available only once the machine has loaded.
  private readonly stockUrl = computed(() => {
    const links = this.resource.value()?._links?.['stock'];
    return links && !Array.isArray(links) ? links.href : undefined;
  });
  private readonly stockResource = httpResource<VendingMachineStock>(() => this.stockUrl());

  readonly isLoading = this.resource.isLoading;
  readonly hasError = computed(() => this.resource.error() !== undefined);
  readonly machine = computed(() => this.resource.value());
  readonly stockLoading = this.stockResource.isLoading;
  readonly stockError = computed(() => this.stockResource.error() !== undefined);
  readonly itemQuantities = computed(() => this.stockResource.value()?.itemQuantities ?? []);

  // The stock resource carries one `item` link per distinct item (not keyed by
  // item, see VendingMachineStockDtoModelAssembler), so the matching link is
  // found by checking which href contains the item's id.
  itemLink(itemId: string): string | undefined {
    const links = this.stockResource.value()?._links?.['item'];
    const itemLinks = Array.isArray(links) ? links : links ? [links] : [];
    return itemLinks.find((link) => link.href.includes(itemId))?.href;
  }

  orderLink(itemId: string): string | undefined {
    const links = this.stockResource.value()?._links?.['order'];
    const orderLinks = Array.isArray(links) ? links : links ? [links] : [];
    return orderLinks.find((link) => link.href.includes(itemId))?.href;
  }

  canOrder(itemId: string): boolean {
    return this.auth.roles().includes('ROLE_USER') && this.orderLink(itemId) !== undefined;
  }

  orderItem(itemQuantity: ItemQuantity): void {
    const orderLink = this.orderLink(itemQuantity.itemId);
    if (!orderLink) {
      return;
    }

    this.dialog
      .open(OrderConfirmDialog, {
        data: {
          itemName: itemQuantity.itemName,
          quantity: itemQuantity.quantity,
        } satisfies OrderConfirmDialogData,
      })
      .afterClosed()
      .subscribe((confirmed) => {
        if (confirmed !== true) {
          return;
        }

        this.ordering.set(itemQuantity.itemId);

        this.http
          .post<ClientOrder>(orderLink, {})
          .pipe(finalize(() => this.ordering.set(null)))
          .subscribe({
            next: (order) => {
              this.snackBar.open(
                `Ordered ${itemQuantity.itemName} for ${order.amount} €`,
                'Close',
                {
                  duration: 5000,
                },
              );
              this.stockResource.reload();
            },
            error: (error) => {
              this.snackBar.open(
                extractErrorMessage(error) ?? 'The item could not be ordered.',
                'Close',
                { duration: 5000 },
              );
              this.stockResource.reload();
            },
          });
      });
  }

  readonly address = computed(() => {
    const address = this.machine()?.address;
    return address
      ? `${address.streetNumber} ${address.streetName}, ${address.postalCode} ${address.city}`
      : null;
  });
}
