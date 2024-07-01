# 钉钉

如果您需要使用到钉钉进行告警，请在告警实例管理里创建告警实例，选择 DingTalk 插件。钉钉的配置样例如下:

![alert-dingtalk](../../../../img/new_ui/dev/alert/alert_dingtalk.png)

参数配置

* Webhook

  > 格式如下：https://oapi.dingtalk.com/robot/send?access_token=XXXXXX

* Keyword

  > 安全设置的自定义关键词

* Secret

  > 安全设置的加签

* 消息类型

  > 支持 text 和 markdown 两种类型

自定义机器人发送消息时，可以通过手机号码指定“被@人列表”。在“被@人列表”里面的人员收到该消息时，会有@消息提醒。免打扰会话仍然通知提醒，首屏出现“有人@你”
* @Mobiles

> 被@人的手机号
> * @UserIds
> 被@人的用户userid
> * @All
> 是否@所有人

[钉钉自定义机器人接入开发文档](https://open.dingtalk.com/document/robots/custom-robot-access)
