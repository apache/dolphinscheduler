# 通用配置

## 语言

DolphinScheduler 支持两种内置语言，包括 `English` 和 `Chinese` 。您可以点击顶部控制栏名为 `English` 或 `Chinese` 的按钮切换语言。
当您将语言从一种切换为另一种时，您所有 DolphinScheduler 的页面语言页面将发生变化。

## 主题

DolphinScheduler 支持两种类型的内置主题，包括 `Dark` 和 `Light`。当您想改变主题时，只需单击顶部控制栏在 [语言](#语言) 左侧名为 `Dark`(or `Light`)
的按钮即可。

## 时区

DolphinScheduler 支持时区设置。

服务时区

使用脚本 `bin/dolphinshceduler_daemon.sh`启动服务， 服务的默认时区为UTC， 可以在 `application.yaml` 文件中进行修改，或通过环境变量修改, 如`export SPRING_JACKSON_TIME_ZONE=${SPRING_JACKSON_TIME_ZONE:-Asia/Shanghai}`。<br>
IDEA 启动服务默认时区为本地时区，可以加jvm参数如`-Duser.timezone=UTC`来修改时区。 时区选择详见[List of tz database time zones](https://en.wikipedia.org/wiki/List_of_tz_database_time_zones)

用户时区

用户的默认时区基于您运行 DolphinScheduler 服务的时区。如果你想要切换时区，可以点击 [语言](#语言) 按钮右侧的时区按钮，
然后点击 `请选择时区` 进行时区选择。当切换完成后，所有与时间相关的组件都将更改。
