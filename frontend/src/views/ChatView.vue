<template>
  <div class="chat-container">
    <!-- 左侧话题列表 -->
    <aside class="sidebar">
      <div class="sidebar-header">
        <button class="new-chat-btn" @click="createNewTopic">
          <svg viewBox="0 0 24 24" width="18" height="18">
            <path fill="currentColor" d="M19 13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
          </svg>
          新建对话
        </button>
      </div>
      
      <div class="topic-list">
        <div 
          v-for="topic in topics" 
          :key="topic.id"
          :class="['topic-item', { active: currentTopicId === topic.id }]"
          @click="switchTopic(topic.id)"
        >
          <svg class="topic-icon" viewBox="0 0 24 24" width="16" height="16">
            <path fill="currentColor" d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2z"/>
          </svg>
          <span class="topic-title">{{ topic.title }}</span>
          <button class="topic-delete" @click.stop="deleteTopic(topic.id)" title="删除">
            <svg viewBox="0 0 24 24" width="14" height="14">
              <path fill="currentColor" d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
            </svg>
          </button>
        </div>
      </div>

      <div class="sidebar-footer">
        <div class="user-info">
          <span class="user-avatar">👤</span>
          <span class="user-name">{{ username }}</span>
        </div>
        <button class="logout-btn" @click="handleLogout" title="退出登录">
          <svg viewBox="0 0 24 24" width="18" height="18">
            <path fill="currentColor" d="M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z"/>
          </svg>
        </button>
      </div>
    </aside>

    <!-- 右侧聊天区域 -->
    <div class="chat-main-wrapper">
      <!-- 顶部导航栏 -->
      <header class="chat-header">
        <button class="sidebar-toggle" @click="sidebarVisible = !sidebarVisible">
          <svg viewBox="0 0 24 24" width="20" height="20">
            <path fill="currentColor" d="M3 18h18v-2H3v2zm0-5h18v-2H3v2zm0-7v2h18V6H3z"/>
          </svg>
        </button>
        <h1 class="chat-title">{{ currentTopic?.title || 'AI 编程助手' }}</h1>
        <div class="header-spacer"></div>
      </header>

      <!-- 聊天主体区域 -->
      <main class="chat-main">
        <!-- 消息列表 -->
        <div class="message-list" ref="messageListRef">
          <!-- 欢迎消息 -->
          <div class="welcome-message" v-if="currentMessages.length === 0">
            <div class="welcome-icon">👋</div>
            <h2>欢迎使用 AI 编程助手</h2>
            <p>我可以帮助您：</p>
            <div class="feature-cards">
              <div class="feature-card" @click="sendQuickMessage('帮我规划一下 Java 后端学习路线')">
                <span class="card-icon">📚</span>
                <span>规划学习路线</span>
              </div>
              <div class="feature-card" @click="sendQuickMessage('推荐一些适合初学者的项目')">
                <span class="card-icon">💡</span>
                <span>项目学习建议</span>
              </div>
              <div class="feature-card" @click="sendQuickMessage('如何准备程序员面试？')">
                <span class="card-icon">💼</span>
                <span>求职面试指南</span>
              </div>
              <div class="feature-card" @click="sendQuickMessage('Java 常见面试题有哪些？')">
                <span class="card-icon">📝</span>
                <span>高频面试题</span>
              </div>
            </div>
          </div>

          <!-- 消息气泡 -->
          <div 
            v-for="(message, index) in currentMessages" 
            :key="index"
            :class="['message-row', message.role]"
          >
            <div class="message-avatar">
              <span v-if="message.role === 'ai'">🤖</span>
              <span v-else>👤</span>
            </div>
            <div class="message-content">
              <div class="message-bubble" v-html="formatMessage(message.content)"></div>
              <div class="message-time">{{ message.time }}</div>
            </div>
          </div>

          <!-- AI 正在输入指示器 -->
          <div class="message-row ai" v-if="waitingFirstChunk">
            <div class="message-avatar">🤖</div>
            <div class="message-content">
              <div class="message-bubble typing">
                <span class="typing-dot"></span>
                <span class="typing-dot"></span>
                <span class="typing-dot"></span>
              </div>
            </div>
          </div>
        </div>
      </main>

      <!-- 底部输入区域 -->
      <footer class="chat-footer">
        <div class="input-wrapper">
          <textarea
            v-model="inputMessage"
            @keydown.enter.exact.prevent="sendMessage"
            placeholder="输入您的问题... (Enter 发送, Shift+Enter 换行)"
            rows="1"
            ref="textareaRef"
          ></textarea>
          <button 
            class="send-btn" 
            @click="sendMessage"
            :disabled="!inputMessage.trim() || isTyping"
          >
            <svg viewBox="0 0 24 24" width="20" height="20">
              <path fill="currentColor" d="M2.01 21L23 12 2.01 3 2 10l15 2-15 2z"/>
            </svg>
          </button>
        </div>
        <p class="input-hint">AI 编程助手可能会产生不准确的信息，请仔细核实重要内容</p>
      </footer>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { chatWithSSE } from '../api/ai'
