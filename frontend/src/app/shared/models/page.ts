import { HalPage } from './hal';

export class Page<T> {
  constructor(
    readonly elements: T[],
    readonly totalElements: number,
    readonly totalPages: number,
    readonly pageIndex: number,
    readonly pageSize: number,
  ) {}

  static empty<T>(): Page<T> {
    return new Page<T>([], 0, 0, 0, 0);
  }

  static fromHalPage<T>(hal: HalPage<T>): Page<T> {
    return new Page(
      hal._embedded?.elements ?? [],
      hal.page.totalElements,
      hal.page.totalPages,
      hal.page.number,
      hal.page.size,
    );
  }

  get isEmpty(): boolean {
    return this.elements.length === 0;
  }
}
