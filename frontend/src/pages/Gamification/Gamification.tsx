import React, { useEffect, useState } from 'react'
import { gamificationService, type UserStats } from '../../services/gamificationService'
import { useParams } from 'react-router-dom'

const Gamification: React.FC = () => {
  const { userId } = useParams<{ userId: string }>()
  const [stats, setStats] = useState<UserStats | null>(null)

  useEffect(() => {
    const fetchStats = async () => {
      try {
        if (userId) {
          const response = await gamificationService.getUserStats(userId)
          setStats(response)
        }
      } catch (error) {
        console.error('Error fetching user stats:', error)
      }
    }

    fetchStats()
  }, [userId])

  if (!stats) {
    return <div>Loading...</div>
  }

  return (
    <div>
      <h1>User Stats</h1>
      <p>Level: {stats.level}</p>
      <p>XP: {stats.xp}</p>
      <h2>Badges</h2>
      <ul>
        {stats.badges.map((badge: string, index: number) => (
          <li key={index}>{badge}</li>
        ))}
      </ul>
    </div>
  )
}

export default Gamification
