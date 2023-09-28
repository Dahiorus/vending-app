package me.dahiorus.project.vending.domain.dao;

import static me.dahiorus.project.vending.domain.model.Report.reportAt;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import me.dahiorus.project.vending.domain.config.DaoConfig;
import me.dahiorus.project.vending.domain.model.ChangeSystemStatus;
import me.dahiorus.project.vending.domain.model.ItemType;
import me.dahiorus.project.vending.domain.model.PowerStatus;
import me.dahiorus.project.vending.domain.model.Report;
import me.dahiorus.project.vending.domain.model.VendingMachine;
import me.dahiorus.project.vending.domain.model.VendingMachineBuilder;
import me.dahiorus.project.vending.domain.model.WorkingStatus;

@Import(DaoConfig.class)
@DataJpaTest
@ActiveProfiles("jpa-test")
class ReportDaoTest
{
  @Autowired
  ReportDao dao;

  Report entity;

  @BeforeEach
  void initTestData()
  {
    entity = dao.save(reportAt(VendingMachineBuilder.builder()
      .serialNumber("1234")
      .address("1 rue Bidon")
      .changeMoneyStatus(ChangeSystemStatus.FULL)
      .id(UUID.fromString("e91a495c-aa53-441d-ba0e-1c7d16fba610"))
      .itemType(ItemType.FOOD)
      .powerStatus(PowerStatus.ON)
      .workingStatus(WorkingStatus.ALERT)
      .lastIntervention(Instant.now()
        .minus(Duration.ofDays(1)))
      .temperature(4)
      .build(), null));
    assertThat(entity.getId()).isNotNull();
  }

  @Test
  void findLastGenerated()
  {
    VendingMachine machine = VendingMachineBuilder.builder()
      .serialNumber("1234")
      .build();

    Optional<Report> lastReport = dao.findLastGenerated(machine);

    assertThat(lastReport).contains(entity);
  }

  @Test
  @DisplayName("Empty result if no report is found")
  void noGeneratedReport()
  {
    VendingMachine machine = VendingMachineBuilder.builder()
      .serialNumber("147852")
      .build();

    Optional<Report> lastReport = dao.findLastGenerated(machine);

    assertThat(lastReport).isEmpty();
  }

  @Test
  @DisplayName("Always get the last generated report")
  void multipleReportOnMachineReturnLast()
  {
    dao.save(reportAt(VendingMachineBuilder.builder()
      .serialNumber("1234")
      .address("1 rue Bidon")
      .changeMoneyStatus(ChangeSystemStatus.NORMAL)
      .id(UUID.fromString("e91a495c-aa53-441d-ba0e-1c7d16fba610"))
      .itemType(ItemType.FOOD)
      .powerStatus(PowerStatus.ON)
      .workingStatus(WorkingStatus.ALERT)
      .lastIntervention(Instant.now()
        .minus(Duration.ofHours(1)))
      .temperature(4)
      .build(), entity.getCreatedAt()));

    VendingMachine machine = VendingMachineBuilder.builder()
      .serialNumber("1234")
      .build();

    Optional<Report> lastReport = dao.findLastGenerated(machine);

    assertThat(lastReport).get()
      .isNotEqualTo(entity);
  }
}
