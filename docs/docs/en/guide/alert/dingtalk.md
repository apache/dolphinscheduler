# DingTalk

If you need to use `DingTalk` for alerting, create an alert instance in the alert instance management and select the DingTalk plugin. 
The following shows the `DingTalk` configuration example:

![dingtalk-plugin](/img/alert/dingtalk-plugin.png)

## Parameter Configuration

* Webhook
  > The format is: https://oapi.dingtalk.com/robot/send?access_token=XXXXXX
* Keyword
  > Custom keywords for security settings
* Secret
  > Signature of security settings
* MessageType
  > Support both text and markdown types

When a custom bot sends a message, you can specify the "@person list" by their mobile phone number. When the selected people in the "@people list" receive the message, there will be a `@` message reminder. `No disturb` mode always receives reminders, and "someone @ you" appears in the message.
* @Mobiles
  > The mobile phone number of the "@person"
* @UserIds
  > The user ID by "@person"
* @All
  > @Everyone

[DingTalk Custom Robot Access Development Documentation](https://open.dingtalk.com/document/robots/custom-robot-access)
