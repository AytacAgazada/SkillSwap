import { useEffect, useState } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { useNavigate } from 'react-router-dom'
import './Dashboard.css'

const Dashboard = () => {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)

  const handleLogout = async () => {
    setLoading(true)
    try {
      await logout()
    } catch (error) {
      console.error('Logout error:', error)
    } finally {
      setLoading(false)
    }
  }

  if (!user) {
    return <div>Yüklənir...</div>
  }

  return (
    <div className="dashboard">
      <div className="container">
        <div className="dashboard-header">
          <h1>Xoş gəlmisiniz, {user.username}!</h1>
          <button 
            onClick={handleLogout} 
            className="btn btn-secondary"
            disabled={loading}
          >
            {loading ? 'Çıxış edilir...' : 'Çıxış'}
          </button>
        </div>
        
        <div className="dashboard-content">
          <div className="profile-card">
            <h2>Profil Məlumatları</h2>
            <div className="profile-info">
              <p><strong>İstifadəçi adı:</strong> {user.username}</p>
              <p><strong>FIN:</strong> {user.fin}</p>
              {user.email && <p><strong>Email:</strong> {user.email}</p>}
              {user.phone && <p><strong>Telefon:</strong> {user.phone}</p>}
              <p><strong>Rollar:</strong> {user.roles.join(', ')}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Dashboard

