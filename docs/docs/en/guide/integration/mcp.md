# Community MCP Integrations

The [Model Context Protocol (MCP)](https://modelcontextprotocol.io/) allows AI applications to call external systems through structured tools. A DolphinScheduler MCP server translates those tool calls into requests to the DolphinScheduler [Open API](../api/open-api.md).

> The integrations on this page are maintained by their respective communities. They are not Apache DolphinScheduler components, and listing them here does not imply endorsement by the Apache Software Foundation. Check each project's compatibility, security, and support policy before using it.

## Available Integrations

|                                       Project                                       |                                                                    Focus                                                                    |                                           Authentication                                            |
|-------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------|
| [iflytek/dolphin-mcp-pilot](https://github.com/iflytek/dolphin-mcp-pilot)           | Curated tools for projects, workflow and DAG creation, schedules, instances, resources, logs, monitoring, and raw API access                | DolphinScheduler API token or user name and password; supports per-request credentials in HTTP mode |
| [ocean-zhc/dolphinscheduler-mcp](https://github.com/ocean-zhc/dolphinscheduler-mcp) | Tools that expose DolphinScheduler REST API operations, including project, process, task, schedule, resource, and administration operations | DolphinScheduler API token                                                                          |

Follow the installation and client configuration instructions in the selected project's repository. Those instructions are versioned with the integration and are more likely to match its current transport and DolphinScheduler compatibility requirements.

## Before You Connect an AI Client

1. Confirm that the integration supports your DolphinScheduler and MCP client versions.
2. Create dedicated DolphinScheduler credentials with only the permissions required for the intended tools. Avoid using an administrator account for routine agent access.
3. Keep the MCP endpoint on a trusted network. If remote access is required, protect it with TLS, authentication, and network access controls.
4. Store credentials in environment variables or a secret manager, and never commit them to a repository or shared client configuration.
5. Review or restrict tools that mutate state. Workflow execution, resource updates, force-success operations, and deletion can have production impact.
6. Enable appropriate DolphinScheduler and gateway audit logs, and rotate credentials according to your organization's policy.

For problems in an MCP server or its setup, use that project's issue tracker. Use the Apache DolphinScheduler issue tracker only when the underlying behavior can be reproduced against DolphinScheduler itself.
