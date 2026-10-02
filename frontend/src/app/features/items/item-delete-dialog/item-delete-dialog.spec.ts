import { ComponentFixture, TestBed } from '@angular/core/testing';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { ItemDeleteDialog } from './item-delete-dialog';

describe('ItemDeleteDialog', () => {
  let fixture: ComponentFixture<ItemDeleteDialog>;
  let dialogRef: { close: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    dialogRef = { close: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [ItemDeleteDialog],
      providers: [
        { provide: MAT_DIALOG_DATA, useValue: { itemName: 'Cola' } },
        { provide: MatDialogRef, useValue: dialogRef },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(ItemDeleteDialog);
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('renders the injected item name', () => {
    expect(fixture.nativeElement.textContent).toContain('Delete Cola?');
  });

  it('closes the dialog when clicking cancel', () => {
    const cancelButton: HTMLButtonElement = fixture.nativeElement.querySelector(
      '[data-testid="item-delete-dialog-cancel"]',
    );

    cancelButton.click();

    expect(dialogRef.close).toHaveBeenCalledWith(false);
  });

  it('confirms the deletion when clicking delete', () => {
    const confirmButton: HTMLButtonElement = fixture.nativeElement.querySelector(
      '[data-testid="item-delete-dialog-confirm"]',
    );

    confirmButton.click();

    expect(dialogRef.close).toHaveBeenCalledWith(true);
  });
});
