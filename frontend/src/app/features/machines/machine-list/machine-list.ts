import { httpResource } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth';
import { machinesPageUrl } from '../vending-machine-api';
import { HalPage, toPage } from '../../../shared/models/hal';
import { VendingMachine } from '../models/vending-machine';

const DEFAULT_PAGE_SIZE = 10;

@Component({
  selector: 'app-machine-list',
  imports: [
    MatButtonModule,
    MatIconModule,
    MatPaginatorModule,
    MatProgressBarModule,
    MatTableModule,
    RouterLink,
  ],
  templateUrl: './machine-list.html',
})
export class MachineList {
  private readonly auth = inject(AuthService);

  readonly isAdmin = computed(() => this.auth.roles().includes('ROLE_ADMIN'));

  protected readonly displayedColumns = [
    'serialNumber',
    'city',
    'itemType',
    'workingStatus',
    'powerStatus',
    'actions',
  ];

  readonly pageIndex = signal(0);
  readonly pageSize = signal(DEFAULT_PAGE_SIZE);

  private readonly resource = httpResource<HalPage<VendingMachine>>(() =>
    machinesPageUrl(this.pageIndex(), this.pageSize()),
  );

  readonly isLoading = this.resource.isLoading;
  readonly hasError = computed(() => this.resource.error() !== undefined);
  readonly machines = computed(() =>
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
