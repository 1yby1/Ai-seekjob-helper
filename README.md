# AI 辅助编程助手 (AI-Coder)

本项目是一个全栈 AI 编程学习与求职面试辅助系统，旨在帮助学习者解答编程难点、规划学习路线、提供项目构建建议以及程序员求职技巧（包含简历优化、高频面试题等）。

项目分为前后端两部分：
- **后端**：基于 Java 21 + Spring Boot 3.5 + LangChain4j 构建的 AI 智能体服务。
- **前端**：基于 Vue 3 + Vite + TypeScript 构建的响应式对话与学习应用。

## 🛠️ 技术栈

### 后端 (Backend)
- **核心框架**: Java 21, Spring Boot 3.5.3
- **AI 引擎**: LangChain4j (1.12.x) 
- **LLM 大模型**: 阿里通义千问 (DashScope), 智谱 MCP 服务
- **数据库**: MySQL 8.x + MyBatis-Plus
- **其他工具**: Lombok, Jsoup, RAG 检索增强生成

### 前端 (Frontend)
- **框架**: Vue 3 (Composition API)
- **构建工具**: Vite
- **语言**: TypeScript

## 📁 项目结构

```text
├── backend/                  # Spring Boot 后端服务
│   ├── src/main/java/        # 核心业务逻辑 (AI会话, 工具链, 安全围栏Guardrail, 提示词)
│   ├── src/main/resources/   # 配置文件与 RAG 知识库 (系统提示词, Markdown文档)
│   └── pom.xml               # Maven 依赖配置
├── frontend/                 # Vue 3 前端应用
│   ├── src/api/              # 接口封装 (AI对话, 认证等)
│   ├── src/views/            # 核心视图层 (ChatView聊天页面, 登录页面)
│   ├── package.json          # 依赖配置
│   └── vite.config.ts        # Vite 构建配置
├── README.md                 # 项目说明文档
└── .gitignore                # Git 忽略配置
```

## 🚀 快速启动

### 1. 环境准备
- Java (JDK 21)
- Node.js (v18.0+ 推荐) & npm/pnpm/yarn
- MySQL (8.0+)

### 2. 后端服务启动

1. 创建 MySQL 数据库命名为 `ai_coder`。
2. 在 `backend/src/main/resources/` 目录下创建或修改 `application-local.yml`，设置数据库凭证及模型 API keys：
   ```yaml
   langchain4j:
     community:
       dashscope:
         chat-model:
           api-key: your-qwen-api-key
   bigmodel:
     api-key: your-zhipu-api-key
   spring:
     datasource:
       username: root
       password: yourpassword
   ```
3. 进入后端目录并运行：
   ```bash
   cd backend
   # Windows 系统使用 .mvnw.cmd
   ./mvnw spring-boot:run
   ```

### 3. 前端服务启动

1. 进入前端目录，安装依赖：
   ```bash
   cd frontend
   npm install
   ```
2. 启动开发服务器：
   ```bash
   npm run dev
   ```

## 🎯 核心功能

- **🚀 学习路径规划**：根据用户的具体情况和目标（如：Java/前端入门）制定合理的学习路线。
- **💡 常见面试题解答**：通过 RAG 技术结合本地的高质量面试题库（位于 `/backend/src/main/resources/docs/`），回答各类八股文、算法解析等。
- **💻 项目实战指导**：解决在个人项目开发时遇到的技术卡点（AI 代码辅助、Bug 排查等）。
- **📝 简历优化与求职**：提供简历编写技巧、投递策略等，助力春招/秋招。

## 🔒 约束与扩展
- **安全拦截防线 (Guardrail)**：系统在输入层限制用户的敏感词查询并在输出层把控回答安全性。
- **动态插件体系 (Tool/MCP)**：借助于 LangChain4j 的能力实现了自定义 Tool 并支持 MCP（Model Context Protocol）扩展。