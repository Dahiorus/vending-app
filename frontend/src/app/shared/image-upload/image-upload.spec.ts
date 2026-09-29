import { ComponentFixture, TestBed } from '@angular/core/testing';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { ImageUpload, isAcceptedImage } from './image-upload';

describe('isAcceptedImage', () => {
  it('accepts JPEG and PNG files only', () => {
    expect(isAcceptedImage(new File([''], 'a.jpg', { type: 'image/jpeg' }))).toBe(true);
    expect(isAcceptedImage(new File([''], 'a.png', { type: 'image/png' }))).toBe(true);
    expect(isAcceptedImage(new File([''], 'a.gif', { type: 'image/gif' }))).toBe(false);
    expect(isAcceptedImage(new File([''], 'a.png', { type: '' }))).toBe(false);
  });
});

describe('ImageUpload', () => {
  let fixture: ComponentFixture<ImageUpload>;
  let emitted: Array<File | null>;
  let createObjectURL: ReturnType<typeof vi.fn>;
  let revokeObjectURL: ReturnType<typeof vi.fn>;

  beforeEach(async () => {
    // jsdom does not implement object URLs.
    let counter = 0;
    createObjectURL = vi.fn(() => `blob:preview-${++counter}`);
    revokeObjectURL = vi.fn();
    URL.createObjectURL = createObjectURL as unknown as typeof URL.createObjectURL;
    URL.revokeObjectURL = revokeObjectURL as unknown as typeof URL.revokeObjectURL;

    await TestBed.configureTestingModule({ imports: [ImageUpload] }).compileComponents();

    fixture = TestBed.createComponent(ImageUpload);
    emitted = [];
    fixture.componentInstance.fileSelected.subscribe((file) => emitted.push(file));
    fixture.detectChanges();
    await fixture.whenStable();
  });

  afterEach(() => vi.restoreAllMocks());

  const query = <T extends Element>(selector: string): T | null =>
    (fixture.nativeElement as HTMLElement).querySelector<T>(selector);

  function selectFile(file: File | null): void {
    const input = query<HTMLInputElement>('[data-testid="image-upload-input"]')!;
    Object.defineProperty(input, 'files', { value: file ? [file] : [], configurable: true });
    input.dispatchEvent(new Event('change'));
    fixture.detectChanges();
  }

  it('shows the fallback icon when there is no current image', () => {
    expect(query('[data-testid="image-upload-fallback"]')).not.toBeNull();
    expect(query('[data-testid="image-upload-preview"]')).toBeNull();
  });

  it('shows the current image and uses the label as alt text', () => {
    fixture.componentRef.setInput('currentImageUrl', '/api/v1/items/i-1/image');
    fixture.componentRef.setInput('label', 'Item image');
    fixture.detectChanges();

    const img = query<HTMLImageElement>('[data-testid="image-upload-preview"]')!;
    expect(img.getAttribute('src')).toBe('/api/v1/items/i-1/image');
    expect(img.getAttribute('alt')).toBe('Item image');
  });

  it('restricts the file picker to JPEG and PNG', () => {
    const input = query<HTMLInputElement>('[data-testid="image-upload-input"]')!;
    expect(input.getAttribute('accept')).toBe('image/jpeg,image/png');
  });

  it('falls back to the icon when the current image fails to load (404)', () => {
    fixture.componentRef.setInput('currentImageUrl', '/api/v1/me/picture');
    fixture.componentRef.setInput('fallbackIcon', 'account_circle');
    fixture.detectChanges();

    query('[data-testid="image-upload-preview"]')!.dispatchEvent(new Event('error'));
    fixture.detectChanges();

    const icon = query('[data-testid="image-upload-fallback"]')!;
    expect(icon.textContent?.trim()).toBe('account_circle');
    expect(query('[data-testid="image-upload-preview"]')).toBeNull();
  });

  it('retries displaying the image when the current image URL changes after a failure', () => {
    fixture.componentRef.setInput('currentImageUrl', '/api/v1/me/picture');
    fixture.detectChanges();
    query('[data-testid="image-upload-preview"]')!.dispatchEvent(new Event('error'));
    fixture.detectChanges();

    fixture.componentRef.setInput('currentImageUrl', '/api/v1/me/picture?v=1');
    fixture.detectChanges();

    const img = query<HTMLImageElement>('[data-testid="image-upload-preview"]')!;
    expect(img.getAttribute('src')).toBe('/api/v1/me/picture?v=1');
  });

  it('emits an accepted file and previews it instead of the current image', () => {
    fixture.componentRef.setInput('currentImageUrl', '/api/v1/items/i-1/image');
    fixture.detectChanges();
    const file = new File(['png'], 'cola.png', { type: 'image/png' });

    selectFile(file);

    expect(emitted).toEqual([file]);
    expect(createObjectURL).toHaveBeenCalledWith(file);
    const img = query<HTMLImageElement>('[data-testid="image-upload-preview"]')!;
    expect(img.getAttribute('src')).toBe('blob:preview-1');
    expect(query('[role="alert"]')).toBeNull();
  });

  it('rejects a non JPEG/PNG file, emits null and drops the previous preview', () => {
    selectFile(new File(['png'], 'cola.png', { type: 'image/png' }));

    selectFile(new File(['gif'], 'cola.gif', { type: 'image/gif' }));

    expect(emitted.at(-1)).toBeNull();
    expect(revokeObjectURL).toHaveBeenCalledWith('blob:preview-1');
    expect(query('[role="alert"]')?.textContent).toContain(
      'Only JPEG and PNG images are accepted.',
    );
    expect(query('[data-testid="image-upload-preview"]')).toBeNull();
    expect(query<HTMLInputElement>('[data-testid="image-upload-input"]')!.value).toBe('');
  });

  it('emits null when the selection is cleared', () => {
    selectFile(new File(['png'], 'cola.png', { type: 'image/png' }));

    selectFile(null);

    expect(emitted).toHaveLength(2);
    expect(emitted[1]).toBeNull();
  });

  it('revokes the previous preview when another file is selected', () => {
    selectFile(new File(['1'], 'one.png', { type: 'image/png' }));

    selectFile(new File(['2'], 'two.jpg', { type: 'image/jpeg' }));

    expect(revokeObjectURL).toHaveBeenCalledWith('blob:preview-1');
    const img = query<HTMLImageElement>('[data-testid="image-upload-preview"]')!;
    expect(img.getAttribute('src')).toBe('blob:preview-2');
  });

  it('revokes the active preview when destroyed', () => {
    selectFile(new File(['png'], 'cola.png', { type: 'image/png' }));

    fixture.destroy();

    expect(revokeObjectURL).toHaveBeenCalledWith('blob:preview-1');
  });

  it('displays an error provided by the parent (e.g. upload failure)', () => {
    fixture.componentRef.setInput('error', 'The image could not be uploaded.');
    fixture.detectChanges();

    expect(query('[role="alert"]')?.textContent).toContain('The image could not be uploaded.');
  });
});
