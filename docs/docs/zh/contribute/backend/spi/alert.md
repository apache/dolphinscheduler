### DolphinScheduler Alert SPI 主要设计

#### DolphinScheduler SPI 设计

DolphinScheduler 正在处于微内核 + 插件化的架构更改之中，所有核心能力如任务、资源存储、注册中心等都将被设计为扩展点，我们希望通过 SPI 来提高 DolphinScheduler 本身的灵活性以及友好性（扩展性）。

告警相关代码可以参考 `dolphinscheduler-alert-api` 模块。该模块定义了告警插件扩展的接口以及一些基础代码，当我们需要实现相关功能的插件化的时候，建议先阅读此块的代码，当然，更建议你阅读文档，这会减少很多时间，不过文档有一定的后滞性，当文档缺失的时候，建议以源码为准（如果有兴趣，我们也欢迎你来提交相关文档），此外，我们几乎不会对扩展接口做变更（不包括新增），除非重大架构调整，出现不兼容升级版本，因此，现有文档一般都能够满足。

我们采用了原生的 JAVA-SPI，当你需要扩展的时候，事实上你只需要关注扩展`org.apache.dolphinscheduler.alert.api.AlertChannelFactory`接口即可，底层相关逻辑如插件加载等内核已经实现，这让我们的开发更加专注且简单。

另外，`AlertChannelFactory` 继承自 `PrioritySPI`，这意味着你可以设置插件的优先级，当你有两个插件同名时，你可以通过重写 `getIdentify` 方法来自定义优先级。高优先级的插件会被加载，但是如果你有两个同名且优先级相同的插件，加载插件时服务器会抛出 `IllegalArgumentException`。

顺便提一句，我们采用了一款优秀的前端组件 form-create，它支持基于 json 生成前端 ui 组件，如果插件开发牵扯到前端，我们会通过 json 来生成相关前端 UI 组件，org.apache.dolphinscheduler.spi.params 里面对插件的参数做了封装，它会将相关参数全部全部转化为对应的 json，这意味这你完全可以通过 Java 代码的方式完成前端组件的绘制（这里主要是表单，我们只关心前后端交互的数据）。

本文主要着重讲解 Alert 告警相关设计以及开发。

#### 主要模块

如果你并不关心它的内部设计，只是想单纯的了解如何开发自己的告警插件，可以略过该内容。

* dolphinscheduler-alert-api

  该模块是 ALERT SPI 的核心模块，该模块定义了告警插件扩展的接口以及一些基础代码，扩展插件必须实现此模块所定义的接口:`org.apache.dolphinscheduler.alert.api.AlertChannelFactory`

* dolphinscheduler-alert-plugins

  该模块是目前我们提供的插件，目前我们已经支持数十种插件，如 Email、DingTalk、Script等。

#### Alert SPI 主要类信息：

AlertChannelFactory
告警插件工厂接口，所有告警插件需要实现该接口，该接口用来定义告警插件的名称，需要的参数，create 方法用来创建具体的告警插件实例。

AlertChannel
告警插件的接口，告警插件需要实现该接口，该接口中只有一个方法 process ，上层告警系统会调用该方法并通过该方法返回的 AlertResult 来获取告警的返回信息。

AlertData
告警内容信息，包括 id，标题，内容，日志。

AlertInfo
告警相关信息，上层系统调用告警插件实例时，将该类的实例通过 process 方法传入具体的告警插件。内部包含告警内容 AlertData 和调用的告警插件实例的前端填写的参数信息。

AlertResult
告警插件发送告警返回信息。

org.apache.dolphinscheduler.spi.params
该包下是插件化的参数定义，我们前端使用 from-create 这个前端库，该库可以基于插件定义返回的参数列表 json 来动态生成前端的 ui，因此我们在做 SPI 插件开发的时候无需关心前端。

该 package 下我们目前只封装了 RadioParam，TextParam，PasswordParam，分别用来定义 text 类型的参数，radio 参数和 password 类型的参数。

AbsPluginParams 该类是所有参数的基类，RadioParam 这些类都继承了该类。每个 DS 的告警插件都会在 AlertChannelFactory 的实现中返回一个 AbsPluginParams 的 list。

alert_spi 具体设计可见 issue：[Alert Plugin Design](https://github.com/apache/incubator-dolphinscheduler/issues/3049)

#### Alert SPI 内置实现

* Email

  电子邮件告警通知

* DingTalk

  钉钉群聊机器人告警

  相关参数配置可以参考钉钉机器人文档。

* EnterpriseWeChat

  企业微信告警通知

  相关参数配置可以参考企业微信机器人文档。

* Script

  我们实现了 Shell 脚本告警，我们会将相关告警参数透传给脚本，你可以在 Shell 中实现你的相关告警逻辑，如果你需要对接内部告警应用，这是一种不错的方法。

* FeiShu

  飞书告警通知

* Slack

  Slack告警通知

* PagerDuty

  PagerDuty告警通知

* WebexTeams

  WebexTeams告警通知
  相关参数配置可以参考WebexTeams文档。

* Telegram

  Telegram告警通知
  相关参数配置可以参考Telegram文档。

* Http

  我们实现了Http告警，调用大部分的告警插件最终都是Http请求，如果我们没有支持你常用插件，可以使用Http来实现你的告警需求，同时也欢迎将你常用插件贡献到社区。

