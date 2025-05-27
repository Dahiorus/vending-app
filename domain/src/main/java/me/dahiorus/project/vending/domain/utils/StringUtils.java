package me.dahiorus.project.vending.domain.utils;

public class StringUtils {
  public static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }

  private StringUtils() {}
}
