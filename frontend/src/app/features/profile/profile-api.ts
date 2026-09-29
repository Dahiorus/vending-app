import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User } from '../../core/auth/models/user';
import { uploadImageFile } from '../../shared/http/upload-image-file';
import { EditPasswordRequest, UserToUpdate } from './models/user-profile';

export function profileUrl(): string {
  return `${environment.apiBaseUrl}/me`;
}

export function updateProfile(
  http: HttpClient,
  href: string,
  payload: UserToUpdate,
): Observable<User> {
  return http.put<User>(href, payload);
}

export function updatePassword(
  http: HttpClient,
  href: string,
  payload: EditPasswordRequest,
): Observable<void> {
  return http.post<void>(href, payload);
}

export function uploadProfilePicture(
  http: HttpClient,
  href: string,
  file: File,
): Observable<User> {
  return uploadImageFile<User>(http, href, file);
}
