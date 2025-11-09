import apiCall from './api';
import type { SkillResponse } from './skillService';

export interface UserBio {
  id?: number;
  authUserId: string;
  firstName: string;
  lastName: string;
  education?: string;
  skillIds?: number[];
  phone?: string;
  jobTitle?: string;
  yearsOfExperience?: number;
  linkedInProfileUrl?: string;
  bio?: string;
}

export interface UserBioResponse {
  id: number;
  authUserId: string;
  firstName: string;
  lastName: string;
  education?: string;
  skills?: SkillResponse[];
  phone?: string;
  jobTitle?: string;
  yearsOfExperience?: number;
  linkedInProfileUrl?: string;
  bio?: string;
}

export interface SkillResponse {
  id: number;
  name: string;
  description?: string;
  level: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';
}

export const userBioService = {
  // Create User Bio
  createUserBio: async (userBio: UserBio): Promise<UserBioResponse> => {
    return apiCall<UserBioResponse>('/api/user-bios', {
      method: 'POST',
      body: JSON.stringify(userBio),
    });
  },

  // Get User Bio by ID
  getUserBio: async (id: number): Promise<UserBioResponse> => {
    return apiCall<UserBioResponse>(`/api/user-bios/${id}`, {
      method: 'GET',
    });
  },

  // Get User Bio by Auth User ID
  getUserBioByAuthUserId: async (authUserId: string): Promise<UserBioResponse> => {
    return apiCall<UserBioResponse>(`/api/user-bios/auth-user/${authUserId}`, {
      method: 'GET',
    });
  },

  // Get Current User Bio
  getMyUserBio: async (): Promise<UserBioResponse> => {
    return apiCall<UserBioResponse>('/api/user-bios/me', {
      method: 'GET',
    });
  },

  // Get All User Bios
  getAllUserBios: async (): Promise<UserBioResponse[]> => {
    return apiCall<UserBioResponse[]>('/api/user-bios', {
      method: 'GET',
    });
  },

  // Update User Bio
  updateUserBio: async (userBio: UserBio): Promise<UserBioResponse> => {
    return apiCall<UserBioResponse>('/api/user-bios', {
      method: 'PUT',
      body: JSON.stringify(userBio),
    });
  },

  // Delete User Bio
  deleteUserBio: async (id: number): Promise<void> => {
    return apiCall<void>(`/api/user-bios/${id}`, {
      method: 'DELETE',
    });
  },

  // Delete Current User Bio
  deleteMyUserBio: async (): Promise<void> => {
    return apiCall<void>('/api/user-bios/me', {
      method: 'DELETE',
    });
  },
};

