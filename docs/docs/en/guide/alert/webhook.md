# WebHook

If you need to use WebHook for alarms, please create an alarm instance and select the WebHook plugin. 

Taking enterprise WeChat as an example, the configuration example of WebHook is as follows:
![webhook-alert-instance-add](../../../../img/alert/webhook-alert-instance-add.png)

## Prerequisites: WebHook Obtain

'webhook' is the address where the robot sends messages. 

Taking enterprise WeChat for example, adding a robot can be done as follows:
![webhook-robot-add](../../../../img/alert/webhook-robot-add.png)

After adding a robot, you can obtain the corresponding webhook address:

![webhook-address-obtain](../../../../img/alert/webhook-address-obtain.png)


## Send Alert Message

Message sending refers to the notification of alarm results through WebHook, which supports sending text and MarkDown format information.

The following figure is an example of message sending:

![webhook-message-send-instance](../../../../img/alert/webhook-message-send-instance.png)


## Reference

Robot configuration instructions of Enterprise WeChat: https://developer.work.weixin.qq.com/document/path/91770
