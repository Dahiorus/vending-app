import { Routes } from '@angular/router';
import { adminGuard } from './core/auth/admin-guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'machines' },
  {
    path: 'machines',
    loadComponent: () =>
      import('./features/machines/machine-list/machine-list').then((m) => m.MachineList),
    title: 'Vending machines',
  },
  {
    path: 'machines/new',
    loadComponent: () =>
      import('./features/machines/machine-create/machine-create').then((m) => m.MachineCreate),
    canActivate: [adminGuard],
    title: 'New vending machine',
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login').then((m) => m.Login),
    title: 'Sign in',
  },
  {
    path: 'register',
    loadComponent: () => import('./features/auth/register/register').then((m) => m.Register),
    title: 'Create account',
  },
  { path: '**', redirectTo: 'machines' },
];
