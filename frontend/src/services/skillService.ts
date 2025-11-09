import apiCall from './api';

export interface Skill {
  id?: number;
  name: string;
  description?: string;
  level: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';
}

export interface SkillResponse {
  id: number;
  name: string;
  description?: string;
  level: 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED' | 'EXPERT';
}

export const skillService = {
  // Create Skill
  createSkill: async (skill: Skill): Promise<SkillResponse> => {
    return apiCall<SkillResponse>('/api/skills', {
      method: 'POST',
      body: JSON.stringify(skill),
    });
  },

  // Get Skill by ID
  getSkill: async (id: number): Promise<SkillResponse> => {
    return apiCall<SkillResponse>(`/api/skills/${id}`, {
      method: 'GET',
    });
  },

  // Get All Skills
  getAllSkills: async (): Promise<SkillResponse[]> => {
    return apiCall<SkillResponse[]>('/api/skills', {
      method: 'GET',
    });
  },

  // Update Skill
  updateSkill: async (skill: Skill): Promise<SkillResponse> => {
    return apiCall<SkillResponse>('/api/skills', {
      method: 'PUT',
      body: JSON.stringify(skill),
    });
  },

  // Delete Skill
  deleteSkill: async (id: number): Promise<void> => {
    return apiCall<void>(`/api/skills/${id}`, {
      method: 'DELETE',
    });
  },
};

