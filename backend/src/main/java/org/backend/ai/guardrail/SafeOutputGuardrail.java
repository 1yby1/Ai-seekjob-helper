package org.backend.ai.guardrail;

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.guardrail.OutputGuardrail;
import dev.langchain4j.guardrail.OutputGuardrailResult;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class SafeOutputGuardrail implements OutputGuardrail {

    private static final List<String> SENSITIVE_KEYWORDS = Arrays.asList(

    );

    @Override
    public OutputGuardrailResult validate(AiMessage aiMessage) {
        String text = aiMessage.text();

        for (String keyword : SENSITIVE_KEYWORDS) {
            if (text.contains(keyword)) {
                return fatal("抱歉，生成的回复涉及敏感词汇 (" + keyword + ")，已被拦截。");
            }
        }

        return success();
    }
}
