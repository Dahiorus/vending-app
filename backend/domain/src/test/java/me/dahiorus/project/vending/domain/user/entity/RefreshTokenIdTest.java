package me.dahiorus.project.vending.domain.user.entity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class RefreshTokenIdTest {

  @Test
  void should_wrap_uuid_value() {
    var uuid = UUID.randomUUID();

    var result = new RefreshTokenId(uuid);

    assertThat(result.value()).isEqualTo(uuid);
  }
}
