import { describe, expect, it } from 'vitest';
import { HalResource, linkHref } from './hal';

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

describe('linkHref', () => {
  it('returns the href of a single link', () => {
    const resource: HalResource = { _links: { self: { href: '/api/v1/me' } } };

    expect(linkHref(resource, 'self')).toBe('/api/v1/me');
  });

  it('returns the first href when the relation holds an array of links', () => {
    const resource: HalResource = {
      _links: { item: [{ href: '/api/v1/items/i-1' }, { href: '/api/v1/items/i-2' }] },
    };

    expect(linkHref(resource, 'item')).toBe('/api/v1/items/i-1');
  });

  it('returns undefined for an empty array of links', () => {
    const resource: HalResource = { _links: { order: [] } };

    expect(linkHref(resource, 'order')).toBeUndefined();
  });

  it('returns undefined when the relation is missing', () => {
    expect(linkHref({ _links: {} }, 'me:picture')).toBeUndefined();
  });

  it('returns undefined when the resource has no _links or is not loaded yet', () => {
    expect(linkHref({}, 'self')).toBeUndefined();
    expect(linkHref(undefined, 'self')).toBeUndefined();
    expect(linkHref(null, 'self')).toBeUndefined();
  });
});
