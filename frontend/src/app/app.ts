import { Component, computed, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';
import { Router, RouterLink, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth/auth';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, RouterLink, MatButtonModule, MatToolbarModule],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  protected readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly isAdmin = this.auth.isAdmin;
  protected readonly isAuthenticated = computed(() => this.auth.isAuthenticated());
  protected readonly currentUser = computed(() => this.auth.currentUser());

  protected logout(): void {
    this.auth.logout();
    void this.router.navigate(['/login']);
  }
}
