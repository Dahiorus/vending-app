import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MatSnackBar } from '@angular/material/snack-bar';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { ProfilePicture } from './profile-picture';

describe('ProfilePicture', () => {
  let fixture: ComponentFixture<ProfilePicture>;
  let component: ProfilePicture;
  let backend: HttpTestingController;
  let snackBar: { open: ReturnType<typeof vi.fn> };
  let createObjectURL: ReturnType<typeof vi.fn>;
  let revokeObjectURL: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    // jsdom does not implement object URLs.
    let counter = 0;
    createObjectURL = vi.fn(() => `blob:picture-${++counter}`);
    revokeObjectURL = vi.fn();
    URL.createObjectURL = createObjectURL as unknown as typeof URL.createObjectURL;
    URL.revokeObjectURL = revokeObjectURL as unknown as typeof URL.revokeObjectURL;

    snackBar = { open: vi.fn() };
    await TestBed.configureTestingModule({
      imports: [ProfilePicture],
      providers: [
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: MatSnackBar, useValue: snackBar },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ProfilePicture);
    component = fixture.componentInstance;
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    backend.verify();
    vi.restoreAllMocks();
  });

  it('does not fetch anything when no pictureHref is available', async () => {
    fixture.componentRef.setInput('pictureHref', undefined);
    fixture.detectChanges();
    await fixture.whenStable();

    backend.expectNone(() => true);
    expect(component.currentImageUrl()).toBeNull();
  });

  it('loads the current picture with an authenticated GET request when the page displays it', async () => {
    // A plain <img src> would bypass the auth interceptor (no Authorization header), so the
    // protected /me/picture endpoint must be fetched through HttpClient instead.
    fixture.componentRef.setInput('pictureHref', '/api/v1/me/picture');
    fixture.detectChanges();
    await fixture.whenStable();

    const request = backend.expectOne('/api/v1/me/picture');
    expect(request.request.method).toBe('GET');
    expect(request.request.responseType).toBe('blob');
    request.flush(new Blob(['picture-bytes'], { type: 'image/png' }));
    await fixture.whenStable();

    expect(createObjectURL).toHaveBeenCalledTimes(1);
    expect(component.currentImageUrl()).toBe('blob:picture-1');
  });

  it('shows no picture when the user has not uploaded one yet (404)', async () => {
    fixture.componentRef.setInput('pictureHref', '/api/v1/me/picture');
    fixture.detectChanges();
    await fixture.whenStable();

    const request = backend.expectOne('/api/v1/me/picture');
    request.flush(null, { status: 404, statusText: 'Not Found' });
    await fixture.whenStable();

    expect(component.currentImageUrl()).toBeNull();
  });

  it('re-fetches and revokes the previous picture after a successful upload', async () => {
    fixture.componentRef.setInput('pictureHref', '/api/v1/me/picture');
    fixture.detectChanges();
    await fixture.whenStable();

    backend.expectOne('/api/v1/me/picture').flush(new Blob(['old-bytes'], { type: 'image/png' }));
    await fixture.whenStable();
    expect(component.currentImageUrl()).toBe('blob:picture-1');

    const emitted: void[] = [];
    component.uploaded.subscribe(() => emitted.push(undefined));
    const file = new File(['avatar'], 'avatar.png', { type: 'image/png' });

    component.onFileSelected(file);

    const uploadRequest = backend.expectOne('/api/v1/me/picture');
    expect(uploadRequest.request.method).toBe('POST');
    expect(uploadRequest.request.body).toBeInstanceOf(FormData);
    expect((uploadRequest.request.body as FormData).get('file')).toBe(file);
    uploadRequest.flush({ id: 'u-1' });
    await fixture.whenStable();

    const reloadRequest = backend.expectOne('/api/v1/me/picture');
    expect(reloadRequest.request.method).toBe('GET');
    reloadRequest.flush(new Blob(['new-bytes'], { type: 'image/png' }));
    await fixture.whenStable();

    expect(revokeObjectURL).toHaveBeenCalledWith('blob:picture-1');
    expect(component.currentImageUrl()).toBe('blob:picture-2');
    expect(snackBar.open).toHaveBeenCalledWith('Profile picture updated.', 'Close', {
      duration: 5000,
    });
    expect(emitted.length).toBe(1);
  });

  it('does not send a request when the shared upload component rejects a file and emits null', () => {
    fixture.componentRef.setInput('pictureHref', '/api/v1/me/picture');
    fixture.detectChanges();
    backend.expectOne('/api/v1/me/picture').flush(null, { status: 404, statusText: 'Not Found' });

    component.onFileSelected(null);

    backend.expectNone(() => true);
  });

  it('shows a clear error when the picture link is unavailable', () => {
    fixture.componentRef.setInput('pictureHref', undefined);
    fixture.detectChanges();

    component.onFileSelected(new File(['avatar'], 'avatar.png', { type: 'image/png' }));

    backend.expectNone(() => true);
    expect(component.errorMessage()).toBe('Profile picture upload link is unavailable.');
  });
});
