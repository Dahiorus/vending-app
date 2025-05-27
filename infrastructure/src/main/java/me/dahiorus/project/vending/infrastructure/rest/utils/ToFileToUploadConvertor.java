package me.dahiorus.project.vending.infrastructure.rest.utils;

import static java.lang.System.currentTimeMillis;
import static org.apache.commons.io.FilenameUtils.getExtension;

import java.io.IOException;
import java.io.UncheckedIOException;
import me.dahiorus.project.vending.domain.file.entity.BinaryContent;
import me.dahiorus.project.vending.domain.file.entity.ContentType;
import me.dahiorus.project.vending.domain.file.entity.FileToUpload;
import me.dahiorus.project.vending.domain.file.entity.Filename;
import org.springframework.web.multipart.MultipartFile;

public record ToFileToUploadConvertor(MultipartFile multipartFile) {

  public static FileToUpload toFileToUpload(final MultipartFile multipartFile) {
    return new ToFileToUploadConvertor(multipartFile).convert();
  }

  public FileToUpload convert() {
    try {
      return new FileToUpload(
          toFilename(),
          new BinaryContent(multipartFile.getBytes()),
          ContentType.of(multipartFile.getContentType()).orElseThrow());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private Filename toFilename() {
    return new Filename(
        currentTimeMillis() + "." + getExtension(multipartFile.getOriginalFilename()));
  }
}
