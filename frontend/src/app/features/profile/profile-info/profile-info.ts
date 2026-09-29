import { HttpClient } from '@angular/common/http';
import { Component, effect, inject, input, output, signal } from '@angular/core';
import { form, FormField, required, submit } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSnackBar } from '@angular/material/snack-bar';
import { firstValueFrom } from 'rxjs';
import { User } from '../../../core/auth/models/user';
import { linkHref } from '../../../shared/models/hal';
import { parseValidationErrors } from '../../../shared/models/validation-error';
import { UserToUpdate } from '../models/user-profile';
import { updateProfile } from '../profile-api';

const FORM_FIELDS = ['firstname', 'lastname'];

@Component({
  selector: 'app-profile-info',
  imports: [FormField, MatButtonModule, MatCardModule, MatFormFieldModule, MatInputModule],
  templateUrl: './profile-info.html',
})
export class ProfileInfo {
  private readonly http = inject(HttpClient);
  private readonly snackBar = inject(MatSnackBar);

  readonly user = input.required<User>();
  readonly saved = output<User>();

  readonly profile = signal<UserToUpdate>({ firstname: '', lastname: '' });
  readonly infoForm = form(this.profile, (path) => {
    required(path.firstname);
    required(path.lastname);
  });
  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly fieldErrors = signal<Record<string, string>>({});

  constructor() {
    effect(() => {
      const user = this.user();
      this.profile.set({ firstname: user.firstname, lastname: user.lastname });
      this.fieldErrors.set({});
      this.errorMessage.set(null);
    });
  }

  submit(): void {
    void submit(this.infoForm, async () => {
      const href = linkHref(this.user(), 'self');
      if (!href) {
        this.errorMessage.set('Profile update link is unavailable.');
        return;
      }

      this.submitting.set(true);
      this.errorMessage.set(null);
      this.fieldErrors.set({});

      try {
        const updatedUser = await firstValueFrom(updateProfile(this.http, href, this.profile()));
        this.submitting.set(false);
        this.snackBar.open('Profile updated.', 'Close', { duration: 5000 });
        this.saved.emit(updatedUser);
      } catch (error) {
        this.submitting.set(false);

        const parsed = parseValidationErrors(error, FORM_FIELDS);
        if (parsed) {
          this.fieldErrors.set(parsed.fieldErrors);
          this.errorMessage.set(
            parsed.hasObjectLevelError ? 'Profile could not be updated.' : null,
          );
          return;
        }

        this.errorMessage.set('Profile could not be updated.');
      }
    });
  }
}
