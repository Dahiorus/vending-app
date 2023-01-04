package me.dahiorus.project.vending.web.api.request;

import static java.time.Duration.ofHours;
import static me.dahiorus.project.vending.domain.service.validation.FieldValidationError.fieldError;
import static org.apache.commons.io.FilenameUtils.getExtension;
import static org.springframework.http.CacheControl.maxAge;
import static org.springframework.http.ContentDisposition.inline;
import static org.springframework.http.MediaType.parseMediaType;
import static org.springframework.http.ResponseEntity.notFound;
import static org.springframework.http.ResponseEntity.ok;

import java.io.IOException;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import me.dahiorus.project.vending.domain.model.dto.BinaryDataDTO;
import me.dahiorus.project.vending.domain.service.validation.ValidationError;
import me.dahiorus.project.vending.domain.service.validation.ValidationResults;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MultipartFileUtils
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
    if (!StringUtils.equalsAny(contentType, MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE))
    {
      results.addError(fieldError(field, ValidationError.getFullCode("image.wrong-content-type"),
          "The file must be a JPEG or a PNG", contentType));
    }

    return results;
  }

  public static ResponseEntity<ByteArrayResource> convertToResponse(final Optional<BinaryDataDTO> dto)
  {
    return dto.map(data -> ok().contentType(parseMediaType(data.getContentType()))
      .header(HttpHeaders.CONTENT_DISPOSITION, inline().filename(data.getName())
        .build()
        .toString())
      .cacheControl(maxAge(ofHours(1))
        .cachePublic())
      .lastModified(data.getCreatedAt())
      .contentLength(data.getSize())
      .body(new ByteArrayResource(data.getContent())))
      .orElse(notFound().build());
  }
}
