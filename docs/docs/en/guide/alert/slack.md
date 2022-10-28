# Slack

If you need to use `Slack` for alerting, create an alert instance in the alert instance management and select
the `Slack` plugin.

The following shows the `Slack` configuration example:

![alert-slack](../../../../img/new_ui/dev/alert/alert_slack.png)

## Parameter Configuration

* Webhook

  > Copy the `Incoming Webhooks` address from the APP, check the image below:

  ![alert-slack-webhook](../../../../img/new_ui/dev/alert/alert_slack_webhook_addr.png)

* UserName

  > (Deprecated) The sender name. Currently, Slack use APP as the sender name due to the Slack updates.

## How to Get Webhook

Please refer to the official article [Slack: Sending messages using Incoming Webhooks](https://api.slack.com/messaging/webhooks) to create new webhooks.

### Create a new Slack APP

Visit [Slack Official Website](https://api.slack.com/apps/new) and create a new APP.

### Activate Incoming Webhooks Setting

When finish creating a new APP, select `Incoming Webhooks` of the column `Feature` on the left of the APP page, and switch `Activate Incoming Webhooks` to `ON`.
![alert-slack-webhook](../../../../img/new_ui/dev/alert/alert_slack_enable.png)

### Create a New Incoming Webhook

Click `Add New Webhook to Workspace` and select a group to post messages.
![select-post-group](../../../../img/new_ui/dev/alert/alert_slack_new.png)

### Acquire Incoming Webhooks Address

Copy the `Incoming Webhooks` address to DolphinScheduler, check the image below:
The default format of the `Incoming Webhooks`: `https://hooks.slack.com/services/T00000000/B00000000/XXXXXXXXXXXXXXXXXXXXXXXX`

![alert-slack-webhook](../../../../img/new_ui/dev/alert/alert_slack_webhook_addr.png)

Reference：[Slack：Sending messages using Incoming Webhooks](https://api.slack.com/messaging/webhooks)
