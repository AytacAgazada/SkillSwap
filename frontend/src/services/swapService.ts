import apiCall from './api';

export interface SwapOffer {
  skillOffered: string;
  skillRequested: string;
  meetingType: 'PHYSICAL' | 'ONLINE' | 'BOTH';
  description: string;
  latitude: number;
  longitude: number;
}

export interface SwapOfferResponse {
  id: number;
  userId: string;
  skillOffered: string;
  skillRequested: string;
  meetingType: 'PHYSICAL' | 'ONLINE' | 'BOTH';
  description: string;
  latitude: number;
  longitude: number;
  isActive?: boolean;
  createdAt?: string;
}

export interface AcceptSwapRequest {
  meetingDateTime: string;
}

export const swapService = {
  // Create Swap Offer
  createOffer: async (offer: SwapOffer): Promise<SwapOfferResponse> => {
    return apiCall<SwapOfferResponse>('/api/skill-swaps/offers', {
      method: 'POST',
      body: JSON.stringify(offer),
    });
  },

  // Get My Offers
  getMyOffers: async (): Promise<SwapOfferResponse[]> => {
    return apiCall<SwapOfferResponse[]>('/api/skill-swaps/offers/me', {
      method: 'GET',
    });
  },

  // Search Offers
  searchOffers: async (skill: string, lat: number, lon: number, radiusKm: number = 10): Promise<SwapOfferResponse[]> => {
    return apiCall<SwapOfferResponse[]>(`/api/skill-swaps/offers/search?skill=${encodeURIComponent(skill)}&lat=${lat}&lon=${lon}&radiusKm=${radiusKm}`, {
      method: 'GET',
    });
  },

  // Match with Offer
  matchOffer: async (offerId: number): Promise<string> => {
    return apiCall<string>(`/api/skil-swaps/match/${offerId}`, {
      method: 'POST',
    });
  },

  // Accept Swap
  acceptSwap: async (swapId: number, meetingDateTime: string): Promise<string> => {
    return apiCall<string>(`/api/skil-swaps/${swapId}/accept`, {
      method: 'POST',
      body: JSON.stringify({ meetingDateTime }),
    });
  },

  // Complete Swap
  completeSwap: async (swapId: number, otherUserId: string): Promise<string> => {
    return apiCall<string>(`/api/skil-swaps/complete/${swapId}?otherUserId=${otherUserId}`, {
      method: 'POST',
    });
  },
};

