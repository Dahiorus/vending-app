import { HalResource } from '../../../shared/models/hal';

export interface User extends HalResource {
  id: string;
  email: string;
  firstname: string;
  lastname: string;
}
