
# 配置参数详解:
- `webHook` : 必填项, Telegram Bots Web Hook, 已包含 `BotToken`.
- `botToken` : 必填项, [从哪获取Telegram机器人Token?](https://core.telegram.org/bots#3-how-do-i-create-a-bot).
- `chatId` : 必填项, TG 频道 Chat ID.
- `parseMode` : 必填项, [Telegram bots api #sendMessage](https://core.telegram.org/bots/api#sendmessage)
- `IsEnableProxy` : 非必须, 是否启用代理.
- `Proxy` : 非必须, 代理 Host.
- `Port` : 非必须, 代理 Port.
- `User`: 非必须, 代理如果鉴权, 用户名.
- `Password`: 代理如果鉴权, 密码.

# 如何创建 Telegram Bot
See [TG API Document](https://core.telegram.org/api)

# 如何订阅 Telegram Channel.
`APP` 或者 `PC` 端建立频道(`Channel`). 将创建的 `Telegram Bot` 拉到频道内,并设置为管理员，给发送消息的权限.

