package me.dahiorus.project.vending.domain.machine.entity;

import static me.dahiorus.project.vending.fixture.AddressFixture.anAddress;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import org.junit.jupiter.api.Test;

class AddressTest {

  /**
   * Address is cached off-heap by Ehcache (see infrastructure/ehcache.xml, "vendingMachines"
   * cache), which requires standard Java serialization of every nested value object, including
   * GeoCoordinates.
   */
  @Test
  void should_be_java_serializable() throws Exception {
    var address = anAddress().build();

    var buffer = new ByteArrayOutputStream();
    try (var out = new ObjectOutputStream(buffer)) {
      out.writeObject(address);
    }

    try (var in = new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray()))) {
      assertThat(in.readObject()).isEqualTo(address);
    }
  }
}
