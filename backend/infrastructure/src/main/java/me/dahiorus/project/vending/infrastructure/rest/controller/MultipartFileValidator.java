package me.dahiorus.project.vending.infrastructure.rest.controller;

import static org.apache.commons.lang3.StringUtils.equalsAny;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;

import org.springframework.web.multipart.MultipartFile;

public record MultipartFileValidator(MultipartFile multipartFile) {
  public static MultipartFileValidator validator(final MultipartFile multipartFile) {
    return new MultipartFileValidator(multipartFile);
  }

  public void validate() {
    String contentType = multipartFile.getContentType();

    if (!equalsAny(contentType, IMAGE_JPEG_VALUE, IMAGE_PNG_VALUE)) {
      throw new IllegalArgumentException(
          "Unsupported image content type for item image: " + contentType);
    }
  }
}
