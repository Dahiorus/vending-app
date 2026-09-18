export interface Credentials {
  username: string;
  password: string;
}

export interface AuthSession {
  accessToken: string;
}

/** Claims issued by JwtTokenIssuer. `roles` holds fully-qualified authorities such as "ROLE_ADMIN". */
export interface JwtPayload {
  sub: string;
  roles?: string[];
  exp: number;
  token_type: string;
}
