import { HttpClient } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { form, FormField, min, required, submit } from '@angular/forms/signals';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { Router, RouterLink } from '@angular/router';
import { firstValueFrom } from 'rxjs';
import { ApiRootApi } from '../../../core/http/api-root-api';
import { uploadImageFile } from '../../../shared/http/upload-image-file';
import { ImageUpload } from '../../../shared/image-upload/image-upload';
import { ITEM_TYPES, ItemType } from '../../../shared/models/item-type';
import { parseValidationErrors } from '../../../shared/models/validation-error';
import { itemImageHref, itemImageUrl } from '../item-api';
import { Item, ItemToCreate } from '../models/item';

const FORM_FIELDS = ['name', 'type', 'price'];

interface CreateNavigationState {
  createHref?: string;
}

interface ItemCreateForm {
  name: string;
  type: ItemType | '';
  price: number;
}

@Component({
  selector: 'app-item-create',
  imports: [
    FormField,
    ImageUpload,
    MatButtonModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    RouterLink,
  ],
  templateUrl: './item-create.html',
})
export class ItemCreate {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly apiRoot = inject(ApiRootApi);

  protected readonly itemTypes = ITEM_TYPES;

  readonly item = signal<ItemCreateForm>({
    name: '',
    type: '',
    price: 0,
  });

  readonly itemForm = form(this.item, (path) => {
    required(path.name);
    required(path.type);
    min(path.price, 0.01);
  });

  readonly submitting = signal(false);
  readonly errorMessage = signal<string | null>(null);
  readonly imageError = signal<string | null>(null);
  readonly fieldErrors = signal<Record<string, string>>({});
  readonly selectedImage = signal<File | null>(null);
  readonly createdItem = signal<Item | null>(null);

  private get createHref(): string | undefined {
    return (
      (history.state as CreateNavigationState | null)?.createHref ?? this.apiRoot.link('items')
    );
  }

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
        const existingItem = this.createdItem();
        if (existingItem) {
          await this.uploadSelectedImage(existingItem);
          this.submitting.set(false);
          void this.router.navigate(['/items']);
          return;
        }

        const href = this.createHref;
        if (!href) {
          this.errorMessage.set('Creation is not available yet. Please try again.');
          this.submitting.set(false);
          return;
        }

        const created = await firstValueFrom(this.http.post<Item>(href, this.payload()));
        this.createdItem.set(created);
        await this.uploadSelectedImage(created);
        this.submitting.set(false);
        void this.router.navigate(['/items']);
      } catch (error) {
        this.submitting.set(false);

        const parsed = parseValidationErrors(error, FORM_FIELDS);
        if (parsed) {
          this.fieldErrors.set(parsed.fieldErrors);
          this.errorMessage.set(
            parsed.hasObjectLevelError ? 'The item could not be created.' : null,
          );
          return;
        }

        if (this.createdItem()) {
          this.imageError.set('The image could not be uploaded.');
          return;
        }

        this.errorMessage.set('Creation failed. Please try again.');
      }
    });
  }

  private payload(): ItemToCreate {
    const value = this.item();
    return {
      name: value.name,
      type: value.type as ItemType,
      price: value.price,
    };
  }

  private async uploadSelectedImage(item: Item): Promise<void> {
    const file = this.selectedImage();
    if (!file) {
      return;
    }

    const href = itemImageHref(item) ?? itemImageUrl(item.id);
    await firstValueFrom(uploadImageFile<Item>(this.http, href, file));
  }
}
