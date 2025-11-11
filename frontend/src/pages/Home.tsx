import { Link } from 'react-router-dom'
import { useLanguage } from '../contexts/LanguageContext'
import './Home.css'

const Home = () => {
  const { language } = useLanguage()

  const content = {
    az: {
      title: 'SkillSwap-a Xoş Gəlmisiniz',
      subtitle: 'Pul yox, biliklə dəyişmə',
      description: 'Bacarıqlarınızı paylaşın, yeni bacarıqlar öyrənin və bir-birinizlə əlaqə saxlayın',
      motto: 'Bilik paylaş, bacarığını artır, yeni dostlar qazan.',
      startButton: 'Başla',
      loginButton: 'Giriş Et',
      whyTitle: 'Niyə SkillSwap?',
      whySubtitle: 'Pul ödəmədən bilik və bacarıq mübadiləsi',
      feature1Title: 'Bacarıq Mübadiləsi',
      feature1Desc: 'Öz bacarıqlarınızı paylaşın və başqalarından öyrənin',
      feature2Title: 'İctimaiyyət',
      feature2Desc: 'Eyni maraqları olan insanlarla tanış olun',
      feature3Title: 'Gamifikasiya',
      feature3Desc: 'Mükafatlar qazanın və səviyyənizi artırın',
      readyTitle: 'Hazırsınız?',
      readyDesc: 'İndi qoşulun və bacarıqlarınızı paylaşmağa başlayın',
      examplesTitle: 'Necə İşləyir?',
      example1Title: 'Java ↔ Photoshop',
      example1Desc: 'Sən Java öyrədirsən, qarşılığında biri sənə Photoshop dərsi verir',
      example2Title: 'Dizayn ↔ Marketing',
      example2Desc: 'Dizayner marketing öyrənir, qarşılığında logo hazırlayır',
      example3Title: 'Dil ↔ Proqramlaşdırma',
      example3Desc: 'İngilis dili müəllimi kod öyrənir, qarşılığında dil dərsi verir',
      example4Title: 'Musiqi ↔ Video',
      example4Desc: 'Musiqiçi video montaj öyrənir, qarşılığında musiqi yazır',
    },
    tr: {
      title: 'SkillSwap\'a Hoş Geldiniz',
      subtitle: 'Para değil, bilgiyle değiş',
      description: 'Yeteneklerinizi paylaşın, yeni yetenekler öğrenin ve birbirinizle iletişim kurun',
      motto: 'Bilgi paylaş, yeteneğini artır, yeni arkadaşlar kazan.',
      startButton: 'Başla',
      loginButton: 'Giriş Yap',
      whyTitle: 'Neden SkillSwap?',
      whySubtitle: 'Para ödemeden bilgi ve yetenek değişimi',
      feature1Title: 'Yetenek Değişimi',
      feature1Desc: 'Kendi yeteneklerinizi paylaşın ve başkalarından öğrenin',
      feature2Title: 'Topluluk',
      feature2Desc: 'Aynı ilgi alanlarına sahip insanlarla tanışın',
      feature3Title: 'Oyunlaştırma',
      feature3Desc: 'Ödüller kazanın ve seviyenizi artırın',
      readyTitle: 'Hazır mısınız?',
      readyDesc: 'Şimdi katılın ve yeteneklerinizi paylaşmaya başlayın',
      examplesTitle: 'Nasıl Çalışır?',
      example1Title: 'Java ↔ Photoshop',
      example1Desc: 'Sen Java öğretirsin, karşılığında biri sana Photoshop dersi verir',
      example2Title: 'Tasarım ↔ Pazarlama',
      example2Desc: 'Tasarımcı pazarlama öğrenir, karşılığında logo hazırlar',
      example3Title: 'Dil ↔ Programlama',
      example3Desc: 'İngilizce öğretmeni kod öğrenir, karşılığında dil dersi verir',
      example4Title: 'Müzik ↔ Video',
      example4Desc: 'Müzisyen video montaj öğrenir, karşılığında müzik yazar',
    },
    en: {
      title: 'Welcome to SkillSwap',
      subtitle: 'No money, exchange knowledge',
      description: 'Share your skills, learn new ones, and connect with others',
      motto: 'Share knowledge, grow your skills, make new friends.',
      startButton: 'Get Started',
      loginButton: 'Login',
      whyTitle: 'Why SkillSwap?',
      whySubtitle: 'Knowledge and skill exchange without payment',
      feature1Title: 'Skill Exchange',
      feature1Desc: 'Share your skills and learn from others',
      feature2Title: 'Community',
      feature2Desc: 'Meet people with similar interests',
      feature3Title: 'Gamification',
      feature3Desc: 'Earn rewards and level up',
      readyTitle: 'Ready?',
      readyDesc: 'Join now and start sharing your skills',
      examplesTitle: 'How It Works?',
      example1Title: 'Java ↔ Photoshop',
      example1Desc: 'You teach Java, in return someone teaches you Photoshop',
      example2Title: 'Design ↔ Marketing',
      example2Desc: 'Designer learns marketing, in return creates a logo',
      example3Title: 'Language ↔ Programming',
      example3Desc: 'English teacher learns coding, in return teaches language',
      example4Title: 'Music ↔ Video',
      example4Desc: 'Musician learns video editing, in return writes music',
    },
    ru: {
      title: 'Добро пожаловать в SkillSwap',
      subtitle: 'Не деньги, обмен знаниями',
      description: 'Делитесь своими навыками, изучайте новые и общайтесь с другими',
      motto: 'Делитесь знаниями, развивайте навыки, находите новых друзей.',
      startButton: 'Начать',
      loginButton: 'Войти',
      whyTitle: 'Почему SkillSwap?',
      whySubtitle: 'Обмен знаниями и навыками без оплаты',
      feature1Title: 'Обмен навыками',
      feature1Desc: 'Делитесь своими навыками и учитесь у других',
      feature2Title: 'Сообщество',
      feature2Desc: 'Знакомьтесь с людьми с похожими интересами',
      feature3Title: 'Геймификация',
      feature3Desc: 'Зарабатывайте награды и повышайте уровень',
      readyTitle: 'Готовы?',
      readyDesc: 'Присоединяйтесь сейчас и начните делиться своими навыками',
      examplesTitle: 'Как Это Работает?',
      example1Title: 'Java ↔ Photoshop',
      example1Desc: 'Вы преподаете Java, взамен кто-то учит вас Photoshop',
      example2Title: 'Дизайн ↔ Маркетинг',
      example2Desc: 'Дизайнер изучает маркетинг, взамен создает логотип',
      example3Title: 'Язык ↔ Программирование',
      example3Desc: 'Учитель английского изучает программирование, взамен преподает язык',
      example4Title: 'Музыка ↔ Видео',
      example4Desc: 'Музыкант изучает видеомонтаж, взамен пишет музыку',
    },
  }

  const text = content[language]

  return (
    <div className="home">
      <section className="hero">
        <div className="container">
          <div className="hero-layout">
            <div className="hero-text">
              <div className="hero-badge">
                <span className="badge-icon">💡</span>
                <span>{text.subtitle}</span>
              </div>
              <h1 className="hero-title">{text.title}</h1>
              <p className="hero-description">{text.description}</p>
              <p className="hero-motto">{text.motto}</p>
              <div className="hero-buttons">
                <Link to="/register" className="btn btn-primary btn-hero">
                  <span>{text.startButton}</span>
                  <span className="btn-icon">→</span>
                </Link>
                <Link to="/login" className="btn btn-secondary btn-hero">
                  {text.loginButton}
                </Link>
              </div>
            </div>
            <div className="hero-image">
              <div className="hero-illustration">
                <div className="illustration-card card-1">
                  <div className="card-icon">💻</div>
                  <div className="card-arrow">↔</div>
                  <div className="card-icon">🎨</div>
                </div>
                <div className="illustration-card card-2">
                  <div className="card-icon">📚</div>
                  <div className="card-arrow">↔</div>
                  <div className="card-icon">🚀</div>
                </div>
                <div className="illustration-card card-3">
                  <div className="card-icon">🎵</div>
                  <div className="card-arrow">↔</div>
                  <div className="card-icon">📹</div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div className="hero-particles">
          {[...Array(30)].map((_, i) => (
            <div key={i} className="particle" style={{ '--delay': `${i * 0.1}s`, '--duration': `${15 + (i % 10)}s` } as React.CSSProperties} />
          ))}
        </div>
      </section>

      <section className="examples-section">
        <div className="container">
          <h2 className="section-title">{text.examplesTitle}</h2>
          <p className="section-subtitle">{text.whySubtitle}</p>
          <div className="examples-grid">
            <div className="example-card">
              <div className="example-icons">
                <div className="example-icon icon-left">💻</div>
                <div className="example-arrow">↔</div>
                <div className="example-icon icon-right">🎨</div>
              </div>
              <h3>{text.example1Title}</h3>
              <p>{text.example1Desc}</p>
            </div>
            <div className="example-card">
              <div className="example-icons">
                <div className="example-icon icon-left">🎨</div>
                <div className="example-arrow">↔</div>
                <div className="example-icon icon-right">📊</div>
              </div>
              <h3>{text.example2Title}</h3>
              <p>{text.example2Desc}</p>
            </div>
            <div className="example-card">
              <div className="example-icons">
                <div className="example-icon icon-left">🌐</div>
                <div className="example-arrow">↔</div>
                <div className="example-icon icon-right">💻</div>
              </div>
              <h3>{text.example3Title}</h3>
              <p>{text.example3Desc}</p>
            </div>
            <div className="example-card">
              <div className="example-icons">
                <div className="example-icon icon-left">🎵</div>
                <div className="example-arrow">↔</div>
                <div className="example-icon icon-right">📹</div>
              </div>
              <h3>{text.example4Title}</h3>
              <p>{text.example4Desc}</p>
            </div>
          </div>
        </div>
      </section>

      <section className="features">
        <div className="container">
          <h2 className="section-title">{text.whyTitle}</h2>
          <p className="section-subtitle">{text.whySubtitle}</p>
          <div className="features-grid">
            <div className="feature-card">
              <div className="feature-icon-wrapper">
                <div className="feature-icon">🔄</div>
              </div>
              <h3>{text.feature1Title}</h3>
              <p>{text.feature1Desc}</p>
            </div>
            <div className="feature-card">
              <div className="feature-icon-wrapper">
                <div className="feature-icon">👥</div>
              </div>
              <h3>{text.feature2Title}</h3>
              <p>{text.feature2Desc}</p>
            </div>
            <div className="feature-card">
              <div className="feature-icon-wrapper">
                <div className="feature-icon">🏆</div>
              </div>
              <h3>{text.feature3Title}</h3>
              <p>{text.feature3Desc}</p>
            </div>
          </div>
        </div>
      </section>

      <section className="cta-section">
        <div className="container">
          <div className="cta-content">
            <h2>{text.readyTitle}</h2>
            <p>{text.readyDesc}</p>
            <Link to="/register" className="btn btn-primary btn-large">
              {text.startButton}
            </Link>
          </div>
        </div>
      </section>
    </div>
  )
}

export default Home
