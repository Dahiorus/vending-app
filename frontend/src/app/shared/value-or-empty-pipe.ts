import { Pipe, PipeTransform } from '@angular/core';

/** Placeholder displayed when a value is missing. */
const EMPTY_PLACEHOLDER = '--';

@Pipe({
  name: 'valueOrEmpty',
})
export class ValueOrEmptyPipe implements PipeTransform {
  transform<T>(value: T): T | '--' {
    return value === null || value === undefined || value === '' ? EMPTY_PLACEHOLDER : value;
  }
}
