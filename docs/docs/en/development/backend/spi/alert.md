### DolphinScheduler Alert SPI main design

#### DolphinScheduler SPI Design

DolphinScheduler is undergoing a microkernel + plug-in architecture change. All core capabilities such as tasks, resource storage, registration centers, etc. will be designed as extension points. We hope to use SPI to improve DolphinScheduler’s own flexibility and friendliness (extended sex).

For alarm-related codes, please refer to the `dolphinscheduler-alert-api` module. This module defines the extension interface of the alarm plug-in and some basic codes. When we need to realize the plug-inization of related functions, it is recommended to read the code of this block first. Of course, it is recommended that you read the document. This will reduce a lot of time, but the document There is a certain degree of lag. When the document is missing, it is recommended to take the source code as the standard (if you are interested, we also welcome you to submit related documents). In addition, we will hardly make changes to the extended interface (excluding new additions) , Unless there is a major structural adjustment, there is an incompatible upgrade version, so the existing documents can generally be satisfied.

We use the native JAVA-SPI, when you need to extend, in fact, you only need to pay attention to the extension of the `org.apache.dolphinscheduler.alert.api.AlertChannelFactory` interface, the underlying logic such as plug-in loading, and other kernels have been implemented, Which makes our development more focused and simple.

By the way, we have adopted an excellent front-end component form-create, which supports the generation of front-end UI components based on JSON. If plug-in development involves the front-end, we will use JSON to generate related front-end UI components, org.apache.dolphinscheduler. The parameters of the plug-in are encapsulated in spi.params, which will convert all the relevant parameters into the corresponding JSON, which means that you can complete the drawing of the front-end components by way of Java code (here is mainly the form, we only care Data exchanged between the front and back ends).

This article mainly focuses on the design and development of Alert.

#### Main Modules

If you don't care about its internal design, but simply want to know how to develop your own alarm plug-in, you can skip this content.

* dolphinscheduler-alert-api

  This module is the core module of ALERT SPI. This module defines the interface of the alarm plug-in extension and some basic codes. The extension plug-in must implement the interface defined by this module: `org.apache.dolphinscheduler.alert.api.AlertChannelFactory`

* dolphinscheduler-alert-plugins

  This module is currently a plug-in provided by us, and now we have supported dozens of plug-ins, such as Email, DingTalk, Script, etc.


#### Alert SPI Main class information.
AlertChannelFactory
Alarm plug-in factory interface. All alarm plug-ins need to implement this interface. This interface is used to define the name of the alarm plug-in and the required parameters. The create method is used to create a specific alarm plug-in instance.

AlertChannel
The interface of the alert plug-in. The alert plug-in needs to implement this interface. There is only one method process in this interface. The upper-level alert system will call this method and obtain the return information of the alert through the AlertResult returned by this method.

AlertData
Alarm content information, including id, title, content, log.

AlertInfo
For alarm-related information, when the upper-level system calls an instance of the alarm plug-in, the instance of this class is passed to the specific alarm plug-in through the process method. It contains the alert content AlertData and the parameter information filled in by the front end of the called alert plug-in instance.

AlertResult
The alarm plug-in sends alarm return information.

org.apache.dolphinscheduler.spi.params
This package is a plug-in parameter definition. Our front-end uses the from-create front-end library http://www.form-create.com, which can dynamically generate the front-end UI based on the parameter list json returned by the plug-in definition, so We don't need to care about the front end when we are doing SPI plug-in development.

Under this package, we currently only encapsulate RadioParam, TextParam, and PasswordParam, which are used to define text type parameters, radio parameters and password type parameters, respectively.

AbsPluginParams This class is the base class of all parameters, RadioParam these classes all inherit this class. Each DS alert plug-in will return a list of AbsPluginParams in the implementation of AlertChannelFactory.

The specific design of alert_spi can be seen in the issue: [Alert Plugin Design](https://github.com/apache/incubator-dolphinscheduler/issues/3049)

#### Alert SPI built-in implementation

* Email

     Email alert notification

* DingTalk

     Alert for DingTalk group chat bots
  
     Related parameter configuration can refer to the DingTalk robot document.

* EnterpriseWeChat

     EnterpriseWeChat alert notifications

     Related parameter configuration can refer to the EnterpriseWeChat robot document.

* Script

     We have implemented a shell script for alerting. We will pass the relevant alert parameters to the script and you can implement your alert logic in the shell. This is a good way to interface with internal alerting applications.

* SMS

     SMS alerts
* FeiShu

  FeiShu alert notification
* Slack

  Slack alert notification
* PagerDuty

  PagerDuty alert notification
* WebexTeams

  WebexTeams alert notification

  Related parameter configuration can refer to the WebexTeams document.

* Telegram

  Telegram alert notification
  
  Related parameter configuration can refer to the Telegram document.

* Http

  We have implemented a Http script for alerting. And calling most of the alerting plug-ins end up being Http requests, if we not support your alert plug-in yet, you can use Http to realize your alert login. Also welcome to contribute your common plug-ins to the community :)
