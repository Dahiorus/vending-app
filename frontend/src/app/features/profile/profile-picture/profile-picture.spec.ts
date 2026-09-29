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

  beforeEach(async () => {
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
    fixture.componentRef.setInput('pictureHref', '/api/v1/me/picture');
    fixture.detectChanges();
    await fixture.whenStable();
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.verify());

  it('passes the original picture URL to the shared upload component before the first upload', () => {
    expect(component.currentImageUrl()).toBe('/api/v1/me/picture');
  });

  it('preserves an absolute picture URL origin when adding the cache-busting version', async () => {
    fixture.componentRef.setInput(
      'pictureHref',
      'https://api.example.test/api/v1/me/picture?size=small',
    );
    fixture.detectChanges();
    const file = new File(['avatar'], 'avatar.png', { type: 'image/png' });

    component.onFileSelected(file);

    backend.expectOne('https://api.example.test/api/v1/me/picture?size=small').flush({ id: 'u-1' });
    await fixture.whenStable();

    expect(component.currentImageUrl()).toBe(
      'https://api.example.test/api/v1/me/picture?size=small&v=1',
    );
  });

  it('uploads a selected image, increments the cache-busting version, and emits uploaded', async () => {
    const emitted: void[] = [];
    component.uploaded.subscribe(() => emitted.push(undefined));
    const file = new File(['avatar'], 'avatar.png', { type: 'image/png' });

    component.onFileSelected(file);

    const request = backend.expectOne('/api/v1/me/picture');
    expect(request.request.method).toBe('POST');
    expect(request.request.body).toBeInstanceOf(FormData);
    expect((request.request.body as FormData).get('file')).toBe(file);
    request.flush({ id: 'u-1' });
    await fixture.whenStable();

    expect(component.currentImageUrl()).toBe('/api/v1/me/picture?v=1');
    expect(snackBar.open).toHaveBeenCalledWith('Profile picture updated.', 'Close', {
      duration: 5000,
    });
    expect(emitted.length).toBe(1);
  });

  it('does not send a request when the shared upload component rejects a file and emits null', () => {
    component.onFileSelected(null);

    backend.expectNone('/api/v1/me/picture');
  });

  it('shows a clear error when the picture link is unavailable', () => {
    fixture.componentRef.setInput('pictureHref', undefined);
    fixture.detectChanges();

    component.onFileSelected(new File(['avatar'], 'avatar.png', { type: 'image/png' }));

    backend.expectNone('/api/v1/me/picture');
    expect(component.errorMessage()).toBe('Profile picture upload link is unavailable.');
  });
});
