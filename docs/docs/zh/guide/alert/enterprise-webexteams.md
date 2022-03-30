# WebexTeams

如果您需要使用到Webex Teams进行告警，请在告警实例管理里创建告警实例，选择 WebexTeams 插件。WebexTeams的配置样例如下:

![enterprise-webexteams-plugin](/img/alert/enterprise-webexteams-plugin.png)

参数配置

* botAccessToken
  > 在创建机器人时，获得的访问令牌
* roomID
  > 接受消息的room ID(只支持一个ID)
* toPersonId
  > 接受消息的用户ID(只支持一个ID)
* toPersonEmail
  > 接受消息的用户邮箱(只支持一个邮箱)
* atSomeoneInRoom
  > 如果消息目的地为room，被@人的用户邮箱，多个邮箱用英文逗号分隔
* destination
  > 消息目的地，一条消息只支持一个目的地

[WebexTeams申请机器人文档](https://developer.webex.com/docs/bots)
[WebexTeamsMessage开发文档](https://developer.webex.com/docs/api/v1/messages/create-a-message)


