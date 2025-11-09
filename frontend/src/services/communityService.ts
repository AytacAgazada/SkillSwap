import apiCall from './api';

export interface CreateGroup {
  name: string;
  description?: string;
  category?: string;
  createdByUserId: string;
}

export interface GroupResponse {
  id: number;
  name: string;
  description?: string;
  category?: string;
  createdByUserId: string;
  members: string[];
  createdAt: string;
}

export interface CreateProblem {
  title: string;
  description: string;
  createdByUserId: string;
  groupId: number;
}

export interface ProblemResponse {
  id: number;
  title: string;
  description: string;
  createdByUserId: string;
  groupId: number;
  solved: boolean;
  solvedByUserId?: string;
  createdAt: string;
  solvedAt?: string;
}

export const communityService = {
  // Create Group
  createGroup: async (group: CreateGroup): Promise<GroupResponse> => {
    return apiCall<GroupResponse>('/community/groups/create', {
      method: 'POST',
      body: JSON.stringify(group),
    });
  },

  // Get All Groups
  getAllGroups: async (): Promise<GroupResponse[]> => {
    return apiCall<GroupResponse[]>('/community/groups', {
      method: 'GET',
    });
  },

  // Join Group
  joinGroup: async (groupId: number, userId: string): Promise<void> => {
    return apiCall<void>(`/community/groups/${groupId}/join?userId=${userId}`, {
      method: 'POST',
    });
  },

  // Create Problem
  createProblem: async (problem: CreateProblem): Promise<ProblemResponse> => {
    return apiCall<ProblemResponse>('/community/problems/create', {
      method: 'POST',
      body: JSON.stringify(problem),
    });
  },

  // Get Problems by Group
  getProblemsByGroup: async (groupId: number, page: number = 0, size: number = 10): Promise<{ content: ProblemResponse[]; totalElements: number }> => {
    return apiCall<{ content: ProblemResponse[]; totalElements: number }>(`/community/problems/${groupId}?page=${page}&size=${size}`, {
      method: 'GET',
    });
  },

  // Solve Problem
  solveProblem: async (problemId: number, userId: string): Promise<void> => {
    return apiCall<void>(`/community/problems/${problemId}/solve?userId=${userId}`, {
      method: 'POST',
    });
  },
};

