package me.dahiorus.project.vending.domain.file.entity;

import java.util.UUID;
import me.dahiorus.project.vending.domain.DomainId;

public record UploadedFileId(UUID value) implements DomainId {}
