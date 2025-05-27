package me.dahiorus.project.vending.infrastructure.jpa.entity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import me.dahiorus.project.vending.domain.file.entity.BinaryContent;
import me.dahiorus.project.vending.domain.file.entity.ContentType;
import me.dahiorus.project.vending.domain.file.entity.FileToUpload;
import me.dahiorus.project.vending.domain.file.entity.Filename;
import me.dahiorus.project.vending.domain.file.entity.UploadedFile;
import me.dahiorus.project.vending.domain.file.entity.UploadedFileId;

@Entity
@Table(
    name = "uploaded_file",
    uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "UK_UPLOADED_FILE_NAME"),
    indexes =
        @Index(columnList = "name, contentType", name = "IDX_UPLOADED_FILE_CONTENT_TYPE_NAME"))
@AttributeOverride(name = "id", column = @Column(name = "uploaded_file_id"))
public class JpaUploadedFile extends JpaEntity {
  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String contentType;

  @Lob
  @Column(nullable = false)
  private byte[] content;

  public static JpaUploadedFile toCreate(FileToUpload fileToUpload) {
    var jpaUploadedFile = new JpaUploadedFile();
    jpaUploadedFile.name = fileToUpload.name();
    jpaUploadedFile.contentType = fileToUpload.type();
    jpaUploadedFile.content = fileToUpload.data();

    return jpaUploadedFile;
  }

  public UploadedFile toDomain() {
    return new UploadedFile(
        new UploadedFileId(getId()),
        new Filename(name),
        new BinaryContent(content),
        ContentType.of(contentType).orElseThrow(),
        getCreatedAt());
  }
}
