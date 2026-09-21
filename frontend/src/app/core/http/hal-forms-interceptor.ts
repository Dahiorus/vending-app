import { HttpEvent, HttpHandlerFn, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';

/**
 * Spring HATEOAS negotiates HAL-FORMS support (`_templates`) from the `Accept` header
 * (`@EnableHypermediaSupport(type = HAL_FORMS)`); `application/json` is kept as a lower
 * priority fallback for endpoints that always produce plain JSON (login, refresh, ...).
 */
const ACCEPT_HAL_FORMS = 'application/prs.hal-forms+json, application/hal+json;q=0.9, application/json;q=0.8';

export const halFormsInterceptor: HttpInterceptorFn = (
  request: HttpRequest<unknown>,
  next: HttpHandlerFn,
): Observable<HttpEvent<unknown>> => {
  const isApiRequest = request.url.startsWith(environment.apiBaseUrl);

  if (!isApiRequest || request.headers.has('Accept')) {
    return next(request);
  }

  return next(request.clone({ setHeaders: { Accept: ACCEPT_HAL_FORMS } }));
};
