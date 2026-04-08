import { http } from './request'

export interface Conversation {
  id: number
  userId: number
  memoryId: string
  title: string
}

/**
 * 创建新对话
 */
export async function createConversation(memoryId: string, title: string = '新对话'): Promise<Conversation> {
  const response = await http.post<Conversation>('/api/conversation', { memoryId, title })
  return response.data
}

/**
 * 获取用户的所有对话列表
 */
export async function getConversations(): Promise<Conversation[]> {
  const response = await http.get<Conversation[]>('/api/conversation')
  return response.data
}

/**
 * 更新对话标题
 */
export async function updateConversationTitle(memoryId: string, title: string): Promise<{ success: boolean }> {
  const response = await http.put<{ success: boolean }>(`/api/conversation/${memoryId}/title`, { title })
  return response.data
}

/**
 * 删除对话
 */
export async function deleteConversation(memoryId: string): Promise<{ success: boolean }> {
  const response = await http.delete<{ success: boolean }>(`/api/conversation/${memoryId}`)
  return response.data
}
