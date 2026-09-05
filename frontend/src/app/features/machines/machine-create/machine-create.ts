import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { form, FormField, required, submit } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { ITEM_TYPES } from '../models/enums';
import { VendingMachineToCreate } from '../models/vending-machine';
import { machinesUrl } from '../vending-machine-api';
import { ValidationErrorResponse } from '../../../shared/models/validation-error';

const FORM_FIELDS = [
  'serialNumber',
  'itemType',
  'address.latitude',
  'address.longitude',
  'address.streetNumber',
  'address.streetName',
  'address.postalCode',
  'address.city',
];

@Component({
  selector: 'app-machine-create',
  imports: [
    FormField,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
  ],
  templateUrl: './machine-create.html',
})
export class MachineCreate {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  protected readonly itemTypes = ITEM_TYPES;

  readonly machine = signal<VendingMachineToCreate>({
    serialNumber: '',
    address: {
      latitude: null,
      longitude: null,
      streetNumber: null,
      streetName: '',
      postalCode: '',
      city: '',
    },
    itemType: null,
  });

  readonly machineForm = form(this.machine, (path) => {
    required(path.serialNumber);
    required(path.itemType);
    required(path.address.latitude);
    required(path.address.longitude);
    required(path.address.streetNumber);
    required(path.address.streetName);
    required(path.address.postalCode);
    required(path.address.city);
  });

  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly fieldErrors = signal<Record<string, string>>({});

  submit(): void {
    void submit(this.machineForm, async () => {
      this.submitting.set(true);
      this.errorMessage.set(null);
      this.fieldErrors.set({});

      try {
        await firstValueFrom(this.http.post(machinesUrl(), this.machine()));
        this.submitting.set(false);
        void this.router.navigate(['/machines']);
      } catch (error) {
        this.submitting.set(false);

        if (error instanceof HttpErrorResponse && this.isValidationErrorResponse(error.error)) {
          const fieldErrors: Record<string, string> = {};
          let hasObjectLevelError = false;

          for (const validationError of error.error.errors) {
            if (validationError.field && FORM_FIELDS.includes(validationError.field)) {
              fieldErrors[validationError.field] = validationError.defaultMessage;
              continue;
            }

            hasObjectLevelError = true;
          }

          this.fieldErrors.set(fieldErrors);
          this.errorMessage.set(
            hasObjectLevelError ? 'The vending machine could not be created.' : null,
          );
          return;
        }

        this.errorMessage.set('Creation failed. Please try again.');
      }
    });
  }

  private isValidationErrorResponse(error: unknown): error is ValidationErrorResponse {
    return (
      typeof error === 'object' &&
      error !== null &&
      'errors' in error &&
      Array.isArray(error.errors)
    );
  }
}
