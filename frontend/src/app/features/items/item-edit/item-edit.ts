import { HttpClient, httpResource } from '@angular/common/http';
import { Component, computed, effect, inject, signal } from '@angular/core';
import { form, FormField, min, submit } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { withCacheBuster } from '../../../shared/http/cache-buster';
import { uploadImageFile } from '../../../shared/http/upload-image-file';
import { ImageUpload } from '../../../shared/image-upload/image-upload';
import { parseValidationErrors } from '../../../shared/models/validation-error';
import { ValueOrEmptyPipe } from '../../../shared/value-or-empty-pipe';
import { itemImageHref, itemImageUrl, itemSelfHref, itemUrl } from '../item-api';
import { Item, ItemToUpdate } from '../models/item';

const FORM_FIELDS = ['price'];

interface EditNavigationState {
  href?: string;
}

@Component({
  selector: 'app-item-edit',
  imports: [
    FormField,
    ImageUpload,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    RouterLink,
    ValueOrEmptyPipe,
  ],
  templateUrl: './item-edit.html',
})
export class ItemEdit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly http = inject(HttpClient);

  protected readonly itemId = this.route.snapshot.paramMap.get('id')!;
  private readonly resourceUrl =
    (history.state as EditNavigationState | null)?.href ?? itemUrl(this.itemId);
  protected readonly resourceHref = this.resourceUrl;
  private readonly resource = httpResource<Item>(() => this.resourceUrl);

  readonly item = computed(() => this.resource.value());
  readonly isLoading = this.resource.isLoading;
  readonly hasError = computed(() => this.resource.error() !== undefined);
  readonly imageVersion = signal(0);
  readonly currentImageUrl = computed(() => {
    const href = itemImageHref(this.item()) ?? itemImageUrl(this.itemId);
    return withCacheBuster(href, this.imageVersion());
  });

  readonly itemPatch = signal<ItemToUpdate>({ price: 0 });
  readonly itemForm = form(this.itemPatch, (path) => {
    min(path.price, 0.01);
  });

  readonly initialized = signal(false);
  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly imageError = signal<string | null>(null);
  readonly fieldErrors = signal<Record<string, string>>({});
  readonly selectedImage = signal<File | null>(null);

  private readonly syncItemToForm = effect(() => {
    const item = this.item();
    if (!item || this.initialized()) {
      return;
    }

    this.itemPatch.set({ price: item.price ?? 0 });
    this.initialized.set(true);
  });

  onFileSelected(file: File | null): void {
    this.selectedImage.set(file);
    this.imageError.set(null);
  }

  submit(): void {
    void submit(this.itemForm, async () => {
      this.submitting.set(true);
      this.errorMessage.set(null);
      this.imageError.set(null);
      this.fieldErrors.set({});

      try {
        const updated = await firstValueFrom(
          this.http.put<Item>(this.resourceUrl, { price: this.itemPatch().price }),
        );
        const uploaded = await this.uploadSelectedImage(updated);
        this.submitting.set(false);
        void this.router.navigate(['/items', updated.id], {
          state: {
            href: itemSelfHref(updated) ?? this.resourceUrl,
            ...(uploaded ? { imageVersion: Date.now() } : {}),
          },
        });
      } catch (error) {
        this.submitting.set(false);

        const parsed = parseValidationErrors(error, FORM_FIELDS);
        if (parsed) {
          this.fieldErrors.set(parsed.fieldErrors);
          this.errorMessage.set(
            parsed.hasObjectLevelError ? 'The item could not be updated.' : null,
          );
          return;
        }

        if (this.selectedImage()) {
          this.imageError.set('The image could not be uploaded.');
          return;
        }

        this.errorMessage.set('Update failed. Please try again.');
      }
    });
  }

  private async uploadSelectedImage(item: Item): Promise<boolean> {
    const file = this.selectedImage();
    if (!file) {
      return false;
    }

    await firstValueFrom(
      uploadImageFile<Item>(this.http, itemImageHref(item) ?? itemImageUrl(item.id), file),
    );
    this.imageVersion.update((version) => version + 1);
    return true;
  }
}
