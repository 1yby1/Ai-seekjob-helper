package org.backend.service;

import org.backend.Model.Conversation;
import java.util.List;

public interface ConversationService {
    
    /**
     * 创建新对话
     */
    Conversation createConversation(Long userId, String memoryId, String title);
    
    /**
     * 获取用户的所有对话列表
     */
    List<Conversation> getConversationsByUserId(Long userId);
    
    /**
     * 根据 memoryId 获取对话
     */
    Conversation getConversationByMemoryId(String memoryId);
    
    /**
     * 更新对话标题
     */
    boolean updateTitle(String memoryId, String title);
    
    /**
     * 删除对话
     */
    boolean deleteConversation(String memoryId, Long userId);
}
