# 资源中心

如果需要用到资源上传功能，针对单机可以选择本地文件目录作为上传文件夹(此操作不需要部署 Hadoop)。当然也可以选择上传到 Hadoop or MinIO 集群上，此时则需要有Hadoop (2.6+) 或者 MinIO 等相关环境

> **_注意:_**
>
> * 如果用到资源上传的功能，那么 [安装部署](installation/standalone.md)中，部署用户需要有这部分的操作权限
> * 如果 Hadoop 集群的 NameNode 配置了 HA 的话，需要开启 HDFS 类型的资源上传，同时需要将 Hadoop 集群下的 `core-site.xml` 和 `hdfs-site.xml` 复制到 `/opt/dolphinscheduler/conf`，非 NameNode HA 跳过次步骤

## hdfs资源配置

- 上传资源文件和udf函数，所有上传的文件和资源都会被存储到hdfs上，所以需要以下配置项：

```  
conf/common.properties  
    # Users who have permission to create directories under the HDFS root path
    hdfs.root.user=hdfs
    # data base dir, resource file will store to this hadoop hdfs path, self configuration, please make sure the directory exists on hdfs and have read write permissions。"/dolphinscheduler" is recommended
    resource.upload.path=/dolphinscheduler
    # resource storage type : HDFS,S3,NONE
    resource.storage.type=HDFS
    # whether kerberos starts
    hadoop.security.authentication.startup.state=false
    # java.security.krb5.conf path
    java.security.krb5.conf.path=/opt/krb5.conf
    # loginUserFromKeytab user
    login.user.keytab.username=hdfs-mycluster@ESZ.COM
    # loginUserFromKeytab path
    login.user.keytab.path=/opt/hdfs.headless.keytab    
    # if resource.storage.type is HDFS，and your Hadoop Cluster NameNode has HA enabled, you need to put core-site.xml and hdfs-site.xml in the installPath/conf directory. In this example, it is placed under /opt/soft/dolphinscheduler/conf, and configure the namenode cluster name; if the NameNode is not HA, modify it to a specific IP or host name.
    # if resource.storage.type is S3，write S3 address，HA，for example ：s3a://dolphinscheduler，
    # Note，s3 be sure to create the root directory /dolphinscheduler
    fs.defaultFS=hdfs://mycluster:8020    
    #resourcemanager ha note this need ips , this empty if single
    yarn.resourcemanager.ha.rm.ids=192.168.xx.xx,192.168.xx.xx    
    # If it is a single resourcemanager, you only need to configure one host name. If it is resourcemanager HA, the default configuration is fine
    yarn.application.status.address=http://xxxx:8088/ws/v1/cluster/apps/%s

```

## 文件管理

> 是对各种资源文件的管理，包括创建基本的 `txt/log/sh/conf/py/java` 等文件、上传jar包等各种类型文件，可进行编辑、重命名、下载、删除等操作。

![file-manage](/img/new_ui/dev/resource/file-manage.png)

* 创建文件

  > 文件格式支持以下几种类型：txt、log、sh、conf、cfg、py、java、sql、xml、hql、properties

![create-file](/img/new_ui/dev/resource/create-file.png)

* 上传文件
  > 上传文件：点击"上传文件"按钮进行上传，将文件拖拽到上传区域，文件名会自动以上传的文件名称补全

![upload-file](/img/new_ui/dev/resource/upload-file.png)

* 文件查看

  > 对可查看的文件类型，点击文件名称，可查看文件详情

    <p align="center">
        <img src="/img/file_detail.png" width="80%" />
    </p>

* 下载文件

  > 点击文件列表的"下载"按钮下载文件或者在文件详情中点击右上角"下载"按钮下载文件

* 文件重命名

![rename-file](/img/new_ui/dev/resource/rename-file.png)

* 删除

>  文件列表->点击"删除"按钮，删除指定文件

* 重新上传文件

  > 点击文件列表中的”重新上传文件“按钮进行重新上传文件，将文件拖拽到上传区域，文件名会自动以上传的文件名称补全

    <p align="center">
      <img src="/img/reupload_file_en.png" width="80%" />
    </p>

## UDF管理

### 资源管理

  > 资源管理和文件管理功能类似，不同之处是资源管理是上传的UDF函数，文件管理上传的是用户程序，脚本及配置文件
  > 操作功能：重命名、下载、删除。

* 上传udf资源

  > 和上传文件相同。

### 函数管理

* 创建 UDF 函数

  > 点击“创建 UDF 函数”，输入 UDF 函数参数，选择udf资源，点击“提交”，创建udf函数。
  > 目前只支持HIVE的临时UDF函数

- UDF 函数名称：输入UDF函数时的名称
- 包名类名：输入UDF函数的全路径  
- UDF 资源：设置创建的 UDF 对应的资源文件

![create-udf](/img/new_ui/dev/resource/create-udf.png)
 
 ## 任务组设置

任务组主要用于控制任务实例并发，旨在控制其他资源的压力（也可以控制Hadoop集群压力，不过集群会有队列管控）。您可在新建任务定义时，可配置对应的任务组，并配置任务在任务组内运行的优先级。

### 任务组配置

#### 新建任务组   

![taskGroup](/img/new_ui/dev/resource/taskGroup.png)

用户点击【资源中心】-【任务组管理】-【任务组配置】-新建任务组

![create-taskGroup](/img/new_ui/dev/resource/create-taskGroup.png) 

您需要输入图片中信息，其中

【任务组名称】：任务组在被使用时显示的名称

【项目名称】：任务组作用的项目，该项为非必选项，如果不选择，则整个系统所有项目均可使用该任务组。

【资源容量】：允许任务实例并发的最大数量

#### 查看任务组队列

![view-queue](/img/new_ui/dev/resource/view-queue.png) 

点击按钮查看任务组使用信息

![view-queue](/img/new_ui/dev/resource/view-groupQueue.png) 

#### 任务组的使用

注：任务组的使用适用于由 worker 执行的任务，例如【switch】节点、【condition】节点、【sub_process】等由 master 负责执行的节点类型不受任务组控制。

我们以 shell 节点为例：

![use-queue](/img/new_ui/dev/resource/use-queue.png)         

关于任务组的配置，您需要做的只需要配置红色框内的部分，其中：

【任务组名称】：任务组配置页面显示的任务组名称，这里只能看到该项目有权限的任务组（新建任务组时选择了该项目），或作用在全局的任务组（新建任务组时没有选择项目）

【组内优先级】：在出现等待资源时，优先级高的任务会最先被 master 分发给 worker 执行，该部分数值越大，优先级越高。

### 任务组的实现逻辑

#### 获取任务组资源：

Master 在分发任务时判断该任务是否配置了任务组，如果任务没有配置，则正常抛给 worker 运行；如果配置了任务组，在抛给 worker 执行之前检查任务组资源池剩余大小是否满足当前任务运行，如果满足资源池 -1，继续运行；如果不满足则退出任务分发，等待其他任务结束唤醒。

#### 释放与唤醒：

当获取到任务组资源的任务结束运行后，会释放任务组资源，释放后会检查当前任务组是否有任务等待，如果有则标记优先级最好的任务可以运行，并新建一个可以执行的event。该event中存储着被标记可以获取资源的任务id，随后在获取任务组资源然后运行。

#### 任务组流程图

<p align="center">
    <img src="/img/task_group_process.png" width="80%" />
</p>        
