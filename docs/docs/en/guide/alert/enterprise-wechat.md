# Enterprise WeChat

If you need to use `Enterprise WeChat` to alert, create an alert instance in the alert instance management, and choose the WeChat plugin.
The following is the `WeChat` configuration example:

![enterprise-wechat-plugin](/img/alert/enterprise-wechat-plugin.png)

The parameter `send.type` corresponds to app and group chat respectively:

APP: https://work.weixin.qq.com/api/doc/90000/90135/90236

Group Chat: https://work.weixin.qq.com/api/doc/90000/90135/90248

The parameter `user.send.msg` corresponds to the `content` in the document, and the corresponding variable is `{msg}`.