export interface ChatStreamParams {
  memoryId: string
  message: string
}

export interface ChatStreamHandlers {
  onMessage: (chunk: string) => void
  onError?: (error: Event | Error) => void
  onClose?: () => void
}

export function chatWithSSE(
  params: ChatStreamParams,
  handlers: ChatStreamHandlers
): EventSource {
  const url = new URL('/api/ai/chat', window.location.origin)
  url.searchParams.set('memoryId', params.memoryId)
  url.searchParams.set('message', params.message)

  // 注意：EventSource 不支持自定义请求头，需要通过URL传递Token
  // 或者使用 fetch + ReadableStream（更好的方案）
  const token = localStorage.getItem('auth_token')
  if (!token) {
    handlers.onError?.(new Error('未登录，请先登录'))
    throw new Error('未登录，请先登录')
  }

  // 使用 fetch API 实现SSE，支持自定义请求头
  return createSSEConnection(url.toString(), token, handlers)
}

function createSSEConnection(
  url: string,
  token: string,
  handlers: ChatStreamHandlers
): EventSource {
  // 创建一个兼容EventSource接口的对象
  const controller = new AbortController()
  let hasReceivedMessage = false
  let buffer = '' // 用于处理跨chunk的数据
  
  // 使用fetch实现流式读取
  fetch(url, {
    method: 'GET',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Accept': 'text/event-stream'
    },
    signal: controller.signal
  })
    .then(async (response) => {
      if (!response.ok) {
        throw new Error(`HTTP ${response.status}: ${response.statusText}`)
      }

      const reader = response.body?.getReader()
      const decoder = new TextDecoder()

      if (!reader) {
        throw new Error('无法读取响应流')
      }

      while (true) {
        const { done, value } = await reader.read()
        
        if (done) {
          handlers.onClose?.()
          break
        }

        // 将新数据追加到缓冲区
        buffer += decoder.decode(value, { stream: true })
        
        // 按SSE事件分割（事件之间用双换行分隔）
        const events = buffer.split('\n\n')
        
        // 最后一个可能是不完整的事件，保留在缓冲区
        buffer = events.pop() || ''
        
        for (const event of events) {
          if (!event.trim()) continue
          
          // 解析事件中的所有data行，并合并它们
          const dataLines: string[] = []
          const lines = event.split('\n')
          
          for (const line of lines) {
            if (line.startsWith('data:')) {
              // 保留data:后的内容（包括空格，但去掉开头的一个空格）
              const content = line.substring(5)
              dataLines.push(content.startsWith(' ') ? content.substring(1) : content)
            }
          }
          
          if (dataLines.length === 0) continue
          
          // 多行data用换行符连接
          const data = dataLines.join('\n')
          
          if (!data) continue
          
          hasReceivedMessage = true

          if (data === '[DONE]') {
            handlers.onClose?.()
            controller.abort()
            return
          }

          handlers.onMessage(data)
        }
      }
    })
    .catch((error) => {
      if (error.name === 'AbortError') {
        return // 正常关闭
      }
      
      if (!hasReceivedMessage) {
        handlers.onError?.(error)
      }
    })

  // 返回兼容EventSource的对象
  return {
    close: () => controller.abort(),
    readyState: 1, // OPEN
    url,
    withCredentials: false,
    CONNECTING: 0,
    OPEN: 1,
    CLOSED: 2,
    addEventListener: () => {},
    removeEventListener: () => {},
    dispatchEvent: () => false,
    onmessage: null,
    onerror: null,
    onopen: null
  } as EventSource
}
