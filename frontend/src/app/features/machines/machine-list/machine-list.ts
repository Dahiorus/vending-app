import { httpResource } from '@angular/common/http';
import { Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth';
import { ApiRootApi } from '../../../core/http/api-root-api';
import { machinesPageUrl } from '../vending-machine-api';
import { intQueryParam } from '../../../shared/http/query-params';
import { HalPage } from '../../../shared/models/hal';
import { VendingMachine } from '../models/vending-machine';
import { ValueOrEmptyPipe } from '../../../shared/value-or-empty-pipe';
import { Page } from '../../../shared/models/page';

const DEFAULT_PAGE_SIZE = 20;

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
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly isAdmin = this.auth.isAdmin;

  protected readonly displayedColumns = [
    'serialNumber',
    'city',
    'itemType',
    'workingStatus',
    'powerStatus',
    'actions',
  ];

  private readonly queryParamMap = toSignal(this.route.queryParamMap, { requireSync: true });
  readonly pageIndex = computed(() => intQueryParam(this.queryParamMap().get('page'), 0));
  readonly pageSize = computed(() =>
    intQueryParam(this.queryParamMap().get('size'), DEFAULT_PAGE_SIZE),
  );

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
    void this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { page: event.pageIndex, size: event.pageSize },
      queryParamsHandling: 'merge',
    });
  }
}
