import { Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { useLanguage } from '../contexts/LanguageContext'
import ThemeSwitcher from './ThemeSwitcher'
import LanguageSwitcher from './LanguageSwitcher'
import './Navbar.css'

const Navbar = () => {
  const { isAuthenticated, user, logout } = useAuth()
  const { t } = useLanguage()

  const handleLogout = async () => {
    try {
      await logout()
    } catch (error) {
      console.error('Logout error:', error)
    }
  }

  return (
    <nav className="navbar">
      <div className="container">
        <div className="navbar-content">
          <Link to="/" className="navbar-logo">
            <span className="home-icon">🏠</span>
            <span className="logo-text">SkillSwap</span>
          </Link>
          <ul className="navbar-menu">
            <li><Link to="/">{t('home')}</Link></li>
            <li className="navbar-controls">
              <ThemeSwitcher />
              <LanguageSwitcher />
            </li>
            {isAuthenticated ? (
              <>
                <li><Link to="/dashboard">{t('dashboard')}</Link></li>
                <li><Link to="/profile">{t('profile')}</Link></li>
                <li><Link to="/swap-offers">{t('exchanges')}</Link></li>
                <li><Link to="/chat">{t('messages')}</Link></li>
                <li><Link to="/community">{t('groups')}</Link></li>
                <li><Link to="/gamification">{t('gamification')}</Link></li>
                <li className="user-info">
                  <span className="username">{user?.username}</span>
                </li>
                <li>
                  <button onClick={handleLogout} className="btn btn-outline">
                    {t('logout')}
                  </button>
                </li>
              </>
            ) : (
              <>
                <li><Link to="/login">{t('login')}</Link></li>
                <li><Link to="/register" className="btn btn-primary">{t('signup')}</Link></li>
              </>
            )}
          </ul>
        </div>
      </div>
    </nav>
  )
}

export default Navbar

