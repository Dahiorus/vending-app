import { httpResource } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth';
import { ApiRootApi } from '../../../core/http/api-root-api';
import { machinesPageUrl } from '../vending-machine-api';
import { HalPage, Page } from '../../../shared/models/hal';
import { VendingMachine } from '../models/vending-machine';
import { ValueOrEmptyPipe } from '../../../shared/value-or-empty-pipe';

@Component({
  selector: 'app-machine-list',
  imports: [
    MatButtonModule,
    MatIconModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatTableModule,
    RouterLink,
    ValueOrEmptyPipe,
  ],
  templateUrl: './machine-list.html',
})
export class MachineList {
  private readonly auth = inject(AuthService);
  private readonly apiRoot = inject(ApiRootApi);

  readonly isAdmin = this.auth.isAdmin;

  protected readonly displayedColumns = [
    'serialNumber',
    'city',
    'itemType',
    'workingStatus',
    'powerStatus',
    'actions',
  ];

  readonly pageIndex = signal(0);
  readonly pageSize = signal(20);

  private readonly resource = httpResource<HalPage<VendingMachine>>(() =>
    machinesPageUrl(this.apiRoot.link('vendingMachines'), this.pageIndex(), this.pageSize()),
  );

  readonly isLoading = this.resource.isLoading;
  readonly hasError = computed(() => this.resource.error() !== undefined);
  readonly machines = computed(() =>
    this.resource.hasValue()
      ? Page.fromHalPage(this.resource.value())
      : Page.empty<VendingMachine>(),
  );
  readonly totalElements = computed(() => this.machines().totalElements);

  onPageChange(event: Pick<PageEvent, 'pageIndex' | 'pageSize' | 'length'>): void {
    this.pageIndex.set(event.pageIndex);
    this.pageSize.set(event.pageSize);
  }
}
