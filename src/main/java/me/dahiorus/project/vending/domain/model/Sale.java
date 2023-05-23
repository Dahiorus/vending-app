package me.dahiorus.project.vending.domain.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "sale", indexes = @Index(name = "IDX_SALE_VENDING_MACHINE", columnList = "vending_machine_id"))
public class Sale extends AbstractEntity
{
  private BigDecimal amount;

  private VendingMachine machine;

  public static Sale sell(final Item item, final VendingMachine machine)
  {
    Sale sale = new Sale();
    sale.setAmount(item.getPrice());
    sale.setMachine(machine);

    return sale;
  }

  @Column(nullable = false, scale = 2, precision = 4)
  public BigDecimal getAmount()
  {
    return amount;
  }

  public void setAmount(final BigDecimal amount)
  {
    this.amount = amount;
  }

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "vending_machine_id", nullable = false, foreignKey = @ForeignKey(name = "FK_SALE_VENDING_MACHINE"))
  public VendingMachine getMachine()
  {
    return machine;
  }

  public void setMachine(final VendingMachine machine)
  {
    this.machine = machine;
  }

  @Override
  public String toString()
  {
    return super.toString() + "[amount=" + amount + "]";
  }
}
