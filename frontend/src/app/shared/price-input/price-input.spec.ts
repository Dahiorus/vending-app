import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { disabled, form, FormField, min } from '@angular/forms/signals';
import { beforeEach, describe, expect, it } from 'vitest';
import { PriceInput } from './price-input';

@Component({
  imports: [FormField, PriceInput],
  template: `<app-price-input label="Price" [formField]="priceForm.price" />`,
})
class Host {
  readonly model = signal<{ price: number | null }>({ price: null });
  readonly locked = signal(false);
  readonly priceForm = form(this.model, (path) => {
    min(path.price, 0.01);
    disabled(path.price, () => this.locked());
  });
}

describe('PriceInput', () => {
  let fixture: ComponentFixture<Host>;
  let host: Host;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [Host] }).compileComponents();
    fixture = TestBed.createComponent(Host);
    host = fixture.componentInstance;
    fixture.detectChanges();
    await fixture.whenStable();
  });

  const input = () => fixture.nativeElement.querySelector('input') as HTMLInputElement;

  async function type(text: string): Promise<void> {
    input().value = text;
    input().dispatchEvent(new Event('input'));
    fixture.detectChanges();
    await fixture.whenStable();
  }

  async function blur(): Promise<void> {
    input().dispatchEvent(new Event('blur'));
    fixture.detectChanges();
    await fixture.whenStable();
  }

  async function setModel(price: number | null): Promise<void> {
    host.model.set({ price });
    fixture.detectChanges();
    await fixture.whenStable();
  }

  it('renders a decimal text input with the label and a euro suffix', () => {
    expect(input().type).toBe('text');
    expect(input().getAttribute('inputmode')).toBe('decimal');
    expect(
      fixture.nativeElement.querySelector('[matTextSuffix], .mat-mdc-form-field-text-suffix')
        ?.textContent,
    ).toContain('€');
    expect(fixture.nativeElement.textContent).toContain('Price');
  });

  it.each([
    ['1,5', 1.5],
    ['1.5', 1.5],
    ['12', 12],
    ['0,05', 0.05],
  ])('accepts %s as %d', async (text, expected) => {
    await type(text);
    expect(host.priceForm.price().value()).toBe(expected);
  });

  it('yields null for empty text', async () => {
    await type('3');
    await type('');
    expect(host.priceForm.price().value()).toBeNull();
  });

  it('ignores non numeric characters', async () => {
    await type('abc');
    expect(input().value).toBe('');
    expect(host.priceForm.price().value()).toBeNull();
    await type('1a2,5b');
    expect(input().value).toBe('12,5');
    expect(host.priceForm.price().value()).toBe(12.5);
  });

  it('keeps a single decimal separator and at most 2 decimals', async () => {
    await type('1,234');
    expect(input().value).toBe('1,23');
    expect(host.priceForm.price().value()).toBe(1.23);
    await type('1.2.3');
    expect(input().value).toBe('1,23');
  });

  it('does not reformat while typing but reformats on blur', async () => {
    await type('1,5');
    expect(input().value).toBe('1,5');
    await blur();
    expect(input().value).toBe('1,50');
    expect(host.priceForm.price().value()).toBe(1.5);
  });

  it('keeps empty text empty on blur', async () => {
    await blur();
    expect(input().value).toBe('');
  });

  it('displays the form model value with two decimals', async () => {
    await setModel(1.5);
    expect(input().value).toBe('1,50');
    await setModel(12);
    expect(input().value).toBe('12,00');
    await setModel(null);
    expect(input().value).toBe('');
  });

  it('marks the field touched on blur', async () => {
    expect(host.priceForm.price().touched()).toBe(false);
    await blur();
    expect(host.priceForm.price().touched()).toBe(true);
  });

  it('applies form validators to the parsed value', async () => {
    await type('0');
    expect(host.priceForm.price().errors().length).toBeGreaterThan(0);
    await type('1,5');
    expect(host.priceForm.price().errors().length).toBe(0);
  });

  it('disables the input when the field is disabled', async () => {
    expect(input().disabled).toBe(false);
    host.locked.set(true);
    fixture.detectChanges();
    await fixture.whenStable();
    expect(input().disabled).toBe(true);
  });
});
