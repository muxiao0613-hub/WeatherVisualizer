<template>
  <el-card class="ai-chat-card" shadow="hover">
    <template #header>
      <div class="card-header">
        <span class="card-title">AI 助手</span>
        <el-icon :size="20" color="#409EFF"><ChatDotRound /></el-icon>
      </div>
    </template>

    <div class="chat-container">
      <div ref="messagesContainer" class="messages">
        <div
          v-for="message in chatStore.messages"
          :key="message.id"
          class="message"
          :class="message.role"
        >
          <div class="message-content">
            <div class="message-avatar">
              <el-icon v-if="message.role === 'user'" :size="24"><User /></el-icon>
              <el-icon v-else :size="24" color="#409EFF"><ChatDotRound /></el-icon>
            </div>
            <div class="message-text">{{ message.content }}</div>
          </div>
        </div>

        <div v-if="chatStore.loading" class="message assistant">
          <div class="message-content">
            <div class="message-avatar">
              <el-icon :size="24" color="#409EFF"><ChatDotRound /></el-icon>
            </div>
            <div class="message-text loading">
              <el-skeleton :rows="2" animated />
            </div>
          </div>
        </div>
      </div>

      <div class="quick-questions" v-if="chatStore.messages.length === 0">
        <el-tag
          v-for="question in quickQuestions"
          :key="question"
          class="question-tag"
          @click="sendQuickQuestion(question)"
        >
          {{ question }}
        </el-tag>
      </div>

      <div class="input-area">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="2"
          placeholder="输入问题..."
          @keydown.enter.prevent="handleSend"
          :disabled="chatStore.loading"
        />
        <el-button
          type="primary"
          :icon="Promotion"
          :loading="chatStore.loading"
          @click="handleSend"
          :disabled="!inputMessage.trim()"
        >
          发送
        </el-button>
      </div>
    </div>
  </el-card>
</template>

<script setup lang="ts">
import { ref, watch, nextTick } from 'vue'
import { ChatDotRound, User, Promotion } from '@element-plus/icons-vue'
import { useChatStore } from '@/stores/chat'
import { useCityStore } from '@/stores/city'
import { aiApi } from '@/services/api'
import type { ChatRequestDTO } from '@/types'
import { ElMessage } from 'element-plus'

const chatStore = useChatStore()
const cityStore = useCityStore()

const inputMessage = ref('')
const messagesContainer = ref<HTMLElement>()

const quickQuestions = [
  '今天适合外出吗？',
  '未来几天天气如何？',
  '需要注意什么？',
  '空气质量怎么样？'
]

const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  })
}

const handleSend = async () => {
  if (!inputMessage.value.trim() || chatStore.loading) return

  const question = inputMessage.value.trim()
  inputMessage.value = ''

  chatStore.addMessage('user', question)
  scrollToBottom()

  chatStore.setLoading(true)

  try {
    const request: ChatRequestDTO = {
      question,
      city: cityStore.currentCity
    }

    const response = await aiApi.chat(request)
    chatStore.addMessage('assistant', response.answer)
  } catch (error) {
    ElMessage.error('AI 回复失败，请稍后重试')
    console.error('Chat failed:', error)
  } finally {
    chatStore.setLoading(false)
    scrollToBottom()
  }
}

const sendQuickQuestion = (question: string) => {
  inputMessage.value = question
  handleSend()
}

watch(() => chatStore.messages, () => {
  scrollToBottom()
}, { deep: true })
</script>

<style scoped>
.ai-chat-card {
  border-radius: 16px;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.chat-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  gap: 16px;
}

.messages {
  flex: 1;
  overflow-y: auto;
  padding: 12px 0;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.message {
  display: flex;
}

.message.user {
  justify-content: flex-end;
}

.message.assistant {
  justify-content: flex-start;
}

.message-content {
  display: flex;
  gap: 12px;
  max-width: 80%;
}

.message.user .message-content {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #f5f7fa;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message-text {
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.6;
  word-wrap: break-word;
}

.message.user .message-text {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-bottom-right-radius: 4px;
}

.message.assistant .message-text {
  background: #f5f7fa;
  color: #303133;
  border-bottom-left-radius: 4px;
}

.message-text.loading {
  padding: 8px 12px;
}

.quick-questions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 12px 0;
  border-top: 1px solid #f0f0f0;
}

.question-tag {
  cursor: pointer;
  transition: all 0.2s;
}

.question-tag:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.input-area {
  display: flex;
  gap: 12px;
  align-items: flex-end;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.input-area :deep(.el-textarea) {
  flex: 1;
}

.input-area :deep(.el-textarea__inner) {
  resize: none;
}
</style>
