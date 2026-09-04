export type ItemType = 'HOT_BEVERAGE' | 'COLD_BEVERAGE' | 'SNACK';
export type PowerStatus = 'POWER_ON' | 'POWER_OFF';
export type WorkingStatus = 'WORKING' | 'WARNING' | 'ERROR' | 'ALERT';
export type CardSystemStatus = 'FAILED' | 'OK';
export type ChangeSystemStatus = 'FULL' | 'EMPTY' | 'NORMAL';

export const ITEM_TYPES: readonly ItemType[] = ['HOT_BEVERAGE', 'COLD_BEVERAGE', 'SNACK'];
