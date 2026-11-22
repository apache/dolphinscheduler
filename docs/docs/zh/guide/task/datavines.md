# Datavines 任务

## 概述

[Datavines](https://github.com/datavane/datavines) 是一个数据质量和数据可观测性平台。Datavines 任务插件用于在 DolphinScheduler 中触发已有的 Datavines 数据质量作业，并等待作业执行结果。

插件使用 Datavines OpenAPI：

- `POST /api/v1/openapi/job/execute/{jobId}`
- `GET /api/v1/openapi/job/execution/status/{executionId}`
- `GET /api/v1/openapi/job/execution/result/{executionId}`

## 前置条件

1. 已部署并启动 Datavines 服务。
2. 已在 Datavines 中创建数据质量作业，并拿到作业 ID。
3. 已创建 Datavines OpenAPI Token。
4. DolphinScheduler Worker 可以访问 Datavines 服务地址。

## 创建 Datavines 任务

1. 创建或编辑工作流，拖入一个 Datavines 节点。
2. 填写 Datavines 地址、作业 ID 和 Token。
3. 根据需要配置轮询间隔、超时时间和重试次数。
4. 保存工作流并运行。

## 参数说明

|      参数      | 必填 |           默认值           |                      说明                      |
|--------------|----|-------------------------|----------------------------------------------|
| Datavines 地址 | 是  | `http://127.0.0.1:5600` | Datavines 服务地址，必须以 `http://` 或 `https://` 开头 |
| 作业 ID        | 是  | -                       | Datavines 数据质量作业 ID                          |
| Token        | 是  | -                       | Datavines OpenAPI Token                      |
| 轮询间隔（秒）      | 否  | `2`                     | 查询执行状态的间隔                                    |
| 超时时间（秒）      | 否  | `3600`                  | 等待作业完成的最长时间                                  |
| 重试次数         | 否  | `3`                     | 接口调用失败时的重试次数                                 |

## 输出参数

任务会把以下值写入任务变量池：

|      参数       |                   说明                    |
|---------------|-----------------------------------------|
| `executionId` | Datavines 执行 ID                         |
| `result`      | Datavines 执行结果，例如 `SUCCESS` 或 `FAILURE` |

下游任务可以通过以下方式使用：

```text
${executionId}
${result}
```

## 结果处理

- Datavines 执行状态为 `SUCCESS` 时，插件会继续查询执行结果。
- 执行结果为 `SUCCESS` 时，DolphinScheduler 任务成功。
- 状态或结果为 `FAILURE`、`CANCELED`、`KILLED` 时，任务失败。
- 超过超时时间时，任务失败并输出超时错误。
- 用户 kill 任务时，插件停止轮询并将任务标记为 kill。

## 常见问题

### Token 无效

检查 Token 是否属于当前地址对应的 Datavines 实例，以及 Token 是否过期。

### 作业 ID 不存在

从 Datavines 作业列表复制作业 ID，并确认作业已经创建。

### 连接超时

检查 DolphinScheduler Worker 是否可以访问 Datavines 地址，以及地址中的端口是否正确。

### 任务长时间处于运行中

如果 Datavines 作业本身需要运行很久，可以适当调大超时时间。插件会持续轮询状态，直到超时。
