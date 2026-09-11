package me.dahiorus.project.vending.domain.item.entity;

import java.io.Serializable;
import me.dahiorus.project.vending.domain.file.entity.UploadedFile;

public record ItemWithImage(ItemId itemId, UploadedFile image) implements Serializable {}
