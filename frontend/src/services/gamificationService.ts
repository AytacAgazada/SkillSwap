import api from './api'

export const getUserStats = (userId: string) => {
  return api.get(`/gamification/${userId}`)
}
