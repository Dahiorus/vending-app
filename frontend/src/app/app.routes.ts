import { Routes } from '@angular/router';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'machines' },
  {
    path: 'machines',
    loadComponent: () =>
      import('./features/machines/machine-list/machine-list').then((m) => m.MachineList),
    title: 'Vending machines',
  },
  {
    path: 'login',
    loadComponent: () => import('./features/auth/login/login').then((m) => m.Login),
    title: 'Sign in',
  },
  { path: '**', redirectTo: 'machines' },
];
