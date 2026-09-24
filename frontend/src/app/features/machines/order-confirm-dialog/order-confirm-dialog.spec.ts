import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { OrderConfirmDialog } from './order-confirm-dialog';

describe('OrderConfirmDialog', () => {
  let fixture: ComponentFixture<OrderConfirmDialog>;
  let dialogRef: { close: ReturnType<typeof vi.fn> };

  beforeEach(async () => {
    dialogRef = { close: vi.fn() };

    await TestBed.configureTestingModule({
      imports: [OrderConfirmDialog],
      providers: [
        {
          provide: MAT_DIALOG_DATA,
          useValue: {
            itemName: 'Cola',
            quantity: 3,
          },
        },
        {
          provide: MatDialogRef,
          useValue: dialogRef,
        },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(OrderConfirmDialog);
    fixture.detectChanges();
    await fixture.whenStable();
  });

  it('renders the injected item name and remaining quantity', () => {
    const content = fixture.nativeElement.textContent;

    expect(content).toContain('Order 1x Cola?');
    expect(content).toContain('Remaining stock: 3.');
  });

  it('closes the dialog when clicking cancel', () => {
    const cancelButton: HTMLButtonElement = fixture.nativeElement.querySelector(
      '[data-testid="order-confirm-dialog-cancel"]',
    );

    cancelButton.click();

    expect(dialogRef.close).toHaveBeenCalledWith(false);
  });

  it('confirms the order when clicking order', () => {
    const orderButton: HTMLButtonElement = fixture.nativeElement.querySelector(
      '[data-testid="order-confirm-dialog-confirm"]',
    );

    orderButton.click();

    expect(dialogRef.close).toHaveBeenCalledWith(true);
  });
});
