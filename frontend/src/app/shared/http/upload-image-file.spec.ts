import { HttpClient, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { uploadImageFile } from './upload-image-file';

describe('uploadImageFile', () => {
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

  it('POSTs the file as multipart form data under the `file` key', () => {
    const file = new File(['png-bytes'], 'cola.png', { type: 'image/png' });
    let response: unknown;

    uploadImageFile(http, '/api/v1/items/i-1/image', file).subscribe((body) => (response = body));

    const request = backend.expectOne('/api/v1/items/i-1/image');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toBeInstanceOf(FormData);
    expect((request.request.body as FormData).get('file')).toBe(file);
    request.flush({ id: 'i-1' });

    expect(response).toEqual({ id: 'i-1' });
  });

  it('does not set the Content-Type header, letting the browser add the multipart boundary', () => {
    const file = new File(['jpg-bytes'], 'me.jpg', { type: 'image/jpeg' });

    uploadImageFile(http, '/api/v1/me/picture', file).subscribe();

    const request = backend.expectOne('/api/v1/me/picture');
    expect(request.request.headers.has('Content-Type')).toBe(false);
    request.flush({});
  });
});
