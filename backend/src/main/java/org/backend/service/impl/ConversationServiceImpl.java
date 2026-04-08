package org.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.backend.Mapper.ChatMessageMapper;
import org.backend.Mapper.ConversationMapper;
import org.backend.Model.ChatMessage;
import org.backend.Model.Conversation;
import org.backend.service.ConversationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.util.List;

@Service
public class ConversationServiceImpl implements ConversationService {

    @Resource
    private ConversationMapper conversationMapper;
    
    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Override
    public Conversation createConversation(Long userId, String memoryId, String title) {
        // 检查是否已存在相同 memoryId
        QueryWrapper<Conversation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("memory_id", memoryId);
        Conversation existing = conversationMapper.selectOne(queryWrapper);
        
        if (existing != null) {
            return existing;
        }
        
        Conversation conversation = new Conversation();
        conversation.setUserId(userId);
        conversation.setMemoryId(memoryId);
        conversation.setTitle(title != null ? title : "新对话");
        
        conversationMapper.insert(conversation);
        return conversation;
    }

    @Override
    public List<Conversation> getConversationsByUserId(Long userId) {
        QueryWrapper<Conversation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                   .orderByDesc("created_at");
        return conversationMapper.selectList(queryWrapper);
    }

    @Override
    public Conversation getConversationByMemoryId(String memoryId) {
        QueryWrapper<Conversation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("memory_id", memoryId);
        return conversationMapper.selectOne(queryWrapper);
    }

    @Override
    public boolean updateTitle(String memoryId, String title) {
        UpdateWrapper<Conversation> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("memory_id", memoryId)
                    .set("title", title);
        return conversationMapper.update(null, updateWrapper) > 0;
    }

    @Override
    @Transactional
    public boolean deleteConversation(String memoryId, Long userId) {
        // 验证对话属于该用户
        QueryWrapper<Conversation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("memory_id", memoryId)
                   .eq("user_id", userId);
        Conversation conversation = conversationMapper.selectOne(queryWrapper);
        
        if (conversation == null) {
            return false;
        }
        
        // 删除对话
        conversationMapper.delete(queryWrapper);
        
        // 同时删除相关的聊天消息
        QueryWrapper<ChatMessage> chatMessageQuery = new QueryWrapper<>();
        chatMessageQuery.eq("memory_id", memoryId)
                       .eq("user_id", userId);
        chatMessageMapper.delete(chatMessageQuery);
        
        return true;
    }
}
