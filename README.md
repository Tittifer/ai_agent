# AI Agent

基于 `Spring Boot 3` + `Spring AI` 构建的垂直场景智能体示例项目，当前以“恋爱咨询 / 约会规划”场景为主，覆盖多轮对话、RAG 检索增强、工具调用、结构化输出、会话记忆等能力。

项目目标不是只做一个简单聊天机器人，而是验证一条更完整的智能体实现链路：

- 通过 `ChatClient + Advisor + ChatMemory` 组织对话主流程
- 通过 `RAG` 为模型补充领域知识
- 通过 `Tool Calling` 让模型具备执行外部工具的能力
- 通过文件、PDF、网页搜索等工具完成从问答到交付的闭环

## 功能特性

- 多轮对话：支持基于 `conversationId` 的上下文连续对话
- 结构化输出：支持将模型返回结果映射为 Java 对象
- RAG 检索增强：基于本地 Markdown 文档构建知识库
- 查询重写：在检索前对用户问题进行 Query Rewrite
- 工具调用：支持搜索、抓取、下载、文件读写、终端执行、PDF 生成
- 日志观测：自定义 Advisor 记录 AI 请求与响应
- 记忆持久化扩展：提供基于 `Kryo` 的文件式 ChatMemory 实现
- 向量存储扩展：支持本地 `SimpleVectorStore`，并预留 `PgVector` 能力

## 技术栈

- Java 21
- Spring Boot 3.4.4
- Spring Web
- Spring AI 1.0.0
- Spring AI Alibaba DashScope
- Spring AI RAG / Vector Store / MCP Client
- PostgreSQL + PgVector
- LangChain4j DashScope
- Hutool
- Jsoup
- iText PDF
- Knife4j
- Kryo

## 项目结构

```text
src/main/java/com/ai/aiagent
├─ advisor      # 自定义 Advisor，如日志记录、Re-Reading 增强
├─ app          # 智能体主应用入口，封装聊天 / RAG / Tool Calling 能力
├─ chatmemory   # 自定义对话记忆实现
├─ constant     # 常量定义
├─ controller   # Web 接口（当前仅健康检查）
├─ rag          # RAG 相关配置、文档加载、查询增强、向量存储
└─ tool         # 可供模型调用的工具实现与注册
```

## 核心模块

### 1. 智能体主链路

核心入口是 `LoveApp`：

- 普通对话：`doChat`
- 结构化报告：`doChatWithReport`
- RAG 对话：`doChatWithRag`
- 工具调用：`doChatWithTools`

主流程基于 `ChatClient` 构建，并结合：

- `MessageChatMemoryAdvisor`
- `MyLoggerAdvisor`
- `QuestionAnswerAdvisor`
- `ToolCallback[]`

完成不同模式下的对话增强。

### 2. RAG 知识增强

当前默认使用本地 Markdown 文档作为知识源，核心流程如下：

1. `LoveAppDocumentLoader` 加载 `src/main/resources/document/*.md`
2. `MarkdownDocumentReader` 读取文档并补充元数据
3. `MyKeywordEnricher` 自动增强关键词元数据
4. `QueryRewriter` 对用户问题进行查询重写
5. `LoveAppVectorStoreConfig` 将文档写入 `SimpleVectorStore`
6. `QuestionAnswerAdvisor` 在对话时执行检索增强

项目中还预留了两种扩展方案：

- `PgVectorVectorStoreConfig`：接入 PostgreSQL / PgVector
- `LoveAppRagCloudAdvisorConfig`：接入 DashScope 云知识库

### 3. Tool Calling

项目通过 `ToolRegistration` 统一注册工具，并转换为 `ToolCallback[]` 注入模型。

当前已实现工具：

- `WebSearchTool`：网页搜索
- `WebScrapingTool`：网页内容抓取
- `ResourceDownloadTool`：资源下载
- `FileOperationTool`：文件读写
- `TerminalOperationTool`：终端命令执行
- `PDFGenerationTool`：PDF 生成
- `TerminateTool`：任务终止

这使模型不只能够“回答”，还能围绕复杂需求完成信息搜集、结果整理和文档输出。

### 4. 会话记忆

当前 `LoveApp` 默认启用的是内存版 `MessageWindowChatMemory`。

