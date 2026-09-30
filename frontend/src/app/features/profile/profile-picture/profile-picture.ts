import { HttpClient } from '@angular/common/http';
import { Component, DestroyRef, effect, inject, input, output, signal, untracked } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar } from '@angular/material/snack-bar';
import { firstValueFrom } from 'rxjs';
import { ImageUpload } from '../../../shared/image-upload/image-upload';
import { uploadProfilePicture } from '../profile-api';

@Component({
  selector: 'app-profile-picture',
  imports: [ImageUpload, MatCardModule],
  templateUrl: './profile-picture.html',
})
export class ProfilePicture {
  private readonly http = inject(HttpClient);
  private readonly snackBar = inject(MatSnackBar);

  readonly pictureHref = input<string | undefined>(undefined);
  readonly uploaded = output<void>();

  readonly uploading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly currentImageUrl = signal<string | null>(null);

  constructor() {
    // A plain <img [src]="pictureHref"> would bypass the auth interceptor (it only attaches the
    // Authorization header to HttpClient requests), so the protected /me/picture endpoint would
    // silently fail to load. Fetch it through HttpClient instead and expose it as an object URL.
    // untracked: the auth interceptor reads the access token signal during the subscription; without
    // it a token refresh would re-run this effect and re-download the picture.
    effect(() => {
      const href = this.pictureHref();
      untracked(() => this.loadPicture(href));
    });

    inject(DestroyRef).onDestroy(() => this.revokeCurrentImageUrl());
  }

  onFileSelected(file: File | null): void {
    if (!file) {
      return;
    }

    const href = this.pictureHref();
    if (!href) {
      this.errorMessage.set('Profile picture upload link is unavailable.');
      return;
    }

    this.uploading.set(true);
    this.errorMessage.set(null);

    void firstValueFrom(uploadProfilePicture(this.http, href, file))
      .then(() => {
        this.uploading.set(false);
        this.loadPicture(href);
        this.snackBar.open('Profile picture updated.', 'Close', { duration: 5000 });
        this.uploaded.emit();
      })
      .catch(() => {
        this.uploading.set(false);
        this.errorMessage.set('Profile picture could not be updated.');
      });
  }

  private loadPicture(href: string | undefined): void {
    if (!href) {
      this.revokeCurrentImageUrl();
      this.currentImageUrl.set(null);
      return;
    }

    void firstValueFrom(this.http.get(href, { responseType: 'blob' }))
      .then((blob) => {
        this.revokeCurrentImageUrl();
        this.currentImageUrl.set(URL.createObjectURL(blob));
      })
      .catch(() => {
        // No picture uploaded yet (404) or a transient error: fall back to the placeholder icon.
        this.revokeCurrentImageUrl();
        this.currentImageUrl.set(null);
      });
  }

  private revokeCurrentImageUrl(): void {
    const url = this.currentImageUrl();
    if (url) {
      URL.revokeObjectURL(url);
    }
  }
}
