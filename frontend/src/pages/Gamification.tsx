import { useState, useEffect } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { gamificationService, type UserStats } from '../services/gamificationService'
import './Gamification.css'

const Gamification = () => {
  const { user } = useAuth()
  const [userStats, setUserStats] = useState<UserStats | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string>('')

  useEffect(() => {
    if (user?.id) {
      loadUserStats()
    }
  }, [user])

  const loadUserStats = async () => {
    if (!user?.id) return

    try {
      setLoading(true)
      const stats = await gamificationService.getUserStats(user.id)
      setUserStats(stats)
    } catch (err: any) {
      setError(err.message || 'Statistika yüklənə bilmədi')
    } finally {
      setLoading(false)
    }
  }

  const calculateXpForNextLevel = (currentLevel: number) => {
    return currentLevel * 100
  }

  const calculateXpProgress = (currentXp: number, currentLevel: number) => {
    const xpForCurrentLevel = (currentLevel - 1) * 100
    const xpForNextLevel = currentLevel * 100
    const xpNeeded = xpForNextLevel - xpForCurrentLevel
    const xpProgress = currentXp - xpForCurrentLevel
    return {
      progress: (xpProgress / xpNeeded) * 100,
      current: xpProgress,
      needed: xpNeeded
    }
  }

  const getBadgeIcon = (badgeName: string) => {
    const icons: Record<string, string> = {
      'Helper': '🤝',
      'Expert': '⭐',
      'Master': '👑',
      'Champion': '🏆',
      'Novice': '🌱',
      'Intermediate': '📚',
      'Advanced': '💎',
    }
    return icons[badgeName] || '🏅'
  }

  if (loading) {
    return <div className="gamification-loading">Yüklənir...</div>
  }

  if (error || !userStats) {
    return (
      <div className="gamification-page">
        <div className="container">
          <div className="error-message">{error || 'Statistika tapılmadı'}</div>
        </div>
      </div>
    )
  }

  const xpProgress = calculateXpProgress(userStats.xp, userStats.level)
  const xpForNextLevel = calculateXpForNextLevel(userStats.level)

  return (
    <div className="gamification-page">
      <div className="container">
        <h1 className="page-title">Gamifikasiya</h1>

        <div className="stats-container">
          <div className="level-card">
            <div className="level-display">
              <div className="level-number">{userStats.level}</div>
              <div className="level-label">Səviyyə</div>
            </div>
            <div className="xp-display">
              <div className="xp-info">
                <span className="xp-current">{userStats.xp}</span>
                <span className="xp-separator">/</span>
                <span className="xp-total">{xpForNextLevel} XP</span>
              </div>
              <div className="xp-progress-bar">
                <div
                  className="xp-progress-fill"
                  style={{ width: `${xpProgress.progress}%` }}
                />
              </div>
              <div className="xp-progress-text">
                {xpProgress.current} / {xpProgress.needed} XP növbəti səviyyəyə
              </div>
            </div>
          </div>

          <div className="stats-grid">
            <div className="stat-card">
              <div className="stat-icon">⭐</div>
              <div className="stat-value">{userStats.xp}</div>
              <div className="stat-label">Ümumi XP</div>
            </div>
            <div className="stat-card">
              <div className="stat-icon">📊</div>
              <div className="stat-value">{userStats.level}</div>
              <div className="stat-label">Səviyyə</div>
            </div>
            <div className="stat-card">
              <div className="stat-icon">🏅</div>
              <div className="stat-value">{userStats.badges?.length || 0}</div>
              <div className="stat-label">Nişanlar</div>
            </div>
          </div>
        </div>

        <div className="badges-section">
          <h2>Nişanlar</h2>
          {userStats.badges && userStats.badges.length > 0 ? (
            <div className="badges-grid">
              {userStats.badges.map((badge, index) => (
                <div key={index} className="badge-card">
                  <div className="badge-icon">{getBadgeIcon(badge)}</div>
                  <div className="badge-name">{badge}</div>
                </div>
              ))}
            </div>
          ) : (
            <div className="no-badges">
              <p>Hələ heç bir nişan qazanmamısınız</p>
              <p className="badge-hint">Aktiv olun və nişanlar qazanın!</p>
            </div>
          )}
        </div>

        <div className="achievements-section">
          <h2>Nailiyyətlər</h2>
          <div className="achievements-list">
            <div className="achievement-item">
              <div className="achievement-icon">🎯</div>
              <div className="achievement-info">
                <h3>İlk Addım</h3>
                <p>Profilinizi tamamlayın</p>
              </div>
              <div className={`achievement-status ${userStats.level >= 1 ? 'completed' : 'pending'}`}>
                {userStats.level >= 1 ? '✓' : '○'}
              </div>
            </div>
            <div className="achievement-item">
              <div className="achievement-icon">🔄</div>
              <div className="achievement-info">
                <h3>Mübadilə Ustası</h3>
                <p>5 mübadilə tamamlayın</p>
              </div>
              <div className={`achievement-status ${userStats.badges?.includes('Helper') ? 'completed' : 'pending'}`}>
                {userStats.badges?.includes('Helper') ? '✓' : '○'}
              </div>
            </div>
            <div className="achievement-item">
              <div className="achievement-icon">💬</div>
              <div className="achievement-info">
                <h3>Sosial</h3>
                <p>10 mesaj göndərin</p>
              </div>
              <div className="achievement-status pending">○</div>
            </div>
            <div className="achievement-item">
              <div className="achievement-icon">👥</div>
              <div className="achievement-info">
                <h3>İctimaiyyət Üzvü</h3>
                <p>3 qrupa qoşulun</p>
              </div>
              <div className="achievement-status pending">○</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Gamification