项目同时提供了一个基于文件系统的持久化实现 `FileBasedChatMemory`：

- 使用 `Kryo` 对消息对象进行序列化
- 按 `conversationId` 写入本地文件
- 支持服务重启后恢复历史会话

如果你希望启用持久化记忆，可以在 `LoveApp` 中切换到 `FileBasedChatMemory`。

## 配置说明

默认配置文件：

- `src/main/resources/application.yml`
- `src/main/resources/application-local.yml`

主要配置项包括：

```yaml
spring:
  ai:
    dashscope:
      api-key: your-api-key
      chat:
        options:
          model: qwen-plus
  datasource:
    url: jdbc:postgresql://127.0.0.1:5432/ai_agent
    username: postgres
    password: your-password

search-api:
  api-key: your-searchapi-key
```

建议：

- 不要直接提交真实密钥到仓库
- 本地开发请使用你自己的 API Key
- 如无需 `PgVector`，可先只使用默认本地向量库能力

## 环境要求

- JDK 21
- Maven 3.9+
- PostgreSQL 15+（使用 PgVector 时需要）
- 可用的 DashScope API Key
- 可用的 SearchAPI Key（网页搜索工具需要）

## 快速开始

### 1. 克隆项目

```bash
git clone <your-repo-url>
cd ai-agent
```

### 2. 配置本地参数

修改 `src/main/resources/application-local.yml`，填入你自己的：

- DashScope Key
- PostgreSQL 连接信息
- SearchAPI Key

### 3. 启动项目

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

### 4. 健康检查

项目启动后可访问：

```text
GET http://localhost:8123/api/health
```

返回：

```text
ok
```

## 测试说明

项目提供了基础测试用例，重点覆盖：

- 普通对话
- 结构化输出
- RAG 对话
- Tool Calling
- 向量库配置
- 文件操作工具

运行测试：

```bash
./mvnw test
```

注意：

- 项目要求 `Java 21`
- 涉及模型、数据库、外部搜索服务的测试依赖真实配置
- 如果本地环境缺少相应依赖，部分测试可能无法通过

## 产物目录

运行过程中生成的临时文件默认位于：

```text
tmp/
```

典型产物包括：

- `tmp/chat-memory`：对话记忆文件
- `tmp/file`：文件工具生成内容
- `tmp/download`：下载资源
- `tmp/pdf`：PDF 输出文件

## 当前状态说明

这个项目当前更偏向“智能体能力验证 / 工程实践样例”，而不是完整业务平台。

目前已经具备：

- 智能体主链路
- RAG 知识增强
- 工具调用执行
- 文件式记忆扩展

当前 Web 层接口较少，公开的控制器主要是健康检查。如果后续需要对外提供完整服务，可以继续补充：

- 对话接口
- 报告生成接口
- RAG 检索接口
- 工具执行审计接口
- 会话管理接口

## 后续可扩展方向

- 将 `LoveApp` 能力封装为正式 REST API
- 接入前端页面实现可视化对话
- 接入 Redis / MySQL 做会话与消息持久化
- 为工具调用增加权限控制和审计日志
- 为终端执行、文件读写增加更严格的安全沙箱
- 完整落地 MCP Client 与外部工具生态集成
- 增加流式输出与 SSE 支持

## 参考入口

- 启动类：[AiAgentApplication.java](/d:/ai-agent/src/main/java/com/ai/aiagent/AiAgentApplication.java:1)
- 智能体主类：[LoveApp.java](/d:/ai-agent/src/main/java/com/ai/aiagent/app/LoveApp.java:1)
- 工具注册：[ToolRegistration.java](/d:/ai-agent/src/main/java/com/ai/aiagent/tool/ToolRegistration.java:1)
- RAG 配置：[LoveAppVectorStoreConfig.java](/d:/ai-agent/src/main/java/com/ai/aiagent/rag/LoveAppVectorStoreConfig.java:1)
- 记忆实现：[FileBasedChatMemory.java](/d:/ai-agent/src/main/java/com/ai/aiagent/chatmemory/FileBasedChatMemory.java:1)

## License

当前仓库未单独声明许可证，如需开源发布，建议补充 `LICENSE` 文件。
