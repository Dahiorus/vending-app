package me.dahiorus.project.vending.web.api.request;

import static me.dahiorus.project.vending.util.ValidationTestUtils.assertHasExactlyFieldErrors;
import static me.dahiorus.project.vending.util.ValidationTestUtils.assertNoFieldError;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import me.dahiorus.project.vending.domain.model.dto.BinaryDataDTO;
import me.dahiorus.project.vending.domain.service.validation.ValidationResults;

class MultipartFileUtilsTest
{
  @Test
  void convertMultipartFileToBinaryDataDto() throws Exception
  {
    MultipartFile file = new MockMultipartFile("file", "image.jpg", MediaType.IMAGE_JPEG_VALUE, new byte[32]);
    BinaryDataDTO binaryDataDto = MultipartFileUtils.convert(file);

    assertThat(binaryDataDto).hasFieldOrPropertyWithValue("contentType", file.getContentType())
      .hasFieldOrPropertyWithValue("content", file.getBytes())
      .satisfies(dto -> assertThat(dto.getName()).endsWith(".jpg")
        .isNotEqualTo(file.getOriginalFilename()));
  }

  @Nested
  class ValidateImageTests
  {
    @ParameterizedTest(name = "Content type {0} is valid")
    @ValueSource(strings = { MediaType.IMAGE_JPEG_VALUE, MediaType.IMAGE_PNG_VALUE })
    void imageIsValid(final String contentType)
    {
      MultipartFile file = new MockMultipartFile("file", "image", contentType, new byte[32]);

      ValidationResults validationResults = MultipartFileUtils.validateImage("file", file);

      assertNoFieldError(validationResults, "file");
    }

    @Test
    void otherContentTypeIsInvalid()
    {
      MultipartFile file = new MockMultipartFile("file", "text.html", MediaType.TEXT_HTML_VALUE, new byte[32]);

      ValidationResults validationResults = MultipartFileUtils.validateImage("file", file);

      assertHasExactlyFieldErrors(validationResults, "file", "validation.constraints.image.wrong-content-type");
    }
  }

  @Nested
  class ConvertToResponseTests
  {
    static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter
      .ofPattern("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US)
      .withZone(ZoneId.of("GMT"));

    @Test
    void convertDtoToResponse()
    {
      BinaryDataDTO dto = new BinaryDataDTO();
      dto.setName("image.jpg");
      dto.setContentType(MediaType.IMAGE_JPEG_VALUE);
      dto.setContent(new byte[32]);
      dto.setCreatedAt(Instant.now());

      ResponseEntity<ByteArrayResource> response = MultipartFileUtils.convertToResponse(Optional.of(dto));

      assertThat(response).satisfies(r -> {
        assertThat(r.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(r.getHeaders()).containsEntry(HttpHeaders.CONTENT_TYPE,
          List.of(dto.getContentType()))
          .containsEntry(HttpHeaders.LAST_MODIFIED,
            List.of(DATE_FORMATTER.format(dto.getCreatedAt())))
          .containsEntry(HttpHeaders.CONTENT_DISPOSITION, List.of("inline; filename=\"" + dto.getName() + "\""))
          .containsEntry(HttpHeaders.CACHE_CONTROL, List.of("max-age=3600, public"))
          .containsEntry(HttpHeaders.CONTENT_LENGTH, List.of(Integer.toString(dto.getSize())));
      });
    }

    @Test
    void convertEmptyToResponse()
    {
      ResponseEntity<ByteArrayResource> response = MultipartFileUtils.convertToResponse(Optional.empty());

      assertThat(response).isEqualTo(ResponseEntity.notFound()
        .build());
    }
  }
}
