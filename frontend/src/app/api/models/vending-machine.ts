import { HalResource } from './hal';
import {
  CardSystemStatus,
  ChangeSystemStatus,
  ItemType,
  PowerStatus,
  WorkingStatus,
} from './enums';

export interface Address {
  latitude: number | null;
  longitude: number | null;
  streetNumber: number | null;
  streetName: string | null;
  postalCode: string | null;
  city: string | null;
}

export interface VendingMachine extends HalResource {
  id: string;
  serialNumber: string | null;
  address: Address | null;
  lastIntervention: string | null;
  temperature: number | null;
  itemType: ItemType | null;
  powerStatus: PowerStatus | null;
  workingStatus: WorkingStatus | null;
  rfidStatus: CardSystemStatus | null;
  smartCardStatus: CardSystemStatus | null;
  changeMoneyStatus: ChangeSystemStatus | null;
}
