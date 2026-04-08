package org.backend.Controller;

import org.backend.ai.AICodeHelperService;
import org.backend.utils.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;

import jakarta.annotation.Resource;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Resource
    private AICodeHelperService aiCodeHelperService;

    @GetMapping(value = "/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestParam String memoryId,
            @RequestParam String message
    ) {
        // 解析Token获取用户ID
        Long userId = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7); // 移除 "Bearer " 前缀
            userId = JwtUtil.getUserIdFromToken(token);
            
            if (userId == null || !JwtUtil.validateToken(token)) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "无效或过期的Token");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "缺少认证Token");
        }
        
        // 组合 userId 和 memoryId 形成完整的会话标识
        String fullMemoryId = userId + ":" + memoryId;
        
        return aiCodeHelperService.chatStream(fullMemoryId, message)
                .map(chunk -> ServerSentEvent.<String>builder()
                        .data(chunk)
                        .build());
    }

}
