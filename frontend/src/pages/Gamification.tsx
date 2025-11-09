import { useState, useEffect } from 'react'
import { getUserStats } from '../services/gamificationService'
import { useAuth } from '../contexts/AuthContext'
import './Gamification.css'

const Gamification = () => {
  const { user } = useAuth()
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    const fetchStats = async () => {
      if (user) {
        try {
          const response = await getUserStats(user.id)
          setStats(response.data)
        } catch (err) {
          setError('Failed to load gamification stats.')
          console.error(err)
        } finally {
          setLoading(false)
        }
      }
    }

    fetchStats()
  }, [user])

  if (loading) {
    return <div>Loading...</div>
  }

  if (error) {
    return <div>{error}</div>
  }

  return (
    <div className="gamification-container">
      <h2>Gamification Stats</h2>
      {stats ? (
        <div className="stats-grid">
          <div className="stat-card">
            <h3>XP</h3>
            <p>{stats.xp}</p>
          </div>
          <div className="stat-card">
            <h3>Level</h3>
            <p>{stats.level}</p>
          </div>
          <div className="stat-card">
            <h3>Badges</h3>
            <ul>
              {stats.badges.map((badge, index) => (
                <li key={index}>{badge}</li>
              ))}
            </ul>
          </div>
        </div>
      ) : (
        <p>No stats available.</p>
      )}
    </div>
  )
}

export default Gamification
