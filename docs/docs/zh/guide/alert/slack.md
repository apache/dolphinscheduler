# Slack

如果您需要使用到Slack进行告警，请在告警实例管理里创建告警实例，选择 Slack 插件。Slack的配置样例如下:

![alert-slack](../../../../img/new_ui/dev/alert/alert_slack.png)

## 参数配置

* Webhook

  > 复制APP的`Incoming Webhooks`地址,如下图所示：

  ![alert-slack-webhook](../../../../img/new_ui/dev/alert/alert_slack_webhook_addr.png)

* 用户名

  > 发送人的昵称（已过时），由于Slack更新原因目前会使用APP名称作为发件人

## 如何获取Webhook地址

请参考官网文章[《Slack：如何通过Incoming Webhooks发送消息？》](https://api.slack.com/messaging/webhooks)进行创建。

### 新建一个Slack APP

打开[Slack官网](https://api.slack.com/apps/new)新建一个APP。

### 激活Incoming Webhooks设置

新建APP后，在APP页面左侧，选择`Feature`栏目下的`Incoming Webhooks`，点击激活`Activate Incoming Webhooks`到`ON`。
![alert-slack-webhook](../../../../img/new_ui/dev/alert/alert_slack_enable.png)

### 创建一个Incoming Webhook

创建一个新的`Incoming Webhook`，并选择消息的发送分组。
![select-post-group](../../../../img/new_ui/dev/alert/alert_slack_new.png)

### 获取Incoming Webhooks地址

复制APP的`Incoming Webhooks`地址,如下图所示：
`Incoming Webhooks`的格式为：`https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXXXXXXXXXXXXXX`

![alert-slack-webhook](../../../../img/new_ui/dev/alert/alert_slack_webhook_addr.png)

参考：[Slack：如何通过Incoming Webhooks发送消息？](https://api.slack.com/messaging/webhooks)
