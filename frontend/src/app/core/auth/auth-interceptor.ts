import {
  HttpErrorResponse,
  HttpEvent,
  HttpHandlerFn,
  HttpInterceptorFn,
  HttpRequest,
} from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, Observable, shareReplay, switchMap, throwError } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthService } from './auth';
import { TokenStore } from './token-store';

const AUTHENTICATION_PATH = `${environment.apiBaseUrl}/authenticate`;

/** Shared across concurrent 401s so that a burst of failures triggers a single refresh. */
let inFlightRefresh: Observable<string> | null = null;

function withBearer(request: HttpRequest<unknown>, token: string): HttpRequest<unknown> {
  return request.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
}

export const authInterceptor: HttpInterceptorFn = (
  request: HttpRequest<unknown>,
  next: HttpHandlerFn,
): Observable<HttpEvent<unknown>> => {
  const tokens = inject(TokenStore);
  const auth = inject(AuthService);

  const isApiRequest = request.url.startsWith(environment.apiBaseUrl);
  const isAuthenticationRequest = request.url.startsWith(AUTHENTICATION_PATH);
  const accessToken = tokens.accessToken();

  const outgoing =
    isApiRequest && !isAuthenticationRequest && accessToken
      ? withBearer(request, accessToken)
      : request;

  return next(outgoing).pipe(
    catchError((error: unknown) => {
      const isUnauthorized = error instanceof HttpErrorResponse && error.status === 401;
      if (!isUnauthorized || isAuthenticationRequest || !tokens.refreshToken()) {
        return throwError(() => error);
      }

      inFlightRefresh ??= auth.refreshAccessToken().pipe(
        catchError((refreshError: unknown) => {
          auth.logout();
          return throwError(() => refreshError);
        }),
        shareReplay({ bufferSize: 1, refCount: false }),
      );

      return inFlightRefresh.pipe(
        catchError((refreshError: unknown) => {
          inFlightRefresh = null;
          return throwError(() => refreshError);
        }),
        switchMap((freshToken) => {
          inFlightRefresh = null;
          return next(withBearer(request, freshToken));
        }),
      );
    }),
  );
};
