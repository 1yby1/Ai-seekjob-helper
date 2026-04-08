package org.backend.Controller;

import org.backend.Model.Conversation;
import org.backend.service.ConversationService;
import org.backend.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/conversation")
public class ConversationController {

    @Resource
    private ConversationService conversationService;

    /**
     * 创建新对话
     */
    @PostMapping
    public ResponseEntity<Conversation> createConversation(
            @RequestHeader(value = "Authorization") String authHeader,
            @RequestBody Map<String, String> body
    ) {
        Long userId = extractUserId(authHeader);
        String memoryId = body.get("memoryId");
        String title = body.getOrDefault("title", "新对话");
        
        if (memoryId == null || memoryId.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "memoryId 不能为空");
        }
        
        Conversation conversation = conversationService.createConversation(userId, memoryId, title);
        return ResponseEntity.ok(conversation);
    }

    /**
     * 获取用户的所有对话列表
     */
    @GetMapping
    public ResponseEntity<List<Conversation>> getConversations(
            @RequestHeader(value = "Authorization") String authHeader
    ) {
        Long userId = extractUserId(authHeader);
        List<Conversation> conversations = conversationService.getConversationsByUserId(userId);
        return ResponseEntity.ok(conversations);
    }

    /**
     * 更新对话标题
     */
    @PutMapping("/{memoryId}/title")
    public ResponseEntity<Map<String, Object>> updateTitle(
            @RequestHeader(value = "Authorization") String authHeader,
            @PathVariable String memoryId,
            @RequestBody Map<String, String> body
    ) {
        extractUserId(authHeader); // 验证登录状态
        String title = body.get("title");
        
        if (title == null || title.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "标题不能为空");
        }
        
        boolean success = conversationService.updateTitle(memoryId, title);
        return ResponseEntity.ok(Map.of("success", success));
    }

    /**
     * 删除对话
     */
    @DeleteMapping("/{memoryId}")
    public ResponseEntity<Map<String, Object>> deleteConversation(
            @RequestHeader(value = "Authorization") String authHeader,
            @PathVariable String memoryId
    ) {
        Long userId = extractUserId(authHeader);
        boolean success = conversationService.deleteConversation(memoryId, userId);
        return ResponseEntity.ok(Map.of("success", success));
    }

    /**
     * 从 Authorization header 提取用户 ID
     */
    private Long extractUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "缺少认证Token");
        }
        
        String token = authHeader.substring(7);
        Long userId = JwtUtil.getUserIdFromToken(token);
        
        if (userId == null || !JwtUtil.validateToken(token)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "无效或过期的Token");
        }
        
        return userId;
    }
}
