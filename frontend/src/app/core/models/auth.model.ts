export interface LoginRequest {
  username: string;
  password: string;
  rememberMe?: boolean;
}

export interface LoginResponse {
  token: string;
  tokenType: string;
  username: string;
  fullName: string;
  role: string;
  expiresInMs: number;
}

export interface UserProfile {
  id?: number;
  username: string;
  fullName: string;
  role: string;
}