import { getCurrentUser, logout, isAuthenticated } from '../api/auth'
import { 
  createConversation, 
  getConversations, 
  deleteConversation as deleteConversationApi,
  updateConversationTitle 
} from '../api/conversation'

interface Message {
  role: 'ai' | 'user'
  content: string
  time: string
}

interface Topic {
  id: string
  title: string
  messages: Message[]
  createdAt: number
}

const router = useRouter()
const currentUser = ref(getCurrentUser())
const username = ref(currentUser.value?.username || '用户')
const inputMessage = ref('')
const isTyping = ref(false)
const waitingFirstChunk = ref(false)
const sidebarVisible = ref(true)
const messageListRef = ref<HTMLElement | null>(null)
const textareaRef = ref<HTMLTextAreaElement | null>(null)
let activeEventSource: EventSource | null = null
let pendingChunks: string[] = []
let typewriterTimer: number | null = null
let currentAiMessageIndex = -1

const ensureAiMessage = () => {
  if (currentAiMessageIndex >= 0) return
  if (!currentTopic.value) return

  currentTopic.value.messages.push({
    role: 'ai',
    content: '',
    time: getCurrentTime()
  })
  currentAiMessageIndex = currentTopic.value.messages.length - 1
  waitingFirstChunk.value = false
}

const stopTypewriter = () => {
  if (typewriterTimer !== null) {
    window.clearInterval(typewriterTimer)
    typewriterTimer = null
  }
}

const startTypewriter = () => {
  if (typewriterTimer !== null) return

  typewriterTimer = window.setInterval(() => {
    const currentAiMessage = currentTopic.value?.messages[currentAiMessageIndex]
    if (!currentAiMessage || currentAiMessage.role !== 'ai') {
      return
    }

    if (pendingChunks.length === 0) {
      if (!activeEventSource) {
        stopTypewriter()
        isTyping.value = false
      }
      return
    }

    const chunk = pendingChunks[0]
    if (!chunk) {
      pendingChunks.shift()
      return
    }

    currentAiMessage.content += chunk[0]
    pendingChunks[0] = chunk.slice(1)
    if (!pendingChunks[0]) {
      pendingChunks.shift()
    }
    scrollToBottom()
  }, 16)
}

// 话题列表
const topics = reactive<Topic[]>([])
const currentTopicId = ref<string>('')

// 当前话题
const currentTopic = computed(() => {
  return topics.find(t => t.id === currentTopicId.value)
})

// 当前消息列表
const currentMessages = computed(() => {
  return currentTopic.value?.messages || []
})

// 生成唯一ID
const generateId = () => {
  return Date.now().toString(36) + Math.random().toString(36).substr(2)
}

// 创建新话题
const createNewTopic = async () => {
  const newTopic: Topic = {
    id: generateId(),
    title: '新对话',
    messages: [],
    createdAt: Date.now()
  }
  
  // 调用后端创建对话
  try {
    await createConversation(newTopic.id, newTopic.title)
  } catch (error) {
    console.error('创建对话失败:', error)
  }
  
  topics.unshift(newTopic)
  currentTopicId.value = newTopic.id
  saveTopicsToStorage()
}

// 切换话题
const switchTopic = (topicId: string) => {
  currentTopicId.value = topicId
  nextTick(() => scrollToBottom())
}

