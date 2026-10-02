import { HttpClient, httpResource } from '@angular/common/http';
import { Component, computed, effect, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { form, FormField, min, required, submit } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { withCacheBuster } from '../../../shared/http/cache-buster';
import { uploadImageFile } from '../../../shared/http/upload-image-file';
import { ImageUpload } from '../../../shared/image-upload/image-upload';
import { PriceInput } from '../../../shared/price-input/price-input';
import { parseValidationErrors } from '../../../shared/models/validation-error';
import { ValueOrEmptyPipe } from '../../../shared/value-or-empty-pipe';
import { itemImageHref, itemImageUrl, itemSelfHref, itemUrl } from '../item-api';
import { Item, ItemToUpdate } from '../models/item';

const FORM_FIELDS = ['price'];

interface EditNavigationState {
  href?: string;
  imageVersion?: number;
}

@Component({
  selector: 'app-item-edit',
  imports: [
    FormField,
    ImageUpload,
    MatButtonModule,
    MatCardModule,
    MatProgressBarModule,
    MatProgressSpinnerModule,
    PriceInput,
    RouterLink,
    ValueOrEmptyPipe,
  ],
  templateUrl: './item-edit.html',
})
export class ItemEdit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly http = inject(HttpClient);

  private readonly paramMap = toSignal(this.route.paramMap, { requireSync: true });
  protected readonly itemId = computed(() => this.paramMap().get('id')!);
  private readonly navigationState = history.state as EditNavigationState | null;

  // The navigation state only describes the item the page was opened for: another id falls back to the built URL.
  private readonly resourceUrl = computed(() => {
    const href = this.navigationState?.href;
    return href?.endsWith(`/${this.itemId()}`) ? href : itemUrl(this.itemId());
  });
  protected readonly resourceHref = this.resourceUrl;
  private readonly resource = httpResource<Item>(() => this.resourceUrl());

  readonly item = computed(() => this.resource.value());
  readonly isLoading = this.resource.isLoading;
  readonly hasError = computed(() => this.resource.error() !== undefined);
  readonly imageVersion = signal(
    typeof this.navigationState?.imageVersion === 'number' ? this.navigationState.imageVersion : 0,
  );
  readonly currentImageUrl = computed(() => {
    const href = itemImageHref(this.item()) ?? itemImageUrl(this.itemId());
    return withCacheBuster(href, this.imageVersion());
  });

  readonly itemPatch = signal<ItemToUpdate>({ price: 0 });
  readonly itemForm = form(this.itemPatch, (path) => {
    required(path.price);
    min(path.price, 0.01);
  });

  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly imageError = signal<string | null>(null);
  readonly fieldErrors = signal<Record<string, string>>({});
  readonly selectedImage = signal<File | null>(null);

  private syncedItemId: string | undefined;
  private readonly syncItemToForm = effect(() => {
    const item = this.item();
    if (!item || item.id === this.syncedItemId) {
      return;
    }

    this.syncedItemId = item.id;
    this.itemPatch.set({ price: item.price ?? 0 });
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

      let priceSaved = false;
      try {
        const updated = await firstValueFrom(
          this.http.put<Item>(this.resourceUrl(), { price: this.itemPatch().price }),
        );
        priceSaved = true;
        const uploaded = await this.uploadSelectedImage(updated);
        this.submitting.set(false);
        void this.router.navigate(['/items', updated.id], {
          state: {
            href: itemSelfHref(updated) ?? this.resourceUrl(),
            ...(uploaded
              ? { imageVersion: Date.now() }
              : this.imageVersion() !== 0
                ? { imageVersion: this.imageVersion() }
                : {}),
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

        if (priceSaved && this.selectedImage()) {
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
