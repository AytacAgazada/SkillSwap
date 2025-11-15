import React, { useEffect, useState } from 'react'
import { gamificationService, type UserStats, type LeaderboardEntry } from '../../services/gamificationService'
import { useParams } from 'react-router-dom'

const Gamification: React.FC = () => {
  const { userId } = useParams<{ userId: string }>()
  const [stats, setStats] = useState<UserStats | null>(null)
  const [leaderboard, setLeaderboard] = useState<LeaderboardEntry[]>([])

  useEffect(() => {
    const fetchData = async () => {
      try {
        if (userId) {
          const statsResponse = await gamificationService.getUserStats(userId)
          setStats(statsResponse)
        }
        const leaderboardResponse = await gamificationService.getLeaderboard()
        setLeaderboard(leaderboardResponse)
      } catch (error) {
        console.error('Error fetching gamification data:', error)
      }
    }

    fetchData()
  }, [userId])

  return (
    <div>
      {stats ? (
        <>
          <h1>User Stats</h1>
          <p>Level: {stats.level}</p>
          <p>XP: {stats.xp}</p>
          <h2>Badges</h2>
          <ul>
            {stats.badges.map((badge: string, index: number) => (
              <li key={index}>{badge}</li>
            ))}
          </ul>
        </>
      ) : (
        <p>Loading user stats...</p>
      )}

      <hr />

      <h2>Leaderboard</h2>
      {leaderboard.length > 0 ? (
        <table>
          <thead>
            <tr>
              <th>Rank</th>
              <th>User</th>
              <th>XP</th>
              <th>Level</th>
            </tr>
          </thead>
          <tbody>
            {leaderboard.map((entry, index) => (
              <tr key={entry.userId}>
                <td>{index + 1}</td>
                <td>{entry.username}</td>
                <td>{entry.xp}</td>
                <td>{entry.level}</td>
              </tr>
            ))}
          </tbody>
        </table>
      ) : (
        <p>Loading leaderboard...</p>
      )}
    </div>
  )
}

export default Gamification
