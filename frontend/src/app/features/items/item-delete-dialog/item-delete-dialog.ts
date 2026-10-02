import { Component, inject } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';

export interface ItemDeleteDialogData {
  itemName: string;
}

@Component({
  imports: [MatButtonModule, MatDialogModule],
  selector: 'app-item-delete-dialog',
  templateUrl: './item-delete-dialog.html',
})
export class ItemDeleteDialog {
  readonly data = inject<ItemDeleteDialogData>(MAT_DIALOG_DATA);
  readonly dialogRef = inject<MatDialogRef<ItemDeleteDialog, boolean>>(MatDialogRef);
}
