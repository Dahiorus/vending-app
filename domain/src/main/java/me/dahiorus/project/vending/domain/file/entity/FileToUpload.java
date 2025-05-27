package me.dahiorus.project.vending.domain.file.entity;

public record FileToUpload(Filename filename, BinaryContent content, ContentType contentType) {
  public FileToUpload {
    if (filename == null || content == null || contentType == null) {
      throw new IllegalArgumentException("Filename, content, and content type cannot be null");
    }
  }

  public String name() {
    return filename.value();
  }

  public byte[] data() {
    return content.value();
  }

  public String type() {
    return contentType.value();
  }
}
