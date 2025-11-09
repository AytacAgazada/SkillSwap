import apiCall from './api';

export interface Notification {
  id: string;
  userId: string;
  title: string;
  message: string;
  type?: string;
  read: boolean;
  createdAt: string;
}

export const notificationService = {
  // Get User Notifications
  getUserNotifications: async (userId: string): Promise<Notification[]> => {
    return apiCall<Notification[]>(`/api/notifications/user/${userId}`, {
      method: 'GET',
    });
  },

  // Mark Notification as Read
  markAsRead: async (id: string): Promise<void> => {
    return apiCall<void>(`/api/notifications/${id}/read`, {
      method: 'POST',
    });
  },
};

