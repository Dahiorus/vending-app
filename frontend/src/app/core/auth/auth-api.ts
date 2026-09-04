import { HttpClient } from '@angular/common/http';
import { inject, Service } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthTokens, Credentials } from './models/auth';
import { User, UserToRegister } from './models/user';

@Service()
export class AuthApi {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = environment.apiBaseUrl;

  login(credentials: Credentials): Observable<AuthTokens> {
    return this.http.post<AuthTokens>(`${this.baseUrl}/authenticate`, credentials);
  }

  register(payload: UserToRegister): Observable<User> {
    return this.http.post<User>(`${this.baseUrl}/register`, payload);
  }

  refresh(refreshToken: string): Observable<AuthTokens> {
    return this.http.post<AuthTokens>(`${this.baseUrl}/authenticate/refresh`, {
      token: refreshToken,
    });
  }

  me(): Observable<User> {
    return this.http.get<User>(`${this.baseUrl}/me`);
  }
}
