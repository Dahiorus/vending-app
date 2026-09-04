import { HalResource } from './hal';

export interface User extends HalResource {
  id: string;
  email: string;
  firstname: string;
  lastname: string;
}
