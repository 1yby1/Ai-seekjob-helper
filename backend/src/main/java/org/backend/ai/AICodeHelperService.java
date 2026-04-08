package org.backend.ai;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.guardrail.InputGuardrails;
import dev.langchain4j.service.guardrail.OutputGuardrails;
import org.backend.ai.guardrail.SafeInputGuardrail;
import org.backend.ai.guardrail.SafeOutputGuardrail;
import reactor.core.publisher.Flux;


@InputGuardrails({SafeInputGuardrail.class})
@OutputGuardrails(SafeOutputGuardrail.class)
public interface AICodeHelperService {

    @SystemMessage(fromResource = "system-prompt.txt")
    public String chat(@MemoryId  Object memoryId, @UserMessage String UserMessage);

    public String chatWithUserMessage(@MemoryId Object memoryId, @UserMessage String message);

    @SystemMessage(fromResource = "system-prompt.txt")
    public String chatWithRag(@MemoryId Object memoryId, @UserMessage String message);

    Flux<String> chatStream(@MemoryId Object memoryId, @UserMessage String message);
}
