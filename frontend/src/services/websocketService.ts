import { Client } from '@stomp/stompjs'
import type { IMessage, StompSubscription } from '@stomp/stompjs'
import SockJS from 'sockjs-client'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:1120'
// SockJS uses HTTP, not WebSocket protocol
const WS_URL = `${API_BASE_URL}/ws`

export interface MessagePayload {
  swapId: string
  receiverId: string
  content: string
}

export interface MessageResponse {
  id: string
  swapId: string
  senderId: string
  receiverId: string
  senderName?: string
  receiverName?: string
  content: string
  timestamp: string
}

class WebSocketService {
  private client: Client | null = null
  private subscriptions: Map<string, StompSubscription> = new Map()
  private isConnected: boolean = false

  connect(userId: string, onConnect?: () => void, onError?: (error: any) => void) {
    if (this.client && this.isConnected) {
      onConnect?.()
      return
    }

    this.client = new Client({
      webSocketFactory: () => new SockJS(WS_URL) as any,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: (frame) => {
        console.log('WebSocket connected:', frame)
        this.isConnected = true
        onConnect?.()
      },
      onStompError: (frame) => {
        console.error('STOMP error:', frame)
        this.isConnected = false
        onError?.(frame)
      },
      onWebSocketClose: () => {
        console.log('WebSocket closed')
        this.isConnected = false
        this.subscriptions.clear()
      },
      onDisconnect: () => {
        console.log('WebSocket disconnected')
        this.isConnected = false
        this.subscriptions.clear()
      },
      connectHeaders: {
        'X-Auth-User-Id': userId,
      },
    })

    this.client.activate()
  }

  subscribe(swapId: string, onMessage: (message: MessageResponse) => void): string {
    if (!this.client || !this.isConnected) {
      console.warn('WebSocket not connected, will retry when connected')
      // Retry subscription when connected
      const originalOnConnect = this.client?.onConnect
      if (this.client) {
        this.client.onConnect = (frame) => {
          originalOnConnect?.(frame)
          this.subscribe(swapId, onMessage)
        }
      }
      return ''
    }

    const topic = `/topic/swap/${swapId}`

    // Unsubscribe if already subscribed
    if (this.subscriptions.has(swapId)) {
      this.unsubscribe(swapId)
    }

    const subscription = this.client.subscribe(topic, (message: IMessage) => {
      try {
        const data: MessageResponse = JSON.parse(message.body)
        // Convert timestamp to ISO string if needed
        if (typeof data.timestamp === 'number') {
          data.timestamp = new Date(data.timestamp).toISOString()
        } else if (data.timestamp && !data.timestamp.includes('T')) {
          // Handle different timestamp formats
          data.timestamp = new Date(data.timestamp).toISOString()
        }
        onMessage(data)
      } catch (error) {
        console.error('Error parsing message:', error)
      }
    })

    this.subscriptions.set(swapId, subscription)
    console.log(`Subscribed to topic: ${topic}`)
    return topic
  }

  unsubscribe(swapId: string) {
    const subscription = this.subscriptions.get(swapId)
    if (subscription) {
      subscription.unsubscribe()
      this.subscriptions.delete(swapId)
    }
  }

  sendMessage(payload: MessagePayload) {
    if (!this.client || !this.isConnected) {
      console.warn('WebSocket not connected, cannot send message')
      return false
    }

    try {
      this.client.publish({
        destination: '/app/chat.sendMessage',
        body: JSON.stringify({
          swapId: payload.swapId,
          receiverId: payload.receiverId,
          content: payload.content,
        }),
      })
      return true
    } catch (error) {
      console.error('Error sending message:', error)
      return false
    }
  }

  disconnect() {
    if (this.client) {
      this.subscriptions.forEach((sub) => sub.unsubscribe())
      this.subscriptions.clear()
      this.client.deactivate()
      this.client = null
      this.isConnected = false
    }
  }

  getConnectionStatus(): boolean {
    return this.isConnected
  }
}

export const websocketService = new WebSocketService()

