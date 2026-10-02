import { CurrencyPipe, registerLocaleData } from '@angular/common';
import localeFr from '@angular/common/locales/fr';
import { Pipe, PipeTransform } from '@angular/core';

/**
 * The backend stores a plain number without currency: prices are displayed in euros,
 * formatted the French way (`1,50 €`).
 */
export const PRICE_CURRENCY = 'EUR';
export const PRICE_LOCALE = 'fr';
export const PRICE_DIGITS_INFO = '1.2-2';
/** Placeholder displayed when an item has no price. */
export const PRICE_EMPTY_PLACEHOLDER = '—';

/** Registers the French locale data needed to format prices (idempotent). */
export function registerPriceLocale(): void {
  registerLocaleData(localeFr, PRICE_LOCALE);
}

registerPriceLocale();

/** Single place where prices are displayed, shared by every page showing an item price. */
@Pipe({
  name: 'price',
})
export class PricePipe implements PipeTransform {
  private readonly currency = new CurrencyPipe(PRICE_LOCALE);

  transform(value: number | null | undefined): string {
    return (
      this.currency.transform(value, PRICE_CURRENCY, 'symbol', PRICE_DIGITS_INFO, PRICE_LOCALE) ??
      PRICE_EMPTY_PLACEHOLDER
    );
  }
}
