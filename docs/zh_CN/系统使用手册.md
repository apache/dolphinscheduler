# 系统使用手册


## 快速上手

  > 请参照[快速上手](快速上手.md)

## 操作指南

### 创建项目

  - 点击“项目管理->创建项目”，输入项目名称，项目描述，点击“提交”，创建新的项目。
  - 点击项目名称，进入项目首页。
<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/project.png" width="60%" />
 </p>

> 项目首页其中包含任务状态统计，流程状态统计、工作流定义统计

 - 任务状态统计：是指在指定时间范围内，统计任务实例中的待运行、失败、运行中、完成、成功的个数
 - 流程状态统计：是指在指定时间范围内，统计工作流实例中的待运行、失败、运行中、完成、成功的个数
 - 工作流定义统计：是统计该用户创建的工作流定义及管理员授予该用户的工作流定义


### 创建工作流定义
  - 进入项目首页，点击“工作流定义”，进入工作流定义列表页。
  - 点击“创建工作流”,创建新的工作流定义。
  - 拖拽“SHELL"节点到画布，新增一个Shell任务。
  - 填写”节点名称“，”描述“，”脚本“字段。
  - 选择“任务优先级”，级别高的任务在执行队列中会优先执行，相同优先级的任务按照先进先出的顺序执行。
  - 超时告警， 填写”超时时长“，当任务执行时间超过**超时时长**可以告警并且超时失败。
  - 填写"自定义参数",参考[自定义参数](#用户自定义参数)
<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/dag1.png" width="60%" />
 </p>

  - 增加节点之间执行的先后顺序： 点击”线条连接“；如图示，任务1和任务3并行执行，当任务1执行完，任务2、3会同时执行。

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/dag2.png" width="60%" />
 </p>

  - 删除依赖关系： 点击箭头图标”拖动节点和选中项“，选中连接线，点击删除图标，删除节点间依赖关系。
<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/dag3.png" width="60%" />
 </p>

  - 点击”保存“，输入工作流定义名称，工作流定义描述，设置全局参数,参考[自定义参数](#用户自定义参数)。

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/dag4.png" width="60%" />
 </p>

  - 其他类型节点，请参考 [任务节点类型和参数设置](#任务节点类型和参数设置)

### 执行工作流定义
  - **未上线状态的工作流定义可以编辑，但是不可以运行**，所以先上线工作流
  > 点击工作流定义，返回工作流定义列表，点击”上线“图标，上线工作流定义。

  > 下线工作流定义的时候，要先将定时管理中的定时任务下线，这样才能成功下线工作流定义  

  - 点击”运行“，执行工作流。运行参数说明：
    * 失败策略：**当某一个任务节点执行失败时，其他并行的任务节点需要执行的策略**。”继续“表示：其他任务节点正常执行，”结束“表示：终止所有正在执行的任务，并终止整个流程。
    * 通知策略：当流程结束，根据流程状态发送流程执行信息通知邮件。
    * 流程优先级：流程运行的优先级，分五个等级：最高（HIGHEST），高(HIGH),中（MEDIUM）,低（LOW），最低（LOWEST）。级别高的流程在执行队列中会优先执行，相同优先级的流程按照先进先出的顺序执行。
    * worker分组： 这个流程只能在指定的机器组里执行。默认是Default，可以在任一worker上执行。
    * 通知组： 当流程结束，或者发生容错时，会发送流程信息邮件到通知组里所有成员。
    * 收件人：输入邮箱后按回车键保存。当流程结束、发生容错时，会发送告警邮件到收件人列表。
    * 抄送人：输入邮箱后按回车键保存。当流程结束、发生容错时，会抄送告警邮件到抄送人列表。
  <p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/run-work.png" width="60%" />
 </p>

  * 补数： 执行指定日期的工作流定义，可以选择补数时间范围（目前只支持针对连续的天进行补数)，比如要补5月1号到5月10号的数据，如图示： 
<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/complement.png" width="60%" />
 </p>

> 补数执行模式有**串行执行、并行执行**，串行模式下，补数会从5月1号到5月10号依次执行；并行模式下，会同时执行5月1号到5月10号的任务。

### 定时工作流定义
  - 创建定时："工作流定义->定时”
  - 选择起止时间，在起止时间范围内，定时正常工作，超过范围，就不会再继续产生定时工作流实例了。
<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/time-schedule.png" width="60%" />
 </p>

  - 添加一个每天凌晨5点执行一次的定时，如图示：
<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/time-schedule2.png" width="60%" />
 </p>

  - 定时上线，**新创建的定时是下线状态，需要点击“定时管理->上线”，定时才能正常工作**。

### 查看工作流实例
  > 点击“工作流实例”，查看工作流实例列表。

  > 点击工作流名称，查看任务执行状态。

  <p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/instance-detail.png" width="60%" />
 </p>

  > 点击任务节点，点击“查看日志”，查看任务执行日志。

  <p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/task-log.png" width="60%" />
 </p>

 > 点击任务实例节点，点击**查看历史**，可以查看该工作流实例运行的该任务实例列表

 <p align="center">
    <img src="https://analysys.github.io/easyscheduler_docs_cn/images/task_history.png" width="60%" />
  </p>
 

  > 对工作流实例的操作：

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/instance-list.png" width="60%" />
</p>

  * 编辑：可以对已经终止的流程进行编辑，编辑后保存的时候，可以选择是否更新到工作流定义。
  * 重跑：可以对已经终止的流程进行重新执行。
  * 恢复失败：针对失败的流程，可以执行恢复失败操作，从失败的节点开始执行。
  * 停止：对正在运行的流程进行**停止**操作，后台会先对worker进程`kill`,再执行`kill -9`操作
  * 暂停：可以对正在运行的流程进行**暂停**操作，系统状态变为**等待执行**，会等待正在执行的任务结束，暂停下一个要执行的任务。
  * 恢复暂停：可以对暂停的流程恢复，直接从**暂停的节点**开始运行
  * 删除：删除工作流实例及工作流实例下的任务实例
  * 甘特图：Gantt图纵轴是某个工作流实例下的任务实例的拓扑排序，横轴是任务实例的运行时间,如图示：
<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/gant-pic.png" width="60%" />
</p>

### 查看任务实例
  > 点击“任务实例”，进入任务列表页，查询任务执行情况

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/task-list.png" width="60%" />
</p>

  > 点击操作列中的“查看日志”，可以查看任务执行的日志情况。

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/task-log2.png" width="60%" />
</p>

### 创建数据源
  > 数据源中心支持MySQL、POSTGRESQL、HIVE及Spark等数据源

#### 创建、编辑MySQL数据源

  - 点击“数据源中心->创建数据源”，根据需求创建不同类型的数据源。

  - 数据源：选择MYSQL
  - 数据源名称：输入数据源的名称
  - 描述：输入数据源的描述
  - IP/主机名：输入连接MySQL的IP
  - 端口：输入连接MySQL的端口
  - 用户名：设置连接MySQL的用户名
  - 密码：设置连接MySQL的密码
  - 数据库名：输入连接MySQL的数据库名称
  - Jdbc连接参数：用于MySQL连接的参数设置，以JSON形式填写

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/mysql_edit.png" width="60%" />
 </p>

  > 点击“测试连接”，测试数据源是否可以连接成功。

#### 创建、编辑POSTGRESQL数据源

- 数据源：选择POSTGRESQL
- 数据源名称：输入数据源的名称
- 描述：输入数据源的描述
- IP/主机名：输入连接POSTGRESQL的IP
- 端口：输入连接POSTGRESQL的端口
- 用户名：设置连接POSTGRESQL的用户名
- 密码：设置连接POSTGRESQL的密码
- 数据库名：输入连接POSTGRESQL的数据库名称
- Jdbc连接参数：用于POSTGRESQL连接的参数设置，以JSON形式填写

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/postgresql_edit.png" width="60%" />
 </p>

#### 创建、编辑HIVE数据源

1.使用HiveServer2方式连接

 <p align="center">
    <img src="https://analysys.github.io/easyscheduler_docs_cn/images/hive_edit.png" width="60%" />
  </p>

  - 数据源：选择HIVE
  - 数据源名称：输入数据源的名称
  - 描述：输入数据源的描述
  - IP/主机名：输入连接HIVE的IP
  - 端口：输入连接HIVE的端口
  - 用户名：设置连接HIVE的用户名
  - 密码：设置连接HIVE的密码
  - 数据库名：输入连接HIVE的数据库名称
  - Jdbc连接参数：用于HIVE连接的参数设置，以JSON形式填写

2.使用HiveServer2 HA Zookeeper方式连接

 <p align="center">
    <img src="https://analysys.github.io/easyscheduler_docs_cn/images/hive_edit2.png" width="60%" />
  </p>


注意：如果开启了**kerberos**，则需要填写 **Principal**
<p align="center">
    <img src="https://analysys.github.io/easyscheduler_docs_cn/images/hive_kerberos.png" width="60%" />
  </p>




#### 创建、编辑Spark数据源

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/spark_datesource.png" width="60%" />
 </p>

- 数据源：选择Spark
- 数据源名称：输入数据源的名称
- 描述：输入数据源的描述
- IP/主机名：输入连接Spark的IP
- 端口：输入连接Spark的端口
- 用户名：设置连接Spark的用户名
- 密码：设置连接Spark的密码
- 数据库名：输入连接Spark的数据库名称
- Jdbc连接参数：用于Spark连接的参数设置，以JSON形式填写



注意：如果开启了**kerberos**，则需要填写 **Principal**

<p align="center">
    <img src="https://analysys.github.io/easyscheduler_docs_cn/images/sparksql_kerberos.png" width="60%" />
  </p>

### 上传资源
  - 上传资源文件和udf函数，所有上传的文件和资源都会被存储到hdfs上，所以需要以下配置项：

```
conf/common/common.properties
    -- hdfs.startup.state=true
conf/common/hadoop.properties  
    -- fs.defaultFS=hdfs://xxxx:8020  
    -- yarn.resourcemanager.ha.rm.ids=192.168.xx.xx,192.168.xx.xx
    -- yarn.application.status.address=http://xxxx:8088/ws/v1/cluster/apps/%s
```

#### 文件管理

  > 是对各种资源文件的管理，包括创建基本的txt/log/sh/conf等文件、上传jar包等各种类型文件，以及编辑、下载、删除等操作。
  <p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/file-manage.png" width="60%" />
 </p>

  * 创建文件
 > 文件格式支持以下几种类型：txt、log、sh、conf、cfg、py、java、sql、xml、hql

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/file_create.png" width="60%" />
 </p>

  * 上传文件

> 上传文件：点击上传按钮进行上传，将文件拖拽到上传区域，文件名会自动以上传的文件名称补全

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/file_upload.png" width="60%" />
 </p>


  * 文件查看

> 对可查看的文件类型，点击 文件名称 可以查看文件详情

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/file_detail.png" width="60%" />
 </p>

  * 下载文件

> 可以在 文件详情 中点击右上角下载按钮下载文件，或者在文件列表后的下载按钮下载文件

  * 文件重命名

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/file_rename.png" width="60%" />
 </p>

#### 删除
>  文件列表->点击"删除"按钮，删除指定文件

#### 资源管理
  > 资源管理和文件管理功能类似，不同之处是资源管理是上传的UDF函数，文件管理上传的是用户程序，脚本及配置文件

  * 上传udf资源
  > 和上传文件相同。

#### 函数管理

  * 创建udf函数
  > 点击“创建UDF函数”，输入udf函数参数，选择udf资源，点击“提交”，创建udf函数。

 > 目前只支持HIVE的临时UDF函数

  - UDF函数名称：输入UDF函数时的名称
  - 包名类名：输入UDF函数的全路径
  - 参数：用来标注函数的输入参数
  - 数据库名：预留字段，用于创建永久UDF函数
  - UDF资源：设置创建的UDF对应的资源文件

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/udf_edit.png" width="60%" />
 </p>

## 安全中心（权限系统）

  - 安全中心是只有管理员账户才有权限的功能，有队列管理、租户管理、用户管理、告警组管理、worker分组、令牌管理等功能，还可以对资源、数据源、项目等授权
  - 管理员登录，默认用户名密码：admin/escheduler123

### 创建队列
  - 队列是在执行spark、mapreduce等程序，需要用到“队列”参数时使用的。
  - “安全中心”->“队列管理”->“创建队列”
 <p align="center">
    <img src="https://analysys.github.io/easyscheduler_docs_cn/images/create-queue.png" width="60%" />
  </p>


### 添加租户
  - 租户对应的是Linux的用户，用于worker提交作业所使用的用户。如果linux没有这个用户，worker会在执行脚本的时候创建这个用户。
  - 租户编码：**租户编码是Linux上的用户，唯一，不能重复**

 <p align="center">
    <img src="https://analysys.github.io/easyscheduler_docs_cn/images/addtenant.png" width="60%" />
  </p>

### 创建普通用户
  -  用户分为**管理员用户**和**普通用户**
    * 管理员有**授权和用户管理**等权限，没有**创建项目和工作流定义**的操作的权限
    * 普通用户可以**创建项目和对工作流定义的创建，编辑，执行**等操作。
    * 注意：**如果该用户切换了租户，则该用户所在租户下所有资源将复制到切换的新租户下**
<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/useredit2.png" width="60%" />
 </p>

### 创建告警组
  * 告警组是在启动时设置的参数，在流程结束以后会将流程的状态和其他信息以邮件形式发送给告警组。
  - 新建、编辑告警组

  <p align="center">
    <img src="https://analysys.github.io/easyscheduler_docs_cn/images/mail_edit.png" width="60%" />
  </p>

### 创建worker分组
  - worker分组，提供了一种让任务在指定的worker上运行的机制。管理员创建worker分组，在任务节点和运行参数中设置中可以指定该任务运行的worker分组，如果指定的分组被删除或者没有指定分组，则该任务会在任一worker上运行。
  - worker分组内多个ip地址（**不能写别名**），以**英文逗号**分隔

  <p align="center">
    <img src="https://analysys.github.io/easyscheduler_docs_cn/images/worker1.png" width="60%" />
  </p>

### 令牌管理
  - 由于后端接口有登录检查，令牌管理，提供了一种可以通过调用接口的方式对系统进行各种操作。
  - 调用示例：

```令牌调用示例
    /**
     * test token
     */
    public  void doPOSTParam()throws Exception{
        // create HttpClient
        CloseableHttpClient httpclient = HttpClients.createDefault();

        // create http post request
        HttpPost httpPost = new HttpPost("http://127.0.0.1:12345/escheduler/projects/create");
        httpPost.setHeader("token", "123");
        // set parameters
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("projectName", "qzw"));
        parameters.add(new BasicNameValuePair("desc", "qzw"));
        UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(parameters);
        httpPost.setEntity(formEntity);
        CloseableHttpResponse response = null;
        try {
            // execute
            response = httpclient.execute(httpPost);
            // response status code 200
            if (response.getStatusLine().getStatusCode() == 200) {
                String content = EntityUtils.toString(response.getEntity(), "UTF-8");
                System.out.println(content);
            }
        } finally {
            if (response != null) {
                response.close();
            }
            httpclient.close();
        }
    }
```

### 授予权限
  - 授予权限包括项目权限，资源权限，数据源权限，UDF函数权限。
> 管理员可以对普通用户进行非其创建的项目、资源、数据源和UDF函数进行授权。因为项目、资源、数据源和UDF函数授权方式都是一样的，所以以项目授权为例介绍。

> 注意：**对于用户自己创建的项目，该用户拥有所有的权限。则项目列表和已选项目列表中不会体现**

  - 1.点击指定人的授权按钮，如下图：
  <p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/auth_user.png" width="60%" />
 </p>

- 2.选中项目按钮，进行项目授权

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/auth_project.png" width="60%" />
 </p>


## 监控中心

### 服务管理
  - 服务管理主要是对系统中的各个服务的健康状况和基本信息的监控和显示

#### master监控
  - 主要是master的相关信息。
<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/master-jk.png" width="60%" />
 </p>

#### worker监控
  - 主要是worker的相关信息。

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/worker-jk.png" width="60%" />
 </p>

#### Zookeeper监控
  - 主要是zookpeeper中各个worker和master的相关配置信息。

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/zk-jk.png" width="60%" />
 </p>

#### Mysql监控
  - 主要是mysql的健康状况

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/mysql-jk.png" width="60%" />
 </p>

## 任务节点类型和参数设置

### Shell节点
  - shell节点，在worker执行的时候，会生成一个临时shell脚本，使用租户同名的linux用户执行这个脚本。
> 拖动工具栏中的![PNG](https://analysys.github.io/easyscheduler_docs_cn/images/toolbar_SHELL.png)任务节点到画板中，双击任务节点，如下图：

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/shell_edit.png" width="60%" />
 </p>

- 节点名称：一个工作流定义中的节点名称是唯一的
- 运行标志：标识这个节点是否能正常调度,如果不需要执行，可以打开禁止执行开关。
- 描述信息：描述该节点的功能
- 失败重试次数：任务失败重新提交的次数，支持下拉和手填
- 失败重试间隔：任务失败重新提交任务的时间间隔，支持下拉和手填
- 脚本：用户开发的SHELL程序
- 资源：是指脚本中需要调用的资源文件列表
- 自定义参数：是SHELL局部的用户自定义参数，会替换脚本中以${变量}的内容

### 子流程节点
  - 子流程节点，就是把外部的某个工作流定义当做一个任务节点去执行。
> 拖动工具栏中的![PNG](https://analysys.github.io/easyscheduler_docs_cn/images/toolbar_SUB_PROCESS.png)任务节点到画板中，双击任务节点，如下图：

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/subprocess_edit.png" width="60%" />
 </p>

- 节点名称：一个工作流定义中的节点名称是唯一的
- 运行标志：标识这个节点是否能正常调度
- 描述信息：描述该节点的功能
- 子节点：是选择子流程的工作流定义，右上角进入该子节点可以跳转到所选子流程的工作流定义

### 依赖(DEPENDENT)节点
  - 依赖节点，就是**依赖检查节点**。比如A流程依赖昨天的B流程执行成功，依赖节点会去检查B流程在昨天是否有执行成功的实例。

> 拖动工具栏中的![PNG](https://analysys.github.io/easyscheduler_docs_cn/images/toolbar_DEPENDENT.png)任务节点到画板中，双击任务节点，如下图：

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/dependent_edit.png" width="60%" />
 </p>

  > 依赖节点提供了逻辑判断功能，比如检查昨天的B流程是否成功，或者C流程是否执行成功。

  <p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/depend-node.png" width="80%" />
 </p>

  > 例如，A流程为周报任务，B、C流程为天任务，A任务需要B、C任务在上周的每一天都执行成功，如图示：

 <p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/depend-node2.png" width="80%" />
 </p>

  > 假如，周报A同时还需要自身在上周二执行成功：

 <p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/depend-node3.png" width="80%" />
 </p>

### 存储过程节点
  - 根据选择的数据源，执行存储过程。
> 拖动工具栏中的![PNG](https://analysys.github.io/easyscheduler_docs_cn/images/toolbar_PROCEDURE.png)任务节点到画板中，双击任务节点，如下图：

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/procedure_edit.png" width="60%" />
 </p>

- 数据源：存储过程的数据源类型支持MySQL和POSTGRESQL两种，选择对应的数据源
- 方法：是存储过程的方法名称
- 自定义参数：存储过程的自定义参数类型支持IN、OUT两种，数据类型支持VARCHAR、INTEGER、LONG、FLOAT、DOUBLE、DATE、TIME、TIMESTAMP、BOOLEAN九种数据类型

### SQL节点
  - 执行非查询SQL功能
  <p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/sql-node.png" width="60%" />
 </p>

  - 执行查询SQL功能，可以选择通过表格和附件形式发送邮件到指定的收件人。
> 拖动工具栏中的![PNG](https://analysys.github.io/easyscheduler_docs_cn/images/toolbar_SQL.png)任务节点到画板中，双击任务节点，如下图：

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/sql-node2.png" width="60%" />
 </p>

- 数据源：选择对应的数据源
- sql类型：支持查询和非查询两种，查询是select类型的查询，是有结果集返回的，可以指定邮件通知为表格、附件或表格附件三种模板。非查询是没有结果集返回的，是针对update、delete、insert三种类型的操作
- sql参数：输入参数格式为key1=value1;key2=value2…
- sql语句：SQL语句
- UDF函数：对于HIVE类型的数据源，可以引用资源中心中创建的UDF函数,其他类型的数据源暂不支持UDF函数
- 自定义参数：SQL任务类型，而存储过程是自定义参数顺序的给方法设置值自定义参数类型和数据类型同存储过程任务类型一样。区别在于SQL任务类型自定义参数会替换sql语句中${变量}

### SPARK节点
  - 通过SPARK节点，可以直接直接执行SPARK程序，对于spark节点，worker会使用`spark-submit`方式提交任务

> 拖动工具栏中的![PNG](https://analysys.github.io/easyscheduler_docs_cn/images/toolbar_SPARK.png)任务节点到画板中，双击任务节点，如下图：

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/spark_edit.png" width="60%" />
 </p>

- 程序类型：支持JAVA、Scala和Python三种语言
- 主函数的class：是Spark程序的入口Main Class的全路径
- 主jar包：是Spark的jar包
- 部署方式：支持yarn-cluster、yarn-client、和local三种模式
- Driver内核数：可以设置Driver内核数及内存数
- Executor数量：可以设置Executor数量、Executor内存数和Executor内核数
- 命令行参数：是设置Spark程序的输入参数，支持自定义参数变量的替换。
- 其他参数：支持 --jars、--files、--archives、--conf格式
- 资源：如果其他参数中引用了资源文件，需要在资源中选择指定
- 自定义参数：是MR局部的用户自定义参数，会替换脚本中以${变量}的内容

 注意：JAVA和Scala只是用来标识，没有区别，如果是Python开发的Spark则没有主函数的class，其他都是一样

### MapReduce(MR)节点
  - 使用MR节点，可以直接执行MR程序。对于mr节点，worker会使用`hadoop jar`方式提交任务


> 拖动工具栏中的![PNG](https://analysys.github.io/easyscheduler_docs_cn/images/toolbar_MR.png)任务节点到画板中，双击任务节点，如下图：

 1. JAVA程序

 <p align="center">
    <img src="https://analysys.github.io/easyscheduler_docs_cn/images/mr_java.png" width="60%" />
  </p>

- 主函数的class：是MR程序的入口Main Class的全路径
- 程序类型：选择JAVA语言 
- 主jar包：是MR的jar包
- 命令行参数：是设置MR程序的输入参数，支持自定义参数变量的替换
- 其他参数：支持 –D、-files、-libjars、-archives格式
- 资源： 如果其他参数中引用了资源文件，需要在资源中选择指定
- 自定义参数：是MR局部的用户自定义参数，会替换脚本中以${变量}的内容

2. Python程序

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/mr_edit.png" width="60%" />
 </p>

- 程序类型：选择Python语言 
- 主jar包：是运行MR的Python jar包
- 其他参数：支持 –D、-mapper、-reducer、-input  -output格式，这里可以设置用户自定义参数的输入，比如：
- -mapper  "mapper.py 1"  -file mapper.py   -reducer reducer.py  -file reducer.py –input /journey/words.txt -output /journey/out/mr/${currentTimeMillis}
- 其中 -mapper 后的 mapper.py 1是两个参数，第一个参数是mapper.py，第二个参数是1
- 资源： 如果其他参数中引用了资源文件，需要在资源中选择指定
- 自定义参数：是MR局部的用户自定义参数，会替换脚本中以${变量}的内容

### Python节点
  - 使用python节点，可以直接执行python脚本，对于python节点，worker会使用`python **`方式提交任务。


> 拖动工具栏中的![PNG](https://analysys.github.io/easyscheduler_docs_cn/images/toolbar_PYTHON.png)任务节点到画板中，双击任务节点，如下图：

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/python_edit.png" width="60%" />
 </p>

- 脚本：用户开发的Python程序
- 资源：是指脚本中需要调用的资源文件列表
- 自定义参数：是Python局部的用户自定义参数，会替换脚本中以${变量}的内容

### 系统参数

<table>
    <tr><th>变量</th><th>含义</th></tr>
    <tr>
        <td>${system.biz.date}</td>
        <td>日常调度实例定时的定时时间前一天，格式为 yyyyMMdd，补数据时，该日期 +1</td>
    </tr>
    <tr>
        <td>${system.biz.curdate}</td>
        <td>日常调度实例定时的定时时间，格式为 yyyyMMdd，补数据时，该日期 +1</td>
    </tr>
    <tr>
        <td>${system.datetime}</td>
        <td>日常调度实例定时的定时时间，格式为 yyyyMMddHHmmss，补数据时，该日期 +1</td>
    </tr>
</table>


### 时间自定义参数

> 支持代码中自定义变量名，声明方式：${变量名}。可以是引用 "系统参数" 或指定 "常量"。

> 我们定义这种基准变量为 $[...] 格式的，$[yyyyMMddHHmmss] 是可以任意分解组合的，比如：$[yyyyMMdd], $[HHmmss], $[yyyy-MM-dd] 等

> 也可以这样：

- 后 N 年：$[add_months(yyyyMMdd,12*N)]
- 前 N 年：$[add_months(yyyyMMdd,-12*N)]
- 后 N 月：$[add_months(yyyyMMdd,N)]
- 前 N 月：$[add_months(yyyyMMdd,-N)]
- 后 N 周：$[yyyyMMdd+7*N]
- 前 N 周：$[yyyyMMdd-7*N]
- 后 N 天：$[yyyyMMdd+N]
- 前 N 天：$[yyyyMMdd-N]
- 后 N 小时：$[HHmmss+N/24]
- 前 N 小时：$[HHmmss-N/24]
- 后 N 分钟：$[HHmmss+N/24/60]
- 前 N 分钟：$[HHmmss-N/24/60]

### 用户自定义参数

> 用户自定义参数分为全局参数和局部参数。全局参数是保存工作流定义和工作流实例的时候传递的全局参数，全局参数可以在整个流程中的任何一个任务节点的局部参数引用。

> 例如：

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/local_parameter.png" width="60%" />
 </p>

> global_bizdate为全局参数，引用的是系统参数。

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/global_parameter.png" width="60%" />
 </p>

> 任务中local_param_bizdate通过${global_bizdate}来引用全局参数，对于脚本可以通过${local_param_bizdate}来引用变量local_param_bizdate的值，或通过JDBC直接将local_param_bizdate的值set进去
