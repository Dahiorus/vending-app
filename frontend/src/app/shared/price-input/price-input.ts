import { Component, effect, input, model, output, signal, untracked } from '@angular/core';
import { FormValueControl, ValidationError } from '@angular/forms/signals';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';

const MAX_DECIMALS = 2;

/** Keeps digits and a single decimal separator (`.` is converted to `,`), max 2 decimals. */
export function sanitizePriceText(raw: string): string {
  const cleaned = raw.replace(/\./g, ',').replace(/[^0-9,]/g, '');
  const separator = cleaned.indexOf(',');
  if (separator === -1) {
    return cleaned;
  }
  const integer = cleaned.slice(0, separator);
  const decimals = cleaned.slice(separator + 1).replace(/,/g, '');
  return `${integer},${decimals.slice(0, MAX_DECIMALS)}`;
}

export function parsePriceText(text: string): number | null {
  const parsed = Number(text.replace(',', '.'));
  return /\d/.test(text) && Number.isFinite(parsed) ? parsed : null;
}

export function formatPriceText(value: number | null): string {
  return value === null || value === undefined ? '' : value.toFixed(MAX_DECIMALS).replace('.', ',');
}

/** Signal Forms custom control editing a price (`number | null`) as French-formatted text. */
@Component({
  selector: 'app-price-input',
  imports: [MatFormFieldModule, MatInputModule],
  templateUrl: './price-input.html',
  host: { class: 'block' },
})
export class PriceInput implements FormValueControl<number | null> {
  readonly value = model<number | null>(null);
  readonly label = input('Price');
  readonly currencySymbol = input('€');

  readonly disabled = input(false);
  readonly readonly = input(false);
  readonly required = input(false);
  readonly name = input('');
  readonly touched = input(false);
  readonly errors = input<readonly ValidationError.WithOptionalFieldTree[]>([]);
  readonly touch = output<void>();

  protected readonly text = signal('');

  constructor() {
    // Reflects model changes made from outside (form reset, loaded item) without
    // reformatting what the user is currently typing.
    effect(() => {
      const value = this.value();
      untracked(() => {
        if (parsePriceText(this.text()) !== value) {
          this.text.set(formatPriceText(value));
        }
      });
    });
  }

  protected onInput(event: Event): void {
    const element = event.target as HTMLInputElement;
    const text = sanitizePriceText(element.value);
    element.value = text;
    this.text.set(text);
    this.value.set(parsePriceText(text));
  }

  protected onBlur(): void {
    this.text.set(formatPriceText(this.value()));
    this.touch.emit();
  }
}
