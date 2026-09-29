import {
  Component,
  computed,
  DestroyRef,
  inject,
  input,
  linkedSignal,
  output,
  signal,
} from '@angular/core';
import { MatIconModule } from '@angular/material/icon';

/** Image types accepted by the backend `MultipartFileValidator`. */
export const ACCEPTED_IMAGE_TYPES: readonly string[] = ['image/jpeg', 'image/png'];

export function isAcceptedImage(file: File): boolean {
  return ACCEPTED_IMAGE_TYPES.includes(file.type);
}

@Component({
  selector: 'app-image-upload',
  imports: [MatIconModule],
  templateUrl: './image-upload.html',
})
export class ImageUpload {
  readonly currentImageUrl = input<string | null>(null);
  readonly label = input('Image');
  readonly fallbackIcon = input('image');
  readonly error = input<string | null>(null);

  readonly fileSelected = output<File | null>();

  protected readonly accept = ACCEPTED_IMAGE_TYPES.join(',');
  protected readonly previewUrl = signal<string | null>(null);
  protected readonly typeError = signal<string | null>(null);
  // Reset whenever the parent points to a new URL (e.g. cache-busted after an upload).
  private readonly currentImageFailed = linkedSignal({
    source: this.currentImageUrl,
    computation: () => false,
  });

  protected readonly displayedUrl = computed(
    () => this.previewUrl() ?? (this.currentImageFailed() ? null : this.currentImageUrl()),
  );
  protected readonly message = computed(() => this.typeError() ?? this.error());

  constructor() {
    inject(DestroyRef).onDestroy(() => this.revokePreview());
  }

  protected onFileChange(event: Event): void {
    const fileInput = event.target as HTMLInputElement;
    const file = fileInput.files?.[0] ?? null;

    this.revokePreview();

    if (file && !isAcceptedImage(file)) {
      this.typeError.set('Only JPEG and PNG images are accepted.');
      fileInput.value = '';
      this.fileSelected.emit(null);
      return;
    }

    this.typeError.set(null);
    this.previewUrl.set(file ? URL.createObjectURL(file) : null);
    this.fileSelected.emit(file);
  }

  protected onImageError(): void {
    if (!this.previewUrl()) {
      this.currentImageFailed.set(true);
    }
  }

  private revokePreview(): void {
    const url = this.previewUrl();
    if (url) {
      URL.revokeObjectURL(url);
      this.previewUrl.set(null);
    }
  }
}
