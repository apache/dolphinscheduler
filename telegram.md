Telegram
========

If you need `Telegram` to alert, create an alert instance in the alert instance management, and choose the `Telegram` plugin. The following shows the `Telegram` configuration example:

![alert-telegram](/img/new_ui/dev/alert/alert_telegram.png)

Parameter Configuration
-----------------------

<table class="wrapped confluenceTable"><colgroup><col><col></colgroup><tbody><tr><th class="confluenceTh">Parameter</th><th class="confluenceTh">Description</th></tr><tr><td class="confluenceTd">WebHook</td><td class="confluenceTd">The WebHook of Telegram when use robot to send message.</td></tr><tr><td class="confluenceTd">BotToken</td><td class="confluenceTd">The access token of robot.</td></tr><tr><td class="confluenceTd">Chat ID</td><td class="confluenceTd">Sub Telegram Channel Chat ID.</td></tr><tr><td colspan="1" class="confluenceTd">Parse Mode</td><td colspan="1" class="confluenceTd">Message parse type (support txt, markdown, markdownV2, html).</td></tr><tr><td colspan="1" class="confluenceTd">Enable Proxy</td><td colspan="1" class="confluenceTd">Enable proxy sever.</td></tr><tr><td colspan="1" class="confluenceTd">Proxy</td><td colspan="1" class="confluenceTd">The proxy address of the proxy server.</td></tr><tr><td colspan="1" class="confluenceTd">Port</td><td colspan="1" class="confluenceTd">The proxy port of Proxy-Server.</td></tr><tr><td colspan="1" class="confluenceTd">User</td><td colspan="1" class="confluenceTd">Authentication (Username) for the proxy server.</td></tr><tr><td colspan="1" class="confluenceTd">Password</td><td colspan="1" class="confluenceTd">Authentication (Password) for the proxy server.</td></tr></tbody></table>

> **NOTE**：The webhook needs to be able to receive and use the same JSON body of HTTP POST that DolphinScheduler constructs and the following shows the JSON body:

```json
{
    "text": "[{\"projectId\":1,\"projectName\":\"p1\",\"owner\":\"admin\",\"processId\":35,\"processDefinitionCode\":4928367293568,               \"processName\":\"s11-3-20220324084708668\",\"taskCode\":4928359068928,\"taskName\":\"s1\",\"taskType\":\"SHELL\",               \"taskState\":\"FAILURE\",\"taskStartTime\":\"2022-03-24 08:47:08\",\"taskEndTime\":\"2022-03-24 08:47:09\",               \"taskHost\":\"192.168.1.103:1234\",\"logPath\":\"\"}]",
    "chat_id": "chat id number"
}
```

References:

*   [Telegram Application Bot Guide](https://core.telegram.org/bots)
*   [Telegram Bots Api](https://core.telegram.org/bots/api)
*   [Telegram SendMessage Api](https://core.telegram.org/bots/api#sendmessage)