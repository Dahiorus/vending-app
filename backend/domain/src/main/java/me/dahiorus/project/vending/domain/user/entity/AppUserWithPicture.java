package me.dahiorus.project.vending.domain.user.entity;

import java.io.Serializable;
import me.dahiorus.project.vending.domain.file.entity.UploadedFile;

public record AppUserWithPicture(UserId userId, UploadedFile profilePicture)
    implements Serializable {}
