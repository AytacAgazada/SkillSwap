import apiCall from './api';

export interface ChatMessage {
  id: string;
  swapId: string;
  senderId: string;
  receiverId: string;
  senderName?: string;
  receiverName?: string;
  content: string;
  timestamp: string;
}

export interface SendMessageRequest {
  swapId: string;
  receiverId: string;
  content: string;
}

export const chatService = {
  // Get Chat History
  getChatHistory: async (swapId: string): Promise<ChatMessage[]> => {
    return apiCall<ChatMessage[]>(`/api/chat/history/${swapId}`, {
      method: 'GET',
    });
  },
};

