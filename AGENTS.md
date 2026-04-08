# AI 辅助编程项目 - Copilot Instructions

## 项目概述

这是一个基于 **Spring Boot 3.5 + LangChain4j** 的 AI 编程助手后端服务，旨在帮助用户解答编程学习和求职面试相关问题。

### 核心功能
- 🎯 编程学习路线规划
- 📚 项目学习建议
- 💼 程序员求职指南（简历优化、投递技巧）
- 📝 高频面试题与面试技巧分享

## 技术栈

| 组件 | 技术 |
|------|------|
| 框架 | Spring Boot 3.5.3 |
| JDK | Java 21 |
| AI 框架 | LangChain4j 1.12.x |
| LLM | 阿里通义千问 (Qwen) via DashScope |
| 数据库 | MySQL + MyBatis-Plus |
| 工具 | Lombok, Jsoup |

## 项目结构

```
backend/
├── src/main/java/org/backend/
│   ├── ai/                    # AI 服务核心
│   │   ├── AICodeHelperService.java    # AI 服务接口
│   │   ├── AICodeHelperFactory.java    # AI 服务工厂 (AiServices 配置)
│   │   ├── AICoderHelper.java          # 基础聊天服务
│   │   └── PersistentChatMemoryStore.java  # 持久化对话记忆
│   ├── guardrail/             # 输入/输出安全检查
│   ├── Mapper/                # MyBatis-Plus Mapper
│   ├── Mcp/                   # MCP (Model Context Protocol) 配置
│   ├── Model/                 # 数据模型
│   ├── Rag/                   # RAG 检索增强生成配置
│   └── tools/                 # LangChain4j 自定义工具
├── src/main/resources/
│   ├── application.yml        # 主配置
│   ├── system-prompt.txt      # AI 系统提示词
│   └── docs/                  # RAG 知识库文档
└── pom.xml
```

## 开发指南

### 构建与运行

```bash
# 进入项目目录
cd backend

# 构建项目 (Windows)
mvnw.cmd clean package

# 运行应用 (需要先配置 MySQL)
mvnw.cmd spring-boot:run

# 运行测试
mvnw.cmd test
```

### 配置要点

1. **数据库**: 需要本地 MySQL，创建 `ai_coder` 数据库
2. **API Keys**: 在 `application.yml` 或 `application-local.yml` 中配置：
   - `langchain4j.community.dashscope.chat-model.api-key` (通义千问)
   - `bigmodel.api-key` (智谱 MCP 服务)

### 代码规范

#### AI 服务开发
- 使用 `AICodeHelperService` 接口定义 AI 交互方法
- 通过 `@SystemMessage` 注解加载系统提示词
- 使用 `@MemoryId` 支持多会话管理

#### 添加新工具 (Tool)
```java
// 在 tools/ 目录下创建新工具类
@Slf4j
public class YourTool {
    @Tool(name = "toolName", value = "工具描述...")
    public String yourMethod(@P("参数描述") String param) {
        // 实现逻辑
    }
}
```

在 `AICodeHelperFactory` 中注册工具：
```java
.tools(new InterviewQuestionTool(), new YourTool())
```

#### RAG 文档
- 知识库文档放在 `src/main/resources/docs/` 目录
- 支持 Markdown 格式
- 文档会自动分段并向量化存储

#### Guardrail (安全检查)
- 输入检查: `SafeInputGuardrail` - 过滤敏感词、限制长度
- 输出检查: `SafeOutputGuardrail` - 确保输出安全

## 常见问题

### 测试失败
- 确保 MySQL 服务已启动
- 检查 `application.yml` 中数据库连接配置
- 确保 API Keys 有效

### MCP 连接问题
- 检查网络连接
- 验证智谱 API Key 有效性

## 相关文档

- [LangChain4j 文档](https://docs.langchain4j.dev/)
- [通义千问 API](https://help.aliyun.com/zh/dashscope/)
- [Spring Boot 文档](https://docs.spring.io/spring-boot/docs/current/reference/html/)
