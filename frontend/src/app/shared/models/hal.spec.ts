import { describe, expect, it } from 'vitest';
import { HalResource } from './hal';

interface Sample {
  id: string;
}

describe('HalResource', () => {
  it('types the HAL-FORMS _templates returned alongside _links', () => {
    const resource: HalResource = {
      _links: { self: { href: '/vending-machines/m-1' } },
      _templates: {
        default: { method: 'POST', target: '/vending-machines/m-1/reset', properties: [] },
        update: {
          method: 'PUT',
          contentType: 'application/json',
          properties: [{ name: 'temperature', required: true, type: 'number' }],
        },
      },
    };

    expect(resource._templates?.['default'].method).toBe('POST');
    expect(resource._templates?.['update'].properties?.[0].name).toBe('temperature');
  });
});
