import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthSession, Credentials } from './models/auth';
import { User, UserToRegister } from './models/user';

@Service()
export class AuthApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiBaseUrl;

  login(credentials: Credentials): Observable<AuthSession> {
    return this.http.post<AuthSession>(`${this.baseUrl}/authenticate`, credentials, {
      withCredentials: true,
    });
  }

  register(payload: UserToRegister): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/register`, payload);
  }

  refresh(): Observable<AuthSession> {
    return this.http.post<AuthSession>(
      `${this.baseUrl}/authenticate/refresh`,
      {},
      { withCredentials: true },
    );
  }

  logout(): Observable<void> {
    return this.http.post<void>(
      `${this.baseUrl}/authenticate/logout`,
      {},
      { withCredentials: true },
    );
  }

  me(): Observable<User> {
    return this.http.get<User>(`${this.baseUrl}/me`);
  }
}
