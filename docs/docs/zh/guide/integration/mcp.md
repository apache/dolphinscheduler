# 社区 MCP 集成

[模型上下文协议（Model Context Protocol，MCP）](https://modelcontextprotocol.io/) 允许 AI 应用通过结构化工具调用外部系统。DolphinScheduler MCP 服务会将工具调用转换为 DolphinScheduler [Open API](../api/open-api.md) 请求。

> 本页所列集成由各自社区维护，不属于 Apache DolphinScheduler 组件。列入本文档不代表 Apache 软件基金会为其背书。使用前，请检查相应项目的兼容性、安全策略和支持政策。

## 可用集成

|                                         项目                                          |                            侧重点                            |                        认证方式                         |
|-------------------------------------------------------------------------------------|-----------------------------------------------------------|-----------------------------------------------------|
| [iflytek/dolphin-mcp-pilot](https://github.com/iflytek/dolphin-mcp-pilot)           | 为项目、工作流与 DAG 创建、调度、实例、资源、日志、监控和原始 API 访问提供经过整理的工具         | DolphinScheduler API Token 或用户名和密码；HTTP 模式支持按请求传递凭据 |
| [ocean-zhc/dolphinscheduler-mcp](https://github.com/ocean-zhc/dolphinscheduler-mcp) | 将 DolphinScheduler REST API 操作暴露为工具，覆盖项目、流程、任务、调度、资源和管理操作 | DolphinScheduler API Token                          |

请按照所选项目仓库中的安装和客户端配置说明进行操作。这些说明会随集成版本更新，能够更准确地反映当前传输方式及 DolphinScheduler 兼容性要求。

## 连接 AI 客户端前的检查

1. 确认集成支持当前使用的 DolphinScheduler 和 MCP 客户端版本。
2. 创建专用的 DolphinScheduler 凭据，并且只授予目标工具所需的权限。日常 Agent 访问应避免使用管理员账号。
3. 将 MCP 端点限制在可信网络内。如需远程访问，请使用 TLS、身份认证和网络访问控制进行保护。
4. 使用环境变量或密钥管理服务保存凭据，切勿将凭据提交到代码仓库或共享的客户端配置中。
5. 审查或限制会修改状态的工具。执行工作流、更新资源、强制任务成功和删除操作都可能影响生产环境。
6. 按需启用 DolphinScheduler 和网关审计日志，并根据组织安全策略轮换凭据。

如果问题来自 MCP 服务或其配置，请在相应项目的 Issue Tracker 中反馈。只有在能够直接通过 DolphinScheduler 复现底层问题时，才应使用 Apache DolphinScheduler Issue Tracker。
