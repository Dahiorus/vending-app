import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

/**
 * Uploads an image to an endpoint validated by the backend `MultipartFileValidator`
 * (multipart param `file`, JPEG/PNG only). The Content-Type header is left to the
 * browser so that it carries the multipart boundary.
 */
export function uploadImageFile<T = unknown>(
  http: HttpClient,
  href: string,
  file: File,
): Observable<T> {
  const body = new FormData();
  body.append('file', file);

  return http.post<T>(href, body);
}
