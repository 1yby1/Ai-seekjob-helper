package org.backend.ai.guardrail;

import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.guardrail.InputGuardrail;
import dev.langchain4j.guardrail.InputGuardrailResult;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class SafeInputGuardrail implements InputGuardrail {

    private static final List<String> BLACKLIST_KEYWORDS = Arrays.asList(
            "暴力", "自残", "非法", "政治敏感"
    );

    @Override
    public InputGuardrailResult validate(UserMessage userMessage) {
        String text = userMessage.singleText().toLowerCase();

        for (String keyword : BLACKLIST_KEYWORDS) {
            if (text.contains(keyword)) {
                return fatal("抱歉，您的输入包含敏感词汇 (" + keyword + ")，请修改后再试。");
            }
        }

        return success();
    }
}
