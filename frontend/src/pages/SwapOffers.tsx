import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../contexts/AuthContext'
import { swapService, type SwapOffer, type SwapOfferResponse } from '../services/swapService'
import { skillService, type SkillResponse } from '../services/skillService'
import './SwapOffers.css'

const SwapOffers = () => {
  const { user } = useAuth()
  const [offers, setOffers] = useState<SwapOfferResponse[]>([])
  const [skills, setSkills] = useState<SkillResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string>('')
  const [success, setSuccess] = useState<string>('')
  const [showForm, setShowForm] = useState(false)
  const [editingOffer, setEditingOffer] = useState<SwapOfferResponse | null>(null)
  const [searchSkill, setSearchSkill] = useState('')
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
    loadSkills()
    getUserLocation()
  }, [])

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
          // Default to Baku coordinates if geolocation fails
          setUserLocation({ lat: 40.4093, lon: 49.8671 })
        }
      )
    } else {
      setUserLocation({ lat: 40.4093, lon: 49.8671 })
    }
  }

  const loadMyOffers = async () => {
    try {
      const myOffers = await swapService.getMyOffers()
      setOffers(myOffers)
    } catch (err: any) {
      setError(err.message || 'Xəta baş verdi')
    } finally {
      setLoading(false)
    }
  }

  const loadSkills = async () => {
    try {
      const allSkills = await skillService.getAllSkills()
      setSkills(allSkills)
    } catch (err: any) {
      console.error('Error loading skills:', err)
    }
  }

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
        // Update not supported by backend, would need to delete and recreate
        setError('Yeniləmə funksiyası hazırda mövcud deyil')
      } else {
        await swapService.createOffer({
          ...formData,
          latitude: userLocation?.lat || formData.latitude,
          longitude: userLocation?.lon || formData.longitude
        })
        setSuccess('Təklif uğurla yaradıldı!')
        await loadMyOffers()
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

  const handleDelete = async (offerId: number) => {
    if (!confirm('Bu təklifi silmək istədiyinizə əminsiniz?')) return

    try {
      // Backend doesn't have delete endpoint, would need to be added
      setError('Silinmə funksiyası hazırda mövcud deyil')
    } catch (err: any) {
      setError(err.message || 'Xəta baş verdi')
    }
  }

  const handleSearch = async () => {
    if (!searchSkill || !userLocation) return

    setLoading(true)
    try {
      const results = await swapService.searchOffers(searchSkill, userLocation.lat, userLocation.lon, 10)
      setOffers(results)
    } catch (err: any) {
      setError(err.message || 'Axtarış zamanı xəta baş verdi')
    } finally {
      setLoading(false)
    }
  }

  if (loading && offers.length === 0) {
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
            <button onClick={loadMyOffers} className="btn btn-outline">
              Mənim Təkliflərim
            </button>
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
            <div className="no-offers">Hələ heç bir təklif yoxdur</div>
          ) : (
            offers.map(offer => (
              <div key={offer.id} className="offer-card">
                <div className="offer-header">
                  <h3>{offer.skillOffered} ↔ {offer.skillRequested}</h3>
                  {offer.userId === user?.id && (
                    <div className="offer-actions">
                      <button
                        onClick={() => handleDelete(offer.id)}
                        className="btn btn-outline btn-sm"
                      >
                        Sil
                      </button>
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
                  {offer.userId !== user?.id && (
                    <div className="offer-actions">
                      <Link
                        to="/chat"
                        className="btn btn-primary btn-sm"
                        onClick={() => {
                          // This will be handled by chat page to select the chat
                          localStorage.setItem('selectedSwapId', offer.id.toString())
                        }}
                      >
                        Chat
                      </Link>
                    </div>
                  )}
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