// 删除话题
const deleteTopic = async (topicId: string) => {
  const index = topics.findIndex(t => t.id === topicId)
  if (index > -1) {
    // 调用后端删除对话
    try {
      await deleteConversationApi(topicId)
    } catch (error) {
      console.error('删除对话失败:', error)
    }
    
    topics.splice(index, 1)
    if (currentTopicId.value === topicId) {
      currentTopicId.value = topics[0]?.id || ''
      if (!currentTopicId.value) {
        createNewTopic()
      }
    }
    saveTopicsToStorage()
  }
}

// 更新话题标题（取第一条用户消息的前20字）
const updateTopicTitle = async (topicId: string, firstMessage: string) => {
  const topic = topics.find(t => t.id === topicId)
  if (topic && topic.title === '新对话') {
    const newTitle = firstMessage.slice(0, 20) + (firstMessage.length > 20 ? '...' : '')
    topic.title = newTitle
    
    // 调用后端更新标题
    try {
      await updateConversationTitle(topicId, newTitle)
    } catch (error) {
      console.error('更新标题失败:', error)
    }
    
    saveTopicsToStorage()
  }
}

// 保存到 localStorage (按用户隔离)
const getStorageKey = () => {
  const userId = currentUser.value?.id || 'guest'
  return `chat_topics_user_${userId}`
}

const saveTopicsToStorage = () => {
  localStorage.setItem(getStorageKey(), JSON.stringify(topics))
}

// 从 localStorage 加载 (按用户隔离) + 从后端同步
const loadTopicsFromStorage = async () => {
  // 先从 localStorage 加载（快速显示）
  const stored = localStorage.getItem(getStorageKey())
  if (stored) {
    const parsed = JSON.parse(stored) as Topic[]
    topics.splice(0, topics.length, ...parsed)
    if (topics.length > 0) {
      currentTopicId.value = topics[0].id
    }
  }
  
  // 然后从后端获取对话列表，同步本地没有的对话到后端
  try {
    const serverConversations = await getConversations()
    const serverMemoryIds = new Set(serverConversations.map(c => c.memoryId))
    
    // 将本地有但服务器没有的对话同步到服务器
    for (const topic of topics) {
      if (!serverMemoryIds.has(topic.id)) {
        try {
          await createConversation(topic.id, topic.title)
        } catch (error) {
          console.error('同步对话到服务器失败:', error)
        }
      }
    }
  } catch (error) {
    console.error('获取对话列表失败:', error)
  }
  
  if (topics.length === 0) {
    createNewTopic()
  }
}

// 退出登录
const handleLogout = () => {
  // 清理活动的 EventSource 连接
  activeEventSource?.close()
  activeEventSource = null
  pendingChunks = []
  currentAiMessageIndex = -1
  waitingFirstChunk.value = false
  stopTypewriter()
  
  // 清除认证信息
  logout()
  router.push('/login')
}

// 检查登录状态
onMounted(() => {
  if (!isAuthenticated() || !currentUser.value) {
    router.push('/login')
    return
  }
  loadTopicsFromStorage()
})

// 监听消息变化，自动保存
watch(topics, () => {
  saveTopicsToStorage()
}, { deep: true })

