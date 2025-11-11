import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { useLanguage } from '../contexts/LanguageContext'
import { authService } from '../services/authService'
import './Auth.css'

const Login = () => {
  const { login } = useAuth()
  const { language } = useLanguage()
  const [formData, setFormData] = useState({
    identifier: '',
    password: '',
    rememberMe: false
  })
  const [error, setError] = useState<string>('')
  const [loading, setLoading] = useState(false)
  
  // Reset Password State
  const [showResetPassword, setShowResetPassword] = useState(false)
  const [resetStep, setResetStep] = useState<'email' | 'otp' | 'newPassword'>('email')
  const [resetData, setResetData] = useState({
    email: '',
    otpCode: '',
    newPassword: '',
    confirmPassword: ''
  })
  const [resetLoading, setResetLoading] = useState(false)
  const [resetError, setResetError] = useState<string>('')
  const [resetSuccess, setResetSuccess] = useState<string>('')

  const t = {
    az: {
      title: 'Giriş',
      subtitle: 'Hesabınıza daxil olun',
      identifier: 'İstifadəçi adı, FIN və ya Email',
      password: 'Şifrə',
      rememberMe: 'Məni xatırla',
      loginButton: 'Giriş Et',
      loading: 'Giriş edilir...',
      noAccount: 'Hesabınız yoxdur?',
      signup: 'Qeydiyyatdan keçin',
      resetPassword: 'Şifrəni unutmusunuz?',
      resetTitle: 'Şifrəni Sıfırla',
      enterEmail: 'Email daxil edin',
      sendOtp: 'OTP Göndər',
      enterOtp: 'OTP kodu daxil edin',
      verifyOtp: 'OTP Təsdiqlə',
      newPassword: 'Yeni Şifrə',
      confirmPassword: 'Şifrəni Təsdiqlə',
      resetButton: 'Şifrəni Sıfırla',
      backToLogin: 'Girişə qayıt',
    },
    tr: {
      title: 'Giriş',
      subtitle: 'Hesabınıza giriş yapın',
      identifier: 'Kullanıcı adı, FIN veya Email',
      password: 'Şifre',
      rememberMe: 'Beni hatırla',
      loginButton: 'Giriş Yap',
      loading: 'Giriş yapılıyor...',
      noAccount: 'Hesabınız yok mu?',
      signup: 'Kayıt olun',
      resetPassword: 'Şifrenizi mi unuttunuz?',
      resetTitle: 'Şifre Sıfırla',
      enterEmail: 'Email girin',
      sendOtp: 'OTP Gönder',
      enterOtp: 'OTP kodu girin',
      verifyOtp: 'OTP Doğrula',
      newPassword: 'Yeni Şifre',
      confirmPassword: 'Şifreyi Onayla',
      resetButton: 'Şifreyi Sıfırla',
      backToLogin: 'Girişe dön',
    },
    en: {
      title: 'Login',
      subtitle: 'Sign in to your account',
      identifier: 'Username, FIN or Email',
      password: 'Password',
      rememberMe: 'Remember me',
      loginButton: 'Login',
      loading: 'Logging in...',
      noAccount: "Don't have an account?",
      signup: 'Sign up',
      resetPassword: 'Forgot password?',
      resetTitle: 'Reset Password',
      enterEmail: 'Enter email',
      sendOtp: 'Send OTP',
      enterOtp: 'Enter OTP code',
      verifyOtp: 'Verify OTP',
      newPassword: 'New Password',
      confirmPassword: 'Confirm Password',
      resetButton: 'Reset Password',
      backToLogin: 'Back to Login',
    },
    ru: {
      title: 'Вход',
      subtitle: 'Войдите в свой аккаунт',
      identifier: 'Имя пользователя, FIN или Email',
      password: 'Пароль',
      rememberMe: 'Запомнить меня',
      loginButton: 'Войти',
      loading: 'Вход...',
      noAccount: 'Нет аккаунта?',
      signup: 'Зарегистрироваться',
      resetPassword: 'Забыли пароль?',
      resetTitle: 'Сброс пароля',
      enterEmail: 'Введите email',
      sendOtp: 'Отправить OTP',
      enterOtp: 'Введите код OTP',
      verifyOtp: 'Подтвердить OTP',
      newPassword: 'Новый пароль',
      confirmPassword: 'Подтвердите пароль',
      resetButton: 'Сбросить пароль',
      backToLogin: 'Вернуться к входу',
    },
  }[language]

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target
    setFormData({
      ...formData,
      [name]: type === 'checkbox' ? checked : value
    })
    setError('')
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      await login(formData)
    } catch (err: any) {
      setError(err.message || 'Giriş zamanı xəta baş verdi')
    } finally {
      setLoading(false)
    }
  }

  const handleResetEmail = async () => {
    setResetError('')
    setResetSuccess('')
    setResetLoading(true)

    try {
      await authService.sendOtp({
        identifier: resetData.email,
        sendMethod: 'email',
        otpType: 'PASSWORD_RESET'
      })
      setResetSuccess('OTP kodunuz email-ə göndərildi')
      setResetStep('otp')
    } catch (err: any) {
      setResetError(err.message || 'Xəta baş verdi')
    } finally {
      setResetLoading(false)
    }
  }

  const handleVerifyOtp = async () => {
    setResetError('')
    setResetLoading(true)

    try {
      await authService.verifyOtp({
        identifier: resetData.email,
        otpCode: resetData.otpCode,
        otpType: 'PASSWORD_RESET'
      })
      setResetStep('newPassword')
    } catch (err: any) {
      setResetError(err.message || 'OTP kodu yanlışdır')
    } finally {
      setResetLoading(false)
    }
  }

  const handleResetPassword = async () => {
    if (resetData.newPassword !== resetData.confirmPassword) {
      setResetError('Şifrələr uyğun gəlmir')
      return
    }

    setResetError('')
    setResetLoading(true)

    try {
      await authService.resetPassword({
        identifier: resetData.email,
        otpCode: resetData.otpCode,
        newPassword: resetData.newPassword,
        confirmPassword: resetData.confirmPassword
      })
      setResetSuccess('Şifrə uğurla yeniləndi! İndi giriş edə bilərsiniz.')
      setTimeout(() => {
        setShowResetPassword(false)
        setResetStep('email')
        setResetData({ email: '', otpCode: '', newPassword: '', confirmPassword: '' })
      }, 2000)
    } catch (err: any) {
      setResetError(err.message || 'Xəta baş verdi')
    } finally {
      setResetLoading(false)
    }
  }

  if (showResetPassword) {
    return (
      <div className="auth-page">
        <div className="container">
          <div className="auth-container">
            <div className="auth-header">
              <h1>{t.resetTitle}</h1>
            </div>
            <form className="auth-form" onSubmit={(e) => {
              e.preventDefault()
              if (resetStep === 'email') handleResetEmail()
              else if (resetStep === 'otp') handleVerifyOtp()
              else handleResetPassword()
            }}>
              {resetError && <div className="error-message">{resetError}</div>}
              {resetSuccess && <div className="success-message">{resetSuccess}</div>}
              
              {resetStep === 'email' && (
                <>
                  <div className="form-group">
                    <label htmlFor="resetEmail">{t.enterEmail}</label>
                    <input
                      type="email"
                      id="resetEmail"
                      value={resetData.email}
                      onChange={(e) => setResetData({ ...resetData, email: e.target.value })}
                      placeholder="email@example.com"
                      required
                    />
                  </div>
                  <button type="submit" className="btn btn-primary btn-full" disabled={resetLoading}>
                    {resetLoading ? 'Göndərilir...' : t.sendOtp}
                  </button>
                </>
              )}

              {resetStep === 'otp' && (
                <>
                  <div className="form-group">
                    <label htmlFor="otpCode">{t.enterOtp}</label>
                    <input
                      type="text"
                      id="otpCode"
                      value={resetData.otpCode}
                      onChange={(e) => setResetData({ ...resetData, otpCode: e.target.value })}
                      placeholder="6 rəqəmli kod"
                      maxLength={6}
                      required
                    />
                  </div>
                  <button type="submit" className="btn btn-primary btn-full" disabled={resetLoading}>
                    {resetLoading ? 'Təsdiqlənir...' : t.verifyOtp}
                  </button>
                </>
              )}

              {resetStep === 'newPassword' && (
                <>
                  <div className="form-group">
                    <label htmlFor="newPassword">{t.newPassword}</label>
                    <input
                      type="password"
                      id="newPassword"
                      value={resetData.newPassword}
                      onChange={(e) => setResetData({ ...resetData, newPassword: e.target.value })}
                      placeholder="Yeni şifrə"
                      required
                      minLength={8}
                    />
                  </div>
                  <div className="form-group">
                    <label htmlFor="confirmNewPassword">{t.confirmPassword}</label>
                    <input
                      type="password"
                      id="confirmNewPassword"
                      value={resetData.confirmPassword}
                      onChange={(e) => setResetData({ ...resetData, confirmPassword: e.target.value })}
                      placeholder="Şifrəni təkrar daxil edin"
                      required
                    />
                  </div>
                  <button type="submit" className="btn btn-primary btn-full" disabled={resetLoading}>
                    {resetLoading ? 'Sıfırlanır...' : t.resetButton}
                  </button>
                </>
              )}

              <button
                type="button"
                onClick={() => {
                  setShowResetPassword(false)
                  setResetStep('email')
                  setResetData({ email: '', otpCode: '', newPassword: '', confirmPassword: '' })
                }}
                className="btn btn-outline btn-full"
                style={{ marginTop: 'var(--spacing-sm)' }}
              >
                {t.backToLogin}
              </button>
            </form>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="auth-page">
      <div className="container">
        <div className="auth-container">
          <div className="auth-header">
            <h1>{t.title}</h1>
            <p>{t.subtitle}</p>
          </div>
          <form onSubmit={handleSubmit} className="auth-form">
            {error && <div className="error-message">{error}</div>}
            <div className="form-group">
              <label htmlFor="identifier">{t.identifier}</label>
              <input
                type="text"
                id="identifier"
                name="identifier"
                value={formData.identifier}
                onChange={handleChange}
                placeholder={t.identifier}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="password">{t.password}</label>
              <input
                type="password"
                id="password"
                name="password"
                value={formData.password}
                onChange={handleChange}
                placeholder={t.password}
                required
              />
            </div>
            <div className="form-group">
              <button
                type="button"
                onClick={() => setShowResetPassword(true)}
                className="reset-password-link"
              >
                {t.resetPassword}
              </button>
            </div>
            <div className="form-group">
              <label className="checkbox-label">
                <input
                  type="checkbox"
                  name="rememberMe"
                  checked={formData.rememberMe}
                  onChange={handleChange}
                />
                <span>{t.rememberMe}</span>
              </label>
            </div>
            <button type="submit" className="btn btn-primary btn-full" disabled={loading}>
              {loading ? t.loading : t.loginButton}
            </button>
            <p className="auth-footer">
              {t.noAccount} <Link to="/register">{t.signup}</Link>
            </p>
          </form>
        </div>
      </div>
    </div>
  )
}

export default Login
