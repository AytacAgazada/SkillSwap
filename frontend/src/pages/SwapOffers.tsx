import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { swapService, type SwapOffer, type SwapOfferResponse } from '../services/swapService'
import './SwapOffers.css'

type ViewMode = 'my-offers' | 'all-offers' | 'search'

const SwapOffers = () => {
  const { user } = useAuth()
  const [offers, setOffers] = useState<SwapOfferResponse[]>([])
  const [myOffers, setMyOffers] = useState<SwapOfferResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string>('')
  const [success, setSuccess] = useState<string>('')
  const [showForm, setShowForm] = useState(false)
  const [editingOffer, setEditingOffer] = useState<SwapOfferResponse | null>(null)
  const [searchSkill, setSearchSkill] = useState('')
  const [viewMode, setViewMode] = useState<ViewMode>('all-offers')
  const [userLocation, setUserLocation] = useState<{ lat: number; lon: number } | null>(null)

  const [formData, setFormData] = useState<SwapOffer>({
    skillOffered: '',
    skillRequested: '',
    meetingType: 'BOTH',
    description: '',
    latitude: 0,
    longitude: 0
  })

  useEffect(() => {
    loadMyOffers()
    getUserLocation()
    loadAllOffers() // İlk yükləmədə də çağırılır
  }, [])

  useEffect(() => {
    if (viewMode === 'my-offers') {
      setOffers(myOffers)
    } else if (viewMode === 'all-offers') {
      loadAllOffers() // Tab dəyişəndə yenidən çağırılır
    }
  }, [viewMode, myOffers])

  const getUserLocation = () => {
    if (navigator.geolocation) {
      navigator.geolocation.getCurrentPosition(
          (position) => {
            setUserLocation({
              lat: position.coords.latitude,
              lon: position.coords.longitude
            })
          },
          () => {
            setUserLocation({ lat: 40.4093, lon: 49.8671 })
          }
      )
    } else {
      setUserLocation({ lat: 40.4093, lon: 49.8671 })
    }
  }

  const loadMyOffers = async () => {
    try {
      const offers = await swapService.getMyOffers()
      setMyOffers(offers)
      if (viewMode === 'my-offers') {
        setOffers(offers)
      }
    } catch (err: any) {
      setError(err.message || 'Xəta baş verdi')
    } finally {
      setLoading(false)
    }
  }

  // 👇 DÜZƏLDİLMİŞ METOD: Bütün aktiv təklifləri birbaşa gətirir
  const loadAllOffers = async () => {
    // Burada userLocation-u yoxlamağa ehtiyac yoxdur, çünki bu artıq yerə bağlı axtarış deyil.
    // Lakin, əgər istəyirsinizsə, aşağıdakı hissəni saxlaya bilərsiniz.
    // if (!userLocation) return

    try {
      setLoading(true)

      // ✅ İndi swapService.ts-də əlavə etdiyimiz yeni metodu çağırırıq
      const allOffers = await swapService.getAllOffers()

      // İstifadəçinin öz təkliflərini filtrləyirik
      const filteredOffers = allOffers.filter(offer => offer.userId !== user?.id)
      setOffers(filteredOffers)
    } catch (err: any) {
      setError(err.message || 'Bütün təklifləri gətirərkən xəta baş verdi')
    } finally {
      setLoading(false)
    }
  }
  // 👆 DÜZƏLİŞİN SONU


  const handleChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target
    setFormData(prev => ({
      ...prev,
      [name]: name === 'latitude' || name === 'longitude' ? parseFloat(value) || 0 : value
    }))
    setError('')
  }

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setSuccess('')
    setSaving(true)

    try {
      if (editingOffer) {
        setError('Yeniləmə funksiyası hazırda mövcud deyil')
      } else {
        await swapService.createOffer({
          ...formData,
          latitude: userLocation?.lat || formData.latitude,
          longitude: userLocation?.lon || formData.longitude
        })
        setSuccess('Təklif uğurla yaradıldı!')
        await loadMyOffers()
        await loadAllOffers()
        setShowForm(false)
        setFormData({
          skillOffered: '',
          skillRequested: '',
          meetingType: 'BOTH',
          description: '',
          latitude: userLocation?.lat || 0,
          longitude: userLocation?.lon || 0
        })
      }
    } catch (err: any) {
      setError(err.message || 'Xəta baş verdi')
    } finally {
      setSaving(false)
    }
  }

  const handleDeactivate = async (offerId: number) => {
    if (!confirm('Bu təklifi deaktiv etmək istədiyinizə əminsiniz?')) return

    try {
      // Backend doesn't have deactivate endpoint, so we'll handle it locally
      // In a real implementation, you'd call an API endpoint
      setMyOffers(prev => prev.filter(o => o.id !== offerId))
      setOffers(prev => prev.filter(o => o.id !== offerId))
      setSuccess('Təklif deaktiv edildi')

      // TODO: Call backend API when available
      // await swapService.deactivateOffer(offerId)
    } catch (err: any) {
      setError(err.message || 'Xəta baş verdi')
    }
  }

  const handleSearch = async () => {
    if (!searchSkill || !userLocation) return

    setLoading(true)
    setViewMode('search')
    try {
      const results = await swapService.searchOffers(searchSkill, userLocation.lat, userLocation.lon, 50)
      // Filter out user's own offers from search results
      const filteredResults = results.filter(offer => offer.userId !== user?.id)
      setOffers(filteredResults)
    } catch (err: any) {
      setError(err.message || 'Axtarış zamanı xəta baş verdi')
    } finally {
      setLoading(false)
    }
  }

  if (loading && offers.length === 0 && viewMode !== 'search') {
    return <div className="swap-loading">Yüklənir...</div>
  }

  return (
      <div className="swap-offers-page">
        <div className="container">
          <div className="swap-header">
            <h1>Bacarıq Mübadiləsi Təklifləri</h1>
            <button
                onClick={() => {
                  setShowForm(!showForm)
                  setEditingOffer(null)
                  setFormData({
                    skillOffered: '',
                    skillRequested: '',
                    meetingType: 'BOTH',
                    description: '',
                    latitude: userLocation?.lat || 0,
                    longitude: userLocation?.lon || 0
                  })
                }}
                className="btn btn-primary"
            >
              {showForm ? 'Ləğv et' : 'Yeni Təklif'}
            </button>
          </div>

          {error && <div className="error-message">{error}</div>}
          {success && <div className="success-message">{success}</div>}

          <div className="view-tabs">
            <button
                className={`tab-btn ${viewMode === 'all-offers' ? 'active' : ''}`}
                onClick={() => setViewMode('all-offers')}
            >
              Bütün Təkliflər
            </button>
            <button
                className={`tab-btn ${viewMode === 'my-offers' ? 'active' : ''}`}
                onClick={() => setViewMode('my-offers')}
            >
              Mənim Təkliflərim
            </button>
          </div>

          <div className="search-section">
            <div className="search-box">
              <input
                  type="text"
                  placeholder="Bacarıq adı ilə axtar..."
                  value={searchSkill}
                  onChange={(e) => setSearchSkill(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && handleSearch()}
              />
              <button onClick={handleSearch} className="btn btn-secondary">
                Axtar
              </button>
              {viewMode === 'search' && (
                  <button onClick={() => setViewMode('all-offers')} className="btn btn-outline">
                    Axtarışı Təmizlə
                  </button>
              )}
            </div>
          </div>

          {showForm && (
              <form onSubmit={handleSubmit} className="swap-form">
                <h2>{editingOffer ? 'Təklifi Yenilə' : 'Yeni Təklif Yarat'}</h2>

                <div className="form-row">
                  <div className="form-group">
                    <label htmlFor="skillOffered">Təklif Etdiyiniz Bacarıq *</label>
                    <input
                        type="text"
                        id="skillOffered"
                        name="skillOffered"
                        value={formData.skillOffered}
                        onChange={handleChange}
                        placeholder="Məsələn: İngilis dili"
                        required
                    />
                  </div>
                  <div className="form-group">
                    <label htmlFor="skillRequested">İstədiyiniz Bacarıq *</label>
                    <input
                        type="text"
                        id="skillRequested"
                        name="skillRequested"
                        value={formData.skillRequested}
                        onChange={handleChange}
                        placeholder="Məsələn: Proqramlaşdırma"
                        required
                    />
                  </div>
                </div>

                <div className="form-group">
                  <label htmlFor="meetingType">Görüş Tipi *</label>
                  <select
                      id="meetingType"
                      name="meetingType"
                      value={formData.meetingType}
                      onChange={handleChange}
                      required
                  >
                    <option value="PHYSICAL">Fiziki</option>
                    <option value="ONLINE">Online</option>
                    <option value="BOTH">Hər ikisi</option>
                  </select>
                </div>

                <div className="form-group">
                  <label htmlFor="description">Təsvir *</label>
                  <textarea
                      id="description"
                      name="description"
                      value={formData.description}
                      onChange={handleChange}
                      rows={4}
                      placeholder="Təklifiniz haqqında ətraflı məlumat"
                      required
                  />
                </div>

                <div className="form-row">
                  <div className="form-group">
                    <label htmlFor="latitude">Enlik (Latitude)</label>
                    <input
                        type="number"
                        id="latitude"
                        name="latitude"
                        value={formData.latitude}
                        onChange={handleChange}
                        step="0.000001"
                        placeholder="40.4093"
                    />
                    <small className="form-hint">Boş buraxsanız, avtomatik olaraq mövqeniz istifadə olunacaq</small>
                  </div>
                  <div className="form-group">
                    <label htmlFor="longitude">Uzunluq (Longitude)</label>
                    <input
                        type="number"
                        id="longitude"
                        name="longitude"
                        value={formData.longitude}
                        onChange={handleChange}
                        step="0.000001"
                        placeholder="49.8671"
                    />
                  </div>
                </div>

                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? 'Saxlanılır...' : editingOffer ? 'Yenilə' : 'Yarat'}
                </button>
              </form>
          )}

          <div className="offers-list">
            {offers.length === 0 ? (
                <div className="no-offers">
                  {viewMode === 'my-offers'
                      ? 'Hələ heç bir təklif yaratmamısınız'
                      : viewMode === 'search'
                          ? 'Axtarış nəticəsi tapılmadı'
                          : 'Hələ heç bir təklif yoxdur'}
                </div>
            ) : (
                offers.map(offer => (
                    <div key={offer.id} className={`offer-card ${offer.userId === user?.id ? 'my-offer' : ''}`}>
                      <div className="offer-header">
                        <h3>{offer.skillOffered} ↔ {offer.skillRequested}</h3>
                        {offer.userId === user?.id && (
                            <div className="offer-status-badge">
                              <span className="badge">Mənim təklifim</span>
                            </div>
                        )}
                      </div>
                      <div className="offer-details">
                        <p className="offer-description">{offer.description}</p>
                        <div className="offer-meta">
                    <span className="meeting-type">
                      {offer.meetingType === 'PHYSICAL' && '📍 Fiziki'}
                      {offer.meetingType === 'ONLINE' && '💻 Online'}
                      {offer.meetingType === 'BOTH' && '📍💻 Hər ikisi'}
                    </span>
                          {offer.latitude && offer.longitude && (
                              <span className="location">
                        📍 {offer.latitude.toFixed(4)}, {offer.longitude.toFixed(4)}
                      </span>
                          )}
                        </div>
                        <div className="offer-actions">
                          {offer.userId === user?.id ? (
                              <button
                                  onClick={() => handleDeactivate(offer.id)}
                                  className="btn btn-outline btn-sm"
                                  title="Təklifi deaktiv et"
                              >
                                Keçərli Deyil
                              </button>
                          ) : (
                              <Link
                                  to="/chat"
                                  className="btn btn-primary btn-sm"
                                  onClick={() => {
                                    localStorage.setItem('selectedSwapId', offer.id.toString())
                                    localStorage.setItem('selectedUserId', offer.userId)
                                  }}
                              >
                                💬 Chat
                              </Link>
                          )}
                        </div>
                      </div>
                    </div>
                ))
            )}
          </div>
        </div>
      </div>
  )
}

export default SwapOffers