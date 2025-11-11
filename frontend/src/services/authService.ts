import apiCall from './api';

// Types
export interface SignupRequest {
  username: string;
  fin: string;
  password: string;
  confirmPassword: string;
  email?: string;
  phone?: string;
  role?: 'USER' | 'ADMIN' | 'PROVIDER';
}

export interface LoginRequest {
  identifier: string; // username, FIN or email
  password: string;
  rememberMe?: boolean;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  type: string;
  id: string;
  username: string;
  fin: string;
  email?: string;
  phone?: string;
  roles: string[];
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface OtpSendRequest {
  identifier: string; // email or phone
  sendMethod: 'email' | 'phone';
  otpType: 'ACCOUNT_CONFIRMATION' | 'PASSWORD_RESET';
}

export interface OtpVerificationRequest {
  identifier: string;
  otpCode: string;
  otpType: 'ACCOUNT_CONFIRMATION' | 'PASSWORD_RESET';
}

export interface ResetPasswordRequest {
  identifier: string;
  otpCode: string;
  newPassword: string;
  confirmPassword: string;
}

// Auth Service
export const authService = {
  // Signup
  signup: async (data: SignupRequest): Promise<string> => {
    return apiCall<string>('/api/auth/signup', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  // Login
  login: async (data: LoginRequest): Promise<AuthResponse> => {
    return apiCall<AuthResponse>('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  // Refresh Token
  refreshToken: async (refreshToken: string): Promise<AuthResponse> => {
    return apiCall<AuthResponse>('/api/auth/refresh-token', {
      method: 'POST',
      body: JSON.stringify({ refreshToken }),
    });
  },

  // Logout
  logout: async (): Promise<string> => {
    return apiCall<string>('/api/auth/logout', {
      method: 'POST',
    });
  },

  // Send OTP
  sendOtp: async (data: OtpSendRequest): Promise<string> => {
    return apiCall<string>('/api/auth/otp/send', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  // Verify OTP
  verifyOtp: async (data: OtpVerificationRequest): Promise<string> => {
    return apiCall<string>('/api/auth/otp/verify', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  // Reset Password
  resetPassword: async (data: ResetPasswordRequest): Promise<string> => {
    return apiCall<string>('/api/auth/password/reset', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  // Get User Profile
  getUserProfile: async (): Promise<string> => {
    return apiCall<string>('/api/auth/user/profile', {
      method: 'GET',
    });
  },
};

