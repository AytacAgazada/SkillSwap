import apiCall from './api';

export interface UserStats {
  userId: string;
  xp: number;
  level: number;
  badges: string[];
}

export interface LeaderboardEntry {
  userId: string;
  username: string;
  xp: number;
  level: number;
}

export interface AddXpRequest {
  userId: string;
  xp: number;
  eventType: string;
}

export const gamificationService = {
  // Get Leaderboard
  getLeaderboard: async (): Promise<LeaderboardEntry[]> => {
    return apiCall<LeaderboardEntry[]>('/api/gamification/leaderboard', {
      method: 'GET',
    });
  },
  
  // Get User Stats
  getUserStats: async (userId: string): Promise<UserStats> => {
    return apiCall<UserStats>(`/api/gamification/${userId}`, {
      method: 'GET',
    });
  },

  // Add XP
  addXp: async (data: AddXpRequest): Promise<void> => {
    return apiCall<void>('/api/gamification/add-xp', {
      method: 'POST',
      body: JSON.stringify(data),
    });
  },

  // Get User Badges (same as getUserStats)
  getUserBadges: async (userId: string): Promise<UserStats> => {
    return apiCall<UserStats>(`/api/gamification/badges/${userId}`, {
      method: 'GET',
    });
  },
};

