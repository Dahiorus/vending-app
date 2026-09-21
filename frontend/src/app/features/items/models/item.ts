import { HalResource } from '../../../shared/models/hal';
import { ItemType } from '../../../shared/models/item-type';

export interface Item extends HalResource {
  id: string;
  name: string | null;
  type: ItemType | null;
  price: number | null;
}
