package org.backend.Model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("chat_message")
public class ChatMessage {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    // 用于区分不同用户或会话的标识
    private String memoryId;

    // 存储消息的 JSON 字符串
    private String messagesJson;
}
