# Webex Teams

If you need to use `Webex Teams` to alert, create an alert instance in the alert instance management, and choose the WebexTeams plugin.
The following is the `WebexTeams` configuration example:

![enterprise-webexteams-plugin](/img/alert/enterprise-webexteams-plugin.png)

## Parameter Configuration

* botAccessToken
  > The robot's access token
* roomID
  > The ID of the room that receives message (only support one room ID)
* toPersonId
  > The person ID of the recipient when sending a private 1:1 message
* toPersonEmail
  > The email address of the recipient when sending a private 1:1 message
* atSomeoneInRoom
  > If the message destination is room, the emails of the person being @, use `,` (eng commas) to separate multiple emails
* destination
  > The destination of the message (one message only support one destination)

[WebexTeams Application Bot Guide](https://developer.webex.com/docs/bots)
[WebexTeams Message Guide](https://developer.webex.com/docs/api/v1/messages/create-a-message)
