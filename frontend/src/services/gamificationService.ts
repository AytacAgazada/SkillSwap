import apiCall from './api';

export interface UserStats {
  userId: string;
  xp: number;
  level: number;
  badges: string[];
}

export interface AddXpRequest {
  userId: string;
  xp: number;
  eventType: string;
}

export const gamificationService = {
  // Get User Stats
  getUserStats: async (userId: string): Promise<UserStats> => {
    return apiCall<UserStats>(`/gamification/${userId}`, {
      method: 'GET',
    });
  },

  // Add XP
  addXp: async (data: AddXpRequest): Promise<void> => {
    return apiCall<void>('/gamification/add-xp', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  // Get User Badges (same as getUserStats)
  getUserBadges: async (userId: string): Promise<UserStats> => {
    return apiCall<UserStats>(`/gamification/badges/${userId}`, {
      method: 'GET',
    });
  },
};

