import React, { createContext, useContext, useState, useEffect, type ReactNode } from 'react';
import { authService, type AuthResponse, type LoginRequest, type SignupRequest } from '../services/authService';
import { userBioService } from '../services/userBioService';

interface AuthContextType {
  user: AuthResponse | null;
  loading: boolean;
  login: (data: LoginRequest) => Promise<void>;
  signup: (data: SignupRequest) => Promise<void>;
  logout: () => Promise<void>;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within an AuthProvider');
  }
  return context;
};

interface AuthProviderProps {
  children: ReactNode;
}

export const AuthProvider: React.FC<AuthProviderProps> = ({ children }) => {
  const [user, setUser] = useState<AuthResponse | null>(null);
  const [loading, setLoading] = useState(true);

  // Load user from localStorage on mount
  useEffect(() => {
    const loadUser = () => {
      try {
        const storedUser = localStorage.getItem('user');
        const accessToken = localStorage.getItem('accessToken');

        if (storedUser && accessToken) {
          setUser(JSON.parse(storedUser));
        }
      } catch (error) {
        console.error('Error loading user from storage:', error);
        localStorage.removeItem('user');
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
      } finally {
        setLoading(false);
      }
    };

    loadUser();
  }, []);

  const login = async (data: LoginRequest) => {
    try {
      const response = await authService.login(data);

      // Store tokens and user data
      localStorage.setItem('accessToken', response.accessToken);
      localStorage.setItem('refreshToken', response.refreshToken);
      localStorage.setItem('user', JSON.stringify(response));

      setUser(response);

      // Check if user has profile, if not redirect to profile page
      try {
        await userBioService.getUserBioByAuthUserId(response.id);
        window.location.href = '/dashboard';
      } catch {
        // Profile doesn't exist, redirect to profile creation
        window.location.href = '/profile';
      }
    } catch (error: any) {
      throw error;
    }
  };

  const signup = async (data: SignupRequest) => {
    try {
      await authService.signup(data);
      // After signup, redirect to login or OTP verification
      window.location.href = '/login';
    } catch (error: any) {
      throw error;
    }
  };

  const logout = async () => {
    try {
      await authService.logout();
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      // Clear local storage regardless of API call success
      localStorage.removeItem('accessToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      setUser(null);
      window.location.href = '/';
    }
  };

  const value: AuthContextType = {
    user,
    loading,
    login,
    signup,
    logout,
    isAuthenticated: !!user,
  };

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
};

