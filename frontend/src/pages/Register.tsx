import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { useLanguage } from '../contexts/LanguageContext'
import { authService } from '../services/authService'
import './Auth.css'

const Register = () => {
  const { signup } = useAuth()
  const { language } = useLanguage()
  const [step, setStep] = useState<'signup' | 'otp'>('signup')
  const [formData, setFormData] = useState({
    username: '',
    fin: '',
    password: '',
    confirmPassword: '',
    email: '',
    phone: '',
    role: 'USER' as 'USER' | 'ADMIN' | 'PROVIDER'
  })
  const [otpCode, setOtpCode] = useState('')
  const [error, setError] = useState<string>('')
  const [success, setSuccess] = useState<string>('')
  const [loading, setLoading] = useState(false)

  const t = {
    az: {
      title: 'Qeydiyyat',
      subtitle: 'Yeni hesab yaradın',
      username: 'İstifadəçi adı',
      fin: 'FIN',
      email: 'Email',
      phone: 'Telefon',
      password: 'Şifrə',
      confirmPassword: 'Şifrəni Təsdiqlə',
      role: 'Rol',
      signupButton: 'Qeydiyyatdan Keç',
      loading: 'Qeydiyyat edilir...',
      hasAccount: 'Artıq hesabınız var?',
      login: 'Giriş edin',
      otpTitle: 'Email Təsdiqlə',
      otpSubtitle: 'Email-ə göndərilən OTP kodunu daxil edin',
      enterOtp: 'OTP kodu',
      verifyButton: 'Təsdiqlə',
      verifying: 'Təsdiqlənir...',
      resendOtp: 'OTP-ni yenidən göndər',
    },
    tr: {
      title: 'Kayıt',
      subtitle: 'Yeni hesap oluşturun',
      username: 'Kullanıcı adı',
      fin: 'FIN',
      email: 'Email',
      phone: 'Telefon',
      password: 'Şifre',
      confirmPassword: 'Şifreyi Onayla',
      role: 'Rol',
      signupButton: 'Kayıt Ol',
      loading: 'Kayıt yapılıyor...',
      hasAccount: 'Zaten hesabınız var mı?',
      login: 'Giriş yapın',
      otpTitle: 'Email Doğrula',
      otpSubtitle: 'Email\'e gönderilen OTP kodunu girin',
      enterOtp: 'OTP kodu',
      verifyButton: 'Doğrula',
      verifying: 'Doğrulanıyor...',
      resendOtp: 'OTP\'yi yeniden gönder',
    },
    en: {
      title: 'Sign Up',
      subtitle: 'Create a new account',
      username: 'Username',
      fin: 'FIN',
      email: 'Email',
      phone: 'Telefon',
      password: 'Password',
      confirmPassword: 'Confirm Password',
      role: 'Role',
      signupButton: 'Sign Up',
      loading: 'Signing up...',
      hasAccount: 'Already have an account?',
      login: 'Login',
      otpTitle: 'Verify Email',
      otpSubtitle: 'Enter the OTP code sent to your email',
      enterOtp: 'OTP code',
      verifyButton: 'Verify',
      verifying: 'Verifying...',
      resendOtp: 'Resend OTP',
    },
    ru: {
      title: 'Регистрация',
      subtitle: 'Создайте новый аккаунт',
      username: 'Имя пользователя',
      fin: 'FIN',
      email: 'Email',
      phone: 'Телефон',
      password: 'Пароль',
      confirmPassword: 'Подтвердите пароль',
      role: 'Роль',
      signupButton: 'Зарегистрироваться',
      loading: 'Регистрация...',
      hasAccount: 'Уже есть аккаунт?',
      login: 'Войти',
      otpTitle: 'Подтвердите Email',
      otpSubtitle: 'Введите код OTP, отправленный на ваш email',
      enterOtp: 'OTP код',
      verifyButton: 'Подтвердить',
      verifying: 'Подтверждение...',
      resendOtp: 'Отправить OTP снова',
    },
  }[language]

  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value } = e.target
    setFormData({
      ...formData,
      [name]: value
    })
    setError('')
    setSuccess('')
  }

  const handleSignup = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setSuccess('')
    setLoading(true)

    // Validation
    if (formData.password !== formData.confirmPassword) {
      setError('Şifrələr uyğun gəlmir')
      setLoading(false)
      return
    }

    if (!formData.email && !formData.phone) {
      setError('Email və ya telefon nömrəsi daxil edilməlidir')
      setLoading(false)
      return
    }

    try {
      await signup(formData)
      // After signup, send OTP
      const identifier = formData.email || formData.phone || formData.fin
      if (identifier) {
        try {
          await authService.sendOtp({
            identifier: identifier,
            sendMethod: formData.email ? 'email' : 'phone',
            otpType: 'ACCOUNT_CONFIRMATION'
          })
          setStep('otp')
          setSuccess(
            formData.email 
              ? 'OTP kodunuz email-ə göndərildi' 
              : 'OTP kodunuz telefon nömrəsinə göndərildi'
          )
        } catch (otpErr: any) {
          // If OTP send fails, still show success for signup but with warning
          setError(otpErr.message || 'Qeydiyyat uğurlu oldu, amma OTP göndərilə bilmədi. Zəhmət olmasa giriş edin və OTP tələb edin.')
        }
      } else {
        setError('Email və ya telefon nömrəsi daxil edilməlidir')
      }
    } catch (err: any) {
      setError(err.message || 'Qeydiyyat zamanı xəta baş verdi')
    } finally {
      setLoading(false)
    }
  }

  const handleVerifyOtp = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      const identifier = formData.email || formData.phone || ''
      await authService.verifyOtp({
        identifier,
        otpCode,
        otpType: 'ACCOUNT_CONFIRMATION'
      })
      setSuccess('Hesabınız uğurla təsdiqləndi! İndi giriş edə bilərsiniz.')
      // Redirect to login after 2 seconds
      setTimeout(() => {
        window.location.href = '/login'
      }, 2000)
    } catch (err: any) {
      setError(err.message || 'OTP kodu yanlışdır')
    } finally {
      setLoading(false)
    }
  }

  const handleResendOtp = async () => {
    setError('')
    setLoading(true)

    try {
      const identifier = formData.email || formData.phone || ''
      await authService.sendOtp({
        identifier,
        sendMethod: formData.email ? 'email' : 'phone',
        otpType: 'ACCOUNT_CONFIRMATION'
      })
      setSuccess('OTP kodunuz yenidən göndərildi')
    } catch (err: any) {
      setError(err.message || 'Xəta baş verdi')
    } finally {
      setLoading(false)
    }
  }

  if (step === 'otp') {
    return (
      <div className="auth-page">
        <div className="container">
          <div className="auth-container">
            <div className="auth-header">
              <h1>{t.otpTitle}</h1>
              <p>{t.otpSubtitle}</p>
            </div>
            <form onSubmit={handleVerifyOtp} className="auth-form">
              {error && <div className="error-message">{error}</div>}
              {success && <div className="success-message">{success}</div>}
              <div className="form-group">
                <label htmlFor="otpCode">{t.enterOtp}</label>
                <input
                  type="text"
                  id="otpCode"
                  value={otpCode}
                  onChange={(e) => setOtpCode(e.target.value)}
                  placeholder="6 rəqəmli kod"
                  maxLength={6}
                  required
                />
              </div>
              <button type="submit" className="btn btn-primary btn-full" disabled={loading}>
                {loading ? t.verifying : t.verifyButton}
              </button>
              <button
                type="button"
                onClick={handleResendOtp}
                className="btn btn-outline btn-full"
                style={{ marginTop: 'var(--spacing-sm)' }}
                disabled={loading}
              >
                {t.resendOtp}
              </button>
              <p className="auth-footer">
                {t.hasAccount} <Link to="/login">{t.login}</Link>
              </p>
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
          <form onSubmit={handleSignup} className="auth-form">
            {error && <div className="error-message">{error}</div>}
            {success && <div className="success-message">{success}</div>}
            <div className="form-group">
              <label htmlFor="username">{t.username}</label>
              <input
                type="text"
                id="username"
                name="username"
                value={formData.username}
                onChange={handleChange}
                placeholder={t.username + ' (3-50 simvol)'}
                required
                minLength={3}
                maxLength={50}
              />
            </div>
            <div className="form-group">
              <label htmlFor="fin">{t.fin}</label>
              <input
                type="text"
                id="fin"
                name="fin"
                value={formData.fin}
                onChange={handleChange}
                placeholder={t.fin + ' (7 simvol)'}
                required
                minLength={7}
                maxLength={7}
                pattern="[0-9A-Z]{7}"
                style={{ textTransform: 'uppercase' }}
              />
            </div>
            <div className="form-group">
              <label htmlFor="email">{t.email}</label>
              <input
                type="email"
                id="email"
                name="email"
                value={formData.email}
                onChange={handleChange}
                placeholder="email@example.com"
              />
            </div>
            <div className="form-group">
              <label htmlFor="phone">{t.phone}</label>
              <input
                type="tel"
                id="phone"
                name="phone"
                value={formData.phone}
                onChange={handleChange}
                placeholder="Telefon nömrəsi"
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
                placeholder={t.password + ' (min 8 simvol)'}
                required
                minLength={8}
              />
              <small className="form-hint">
                Şifrə ən azı 8 simvol, böyük hərf, kiçik hərf, rəqəm və xüsusi simvol ehtiva etməlidir
              </small>
            </div>
            <div className="form-group">
              <label htmlFor="confirmPassword">{t.confirmPassword}</label>
              <input
                type="password"
                id="confirmPassword"
                name="confirmPassword"
                value={formData.confirmPassword}
                onChange={handleChange}
                placeholder={t.confirmPassword}
                required
              />
            </div>
            <button type="submit" className="btn btn-primary btn-full" disabled={loading}>
              {loading ? t.loading : t.signupButton}
            </button>
            <p className="auth-footer">
              {t.hasAccount} <Link to="/login">{t.login}</Link>
            </p>
          </form>
        </div>
      </div>
    </div>
  )
}

export default Register
