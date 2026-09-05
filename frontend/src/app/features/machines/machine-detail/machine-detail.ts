import { httpResource } from '@angular/common/http';
import { Component, computed, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { machineUrl } from '../vending-machine-api';
import { VendingMachine } from '../models/vending-machine';

interface DetailNavigationState {
  href?: string;
}

@Component({
  selector: 'app-machine-detail',
  imports: [MatButtonModule, MatCardModule, MatProgressBarModule, RouterLink],
  templateUrl: './machine-detail.html',
})
export class MachineDetail {
  private readonly route = inject(ActivatedRoute);

  // Follow the HATEOAS `self` link carried over from the listing via router
  // navigation state rather than reconstructing the resource URL. Fall back to
  // `machineUrl(id)` only when navigating directly (page refresh, bookmark),
  // where no navigation state is available.
  private readonly resourceUrl =
    (history.state as DetailNavigationState | null)?.href ??
    machineUrl(this.route.snapshot.paramMap.get('id')!);

  private readonly resource = httpResource<VendingMachine>(() => this.resourceUrl);

  readonly isLoading = this.resource.isLoading;
  readonly hasError = computed(() => this.resource.error() !== undefined);
  readonly machine = computed(() => this.resource.value());
}
