package me.dahiorus.project.vending.infrastructure.rest.utils;

import static java.time.Duration.ofHours;
import static java.time.ZoneId.systemDefault;
import static org.springframework.http.CacheControl.maxAge;
import static org.springframework.http.ContentDisposition.inline;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.MediaType.parseMediaType;
import static org.springframework.http.ResponseEntity.ok;

import me.dahiorus.project.vending.domain.file.entity.UploadedFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

public record ToByteArrayResponseConverter(UploadedFile uploadedFile) {

  public static ResponseEntity<ByteArrayResource> toResponseEntity(
      final UploadedFile uploadedFile) {
    return new ToByteArrayResponseConverter(uploadedFile).convert();
  }

  public ResponseEntity<ByteArrayResource> convert() {
    return ok().header(
            CONTENT_DISPOSITION, inline().filename(uploadedFile.name()).build().toString())
        .contentType(parseMediaType(uploadedFile.type()))
        .contentLength(uploadedFile.size())
        .cacheControl(maxAge(ofHours(1)).cachePublic())
        .lastModified(uploadedFile.uploadedAt().atZone(systemDefault()))
        .body(new ByteArrayResource(uploadedFile.data()));
  }
}
