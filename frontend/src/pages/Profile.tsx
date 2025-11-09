import { useState, useEffect } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { userBioService, type UserBio, type UserBioResponse } from '../services/userBioService'
import { skillService, type Skill, type SkillResponse } from '../services/skillService'
import './Profile.css'

const Profile = () => {
  const { user } = useAuth()
  const [userBio, setUserBio] = useState<UserBioResponse | null>(null)
  const [skills, setSkills] = useState<SkillResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string>('')
  const [success, setSuccess] = useState<string>('')
  const [showSkillForm, setShowSkillForm] = useState(false)
  const [editingSkill, setEditingSkill] = useState<SkillResponse | null>(null)

  const [formData, setFormData] = useState<UserBio>({
    authUserId: user?.id || '',
    firstName: '',
    lastName: '',
    education: '',
    skillIds: [],
    phone: '',
    jobTitle: '',
    yearsOfExperience: undefined,
    linkedInProfileUrl: '',
    bio: ''
  })

  const [skillForm, setSkillForm] = useState<Skill>({
    name: '',
    description: '',
    level: 'BEGINNER'
  })

  useEffect(() => {
    loadProfile()
    loadAllSkills()
  }, [])

  const loadProfile = async () => {
    try {
      if (user?.id) {
        const bio = await userBioService.getUserBioByAuthUserId(user.id)
        setUserBio(bio)
        setFormData({
          authUserId: user.id,
          firstName: bio.firstName || '',
          lastName: bio.lastName || '',
          education: bio.education || '',
          skillIds: bio.skills?.map(s => s.id) || [],
          phone: bio.phone || '',
          jobTitle: bio.jobTitle || '',
          yearsOfExperience: bio.yearsOfExperience,
          linkedInProfileUrl: bio.linkedInProfileUrl || '',
          bio: bio.bio || ''
        })
      }
    } catch (err: any) {
      // Profile doesn't exist yet, that's okay
      if (user?.id) {
        setFormData(prev => ({ ...prev, authUserId: user.id }))
      }
    } finally {
      setLoading(false)
    }
  }

  const loadAllSkills = async () => {
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
      [name]: name === 'yearsOfExperience' ? (value ? parseInt(value) : undefined) : value
    }))
    setError('')
  }

  const handleSkillChange = (e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement | HTMLSelectElement>) => {
    const { name, value } = e.target
    setSkillForm(prev => ({
      ...prev,
      [name]: value
    }))
  }

  const handleSkillSelect = (skillId: number) => {
    setFormData(prev => ({
      ...prev,
      skillIds: prev.skillIds?.includes(skillId)
        ? prev.skillIds.filter(id => id !== skillId)
        : [...(prev.skillIds || []), skillId]
    }))
  }

  const handleSave = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setSuccess('')
    setSaving(true)

    try {
      if (userBio) {
        // Update existing profile
        await userBioService.updateUserBio({
          ...formData,
          id: userBio.id
        })
      } else {
        // Create new profile
        await userBioService.createUserBio(formData)
      }
      setSuccess('Profil uğurla saxlanıldı!')
      await loadProfile()
    } catch (err: any) {
      setError(err.message || 'Xəta baş verdi')
    } finally {
      setSaving(false)
    }
  }

  const handleCreateSkill = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setSaving(true)

    try {
      if (editingSkill) {
        await skillService.updateSkill({
          ...skillForm,
          id: editingSkill.id
        })
        setSuccess('Bacarıq uğurla yeniləndi!')
      } else {
        await skillService.createSkill(skillForm)
        setSuccess('Bacarıq uğurla yaradıldı!')
      }
      await loadAllSkills()
      setShowSkillForm(false)
      setEditingSkill(null)
      setSkillForm({ name: '', description: '', level: 'BEGINNER' })
    } catch (err: any) {
      setError(err.message || 'Xəta baş verdi')
    } finally {
      setSaving(false)
    }
  }

  const handleEditSkill = (skill: SkillResponse) => {
    setEditingSkill(skill)
    setSkillForm({
      name: skill.name,
      description: skill.description || '',
      level: skill.level
    })
    setShowSkillForm(true)
  }

  const handleDeleteSkill = async (skillId: number) => {
    if (!confirm('Bu bacarığı silmək istədiyinizə əminsiniz?')) return

    try {
      await skillService.deleteSkill(skillId)
      setSuccess('Bacarıq silindi!')
      await loadAllSkills()
    } catch (err: any) {
      setError(err.message || 'Xəta baş verdi')
    }
  }

  if (loading) {
    return <div className="profile-loading">Yüklənir...</div>
  }

  return (
    <div className="profile-page">
      <div className="container">
        <h1 className="profile-title">Profil</h1>

        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">{success}</div>}

        <div className="profile-content">
          <form onSubmit={handleSave} className="profile-form">
            <h2>Şəxsi Məlumatlar</h2>
            
            <div className="form-row">
              <div className="form-group">
                <label htmlFor="firstName">Ad *</label>
                <input
                  type="text"
                  id="firstName"
                  name="firstName"
                  value={formData.firstName}
                  onChange={handleChange}
                  required
                />
              </div>
              <div className="form-group">
                <label htmlFor="lastName">Soyad *</label>
                <input
                  type="text"
                  id="lastName"
                  name="lastName"
                  value={formData.lastName}
                  onChange={handleChange}
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="education">Təhsil</label>
              <input
                type="text"
                id="education"
                name="education"
                value={formData.education}
                onChange={handleChange}
                placeholder="Təhsil məlumatları"
              />
            </div>

            <div className="form-group">
              <label htmlFor="phone">Telefon</label>
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
              <label htmlFor="jobTitle">Vəzifə</label>
              <input
                type="text"
                id="jobTitle"
                name="jobTitle"
                value={formData.jobTitle}
                onChange={handleChange}
                placeholder="İş yeri və vəzifə"
              />
            </div>

            <div className="form-group">
              <label htmlFor="yearsOfExperience">Təcrübə (il)</label>
              <input
                type="number"
                id="yearsOfExperience"
                name="yearsOfExperience"
                value={formData.yearsOfExperience || ''}
                onChange={handleChange}
                min="0"
                placeholder="İllə sayı"
              />
            </div>

            <div className="form-group">
              <label htmlFor="linkedInProfileUrl">LinkedIn Profil</label>
              <input
                type="url"
                id="linkedInProfileUrl"
                name="linkedInProfileUrl"
                value={formData.linkedInProfileUrl}
                onChange={handleChange}
                placeholder="https://linkedin.com/in/username"
              />
            </div>

            <div className="form-group">
              <label htmlFor="bio">Bio</label>
              <textarea
                id="bio"
                name="bio"
                value={formData.bio}
                onChange={handleChange}
                rows={4}
                placeholder="Özünüz haqqında qısa məlumat"
              />
            </div>

            <div className="form-group">
              <label>Bacarıqlar</label>
              <div className="skills-selector">
                {skills.map(skill => (
                  <label key={skill.id} className="skill-checkbox">
                    <input
                      type="checkbox"
                      checked={formData.skillIds?.includes(skill.id)}
                      onChange={() => handleSkillSelect(skill.id)}
                    />
                    <span>{skill.name} ({skill.level})</span>
                  </label>
                ))}
              </div>
            </div>

            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Saxlanılır...' : 'Saxla'}
            </button>
          </form>

          <div className="skills-section">
            <div className="skills-header">
              <h2>Bacarıq İdarəetməsi</h2>
              <button
                onClick={() => {
                  setShowSkillForm(!showSkillForm)
                  setEditingSkill(null)
                  setSkillForm({ name: '', description: '', level: 'BEGINNER' })
                }}
                className="btn btn-secondary"
              >
                {showSkillForm ? 'Ləğv et' : 'Yeni Bacarıq'}
              </button>
            </div>

            {showSkillForm && (
              <form onSubmit={handleCreateSkill} className="skill-form">
                <div className="form-group">
                  <label htmlFor="skillName">Ad *</label>
                  <input
                    type="text"
                    id="skillName"
                    name="name"
                    value={skillForm.name}
                    onChange={handleSkillChange}
                    required
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="skillDescription">Təsvir</label>
                  <textarea
                    id="skillDescription"
                    name="description"
                    value={skillForm.description}
                    onChange={handleSkillChange}
                    rows={3}
                  />
                </div>
                <div className="form-group">
                  <label htmlFor="skillLevel">Səviyyə *</label>
                  <select
                    id="skillLevel"
                    name="level"
                    value={skillForm.level}
                    onChange={handleSkillChange}
                    required
                  >
                    <option value="BEGINNER">Başlanğıc</option>
                    <option value="INTERMEDIATE">Orta</option>
                    <option value="ADVANCED">Qabaqcıl</option>
                    <option value="EXPERT">Ekspert</option>
                  </select>
                </div>
                <button type="submit" className="btn btn-primary" disabled={saving}>
                  {saving ? 'Saxlanılır...' : editingSkill ? 'Yenilə' : 'Yarat'}
                </button>
              </form>
            )}

            <div className="skills-list">
              {skills.map(skill => (
                <div key={skill.id} className="skill-item">
                  <div className="skill-info">
                    <h3>{skill.name}</h3>
                    <p className="skill-level">{skill.level}</p>
                    {skill.description && <p className="skill-description">{skill.description}</p>}
                  </div>
                  <div className="skill-actions">
                    <button
                      onClick={() => handleEditSkill(skill)}
                      className="btn btn-outline btn-sm"
                    >
                      Redaktə
                    </button>
                    <button
                      onClick={() => handleDeleteSkill(skill.id)}
                      className="btn btn-outline btn-sm"
                    >
                      Sil
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Profile

