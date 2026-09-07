import { Component, computed, inject, signal } from '@angular/core';
import { email, form, FormField, required, validate } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/auth/auth';
import { parseValidationErrors } from '../../../shared/models/validation-error';

const FORM_FIELDS = ['email', 'password', 'firstname', 'lastname'];

@Component({
  selector: 'app-register',
  imports: [
    FormField,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    RouterLink,
  ],
  templateUrl: './register.html',
})
export class Register {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly registration = signal({
    email: '',
    password: '',
    confirmPassword: '',
    firstname: '',
    lastname: '',
  });
  readonly registerForm = form(this.registration, (path) => {
    required(path.email);
    email(path.email);
    required(path.password);
    validate(path.confirmPassword, ({ value, valueOf }) => {
      const confirmPassword = value();
      const password = valueOf(path.password);

      if (confirmPassword === password) {
        return undefined;
      }

      return { kind: 'mismatch', message: 'Passwords do not match.' };
    });
    required(path.firstname);
    required(path.lastname);
  });

  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly fieldErrors = signal<Record<string, string>>({});
  readonly passwordsMatch = computed(
    () => this.registration().password === this.registration().confirmPassword,
  );

  submit(): void {
    if (!this.registerForm().valid() || !this.passwordsMatch()) {
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);
    this.fieldErrors.set({});

    const { confirmPassword: _, ...payload } = this.registration();

    this.auth.register(payload).subscribe({
      next: () => {
        this.submitting.set(false);
        void this.router.navigate(['/machines']);
      },
      error: (error: unknown) => {
        this.submitting.set(false);

        const parsed = parseValidationErrors(error, FORM_FIELDS);

        if (parsed) {
          this.fieldErrors.set(parsed.fieldErrors);
          this.errorMessage.set(
            parsed.hasObjectLevelError ? 'An account with this email may already exist.' : null,
          );
          return;
        }

        this.errorMessage.set('Registration failed. Please try again.');
      },
    });
  }
}
