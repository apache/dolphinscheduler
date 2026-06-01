## DolphinScheduler Task SPI extension

#### How to develop task plugins?

org.apache.dolphinscheduler.spi.task.TaskChannel

The plug-in can implement the above interface. It mainly includes creating tasks (task initialization, task running, etc.) and task cancellation. If it is a yarn task, you need to implement org.apache.dolphinscheduler.plugin.task.api.AbstractYarnTask.

We provide APIs for external access to all tasks in the dolphinscheduler-task-api module, while the dolphinscheduler-spi module is the spi general code library, which defines all the plug-in modules, such as the alarm module, the registry module, etc., you can read and view in detail .

In additional, the `TaskChannelFactory` extends from `PrioritySPI`, this means you can set the plugin priority, when you have two plugin has the same name, you can customize the priority by override the `getIdentify` method. The high priority plugin will be load, but if you have two plugin with the same name and same priority, the server will throw `IllegalArgumentException` when load the plugin.

*NOTICE*

Since the task plug-in involves the front-end page, the front-end SPI has not yet been implemented, so you need to implement the front-end page corresponding to the plug-in separately.

If there is a class conflict in the task plugin, you can use [Shade-Relocating Classes](https://maven.apache.org/plugins/maven-shade-plugin/) to solve this problem.
