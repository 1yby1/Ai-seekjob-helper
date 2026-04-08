package org.backend.ai.Listener;

import dev.langchain4j.model.chat.listener.ChatModelErrorContext;
import dev.langchain4j.model.chat.listener.ChatModelListener;
import dev.langchain4j.model.chat.listener.ChatModelRequestContext;
import dev.langchain4j.model.chat.listener.ChatModelResponseContext;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class ChatModelListenerConfig {

    @Bean
    ChatModelListener chatModelListener() {
        return new ChatModelListener() {

            private static final Logger log = LoggerFactory.getLogger(ChatModelListenerConfig.class);
            @Override
            public void onRequest(ChatModelRequestContext request) {
                log.info("ChatModel 请求: {}", request.chatRequest());
            }

            @Override
            public void onResponse(ChatModelResponseContext response) {
                log.info("ChatModel 响应: {}", response.chatResponse());
            }
            @Override
            public void onError(ChatModelErrorContext error) {
                log.info("onError(): {}",error.error().getMessage());
            }
        };
    }
}
