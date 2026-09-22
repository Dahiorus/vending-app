import { httpResource } from '@angular/common/http';
import { Component, computed, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth';
import { ValueOrEmptyPipe } from '../../../shared/value-or-empty-pipe';
import { machineUrl } from '../vending-machine-api';
import { VendingMachine, VendingMachineStock } from '../models/vending-machine';
import { DatePipe } from '@angular/common';

interface DetailNavigationState {
  href?: string;
}

@Component({
  selector: 'app-machine-detail',
  imports: [
    MatButtonModule,
    MatCardModule,
    MatIconModule,
    MatProgressBarModule,
    RouterLink,
    ValueOrEmptyPipe,
    DatePipe,
  ],
  templateUrl: './machine-detail.html',
})
export class MachineDetail {
  private readonly route = inject(ActivatedRoute);
  private readonly auth = inject(AuthService);

  readonly isAdmin = computed(() => this.auth.roles().includes('ROLE_ADMIN'));

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

  readonly address = computed(() => {
    const address = this.machine()?.address;
    return address
      ? `${address.streetNumber} ${address.streetName}, ${address.postalCode} ${address.city}`
      : null;
  });
}
