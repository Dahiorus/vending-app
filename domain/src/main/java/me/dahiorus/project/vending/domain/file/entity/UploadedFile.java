package me.dahiorus.project.vending.domain.file.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

public record UploadedFile(
    UploadedFileId id,
    Filename filename,
    BinaryContent content,
    ContentType contentType,
    LocalDateTime uploadedAt)
    implements Serializable {
  public String name() {
    return filename.value();
  }

  public byte[] data() {
    return content.value();
  }

  public int size() {
    return content.value().length;
  }

  public String type() {
    return contentType.value();
  }
}
