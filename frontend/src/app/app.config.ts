import { provideHttpClient, withInterceptors, withXsrfConfiguration } from '@angular/common/http';
import {
  ApplicationConfig,
  inject,
  provideAppInitializer,
  provideBrowserGlobalErrorListeners,
} from '@angular/core';
import { provideRouter } from '@angular/router';
import { catchError, firstValueFrom, of } from 'rxjs';
import { AuthService } from './core/auth/auth';
import { authInterceptor } from './core/auth/auth-interceptor';
import { halFormsInterceptor } from './core/http/hal-forms-interceptor';
import { routes } from './app.routes';

export function restoreSessionOnStartup(): Promise<string | undefined> {
  return firstValueFrom(
    inject(AuthService)
      .refreshAccessToken()
      .pipe(catchError(() => of(undefined))),
  );
}

export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideAppInitializer(restoreSessionOnStartup),
    provideRouter(routes),
    provideHttpClient(
      withInterceptors([halFormsInterceptor, authInterceptor]),
      withXsrfConfiguration({ cookieName: 'XSRF-TOKEN', headerName: 'X-XSRF-TOKEN' }),
    ),
  ],
};
