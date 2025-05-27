package me.dahiorus.project.vending.domain.item.entity;

import java.io.Serializable;
import me.dahiorus.project.vending.domain.file.entity.UploadedFile;

public record ItemWithImage(Item item, UploadedFile image) implements Serializable {}
