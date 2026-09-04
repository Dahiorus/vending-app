package me.dahiorus.project.vending.fixture;

import java.util.HashMap;
import me.dahiorus.project.vending.domain.stock.entity.VendingMachineStock;

public class VendingMachineStocksFixture {
  public static VendingMachineStock emptyStock() {
    return new VendingMachineStock(new HashMap<>());
  }
}
