import { HttpClient, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import {
  profileUrl,
  updatePassword,
  updateProfile,
  uploadProfilePicture,
} from './profile-api';

describe('profile-api', () => {
  let http: HttpClient;
  let backend: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    http = TestBed.inject(HttpClient);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('builds the singleton profile URL from the API base URL', () => {
    expect(profileUrl()).toBe('/api/v1/me');
  });

  it('updates the profile with the JSON contract expected by PUT /me', () => {
    updateProfile(http, '/api/v1/me', { firstname: 'Ada', lastname: 'Lovelace' }).subscribe();

    const request = backend.expectOne('/api/v1/me');
    expect(request.request.method).toBe('PUT');
    expect(request.request.body).toEqual({
      firstname: 'Ada',
      lastname: 'Lovelace',
    });
    request.flush({
      id: 'u-1',
      email: 'ada@vending.me',
      firstname: 'Ada',
      lastname: 'Lovelace',
    });
  });

  it('updates the password with the JSON contract expected by POST /me/password', () => {
    updatePassword(http, '/api/v1/me/password', {
      oldPassword: 'OldPassw0rd!',
      newPassword: 'NewPassw0rd!',
    }).subscribe();

    const request = backend.expectOne('/api/v1/me/password');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toEqual({
      oldPassword: 'OldPassw0rd!',
      newPassword: 'NewPassw0rd!',
    });
    request.flush(null, { status: 204, statusText: 'No Content' });
  });

  it('uploads the profile picture as multipart FormData with field name file', () => {
    const file = new File(['avatar'], 'avatar.png', { type: 'image/png' });

    uploadProfilePicture(http, '/api/v1/me/picture', file).subscribe();

    const request = backend.expectOne('/api/v1/me/picture');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toBeInstanceOf(FormData);
    expect((request.request.body as FormData).get('file')).toBe(file);
    expect(request.request.headers.has('Content-Type')).toBe(false);
    request.flush({
      id: 'u-1',
      email: 'ada@vending.me',
      firstname: 'Ada',
      lastname: 'Lovelace',
    });
  });
});
