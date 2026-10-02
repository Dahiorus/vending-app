import { describe, expect, it } from 'vitest';
import { PricePipe } from './price';

describe('PricePipe', () => {
  const pipe = new PricePipe();
  const normalize = (text: string) => text.replace(/\s/g, ' ');

  it('formats a price as French euros with 2 decimals', () => {
    expect(normalize(pipe.transform(1.5))).toBe('1,50 €');
    expect(normalize(pipe.transform(2))).toBe('2,00 €');
    expect(normalize(pipe.transform(0.05))).toBe('0,05 €');
  });

  it('falls back to a dash when the price is missing', () => {
    expect(pipe.transform(null)).toBe('—');
    expect(pipe.transform(undefined)).toBe('—');
  });
});
