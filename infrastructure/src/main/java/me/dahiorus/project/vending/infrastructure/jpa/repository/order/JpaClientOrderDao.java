package me.dahiorus.project.vending.infrastructure.jpa.repository.order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaClientOrder;
import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaVendingMachine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface JpaClientOrderDao extends JpaRepository<JpaClientOrder, UUID> {

  List<JpaClientOrder> findAllByVendingMachineOrderByCreatedAtDesc(
      JpaVendingMachine vendingMachine);

  @Query(
      """
          FROM JpaClientOrder clientOrder
          WHERE clientOrder.vendingMachine = ?1 AND clientOrder.createdAt >= ?2
          ORDER BY clientOrder.createdAt DESC
          """)
  List<JpaClientOrder> findAllClientOrdersSince(
      JpaVendingMachine vendingMachine, LocalDateTime since);
}
