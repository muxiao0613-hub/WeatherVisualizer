import { defineStore } from 'pinia'
import { ref } from 'vue'

export interface ChatMessage {
  id: number
  role: 'user' | 'assistant'
  content: string
  timestamp: number
}

export const useChatStore = defineStore('chat', () => {
  const messages = ref<ChatMessage[]>([])
  const loading = ref(false)

  const addMessage = (role: 'user' | 'assistant', content: string) => {
    messages.value.push({
      id: Date.now(),
      role,
      content,
      timestamp: Date.now()
    })
  }

  const setLoading = (value: boolean) => {
    loading.value = value
  }

  const clearMessages = () => {
    messages.value = []
  }

  return {
    messages,
    loading,
    addMessage,
    setLoading,
    clearMessages
  }
})
