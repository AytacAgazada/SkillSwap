import './Footer.css'

const Footer = () => {
  return (
    <footer className="footer">
      <div className="container">
        <div className="footer-content">
          <div className="footer-section">
            <h3>SkillSwap</h3>
            <p>Bacarıqlarınızı paylaşın, yeni bacarıqlar öyrənin</p>
          </div>
          <div className="footer-section">
            <h4>Keçidlər</h4>
            <ul>
              <li><a href="/">Ana Səhifə</a></li>
              <li><a href="/login">Giriş</a></li>
              <li><a href="/register">Qeydiyyat</a></li>
            </ul>
          </div>
          <div className="footer-section">
            <h4>Əlaqə</h4>
            <p>info@skillswap.az</p>
          </div>
        </div>
        <div className="footer-bottom">
          <p>&copy; 2024 SkillSwap. Bütün hüquqlar qorunur.</p>
        </div>
      </div>
    </footer>
  )
}

export default Footer

