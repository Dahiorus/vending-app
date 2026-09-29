import { httpResource } from '@angular/common/http';
import { Component, computed } from '@angular/core';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { User } from '../../../core/auth/models/user';
import { linkHref } from '../../../shared/models/hal';
import { PROFILE_PASSWORD_REL, PROFILE_PICTURE_REL } from '../models/user-profile';
import { profileUrl } from '../profile-api';
import { ProfileInfo } from '../profile-info/profile-info';
import { ProfilePassword } from '../profile-password/profile-password';
import { ProfilePicture } from '../profile-picture/profile-picture';

@Component({
  selector: 'app-profile',
  imports: [MatProgressSpinnerModule, ProfileInfo, ProfilePassword, ProfilePicture],
  templateUrl: './profile.html',
})
export class Profile {
  private readonly resource = httpResource<User>(() => profileUrl());

  readonly isLoading = this.resource.isLoading;
  readonly hasError = computed(() => this.resource.error() !== undefined);
  readonly profile = computed(() => this.resource.value());
  readonly pictureHref = computed(() => linkHref(this.profile(), PROFILE_PICTURE_REL));
  readonly passwordHref = computed(() => linkHref(this.profile(), PROFILE_PASSWORD_REL));

  reload(): void {
    this.resource.reload();
  }
}
