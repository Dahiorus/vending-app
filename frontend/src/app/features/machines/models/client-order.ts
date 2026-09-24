import { HalResource } from '../../../shared/models/hal';

export interface ClientOrder extends HalResource {
  amount: number;
  createdAt: Date;
}
