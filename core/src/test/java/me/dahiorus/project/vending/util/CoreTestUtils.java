package me.dahiorus.project.vending.util;

import static org.mockito.ArgumentMatchers.any;

import org.springframework.data.jpa.domain.Specification;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.dahiorus.project.vending.domain.model.AbstractEntity;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CoreTestUtils
{
  public static <T extends AbstractEntity> Specification<T> anySpec()
  {
    return any();
  }
}
