package me.dahiorus.project.vending.web.api.request;

import static me.dahiorus.project.vending.core.service.validation.FieldValidationError.fieldError;
import static org.apache.commons.io.FilenameUtils.getExtension;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
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
}
