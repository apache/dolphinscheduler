# WebHook

如果您需要使用到WebHook进行告警，请在告警实例管理里创建告警实例，选择 WebHook 插件。以企业微信为例，WebHook的配置样例如下：

![webhook-alert-instance-add](../../../../img/alert/webhook-alert-instance-add.png)

## 前置：WebHook地址

其中`webhook`为机器人发送消息的地址，以企业微信为例，可以按下列方式新增机器人:

![webhook-robot-add](../../../../img/alert/webhook-robot-add.png)

新增机器人后，便可以获得对应的webhook地址：
![webhook-address-obtain](../../../../img/alert/webhook-address-obtain.png)


## 消息发送

消息发送指的是将告警结果通过WebHook进行通知，支持向群内发送Text格式和MarkDown格式信息。
下图是消息发送的示例:

![webhook-message-send-instance](../../../../img/alert/webhook-message-send-instance.png)


## 参考文档

群机器人配置说明：https://developer.work.weixin.qq.com/document/path/91770
