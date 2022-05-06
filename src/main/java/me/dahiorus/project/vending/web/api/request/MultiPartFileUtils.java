package me.dahiorus.project.vending.web.api.request;

import static me.dahiorus.project.vending.core.service.validation.FieldValidationError.fieldError;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

import java.io.IOException;
import java.time.Duration;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.dahiorus.project.vending.core.model.dto.BinaryDataDTO;
import me.dahiorus.project.vending.core.service.validation.ValidationError;
import me.dahiorus.project.vending.core.service.validation.ValidationResults;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MultiPartFileUtils
{
  public static BinaryDataDTO convert(final MultipartFile file) throws IOException
  {
    BinaryDataDTO dto = new BinaryDataDTO();
    dto.setName(System.currentTimeMillis() + "." + getExtension(file.getOriginalFilename()));
    dto.setContentType(file.getContentType());
    dto.setSize(file.getSize());
    dto.setContent(file.getBytes());

    return dto;
  }

  public static ValidationResults validateImage(final String field, final MultipartFile file)
  {
    ValidationResults results = new ValidationResults();

    String contentType = file.getContentType();
    if (!StringUtils.startsWith(contentType, "image/"))
    {
      results.addError(fieldError(field, ValidationError.getFullCode("image.wring-content-type"),
          "The file must be an image", contentType));
    }

    return results;
  }

  public static ResponseEntity<ByteArrayResource> convertToResponse(final Optional<BinaryDataDTO> dto)
  {
    return dto.map(data -> ok().contentType(MediaType.parseMediaType(data.getContentType()))
      .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + data.getName() + "\"")
      .cacheControl(CacheControl.maxAge(Duration.ofHours(1))
        .cachePublic())
      .lastModified(data.getCreatedAt())
      .contentLength(data.getSize())
      .body(new ByteArrayResource(data.getContent())))
      .orElse(notFound().build());
  }
}
