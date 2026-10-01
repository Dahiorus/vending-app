export const PROFILE_PASSWORD_REL = 'me:password';
export const PROFILE_PICTURE_REL = 'me:picture';

export interface UserToUpdate {
  firstname: string;
  lastname: string;
}

export interface EditPasswordRequest {
  oldPassword: string;
  newPassword: string;
}
