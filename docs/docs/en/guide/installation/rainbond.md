# Use Rainbond Deployment

This article describes the one-click deployment of highly available DolphinScheduler clusters through the [Rainbond](https://www.rainbond.com/) cloud native application management platform,This method is suitable for users who don't know much about complex technologies such as Kubernetes,Lowered the threshold for deploying DolphinScheduler in Kubernetes.

## Prerequisites

* Available Rainbond cloud native application management platform，Please refer to the documentation [Rainbond Quick install](https://www.rainbond.com/docs/quick-start/quick-install)

## DolphinScheduler Cluster One-click Deployment 

* Docking and accessing the built-in open source app store,Search the keyword `dolphinscheduler` to find App DolphinScheduler.

![](img/rainbond/appstore-dolphinscheduler.png)

* Click install on the right side of DolphinScheduler to go to the installation page,Fill in the corresponding information,Click OK to start the installation,Automatically jump to the application view.

| Select item  | Description                          |
| ------------ | ------------------------------------ |
| Team name    | user workspace，Isolate by namespace |
| Cluster name | select kubernetes cluster            |
| Select app   | select application                   |
| app version  | select DolphinScheduler version      |

![](img/rainbond/install-dolphinscheduler.png)

* Wait a few minutes,DolphinScheduler Installation is complete and running.

![](img/rainbond/topology-dolphinscheduler.png)

* Access DolphinScheduler-API components,The default user password is `admin` / `dolphinscheduler123`.

![](img/rainbond/homepage-dolphinscheduler.png)

## API Master Worker Node Telescopic

DolphinScheduler API、Master、Worker Both support scaling multiple instances,Multiple instances can ensure the high availability of the entire cluster.

Take worker as an example,Enter into the component -> Telescopic,Set the number of instances.

![](img/rainbond/dolpscheduler-worker.png)

Verify Worker Node,Enter DolphinScheduler UI -> Monitoring -> Worker View node information.

![](img/rainbond/monitor-dolphinscheduler.png)
## Configuration file

API and Worker Service sharing `/opt/dolphinscheduler/conf/common.properties` ,To modify the configuration, you only need to modify the configuration file of the API service.

## How to support Python 3？

Worker service is installed by default Python3，You can add environment variables when you use them `PYTHON_HOME=/usr/bin/python3`

## How to support Hadoop, Spark, DataX ？

Take Datax as an example:

1. Install the plugin。Rainbond Team View -> Plugin -> Install plugin from the App Store -> search `initialization plugin` Install.
2. Open plugin.enter Worker component -> plugin -> open `initialization plugin` And modify the configuration.
   * FILE_URL：http://datax-opensource.oss-cn-hangzhou.aliyuncs.com/datax.tar.gz
   * FILE_PATH：/opt/soft
   * LOCK_PATH：/opt/soft
3. Update component,The initialization plug-in will be downloaded automatically `Datax` and decompress to `/opt/soft`
![](img/rainbond/plugin.png)
---
