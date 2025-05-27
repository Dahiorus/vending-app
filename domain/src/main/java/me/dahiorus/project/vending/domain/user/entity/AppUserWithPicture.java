package me.dahiorus.project.vending.domain.user.entity;

import java.io.Serializable;
import me.dahiorus.project.vending.domain.file.entity.UploadedFile;

public record AppUserWithPicture(AppUser user, UploadedFile profilePicture)
    implements Serializable {}
