import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { email, form, FormField, required } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/auth/auth';

@Component({
  selector: 'app-login',
  imports: [FormField, MatButtonModule, MatCardModule, MatFormFieldModule, MatInputModule],
  templateUrl: './login.html',
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  readonly credentials = signal({ username: '', password: '' });
  readonly loginForm = form(this.credentials, (path) => {
    required(path.username);
    email(path.username);
    required(path.password);
  });

  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);

  submit(): void {
    if (!this.loginForm().valid()) {
      return;
    }

    this.submitting.set(true);
    this.errorMessage.set(null);

    this.auth.login(this.credentials()).subscribe({
      next: () => {
        this.submitting.set(false);
        void this.router.navigate(['/machines']);
      },
      error: (error: unknown) => {
        this.submitting.set(false);
        this.errorMessage.set(
          error instanceof HttpErrorResponse && error.status === 401
            ? 'Invalid email or password.'
            : 'Login failed. Please try again.',
        );
      },
    });
  }
}
