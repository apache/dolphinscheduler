# Telegram

如果您需要使用 `Telegram` 进行告警，请在告警实例管理模块创建告警实例，选择 `Telegram` 插件。

`Telegram` 的配置样例如下:

![alert-telegram](../../../../img/new_ui/dev/alert/alert_telegram.png)

参数配置:
* WebHook:

> 使用 Telegram 的机器人，发送消息的 WebHook。
> * botToken
> 创建 Telegram 的机器人，获取的访问令牌。
> * chatId
> 订阅的 Telegram 频道
> * parseMode
> 消息解析类型, 支持: txt、markdown、markdownV2、html
> * EnableProxy
> 开启代理
> * Proxy
> 代理地址
> * Port
> 代理端口
> * User
> 代理鉴权用户
> * Password
> 代理鉴权密码

**注意**：用户配置的 WebHook 需要能够接收和使用与 DolphinScheduler 构造的 HTTP POST 请求 BODY 相同的结构，JSON 结构如下：

```json
{
    "text": "[{\"projectId\":1,\"projectName\":\"p1\",\"owner\":\"admin\",\"processId\":35,\"processDefinitionCode\":4928367293568,\"processName\":\"s11-3-20220324084708668\",\"taskCode\":4928359068928,\"taskName\":\"s1\",\"taskType\":\"SHELL\",\"taskState\":\"FAILURE\",\"taskStartTime\":\"2022-03-24 08:47:08\",\"taskEndTime\":\"2022-03-24 08:47:09\",\"taskHost\":\"192.168.1.103:1234\",\"logPath\":\"\"}]",
    "chat_id": "chat id number"
}
```

[Telegram 如何申请机器人，如何创建频道](https://core.telegram.org/bots)
[Telegram 机器人开发文档](https://core.telegram.org/bots/api)
[Telegram SendMessage 接口文档](https://core.telegram.org/bots/api#sendmessage)

