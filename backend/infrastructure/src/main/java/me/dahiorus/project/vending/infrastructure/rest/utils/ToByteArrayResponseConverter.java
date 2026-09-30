package me.dahiorus.project.vending.infrastructure.rest.utils;

import static java.time.Duration.ofHours;
import static java.time.ZoneId.systemDefault;
import static org.springframework.http.CacheControl.maxAge;
import static org.springframework.http.CacheControl.noCache;
import static org.springframework.http.ContentDisposition.inline;
import static org.springframework.http.HttpHeaders.CONTENT_DISPOSITION;
import static org.springframework.http.MediaType.parseMediaType;
import static org.springframework.http.ResponseEntity.ok;

import me.dahiorus.project.vending.domain.file.entity.UploadedFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;

public record ToByteArrayResponseConverter(UploadedFile uploadedFile, CacheControl cacheControl) {

  public static ResponseEntity<ByteArrayResource> toResponseEntity(
      final UploadedFile uploadedFile) {
    return new ToByteArrayResponseConverter(uploadedFile, maxAge(ofHours(1)).cachePublic())
        .convert();
  }

  /** For user-specific content: never shared between users, revalidated on each request. */
  public static ResponseEntity<ByteArrayResource> toPrivateResponseEntity(
      final UploadedFile uploadedFile) {
    return new ToByteArrayResponseConverter(uploadedFile, noCache().cachePrivate()).convert();
  }

  public ResponseEntity<ByteArrayResource> convert() {
    return ok().header(
            CONTENT_DISPOSITION, inline().filename(uploadedFile.name()).build().toString())
        .contentType(parseMediaType(uploadedFile.type()))
        .contentLength(uploadedFile.size())
        .cacheControl(cacheControl)
        .lastModified(uploadedFile.uploadedAt().atZone(systemDefault()))
        .body(new ByteArrayResource(uploadedFile.data()));
  }
}
