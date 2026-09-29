import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Component, computed, inject, input, signal } from '@angular/core';
import { form, FormField, required, submit, validate } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { Router } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { AuthService } from '../../../core/auth/auth';
import { parseValidationErrors } from '../../../shared/models/validation-error';
import { updatePassword } from '../profile-api';

@Component({
  imports: [FormField, MatButtonModule, MatCardModule, MatFormFieldModule, MatInputModule],
  selector: 'app-profile-password',
  templateUrl: './profile-password.html',
})
export class ProfilePassword {
  private readonly http = inject(HttpClient);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly snackBar = inject(MatSnackBar);

  readonly passwordHref = input<string | undefined>(undefined);
  readonly passwords = signal({ oldPassword: '', newPassword: '', confirmNewPassword: '' });
  readonly passwordForm = form(this.passwords, (path) => {
    required(path.oldPassword);
    required(path.newPassword);
    validate(path.confirmNewPassword, ({ value, valueOf }) => {
      if (value() === valueOf(path.newPassword)) {
        return undefined;
      }
      return { kind: 'mismatch', message: 'Passwords do not match.' };
    });
  });
  readonly passwordsMatch = computed(
    () => this.passwords().newPassword === this.passwords().confirmNewPassword,
  );
  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly fieldErrors = signal<Record<string, string>>({});

  submit(): void {
    if (!this.passwordsMatch()) {
      return;
    }

    void submit(this.passwordForm, async () => {
      const href = this.passwordHref();
      if (!href) {
        this.errorMessage.set('Password update link is unavailable.');
        return;
      }

      this.submitting.set(true);
      this.errorMessage.set(null);
      this.fieldErrors.set({});

      try {
        const { oldPassword, newPassword } = this.passwords();
        await firstValueFrom(updatePassword(this.http, href, { oldPassword, newPassword }));
        this.submitting.set(false);
        this.auth.logout();
        await this.router.navigate(['/login']);
        this.snackBar.open('Password changed. Please sign in again.', 'Close', { duration: 5000 });
      } catch (error) {
        this.submitting.set(false);

        const parsed = parseValidationErrors(error, ['password']);
        if (parsed?.fieldErrors['password']) {
          this.fieldErrors.set({ newPassword: parsed.fieldErrors['password'] });
          return;
        }

        if (error instanceof HttpErrorResponse && error.status === 400) {
          this.fieldErrors.set({ oldPassword: 'Current password is incorrect.' });
          return;
        }

        this.errorMessage.set('Password could not be changed.');
      }
    });
  }
}
