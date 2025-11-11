import { useState, useEffect, useRef } from 'react'
import { useAuth } from '../contexts/AuthContext'
import { chatService, type ChatMessage } from '../services/chatService'
import { swapService, type SwapOfferResponse } from '../services/swapService'
import { userBioService } from '../services/userBioService'
import { websocketService, type MessageResponse } from '../services/websocketService'
import './Chat.css'

interface ChatListItem {
  swapId: string
  swapOffer: SwapOfferResponse
  lastMessage?: ChatMessage
  otherUserId: string
  otherUserName: string
}

interface UserSearchResult {
  id: string
  firstName: string
  lastName: string
  username?: string
  bio?: string
}

const Chat = () => {
  const { user } = useAuth()
  const [chatList, setChatList] = useState<ChatListItem[]>([])
  const [selectedChat, setSelectedChat] = useState<ChatListItem | null>(null)
  const [messages, setMessages] = useState<ChatMessage[]>([])
  const [newMessage, setNewMessage] = useState('')
  const [loading, setLoading] = useState(true)
  const [sending, setSending] = useState(false)
  const [error, setError] = useState<string>('')
  const [searchQuery, setSearchQuery] = useState('')
  const [searchResults, setSearchResults] = useState<UserSearchResult[]>([])
  const [showSearch, setShowSearch] = useState(false)
  const [searching, setSearching] = useState(false)
  const [isConnected, setIsConnected] = useState(false)
  const messagesEndRef = useRef<HTMLDivElement>(null)
  const statusIntervalRef = useRef<ReturnType<typeof setInterval> | null>(null)

  useEffect(() => {
    if (user?.id) {
      loadChatList()
      // Connect to WebSocket
      websocketService.connect(
        user.id,
        () => {
          console.log('WebSocket connected')
          setIsConnected(true)
        },
        (error) => {
          console.error('WebSocket connection error:', error)
          setError('WebSocket bağlantısı qurula bilmədi')
          setIsConnected(false)
        }
      )
      
      // Check connection status periodically
      statusIntervalRef.current = setInterval(() => {
        setIsConnected(websocketService.getConnectionStatus())
      }, 1000)
    }

    return () => {
      // Disconnect on unmount
      websocketService.disconnect()
      if (statusIntervalRef.current) {
        clearInterval(statusIntervalRef.current)
        statusIntervalRef.current = null
      }
    }
  }, [user])

  useEffect(() => {
    // Check if there's a selected userId from SwapOffers
    const selectedUserId = localStorage.getItem('selectedUserId')
    const selectedSwapId = localStorage.getItem('selectedSwapId')
    
    if (selectedUserId && chatList.length > 0) {
      // Find chat by userId
      const chat = chatList.find(c => c.otherUserId === selectedUserId)
      if (chat) {
        setSelectedChat(chat)
        localStorage.removeItem('selectedUserId')
        localStorage.removeItem('selectedSwapId')
      } else if (selectedSwapId) {
        // Find chat by swapId
        const chat = chatList.find(c => c.swapId === selectedSwapId)
        if (chat) {
          setSelectedChat(chat)
          localStorage.removeItem('selectedUserId')
          localStorage.removeItem('selectedSwapId')
        }
      }
    } else if (selectedSwapId && chatList.length > 0) {
      const chat = chatList.find(c => c.swapId === selectedSwapId)
      if (chat) {
        setSelectedChat(chat)
        localStorage.removeItem('selectedSwapId')
      }
    }
  }, [chatList])


  useEffect(() => {
    if (selectedChat) {
      loadChatHistory(selectedChat.swapId)
      
      // Subscribe to WebSocket topic for this chat
      const topic = websocketService.subscribe(selectedChat.swapId, (message: MessageResponse) => {
        // Convert MessageResponse to ChatMessage
        const chatMessage: ChatMessage = {
          id: message.id,
          swapId: message.swapId,
          senderId: message.senderId,
          receiverId: message.receiverId,
          content: message.content,
          timestamp: message.timestamp,
          senderName: message.senderName,
          receiverName: message.receiverName,
        }
        
        // Add message to current chat (only if it's for the currently selected chat)
        if (selectedChat && chatMessage.swapId === selectedChat.swapId) {
          setMessages(prev => {
            // Check if message already exists
            if (prev.find(m => m.id === chatMessage.id)) {
              return prev
            }
            return [...prev, chatMessage]
          })
        }
        
        // Always update chat list with last message
        setChatList(prev => prev.map(chat => 
          chat.swapId === chatMessage.swapId 
            ? { ...chat, lastMessage: chatMessage }
            : chat
        ))
      })

      return () => {
        // Unsubscribe when chat changes
        if (topic) {
          websocketService.unsubscribe(selectedChat.swapId)
        }
      }
    }
  }, [selectedChat])

  useEffect(() => {
    scrollToBottom()
  }, [messages])

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }

  const loadChatList = async () => {
    if (!user?.id) return

    try {
      setLoading(true)
      // Get user's swap offers
      const myOffers = await swapService.getMyOffers()
      
      // Also get all offers to find matches (offers where user matched)
      let allOffers: SwapOfferResponse[] = []
      try {
        // Search for offers to find potential matches
        // For now, we'll use myOffers and create chats from them
        allOffers = myOffers
      } catch {
        // If search fails, just use myOffers
        allOffers = myOffers
      }
      
      // Create chat list from offers
      const chats: ChatListItem[] = allOffers.map(offer => ({
        swapId: offer.id.toString(),
        swapOffer: offer,
        otherUserId: offer.userId === user.id ? '' : offer.userId, // Will be updated from messages
        otherUserName: 'İstifadəçi' // Will be updated when we load chat history
      }))

      // Try to load last message for each chat
      for (const chat of chats) {
        try {
          const history = await chatService.getChatHistory(chat.swapId)
          if (history.length > 0) {
            chat.lastMessage = history[history.length - 1]
            // Find other user
            const otherUser = history.find(msg => 
              (msg.senderId !== user.id && msg.senderId) || 
              (msg.receiverId !== user.id && msg.receiverId)
            )
            if (otherUser) {
              chat.otherUserId = otherUser.senderId === user.id ? otherUser.receiverId : otherUser.senderId
              chat.otherUserName = otherUser.senderId === user.id 
                ? (otherUser.receiverName || 'İstifadəçi')
                : (otherUser.senderName || 'İstifadəçi')
            }
          } else {
            // If no messages, try to get other user from swap offer
            // For now, we'll keep default values
          }
        } catch {
          // Chat history doesn't exist yet, that's okay
        }
      }

      // Filter out chats without other user info (if needed)
      const validChats = chats.filter(chat => chat.otherUserId || chat.lastMessage)

      // Sort by last message time
      validChats.sort((a, b) => {
        if (!a.lastMessage && !b.lastMessage) return 0
        if (!a.lastMessage) return 1
        if (!b.lastMessage) return -1
        return new Date(b.lastMessage.timestamp).getTime() - new Date(a.lastMessage.timestamp).getTime()
      })

      setChatList(validChats)
      
      // Check for selected swapId from localStorage
      const selectedSwapId = localStorage.getItem('selectedSwapId')
      if (selectedSwapId) {
        const chat = validChats.find(c => c.swapId === selectedSwapId)
        if (chat) {
          setSelectedChat(chat)
          localStorage.removeItem('selectedSwapId')
        } else if (validChats.length > 0) {
          setSelectedChat(validChats[0])
        }
      } else if (validChats.length > 0 && !selectedChat) {
        setSelectedChat(validChats[0])
      }
    } catch (err: any) {
      setError(err.message || 'Chat siyahısı yüklənə bilmədi')
    } finally {
      setLoading(false)
    }
  }

  const loadChatHistory = async (swapId: string) => {
    try {
      const history = await chatService.getChatHistory(swapId)
      setMessages(history)
    } catch (err: any) {
      setError(err.message || 'Mesajlar yüklənə bilmədi')
      setMessages([])
    }
  }

  const handleSendMessage = async (e: React.FormEvent) => {
    e.preventDefault()
    if (!newMessage.trim() || !selectedChat || !user?.id) return

    setSending(true)
    setError('')

    try {
      // Send message via WebSocket
      const success = websocketService.sendMessage({
        swapId: selectedChat.swapId,
        receiverId: selectedChat.otherUserId,
        content: newMessage.trim(),
      })

      if (success) {
        // Optimistically add message to UI (will be confirmed by WebSocket response)
        const tempMessage: ChatMessage = {
          id: `temp-${Date.now()}`,
          swapId: selectedChat.swapId,
          senderId: user.id,
          receiverId: selectedChat.otherUserId,
          content: newMessage.trim(),
          timestamp: new Date().toISOString(),
          senderName: user.username || 'User'
        }
        
        setMessages(prev => [...prev, tempMessage])
        setNewMessage('')
        
        // Update chat list with last message
        setChatList(prev => prev.map(chat => 
          chat.swapId === selectedChat.swapId 
            ? { ...chat, lastMessage: tempMessage }
            : chat
        ))
      } else {
        setError('Mesaj göndərilə bilmədi. Zəhmət olmasa yenidən cəhd edin.')
      }
    } catch (err: any) {
      setError(err.message || 'Mesaj göndərilə bilmədi')
    } finally {
      setSending(false)
    }
  }

  const formatTime = (timestamp: string) => {
    const date = new Date(timestamp)
    const now = new Date()
    const diff = now.getTime() - date.getTime()
    const days = Math.floor(diff / (1000 * 60 * 60 * 24))

    if (days === 0) {
      return date.toLocaleTimeString('az-AZ', { hour: '2-digit', minute: '2-digit' })
    } else if (days === 1) {
      return 'Dünən'
    } else if (days < 7) {
      return `${days} gün əvvəl`
    } else {
      return date.toLocaleDateString('az-AZ', { day: 'numeric', month: 'short' })
    }
  }

  const formatLastMessageTime = (timestamp: string) => {
    const date = new Date(timestamp)
    const now = new Date()
    const diff = now.getTime() - date.getTime()
    const minutes = Math.floor(diff / (1000 * 60))

    if (minutes < 1) return 'İndi'
    if (minutes < 60) return `${minutes} dəq`
    if (minutes < 1440) return `${Math.floor(minutes / 60)} saat`
    return date.toLocaleDateString('az-AZ', { day: 'numeric', month: 'short' })
  }

  const handleSearchUsers = async (query: string) => {
    if (!query.trim()) {
      setSearchResults([])
      return
    }

    setSearching(true)
    try {
      // Get all user bios and filter by search query
      const allUsers = await userBioService.getAllUserBios()
      const filtered = allUsers
        .filter(u => 
          u.authUserId !== user?.id && // Exclude current user
          (
            u.firstName?.toLowerCase().includes(query.toLowerCase()) ||
            u.lastName?.toLowerCase().includes(query.toLowerCase()) ||
            `${u.firstName} ${u.lastName}`.toLowerCase().includes(query.toLowerCase())
          )
        )
        .slice(0, 15) // Limit to 15 results
        .map(u => ({
          id: u.authUserId,
          firstName: u.firstName,
          lastName: u.lastName,
          bio: u.bio
        }))
      
      setSearchResults(filtered)
    } catch (err: any) {
      setError(err.message || 'Axtarış zamanı xəta baş verdi')
    } finally {
      setSearching(false)
    }
  }

  const handleStartChat = async (otherUserId: string, otherUserName: string) => {
    if (!user?.id) return

    try {
      // Check if chat already exists
      const existingChat = chatList.find(chat => chat.otherUserId === otherUserId)
      if (existingChat) {
        setSelectedChat(existingChat)
        setShowSearch(false)
        setSearchQuery('')
        return
      }

      // Create a temporary swap offer for chat (or use existing one)
      // For now, we'll create a simple chat entry
      // In a real implementation, you'd create a swap offer first
      const newChat: ChatListItem = {
        swapId: `temp-${Date.now()}`, // Temporary ID
        swapOffer: {
          id: 0,
          userId: otherUserId,
          skillOffered: 'Chat',
          skillRequested: 'Chat',
          meetingType: 'ONLINE',
          description: 'Yeni chat',
          latitude: 0,
          longitude: 0
        },
        otherUserId,
        otherUserName
      }

      setChatList(prev => [newChat, ...prev])
      setSelectedChat(newChat)
      setShowSearch(false)
      setSearchQuery('')
      setSearchResults([])
    } catch (err: any) {
      setError(err.message || 'Chat başlatıla bilmədi')
    }
  }

  useEffect(() => {
    if (searchQuery) {
      const timeoutId = setTimeout(() => {
        handleSearchUsers(searchQuery)
      }, 300) // Debounce

      return () => clearTimeout(timeoutId)
    } else {
      setSearchResults([])
    }
  }, [searchQuery])

  if (loading) {
    return <div className="chat-loading">Yüklənir...</div>
  }

  return (
    <div className="chat-page">
      <div className="container">
        <div className="chat-layout">
          {/* Chat List - Left Side */}
          <div className="chat-list-container">
            <div className="chat-list-header">
              <h2>Mesajlar</h2>
              <button
                onClick={() => setShowSearch(!showSearch)}
                className="search-toggle-btn"
                title="Yeni chat başlat"
              >
                {showSearch ? '✕' : '➕'}
              </button>
            </div>

            {showSearch && (
              <div className="user-search-section">
                <input
                  type="text"
                  placeholder="İstifadəçi axtar..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="user-search-input"
                  autoFocus
                />
                {searching && <div className="search-loading">Axtarılır...</div>}
                {searchResults.length > 0 && (
                  <div className="search-results">
                    {searchResults.map((result) => (
                      <div
                        key={result.id}
                        className="search-result-item"
                        onClick={() => handleStartChat(result.id, `${result.firstName} ${result.lastName}`)}
                      >
                        <div className="search-avatar">
                          {result.firstName.charAt(0).toUpperCase()}
                        </div>
                        <div className="search-user-info">
                          <div className="search-user-name">
                            {result.firstName} {result.lastName}
                          </div>
                          {result.bio && (
                            <div className="search-user-bio">{result.bio}</div>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>
                )}
                {searchQuery && !searching && searchResults.length === 0 && (
                  <div className="no-search-results">İstifadəçi tapılmadı</div>
                )}
              </div>
            )}

            <div className="chat-list">
              {chatList.length === 0 ? (
                <div className="no-chats">Hələ heç bir chat yoxdur</div>
              ) : (
                chatList.map((chat) => (
                  <div
                    key={chat.swapId}
                    className={`chat-item ${selectedChat?.swapId === chat.swapId ? 'active' : ''}`}
                    onClick={() => setSelectedChat(chat)}
                  >
                    <div className="chat-avatar">
                      {chat.otherUserName.charAt(0).toUpperCase()}
                    </div>
                    <div className="chat-info">
                      <div className="chat-header-row">
                        <span className="chat-name">{chat.otherUserName}</span>
                        {chat.lastMessage && (
                          <span className="chat-time">{formatLastMessageTime(chat.lastMessage.timestamp)}</span>
                        )}
                      </div>
                      <div className="chat-preview">
                        {chat.lastMessage ? (
                          <span className={chat.lastMessage.senderId === user?.id ? 'sent' : ''}>
                            {chat.lastMessage.senderId === user?.id ? 'Siz: ' : ''}
                            {chat.lastMessage.content}
                          </span>
                        ) : (
                          <span className="no-message">Yeni chat</span>
                        )}
                      </div>
                      <div className="chat-context">
                        {chat.swapOffer.skillOffered} ↔ {chat.swapOffer.skillRequested}
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Chat Messages - Right Side */}
          <div className="chat-messages-container">
            {selectedChat ? (
              <>
                <div className="chat-header">
                  <div className="chat-header-info">
                    <div className="chat-avatar-large">
                      {selectedChat.otherUserName.charAt(0).toUpperCase()}
                    </div>
                    <div>
                      <h3>{selectedChat.otherUserName}</h3>
                      <p className="chat-context-small">
                        {selectedChat.swapOffer.skillOffered} ↔ {selectedChat.swapOffer.skillRequested}
                      </p>
                    </div>
                  </div>
                  <div className="connection-status">
                    {isConnected ? (
                      <span className="status-indicator online" title="Bağlı">🟢</span>
                    ) : (
                      <span className="status-indicator offline" title="Bağlı deyil">🔴</span>
                    )}
                  </div>
                </div>

                {error && <div className="error-message">{error}</div>}

                <div className="messages-container">
                  {messages.length === 0 ? (
                    <div className="no-messages">
                      <p>Hələ heç bir mesaj yoxdur</p>
                      <p className="hint">İlk mesajı siz göndərin!</p>
                    </div>
                  ) : (
                    messages.map((message, index) => {
                      const isOwn = message.senderId === user?.id
                      const showDate = index === 0 || 
                        new Date(message.timestamp).toDateString() !== 
                        new Date(messages[index - 1].timestamp).toDateString()
                      
                      return (
                        <div key={message.id}>
                          {showDate && (
                            <div className="date-divider">
                              {new Date(message.timestamp).toLocaleDateString('az-AZ', { 
                                weekday: 'long', 
                                year: 'numeric', 
                                month: 'long', 
                                day: 'numeric' 
                              })}
                            </div>
                          )}
                          <div className={`message ${isOwn ? 'own-message' : 'other-message'}`}>
                            <div className="message-content">
                              <div className="message-text">{message.content}</div>
                              <div className="message-time">{formatTime(message.timestamp)}</div>
                            </div>
                          </div>
                        </div>
                      )
                    })
                  )}
                  <div ref={messagesEndRef} />
                </div>

                <form onSubmit={handleSendMessage} className="message-input-form">
                  <input
                    type="text"
                    value={newMessage}
                    onChange={(e) => setNewMessage(e.target.value)}
                    placeholder="Mesaj yazın..."
                    className="message-input"
                    disabled={sending}
                  />
                  <button
                    type="submit"
                    className="btn btn-primary send-button"
                    disabled={sending || !newMessage.trim()}
                  >
                    {sending ? '...' : '➤'}
                  </button>
                </form>
              </>
            ) : (
              <div className="no-chat-selected">
                <div className="empty-chat-icon">💬</div>
                <p>Chat seçin və ya yeni chat başladın</p>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

export default Chat
