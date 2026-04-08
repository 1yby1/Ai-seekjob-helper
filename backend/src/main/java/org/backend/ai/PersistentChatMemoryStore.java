package org.backend.ai;

import dev.langchain4j.data.message.ChatMessageDeserializer;
import dev.langchain4j.data.message.ChatMessageSerializer;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.backend.Mapper.ChatMessageMapper;
import org.backend.Mapper.ConversationMapper;
import org.backend.Model.ChatMessage;
import org.springframework.stereotype.Component;
import jakarta.annotation.Resource;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import java.util.ArrayList;
import java.util.List;

@Component
public class PersistentChatMemoryStore implements ChatMemoryStore {

    @Resource
    private ChatMessageMapper chatMessageMapper;

    @Resource
    private ConversationMapper conversationMapper;

    @Override
    public List<dev.langchain4j.data.message.ChatMessage> getMessages(Object memoryId) {
        // 解析memoryId（格式：userId:topicId）
        String[] parts = memoryId.toString().split(":");
        if (parts.length != 2) {
            // 格式错误，返回空列表
            System.err.println("Invalid memoryId format: " + memoryId + ", expected format: userId:topicId");
            return new ArrayList<>();
        }
        
        try {
            Long userId = Long.parseLong(parts[0]);
            String topicId = parts[1];

            QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId).eq("memory_id", topicId);
            ChatMessage chatMessageRecord = chatMessageMapper.selectOne(queryWrapper);

            if (chatMessageRecord == null || chatMessageRecord.getMessagesJson() == null 
                || chatMessageRecord.getMessagesJson().trim().isEmpty() 
                || "[]".equals(chatMessageRecord.getMessagesJson())) {
                return new ArrayList<>();
            }

            // 将 JSON 反序列化成 List<ChatMessage>
            return ChatMessageDeserializer.messagesFromJson(chatMessageRecord.getMessagesJson());
        } catch (NumberFormatException e) {
            System.err.println("Failed to parse userId from memoryId: " + memoryId);
            return new ArrayList<>();
        }
    }

    @Override
    public void updateMessages(Object memoryId, List<dev.langchain4j.data.message.ChatMessage> messages) {
        // 解析memoryId（格式：userId:topicId）
        String[] parts = memoryId.toString().split(":");
        if (parts.length != 2) {
            System.err.println("Invalid memoryId format: " + memoryId + ", expected format: userId:topicId");
            return;
        }
        
        try {
            Long userId = Long.parseLong(parts[0]);
            String topicId = parts[1];

            String messagesJson = ChatMessageSerializer.messagesToJson(messages);

            // 检查是否存在记录
            QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId).eq("memory_id", topicId);
            ChatMessage existingRecord = chatMessageMapper.selectOne(queryWrapper);

            if (existingRecord != null) {
                // 更新已存在的记录
                existingRecord.setMessagesJson(messagesJson);
                chatMessageMapper.updateById(existingRecord);
                System.out.println("Updated chat messages for userId: " + userId + ", topicId: " + topicId);
            } else {
                // 插入新记录
                ChatMessage newRecord = new ChatMessage();
                newRecord.setUserId(userId);
                newRecord.setMemoryId(topicId);
                newRecord.setMessagesJson(messagesJson);
                chatMessageMapper.insert(newRecord);
                System.out.println("Inserted new chat messages for userId: " + userId + ", topicId: " + topicId);
            }
        } catch (NumberFormatException e) {
            System.err.println("Failed to parse userId from memoryId: " + memoryId);
        }
    }

    @Override
    public void deleteMessages(Object memoryId) {
        // 解析memoryId（格式：userId:topicId）
        String[] parts = memoryId.toString().split(":");
        if (parts.length != 2) {
            // 兼容旧格式或异常情况
            QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("memory_id", memoryId.toString());
            chatMessageMapper.delete(queryWrapper);
            return;
        }
        
        Long userId = Long.parseLong(parts[0]);
        String topicId = parts[1];
        
        // 删除时也要验证用户ID，避免误删其他用户数据
        QueryWrapper<ChatMessage> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).eq("memory_id", topicId);
        chatMessageMapper.delete(queryWrapper);
    }
}
