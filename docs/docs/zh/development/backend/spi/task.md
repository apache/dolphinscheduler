## DolphinScheduler Task SPI 扩展

#### 如何进行任务插件开发？

org.apache.dolphinscheduler.spi.task.TaskChannel

插件实现以上接口即可。主要包含创建任务（任务初始化，任务运行等方法）、任务取消，如果是 yarn 任务，则需要实现 org.apache.dolphinscheduler.plugin.task.api.AbstractYarnTask。

我们在 dolphinscheduler-task-api 模块提供了所有任务对外访问的 API，而 dolphinscheduler-spi 模块则是 spi 通用代码库，定义了所有的插件模块，比如告警模块，注册中心模块等，你可以详细阅读查看。

*NOTICE*

由于任务插件涉及到前端页面，目前前端的SPI还没有实现，因此你需要单独实现插件对应的前端页面。

如果任务插件存在类冲突，你可以采用 [Shade-Relocating Classes](https://maven.apache.org/plugins/maven-shade-plugin/) 来解决这种问题。