// 格式化消息 (增强的 Markdown 支持)
const formatMessage = (content: string): string => {
  const escaped = content
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

  const withMarkdownLinks = escaped.replace(
    /\[([^\]]+)\]\((https?:\/\/[^\s)]+)\)/g,
    '<a href="$2" target="_blank" rel="noopener noreferrer">$1</a>'
  )

  const withLinks = withMarkdownLinks.replace(
    /(^|[\s(])(https?:\/\/[^\s<)]+)/g,
    '$1<a href="$2" target="_blank" rel="noopener noreferrer">$2</a>'
  )

  // 处理代码块
  let result = withLinks.replace(/```(\w*)\n([\s\S]*?)```/g, '<pre><code class="language-$1">$2</code></pre>')
  
  // 处理行内代码
  result = result.replace(/`([^`]+)`/g, '<code>$1</code>')
  
  // 处理粗体
  result = result.replace(/\*\*([^*]+)\*\*/g, '<strong>$1</strong>')
  
  // 处理标题 (### ## #)
  result = result.replace(/^### (.+)$/gm, '<h4>$1</h4>')
  result = result.replace(/^## (.+)$/gm, '<h3>$1</h3>')
  result = result.replace(/^# (.+)$/gm, '<h2>$1</h2>')
  
  // 处理有序列表 (1. 2. 3.)
  result = result.replace(/^(\d+)\. (.+)$/gm, '<li class="ordered">$2</li>')
  
  // 处理无序列表 (- 或 *)
  result = result.replace(/^[-*] (.+)$/gm, '<li>$1</li>')
  
  // 将连续的 <li> 包裹在 <ul> 或 <ol> 中
  result = result.replace(/(<li class="ordered">[\s\S]*?<\/li>)(\n(?!<li)|\s*$)/g, '<ol>$1</ol>$2')
  result = result.replace(/(<li>[\s\S]*?<\/li>)(\n(?!<li)|\s*$)/g, '<ul>$1</ul>$2')
  result = result.replace(/<\/li>\n<li/g, '</li><li')
  result = result.replace(/ class="ordered"/g, '')
  
  // 处理换行
  result = result.replace(/\n/g, '<br>')
  
  // 清理多余的 <br>
  result = result.replace(/<br><\/?(ul|ol|li|h[234]|pre)>/g, '</$1>')
  result = result.replace(/<(ul|ol|li|h[234]|pre)><br>/g, '<$1>')
  
  return result
}

// 获取当前时间
const getCurrentTime = (): string => {
  const now = new Date()
  return now.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

// 滚动到底部
const scrollToBottom = async () => {
  await nextTick()
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight
  }
}

// 发送消息
const sendMessage = async () => {
  const content = inputMessage.value.trim()
  if (!content || isTyping.value || !currentTopic.value) return

  const isFirstMessage = currentTopic.value.messages.length === 0

  // 添加用户消息
  currentTopic.value.messages.push({
    role: 'user',
    content,
    time: getCurrentTime()
  })

  // 更新话题标题
  if (isFirstMessage) {
    updateTopicTitle(currentTopicId.value, content)
  }
  
  inputMessage.value = ''
  isTyping.value = true
  waitingFirstChunk.value = true
  scrollToBottom()

  try {
    currentAiMessageIndex = -1
    pendingChunks = []
    stopTypewriter()

    activeEventSource?.close()
    activeEventSource = chatWithSSE(
      {
        memoryId: currentTopicId.value,
        message: content
      },
      {
        onMessage: (chunk) => {
          ensureAiMessage()
          const currentAiMessage = currentTopic.value?.messages[currentAiMessageIndex]
          if (!currentAiMessage || currentAiMessage.role !== 'ai') return
          pendingChunks.push(chunk)
          startTypewriter()
        },
        onError: (error) => {
          console.error('发送消息失败:', error)
          const currentAiMessage = currentTopic.value?.messages[currentAiMessageIndex]
          if (currentAiMessage && currentAiMessage.role === 'ai') {
            if (!currentAiMessage.content) {
              currentAiMessage.content = '抱歉，网络出现问题，请稍后再试。'
            }
          } else if (currentTopic.value) {
            currentTopic.value.messages.push({
              role: 'ai',
              content: '抱歉，网络出现问题，请稍后再试。',
              time: getCurrentTime()
            })
          }
          activeEventSource = null
          waitingFirstChunk.value = false
          if (pendingChunks.length === 0) {
            stopTypewriter()
            isTyping.value = false
          }
          scrollToBottom()
        },
        onClose: () => {
          activeEventSource = null
          waitingFirstChunk.value = false
          if (pendingChunks.length === 0) {
            stopTypewriter()
            isTyping.value = false
          }
          scrollToBottom()
        }
      }
    )
  } catch (error) {
    console.error('发送消息失败:', error)
    waitingFirstChunk.value = false
    currentTopic.value.messages.push({
      role: 'ai',
      content: '抱歉，网络出现问题，请稍后再试。',
      time: getCurrentTime()
    })
  } finally {
    if (!activeEventSource) {
      isTyping.value = false
      scrollToBottom()
    }
  }
}

// 快捷消息
const sendQuickMessage = (content: string) => {
  inputMessage.value = content
  sendMessage()
}
</script>

<style lang="scss" scoped>
// 科技感配色 - 白色系
$bg-light: #f8f9fc;
$bg-secondary: #ffffff;
$bg-card: #ffffff;
$bg-sidebar: #f3f4f6;
$border-color: #e5e7eb;
$text-primary: #1a1a2e;
$text-secondary: #6b7280;
$accent: #00c896;
$accent-dim: rgba(0, 200, 150, 0.08);

.chat-container {
  height: 100vh;
  display: flex;
  background: $bg-light;
}

// 左侧边栏
.sidebar {
  width: 260px;
  background: $bg-sidebar;
  border-right: 1px solid $border-color;
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
}

.sidebar-header {
  padding: 16px;
  border-bottom: 1px solid $border-color;
}

.new-chat-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 12px 16px;
  background: $accent;
  border: none;
  border-radius: 8px;
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;

  &:hover {
    filter: brightness(1.05);
    box-shadow: 0 4px 12px rgba(0, 200, 150, 0.3);
  }
}

.topic-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.topic-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 4px;

  .topic-icon {
    flex-shrink: 0;
    color: $text-secondary;
  }

  .topic-title {
    flex: 1;
    font-size: 13px;
    color: $text-primary;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .topic-delete {
    opacity: 0;
    padding: 4px;
    background: transparent;
    border: none;
    border-radius: 4px;
    color: $text-secondary;
    cursor: pointer;
    transition: all 0.2s;

    &:hover {
      background: rgba(239, 68, 68, 0.1);
      color: #ef4444;
    }
  }

  &:hover {
    background: rgba(0, 0, 0, 0.04);

    .topic-delete {
      opacity: 1;
    }
  }

  &.active {
    background: $bg-secondary;
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);

    .topic-icon {
      color: $accent;
    }
  }
}

.sidebar-footer {
  padding: 12px 16px;
  border-top: 1px solid $border-color;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;

  .user-avatar {
    font-size: 20px;
  }

  .user-name {
    font-size: 13px;
    color: $text-primary;
    font-weight: 500;
  }
}

.sidebar-footer .logout-btn {
  padding: 8px;
  background: transparent;
  border: 1px solid $border-color;
  border-radius: 6px;
  color: $text-secondary;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;

  &:hover {
    border-color: #ef4444;
    color: #ef4444;
    background: rgba(239, 68, 68, 0.05);
  }
}

// 右侧主区域
.chat-main-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-width: 0;
}

// 顶部导航
.chat-header {
  display: flex;
  align-items: center;
  padding: 0 20px;
  height: 56px;
  background: $bg-secondary;
  border-bottom: 1px solid $border-color;
  flex-shrink: 0;
  gap: 16px;

  .sidebar-toggle {
    padding: 8px;
    background: transparent;
    border: none;
    border-radius: 6px;
    color: $text-secondary;
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;

    &:hover {
      background: $bg-light;
      color: $text-primary;
    }
  }

  .chat-title {
    flex: 1;
    font-size: 15px;
    font-weight: 500;
    color: $text-primary;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .header-spacer {
    width: 36px;
  }
}

// 聊天主体
.chat-main {
  flex: 1;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  scroll-behavior: smooth;
}

// 欢迎消息
.welcome-message {
  text-align: center;
  padding: 80px 20px;

  .welcome-icon {
    font-size: 48px;
    margin-bottom: 20px;
  }

  h2 {
    font-size: 22px;
    color: $text-primary;
    margin: 0 0 8px;
    font-weight: 500;
  }

  p {
    color: $text-secondary;
    margin: 0 0 32px;
    font-size: 14px;
  }
}

.feature-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  max-width: 500px;
  margin: 0 auto;
}

.feature-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 16px;
  background: $bg-card;
  border: 1px solid $border-color;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;

  .card-icon {
    font-size: 18px;
  }

  span:last-child {
    font-size: 13px;
    color: $text-secondary;
  }

  &:hover {
    border-color: $accent;
    background: $accent-dim;

    span:last-child {
      color: $text-primary;
    }
  }
}

// 消息行
.message-row {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  max-width: 80%;

  &.user {
    flex-direction: row-reverse;
    margin-left: auto;

    .message-bubble {
      background: $accent;
      color: #fff;
      border-radius: 16px 16px 4px 16px;
    }

    .message-avatar {
      background: $accent;
      color: #fff;
    }

    .message-time {
      text-align: right;
    }
  }

  &.ai {
    .message-bubble {
      background: $bg-card;
      color: $text-primary;
      border: 1px solid $border-color;
      border-radius: 16px 16px 16px 4px;
    }

    .message-avatar {
      background: $bg-light;
      border: 1px solid $border-color;
    }
  }
}

.message-avatar {
  width: 36px;
  height: 36px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.message-bubble {
  padding: 12px 16px;
  font-size: 14px;
  line-height: 1.7;
  word-break: break-word;

  :deep(code) {
    background: $accent-dim;
    color: #059669;
    padding: 2px 6px;
    border-radius: 4px;
    font-family: 'JetBrains Mono', 'Fira Code', monospace;
    font-size: 13px;
  }

  :deep(pre) {
    background: #1e1e2e;
    color: #d4d4d4;
    padding: 16px;
    border-radius: 8px;
    border: 1px solid $border-color;
    overflow-x: auto;
    margin: 10px 0;

    code {
      background: none;
      padding: 0;
      color: inherit;
    }
  }

  :deep(a) {
    color: #2563eb;
    text-decoration: underline;
    text-underline-offset: 2px;
  }

  :deep(h2), :deep(h3), :deep(h4) {
    margin: 16px 0 8px 0;
    font-weight: 600;
    color: $text-primary;
  }

  :deep(h2) { font-size: 18px; }
  :deep(h3) { font-size: 16px; }
  :deep(h4) { font-size: 15px; }

  :deep(ul), :deep(ol) {
    margin: 8px 0;
    padding-left: 24px;
  }

  :deep(li) {
    margin: 6px 0;
    line-height: 1.6;
  }

  :deep(ol) {
    list-style-type: decimal;
  }

  :deep(ul) {
    list-style-type: disc;
  }

  &.typing {
    display: flex;
    gap: 6px;
    padding: 16px 20px;
  }
}

.typing-dot {
  width: 6px;
  height: 6px;
  background: $accent;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out;

  &:nth-child(2) {
    animation-delay: 0.2s;
  }

  &:nth-child(3) {
    animation-delay: 0.4s;
  }
}

@keyframes typing {
  0%, 60%, 100% {
    transform: translateY(0);
    opacity: 0.3;
  }
  30% {
    transform: translateY(-6px);
    opacity: 1;
  }
}

.message-time {
  font-size: 11px;
  color: $text-secondary;
  padding: 0 4px;
}

// 底部输入区域
.chat-footer {
  padding: 16px 24px 20px;
  background: $bg-secondary;
  border-top: 1px solid $border-color;
  flex-shrink: 0;
}

.input-wrapper {
  display: flex;
  align-items: flex-end;
  gap: 12px;
  background: $bg-light;
  border: 1px solid $border-color;
  border-radius: 10px;
  padding: 8px 8px 8px 16px;
  transition: all 0.2s;

  &:focus-within {
    border-color: $accent;
    box-shadow: 0 0 0 3px rgba(0, 200, 150, 0.1);
  }

  textarea {
    flex: 1;
    border: none;
    background: transparent;
    resize: none;
    font-size: 14px;
    line-height: 1.5;
    max-height: 120px;
    padding: 8px 0;
    color: $text-primary;

    &:focus {
      outline: none;
    }

    &::placeholder {
      color: #9ca3af;
    }
  }
}

.send-btn {
  width: 40px;
  height: 40px;
  border: none;
  background: $accent;
  border-radius: 8px;
  color: #fff;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  flex-shrink: 0;

  &:hover:not(:disabled) {
    filter: brightness(1.05);
    box-shadow: 0 4px 12px rgba(0, 200, 150, 0.3);
  }

  &:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }
}

.input-hint {
  text-align: center;
  font-size: 11px;
  color: $text-secondary;
  margin: 10px 0 0;
}
</style>
