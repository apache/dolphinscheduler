# Alert Component User Guide

## How to Create Alert Plugins and Alert Groups

In version 2.0.0, users need to create alert instances, and needs to choose an alarm policy when defining an alarm instance, there are three options: send if the task succeeds, send on failure, and send on both success and failure. when the workflow or task is executed, if an alarm is triggered, calling the alarm instance send method needs a logical judgment, which matches the alarm instance with the task status, executes the alarm instance sending logic if it matches, and filters if it does not match. When create alert instances then associate them with alert groups. Alert group can use multiple alert instances.
The alarm module supports the following scenarios:
<img src="/img/alert/alert_scenarios_en.png">

The steps to use are as follows:

First, go to the Security Center page. Select Alarm Group Management, click Alarm Instance Management on the left and create an alarm instance. Select the corresponding alarm plug-in and fill in the relevant alarm parameters.

Then select Alarm Group Management, create an alarm group, and choose the corresponding alarm instance.

<img src="/img/alert/alert_step_1.png">
<img src="/img/alert/alert_step_2.png">
<img src="/img/alert/alert_step_3.png">
<img src="/img/alert/alert_step_4.png">
