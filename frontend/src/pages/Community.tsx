import { useState, useEffect } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { communityService, type CreateGroup, type GroupResponse, type CreateProblem, type ProblemResponse } from '../services/communityService'
import './Community.css'

const Community = () => {
  const { user } = useAuth()
  const [groups, setGroups] = useState<GroupResponse[]>([])
  const [selectedGroup, setSelectedGroup] = useState<GroupResponse | null>(null)
  const [problems, setProblems] = useState<ProblemResponse[]>([])
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState<string>('')
  const [success, setSuccess] = useState<string>('')
  const [showGroupForm, setShowGroupForm] = useState(false)
  const [showProblemForm, setShowProblemForm] = useState(false)

  const [groupForm, setGroupForm] = useState<CreateGroup>({
    name: '',
    description: '',
    category: '',
    createdByUserId: user?.id || ''
  })

  const [problemForm, setProblemForm] = useState<CreateProblem>({
    title: '',
    description: '',
    createdByUserId: user?.id || '',
    groupId: 0
  })

  useEffect(() => {
    loadGroups()
  }, [])

  useEffect(() => {
    if (selectedGroup) {
      loadProblems(selectedGroup.id)
    }
  }, [selectedGroup])

  const loadGroups = async () => {
    try {
      setLoading(true)
      const allGroups = await communityService.getAllGroups()
      setGroups(allGroups)
    } catch (err: any) {
      setError(err.message || 'Xəta baş verdi')
    } finally {
      setLoading(false)
    }
  }

  const loadProblems = async (groupId: number) => {
    try {
      const result = await communityService.getProblemsByGroup(groupId)
      setProblems(result.content || [])
    } catch (err: any) {
      setError(err.message || 'Problemlər yüklənə bilmədi')
    }
  }

  const handleCreateGroup = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setSuccess('')
    setSaving(true)

    try {
      await communityService.createGroup({
        ...groupForm,
        createdByUserId: user?.id || ''
      })
      setSuccess('Qrup uğurla yaradıldı!')
      await loadGroups()
      setShowGroupForm(false)
      setGroupForm({ name: '', description: '', category: '', createdByUserId: user?.id || '' })
    } catch (err: any) {
      setError(err.message || 'Xəta baş verdi')
    } finally {
      setSaving(false)
    }
  }

  const handleJoinGroup = async (groupId: number) => {
    if (!user?.id) {
      setError('Giriş etməlisiniz')
      return
    }

    try {
      await communityService.joinGroup(groupId, user.id)
      setSuccess('Qrupa qoşulduğunuz üçün təşəkkürlər!')
      await loadGroups()
    } catch (err: any) {
      setError(err.message || 'Qrupa qoşula bilmədiniz')
    }
  }

  const handleCreateProblem = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!selectedGroup) return

    setError('')
    setSuccess('')
    setSaving(true)

    try {
      await communityService.createProblem({
        ...problemForm,
        groupId: selectedGroup.id,
        createdByUserId: user?.id || ''
      })
      setSuccess('Problem uğurla yaradıldı!')
      await loadProblems(selectedGroup.id)
      setShowProblemForm(false)
      setProblemForm({ title: '', description: '', createdByUserId: user?.id || '', groupId: 0 })
    } catch (err: any) {
      setError(err.message || 'Xəta baş verdi')
    } finally {
      setSaving(false)
    }
  }

  const handleSolveProblem = async (problemId: number) => {
    if (!user?.id) {
      setError('Giriş etməlisiniz')
      return
    }

    try {
      await communityService.solveProblem(problemId, user.id)
      setSuccess('Problem həll edildi kimi işarələndi!')
      if (selectedGroup) {
        await loadProblems(selectedGroup.id)
      }
    } catch (err: any) {
      setError(err.message || 'Xəta baş verdi')
    }
  }

  const isMember = (group: GroupResponse) => {
    return user?.id && group.members?.includes(user.id)
  }

  if (loading) {
    return <div className="community-loading">Yüklənir...</div>
  }

  return (
    <div className="community-page">
      <div className="container">
        <div className="community-header">
          <h1>İctimaiyyət</h1>
          <button
            onClick={() => {
              setShowGroupForm(!showGroupForm)
              setSelectedGroup(null)
              setProblems([])
            }}
            className="btn btn-primary"
          >
            {showGroupForm ? 'Ləğv et' : 'Yeni Qrup'}
          </button>
        </div>

        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">{success}</div>}

        {showGroupForm && (
          <form onSubmit={handleCreateGroup} className="community-form">
            <h2>Yeni Qrup Yarat</h2>
            <div className="form-group">
              <label htmlFor="groupName">Qrup Adı *</label>
              <input
                type="text"
                id="groupName"
                value={groupForm.name}
                onChange={(e) => setGroupForm({ ...groupForm, name: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label htmlFor="groupDescription">Təsvir</label>
              <textarea
                id="groupDescription"
                value={groupForm.description}
                onChange={(e) => setGroupForm({ ...groupForm, description: e.target.value })}
                rows={3}
              />
            </div>
            <div className="form-group">
              <label htmlFor="groupCategory">Kateqoriya</label>
              <input
                type="text"
                id="groupCategory"
                value={groupForm.category}
                onChange={(e) => setGroupForm({ ...groupForm, category: e.target.value })}
                placeholder="Məsələn: Proqramlaşdırma, Dizayn, Dil"
              />
            </div>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Yaradılır...' : 'Yarat'}
            </button>
          </form>
        )}

        <div className="community-content">
          <div className="groups-section">
            <h2>Qruplar</h2>
            {groups.length === 0 ? (
              <div className="no-items">Hələ heç bir qrup yoxdur</div>
            ) : (
              <div className="groups-list">
                {groups.map((group) => (
                  <div
                    key={group.id}
                    className={`group-card ${selectedGroup?.id === group.id ? 'selected' : ''}`}
                    onClick={() => setSelectedGroup(group)}
                  >
                    <div className="group-header">
                      <h3>{group.name}</h3>
                      {group.category && <span className="category">{group.category}</span>}
                    </div>
                    {group.description && <p className="group-description">{group.description}</p>}
                    <div className="group-meta">
                      <span>👥 {group.members?.length || 0} üzv</span>
                      {!isMember(group) && (
                        <button
                          onClick={(e) => {
                            e.stopPropagation()
                            handleJoinGroup(group.id)
                          }}
                          className="btn btn-sm btn-secondary"
                        >
                          Qoşul
                        </button>
                      )}
                      {isMember(group) && (
                        <span className="member-badge">Üzv</span>
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>

          {selectedGroup && (
            <div className="problems-section">
              <div className="problems-header">
                <h2>{selectedGroup.name} - Problemlər</h2>
                {isMember(selectedGroup) && (
                  <button
                    onClick={() => setShowProblemForm(!showProblemForm)}
                    className="btn btn-secondary"
                  >
                    {showProblemForm ? 'Ləğv et' : 'Yeni Problem'}
                  </button>
                )}
              </div>

              {showProblemForm && isMember(selectedGroup) && (
                <form onSubmit={handleCreateProblem} className="community-form">
                  <div className="form-group">
                    <label htmlFor="problemTitle">Başlıq *</label>
                    <input
                      type="text"
                      id="problemTitle"
                      value={problemForm.title}
                      onChange={(e) => setProblemForm({ ...problemForm, title: e.target.value })}
                      required
                    />
                  </div>
                  <div className="form-group">
                    <label htmlFor="problemDescription">Təsvir *</label>
                    <textarea
                      id="problemDescription"
                      value={problemForm.description}
                      onChange={(e) => setProblemForm({ ...problemForm, description: e.target.value })}
                      rows={4}
                      required
                    />
                  </div>
                  <button type="submit" className="btn btn-primary" disabled={saving}>
                    {saving ? 'Yaradılır...' : 'Yarat'}
                  </button>
                </form>
              )}

              {problems.length === 0 ? (
                <div className="no-items">Bu qrupda hələ problem yoxdur</div>
              ) : (
                <div className="problems-list">
                  {problems.map((problem) => (
                    <div key={problem.id} className={`problem-card ${problem.solved ? 'solved' : ''}`}>
                      <div className="problem-header">
                        <h3>{problem.title}</h3>
                        {problem.solved ? (
                          <span className="solved-badge">✓ Həll edilib</span>
                        ) : (
                          isMember(selectedGroup) && (
                            <button
                              onClick={() => handleSolveProblem(problem.id)}
                              className="btn btn-sm btn-primary"
                            >
                              Həll Et
                            </button>
                          )
                        )}
                      </div>
                      <p className="problem-description">{problem.description}</p>
                      <div className="problem-meta">
                        <span>Yaradılıb: {new Date(problem.createdAt).toLocaleDateString('az-AZ')}</span>
                        {problem.solved && problem.solvedAt && (
                          <span>Həll edilib: {new Date(problem.solvedAt).toLocaleDateString('az-AZ')}</span>
                        )}
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default Community

