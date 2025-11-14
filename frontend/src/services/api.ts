import { authService } from './authService';
// API Base Configuration
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:1120';

// API Response Types
export interface ApiError {
  message: string;
  status: number;
  errors?: Record<string, string[]>;
}

// Generic API call function
async function apiCall<T>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const makeRequest = async (isRetry = false): Promise<T> => {
    const token = localStorage.getItem('accessToken');
    const user = localStorage.getItem('user');
    const userId = user ? JSON.parse(user).id : null;

    const headers: HeadersInit = {
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` }),
      ...(userId && { 'X-Auth-User-Id': userId }),
      ...options.headers,
    };

    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      ...options,
      headers,
    });

    if (!response.ok) {
      if (response.status === 401 && !isRetry) {
        const refreshToken = localStorage.getItem('refreshToken');
        if (refreshToken) {
          try {
            const refreshResponse = await authService.refreshToken(refreshToken);
            localStorage.setItem('accessToken', refreshResponse.accessToken);
            localStorage.setItem('refreshToken', refreshResponse.refreshToken);
            return makeRequest(true); // Retry the request with the new token
          } catch (refreshError) {
            // Handle refresh token failure (e.g., log out the user)
            console.error('Failed to refresh token:', refreshError);
            // Optionally, dispatch a logout event or redirect to login
            localStorage.removeItem('accessToken');
            localStorage.removeItem('refreshToken');
            localStorage.removeItem('user');
            window.location.href = '/login';
          }
        }
      }

      let errorData: any = { message: response.statusText };
      try {
        const text = await response.text();
        errorData = text ? JSON.parse(text) : errorData;
      } catch {
        // If parsing fails, use default error message
      }

      const error: ApiError = {
        message: errorData.message || errorData.error || `HTTP ${response.status}: ${response.statusText}`,
        status: response.status,
        errors: errorData.errors || errorData.validationErrors,
      };
      throw error;
    }

    // Handle empty responses
    const contentType = response.headers.get('content-type');
    if (contentType && contentType.includes('application/json')) {
      return response.json();
    }

    const text = await response.text();
    return (text ? JSON.parse(text) : {}) as T;
  };

  return makeRequest();
}

export default apiCall;

