import { HttpClient } from '@angular/common/http';
import { Component, computed, inject, input, output, signal } from '@angular/core';
import { MatCardModule } from '@angular/material/card';
import { MatSnackBar } from '@angular/material/snack-bar';
import { firstValueFrom } from 'rxjs';
import { withCacheBuster } from '../../../shared/http/cache-buster';
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

  readonly version = signal(0);
  readonly uploading = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly currentImageUrl = computed(() => {
    const href = this.pictureHref();
    return href ? withCacheBuster(href, this.version()) : null;
  });

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
        this.version.update((value) => value + 1);
        this.snackBar.open('Profile picture updated.', 'Close', { duration: 5000 });
        this.uploaded.emit();
      })
      .catch(() => {
        this.uploading.set(false);
        this.errorMessage.set('Profile picture could not be updated.');
      });
  }
}
