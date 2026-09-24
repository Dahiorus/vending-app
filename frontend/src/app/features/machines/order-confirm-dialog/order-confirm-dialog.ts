import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';

export interface OrderConfirmDialogData {
  itemName: string;
  quantity: number;
}

@Component({
  imports: [MatButtonModule, MatDialogModule],
  selector: 'app-order-confirm-dialog',
  templateUrl: './order-confirm-dialog.html',
})
export class OrderConfirmDialog {
  readonly data = inject<OrderConfirmDialogData>(MAT_DIALOG_DATA);
  readonly dialogRef = inject(MatDialogRef<OrderConfirmDialog, boolean>);
}
