import { Link, Navigate } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { useLanguage } from '../contexts/LanguageContext'
import './Home.css'

const Home = () => {
  const { isAuthenticated } = useAuth()
  const { language } = useLanguage()

  if (isAuthenticated) {
    return <Navigate to="/dashboard" />
  }

  const content = {
    az: {
      title: 'SkillSwap – 🚀 Bilik və Bacarıq Mübadilə Platforması',
      subtitle: 'Fərqlilik: Pul yox, biliklə dəyişmə 💡',
      description:
        'SkillSwap — insanlar arasında bilik mübadiləsini mümkün edən bir platformadır. Pul ödəmək əvəzinə, insanlar öz bilik və bacarıqları ilə başqasına xidmət edir, qarşılığında da başqa sahədə öyrənmək şansı əldə edir.',
      example: 'Məsələn: Sən Java öyrədirsən ☕, qarşılığında biri sənə Photoshop dərsi verir 🎨. Dizayner marketing öyrənir 📈, qarşılığında logo hazırlayır 🖼️.',
      quote1: '“Pul dövründə yaşasaq da, SkillSwap insanları bir-birinə bacarıqla yaxınlaşdırır. Mən burada həm sosial problem həll edirəm, həm də texniki baxımdan real-time, geo-based, gamified platforma qururam.”',
      quote2: '“SkillSwap artıq sadəcə bilik mübadiləsi deyil — bu, insanların problem həll etdiyi, sahələr üzrə qruplar qurduğu və real-time mentorship aldığı bir ekosistemdir. Burada texniki olaraq real-time, geo-based, AI-powered matching və gamified community funksiyaları birləşdirilib.”',
      startButton: 'İndi Başla! 🎉',
      ctaTitle: 'Hərəkatə Qoşulun! 🌟',
      ctaSubtitle: 'Bu gün qeydiyyatdan keçin və biliklərinizi paylaşmağa başlayın!',
      feature1Title: '🤝 Bilik Mübadiləsi',
      feature1Desc: 'Öz bilik və bacarıqlarınızı paylaşaraq başqalarından öyrənin.',
      feature2Title: '💸 Qarşılıqlı Fayda',
      feature2Desc: 'Pul ödəmədən, yalnız biliklərinizi dəyişərək yeni bacarıqlar qazanın.',
      feature3Title: '🏆 İnkişaf',
      feature3Desc: 'Həm öyrənərək, həm də öyrədərək öz sahənizdə daha da peşəkarlaşın.',
    },
    // Add other languages if needed
  }

  const t = content[language] || content['az'] // Default to Azerbaijani

  return (
    <div className="home-container">
      <header className="home-header">
        <div className="container">
          <h1 className="home-title">{t.title}</h1>
          <p className="home-subtitle">{t.subtitle}</p>
        </div>
      </header>

      <div className="container">
        <main>
          <section className="description-section card animate-fade-in">
            <h2>Layihənin Geniş Təsviri</h2>
            <p>{t.description}</p>
            <p className="example-text">{t.example}</p>
          </section>

          <section className="features-section">
            <div className="feature-item card animate-slide-in-left">
              <h3>{t.feature1Title}</h3>
              <p>{t.feature1Desc}</p>
            </div>
            <div className="feature-item card animate-fade-in">
              <h3>{t.feature2Title}</h3>
              <p>{t.feature2Desc}</p>
            </div>
            <div className="feature-item card animate-slide-in-right">
              <h3>{t.feature3Title}</h3>
              <p>{t.feature3Desc}</p>
            </div>
          </section>

          <section className="quotes-section">
            <div className="quote-card card animate-slide-in-left">
              <p>{t.quote1}</p>
            </div>
            <div className="quote-card card animate-slide-in-right">
              <p>{t.quote2}</p>
            </div>
          </section>

          <section className="cta-section card animate-fade-in">
            <h2>{t.ctaTitle}</h2>
            <p>{t.ctaSubtitle}</p>
            <Link to="/register" className="btn btn-primary btn-large">
              {t.startButton}
            </Link>
          </section>
        </main>
      </div>
    </div>
  )
}

export default Home