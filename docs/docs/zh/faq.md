<!-- markdown-link-check-disable -->
## Q：项目的名称是？

A：DolphinScheduler

---

## Q：DolphinScheduler 服务介绍及建议运行内存

A：DolphinScheduler 由 5 个服务组成，MasterServer、WorkerServer、ApiServer、AlertServer、LoggerServer 和 UI。

| 服务                      | 说明                                                         |
| ------------------------- | ------------------------------------------------------------ |
| MasterServer              | 主要负责 **DAG** 的切分和任务状态的监控                      |
| WorkerServer/LoggerServer | 主要负责任务的提交、执行和任务状态的更新。LoggerServer 用于 Rest Api 通过 **RPC** 查看日志 |
| ApiServer                 | 提供 Rest Api 服务，供 UI 进行调用                            |
| AlertServer               | 提供告警服务                                                 |
| UI                        | 前端页面展示                                                 |

注意：**由于服务比较多，建议单机部署最好是 4 核 16G 以上**

---


## Q：系统支持哪些邮箱？

A：支持绝大多数邮箱，qq、163、126、139、outlook、aliyun 等皆支持。支持 **TLS 和 SSL** 协议，可以在 alert.properties 中选择性配置

---

## Q：常用的系统变量时间参数有哪些，如何使用？

A：请参考[使用手册](https://dolphinscheduler.apache.org/zh-cn/docs/latest/user_doc/guide/parameter/built-in.html) 第8小节

---

## Q：pip install kazoo 这个安装报错。是必须安装的吗？

A： 这个是 python 连接 Zookeeper 需要使用到的，用于删除Zookeeper中的master/worker临时节点信息。所以如果是第一次安装，就可以忽略错误。在1.3.0之后，kazoo不再需要了，我们用程序来代替kazoo所做的

---

## Q：怎么指定机器运行任务

A：使用 **管理员** 创建 Worker 分组，在 **流程定义启动** 的时候可**指定Worker分组**或者在**任务节点上指定Worker分组**。如果不指定，则使用 Default，**Default默认是使用的集群里所有的Worker中随机选取一台来进行任务提交、执行**

---

## Q：任务的优先级

A：我们同时 **支持流程和任务的优先级**。优先级我们有 **HIGHEST、HIGH、MEDIUM、LOW 和 LOWEST** 五种级别。**可以设置不同流程实例之间的优先级，也可以设置同一个流程实例中不同任务实例的优先级**。详细内容请参考任务优先级设计 https://analysys.github.io/easyscheduler_docs_cn/%E7%B3%BB%E7%BB%9F%E6%9E%B6%E6%9E%84%E8%AE%BE%E8%AE%A1.html#%E7%B3%BB%E7%BB%9F%E6%9E%B6%E6%9E%84%E8%AE%BE%E8%AE%A1

----

## Q：dolphinscheduler-grpc 报错

A：在 1.2 及以前版本中，在根目录下执行：mvn -U clean package assembly:assembly -Dmaven.test.skip=true,然后刷新下整个项目就好，1.3版本中不再使用 GRPC 进行通信了

----

## Q：DolphinScheduler 支持 windows 上运行么

A： 理论上只有 **Worker 是需要在 Linux 上运行的**，其它的服务都是可以在 windows 上正常运行的。但是还是建议最好能在 linux 上部署使用

-----

## Q：UI 在 linux 编译 node-sass 提示：Error：EACCESS:permission denied，mkdir xxxx

A：单独安装 **npm install node-sass --unsafe-perm**，之后再 **npm install**

---

## Q：UI 不能正常登陆访问

A：     1，如果是 node 启动的查看 dolphinscheduler-ui 下的 .env 文件里的 API_BASE 配置是否是 Api Server 服务地址

​       2，如果是 nginx 启动的并且是通过 **install-dolphinscheduler-ui.sh** 安装的，查看             

​              **/etc/nginx/conf.d/dolphinscheduler.conf** 中的 proxy_pass 配置是否是 Api Server 服务地址

​       3，如果以上配置都是正确的，那么请查看 Api Server 服务是否是正常的，

​			curl http://localhost:12345/dolphinscheduler/users/get-user-info 查看 Api Server 日志，

​			如果提示 cn.dolphinscheduler.api.interceptor.LoginHandlerInterceptor:[76] - session info is null，则证明 Api Server 服务是正常的

​       4，如果以上都没有问题，需要查看一下 **application.properties** 中的 **server.context-path 和 server.port 配置**是否正确
注意：1.3 版本直接使用 Jetty 进行前端代码的解析，无需再安装配置 nginx 了

---

## Q：流程定义手动启动或调度启动之后，没有流程实例生成

A： 	  1，首先通过 **jps 查看MasterServer服务是否存在**，或者从服务监控直接查看 zk 中是否存在 master 服务

​	   2，如果存在 master 服务，查看 **命令状态统计** 或者 **t_ds_error_command** 中是否增加的新记录，如果增加了，**请查看 message 字段定位启动异常原因**

---

## Q：任务状态一直处于提交成功状态

A：        1，首先通过 **jps 查看 WorkerServer 服务是否存在**，或者从服务监控直接查看 zk 中是否存在 worker 服务

​          2，如果 **WorkerServer** 服务正常，需要 **查看 MasterServer 是否把 task 任务放到 zk 队列中** ，**需要查看 MasterServer 日志及 zk 队列中是否有任务阻塞**

​	   3，如果以上都没有问题，需要定位是否指定了 Worker 分组，但是 **Worker 分组的机器不是在线状态**

---

## Q：install.sh 中需要注意问题

A：  	   1，如果替换变量中包含特殊字符，**请用 \ 转移符进行转移**

​	    2，installPath="/data1_1T/dolphinscheduler"，**这个目录不能和当前要一键安装的 install.sh 目录是一样的**

​	    3，deployUser="dolphinscheduler"，**部署用户必须具有 sudo 权限**，因为 worker 是通过 sudo -u 租户 sh xxx.command 进行执行的

​	    4，monitorServerState="false"，服务监控脚本是否启动，默认是不启动服务监控脚本的。**如果启动服务监控脚本，则每 5 分钟定时来监控 master 和 worker 的服务是否 down 机，如果 down 机则会自动重启**

​	    5，hdfsStartupSate="false"，是否开启 HDFS 资源上传功能。默认是不开启的，**如果不开启则资源中心是不能使用的**。如果开启，需要 conf/common/hadoop/hadoop.properties 中配置 fs.defaultFS 和 yarn 的相关配置，如果使用 namenode HA，需要将 core-site.xml 和 hdfs-site.xml 复制到conf根目录下

​	注意：**1.0.x 版本是不会自动创建 hdfs 根目录的，需要自行创建，并且需要部署用户有hdfs的操作权限**

---

## Q：流程定义和流程实例下线异常

A ： 对于 **1.0.4 以前的版本中**，修改 dolphinscheduler-api cn.dolphinscheduler.api.quartz 包下的代码即可

```
public boolean deleteJob(String jobName, String jobGroupName) {
    lock.writeLock().lock();
    try {
      JobKey jobKey = new JobKey(jobName,jobGroupName);
      if(scheduler.checkExists(jobKey)){
        logger.info("try to delete job, job name: {}, job group name: {},", jobName, jobGroupName);
        return scheduler.deleteJob(jobKey);
      }else {
        return true;
      }

    } catch (SchedulerException e) {
      logger.error(String.format("delete job : %s failed",jobName), e);
    } finally {
      lock.writeLock().unlock();
    }
    return false;
  }
```

---

## Q：HDFS 启动之前创建的租户，能正常使用资源中心吗

A： 不能。因为在未启动 HDFS 创建的租户，不会在 HDFS 中注册租户目录。所以上次资源会报错

## Q：多 Master 和多 Worker 状态下，服务掉了，怎么容错

A：  **注意：Master 监控 Master 及 Worker 服务。**

​	1，如果 Master 服务掉了，其它的 Master 会接管挂掉的 Master 的流程，继续监控 Worker task 状态

​	2，如果 Worker 服务掉了，Master 会监控到 Worker 服务掉了，如果存在 Yarn 任务，Kill Yarn 任务之后走重试

具体请看容错设计：https://analysys.github.io/easyscheduler_docs_cn/%E7%B3%BB%E7%BB%9F%E6%9E%B6%E6%9E%84%E8%AE%BE%E8%AE%A1.html#%E7%B3%BB%E7%BB%9F%E6%9E%B6%E6%9E%84%E8%AE%BE%E8%AE%A1

---

## Q：对于 Master 和 Worker 一台机器伪分布式下的容错

A ： 1.0.3 版本只实现了 Master 启动流程容错，不走 Worker 容错。也就是说如果 Worker 挂掉的时候，没有 Master 存在。这流程将会出现问题。我们会在 **1.1.0** 版本中增加 Master 和 Worker 启动自容错，修复这个问题。如果想手动修改这个问题，需要针对 **跨重启正在运行流程** **并且已经掉的正在运行的 Worker 任务，需要修改为失败**，**同时跨重启正在运行流程设置为失败状态**。然后从失败节点进行流程恢复即可

---

## Q：定时容易设置成每秒执行

A ： 设置定时的时候需要注意，如果第一位（* * * * * ? *）设置成 \* ，则表示每秒执行。**我们将会在 1.1.0 版本中加入显示最近调度的时间列表** ，使用 http://cron.qqe2.com/  可以在线看近 5 次运行时间



## Q：定时有有效时间范围吗

A：有的，**如果定时的起止时间是同一个时间，那么此定时将是无效的定时**。**如果起止时间的结束时间比当前的时间小，很有可能定时会被自动删除**



## Q：任务依赖有几种实现

A：  1，**DAG** 之间的任务依赖关系，是从 **入度为零** 进行 DAG 切分的

​	 2，有 **任务依赖节点** ，可以实现跨流程的任务或者流程依赖，具体请参考 依赖(DEPENDENT)节点：https://analysys.github.io/easyscheduler_docs_cn/%E7%B3%BB%E7%BB%9F%E4%BD%BF%E7%94%A8%E6%89%8B%E5%86%8C.html#%E4%BB%BB%E5%8A%A1%E8%8A%82%E7%82%B9%E7%B1%BB%E5%9E%8B%E5%92%8C%E5%8F%82%E6%95%B0%E8%AE%BE%E7%BD%AE


## Q：流程定义有几种启动方式

A： 1，在 **流程定义列表**，点击 **启动** 按钮

​		2，**流程定义列表添加定时器**，调度启动流程定义

​		3，流程定义 **查看或编辑** DAG 页面，任意 **任务节点右击** 启动流程定义

​		4，可以对流程定义 DAG 编辑，设置某些任务的运行标志位 **禁止运行**，则在启动流程定义的时候，将该节点的连线将从 DAG 中去掉

## Q：Python 任务设置 Python 版本

A：  只需要修改 `bin/env/dolphinscheduler_env.sh` 中的 PYTHON_HOME

```
export PYTHON_HOME=/bin/python
```

注意：这了 **PYTHON_HOME** ，是 python 命令的绝对路径，而不是单纯的 PYTHON_HOME，还需要注意的是 export PATH 的时候，需要直接

```
export PATH=$HADOOP_HOME/bin:$SPARK_HOME1/bin:$SPARK_HOME2/bin:$PYTHON_HOME:$JAVA_HOME/bin:$HIVE_HOME/bin:$PATH
```


## Q：Worker Task 通过 sudo -u 租户 sh xxx.command 会产生子进程，在 kill 的时候，是否会杀掉

A： 我们会在 1.0.4 中增加 kill 任务同时，kill 掉任务产生的各种所有子进程



## Q：DolphinScheduler 中的队列怎么用，用户队列和租户队列是什么意思

A ： DolphinScheduler 中的队列可以在用户或者租户上指定队列，**用户指定的队列优先级是高于租户队列的优先级的。**，例如：对 MR 任务指定队列，是通过 mapreduce.job.queuename 来指定队列的。

注意：MR 在用以上方法指定队列的时候，传递参数请使用如下方式：

```
	Configuration conf = new Configuration();
        GenericOptionsParser optionParser = new GenericOptionsParser(conf, args);
        String[] remainingArgs = optionParser.getRemainingArgs();
```


如果是 Spark 任务 --queue 方式指定队列



## Q：Master 或者 Worker 报如下告警

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/master_worker_lack_res.png" width="60%" />
 </p>


A ： 修改 conf 下的 master.properties **master.reserved.memory** 的值为更小的值，比如说 0.1 或者

worker.properties **worker.reserved.memory** 的值为更小的值，比如说 0.1



## Q：hive 版本是 1.1.0+cdh5.15.0，SQL hive 任务连接报错

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/cdh_hive_error.png" width="60%" />
 </p>



A： 将 hive pom

```
<dependency>
    <groupId>org.apache.hive</groupId>
    <artifactId>hive-jdbc</artifactId>
    <version>2.1.0</version>
</dependency>
```

修改为

```
<dependency>
    <groupId>org.apache.hive</groupId>
    <artifactId>hive-jdbc</artifactId>
    <version>1.1.0</version>
</dependency>
```

---

## Q：如何增加一台工作服务器
A： 1，参考官网[部署文档](https://dolphinscheduler.apache.org/zh-cn/docs/laster/user_doc/installation/cluster.html) 1.3 小节，创建部署用户和 hosts 映射

​	2，参考官网[部署文档](https://dolphinscheduler.apache.org/zh-cn/docs/laster/user_doc/installation/cluster.html) 1.4 小节，配置 hosts 映射和 ssh 打通及修改目录权限.
​          1.4 小节的最后一步是在当前新增机器上执行的，即需要给部署目录部署用户的权限

​	3，复制正在运行的服务器上的部署目录到新机器的同样的部署目录下

​	4，到 bin 下，启动 worker server
```
        ./dolphinscheduler-daemon.sh start worker-server
```

---

## Q：DolphinScheduler 什么时候发布新版本，同时新旧版本区别，以及如何升级，版本号规范 
A：1，Apache 项目的发版流程是通过邮件列表完成的。 你可以订阅 DolphinScheduler 的邮件列表，订阅之后如果有发版，你就可以收到邮件。请参照这篇[指引](https://github.com/apache/dolphinscheduler#get-help)来订阅 DolphinScheduler 的邮件列表。

   2，当项目发版的时候，会有发版说明告知具体的变更内容，同时也会有从旧版本升级到新版本的升级文档。

   3，版本号为 x.y.z, 当 x 增加时代表全新架构的版本。当 y 增加时代表与 y 版本之前的不兼容需要升级脚本或其他人工处理才能升级。当 z 增加代表是 bug 修复，升级完全兼容。无需额外处理。之前有个问题 1.0.2 的升级不兼容 1.0.1 需要升级脚本。

---

## Q：后续任务在前置任务失败情况下仍旧可以执行
A：在启动工作流的时候，你可以设置失败策略：继续还是失败。
![设置任务失败策略](https://user-images.githubusercontent.com/15833811/80368215-ee378080-88be-11ea-9074-01a33d012b23.png)

---

## Q：工作流模板 DAG、工作流实例、工作任务及实例之间是什么关系 工作流模板 DAG、工作流实例、工作任务及实例之间是什么关系，一个 dag 支持最大并发 100，是指产生 100 个工作流实例并发运行吗？一个 dag 中的任务节点，也有并发数的配置，是指任务也可以并发多个线程运行吗？最大数 100 吗？
A：

1.2.1 version
```
   master.properties
   设置 master 节点并发执行的最大工作流数
   master.exec.threads=100
   
   Control the number of parallel tasks in each workflow
   设置每个工作流可以并发执行的最大任务数
   master.exec.task.number=20
   
   worker.properties
   设置 worker 节点并发执行的最大任务数
   worker.exec.threads=100
```

---

## Q：工作组管理页面没有展示按钮
<p align="center">
   <img src="https://user-images.githubusercontent.com/39816903/81903776-d8cb9180-95f4-11ea-98cb-94ca1e6a1db5.png" width="60%" />
</p>
A：1.3.0 版本，为了支持 k8s，worker ip 一直变动，因此我们不能在 UI 界面上配置，工作组可以配置在 worker.properties 上配置名称。

---

## Q：为什么不把 mysql 的 jdbc 连接包添加到 docker 镜像里面
A：Mysql jdbc 连接包的许可证和 apache v2 的许可证不兼容，因此它不能被加入到 docker 镜像里面。

---

## Q：当一个任务提交多个 yarn 程序的时候经常失败
<p align="center">
   <img src="https://user-images.githubusercontent.com/16174111/81312485-476e9380-90b9-11ea-9aad-ed009db899b1.png" width="60%" />
</p>
A：这个 Bug 在 dev 分支已修复，并加入到需求/待做列表。

---

## Q：Master 服务和 Worker 服务在运行几天之后停止了
<p align="center">
   <img src="https://user-images.githubusercontent.com/18378986/81293969-c3101680-90a0-11ea-87e5-ac9f0dd53f5e.png" width="60%" />
</p>
A：会话超时时间太短了，只有 0.3 秒，修改 zookeeper.properties 的配置项：

```
   zookeeper.session.timeout=60000
   zookeeper.connection.timeout=30000
```

---

## Q：使用 docker-compose 默认配置启动，显示 zookeeper 错误
<p align="center">
   <img src="https://user-images.githubusercontent.com/42579056/80374318-13c98780-88c9-11ea-8d5f-53448b957f02.png" width="60%" />
 </p>
A：这个问题在 dev-1.3.0 版本解决了。这个 [pr](https://github.com/apache/dolphinscheduler/pull/2595) 已经解决了这个 bug，主要的改动点：

```
    在docker-compose.yml文件中增加zookeeper的环境变量ZOO_4LW_COMMANDS_WHITELIST。
    把minLatency,avgLatency and maxLatency的类型从int改成float。
```

---

## Q：界面上显示任务一直运行，结束不了，从日志上看任务实例为空
<p align="center">
   <img src="https://user-images.githubusercontent.com/51871547/80302626-b1478d00-87dd-11ea-97d4-08aa2244a6d0.jpg" width="60%" />
 </p>
<p align="center">
   <img src="https://user-images.githubusercontent.com/51871547/80302626-b1478d00-87dd-11ea-97d4-08aa2244a6d0.jpg" width="60%" />
 </p>
A：这个 [bug](https://github.com/apache/dolphinscheduler/issues/1477)  描述了问题的详情。这个问题在 1.2.1 版本已经被修复了。
对于 1.2.1 以下的版本，这种情况的一些提示：

```
1，清空 zk 下这个路径的任务：/dolphinscheduler/task_queue
2，修改任务状态为失败（int 值 6）
3，运行工作流来从失败中恢复
```

---

## Q：zk 中注册的 master 信息 ip 地址是 127.0.0.1，而不是配置的域名所对应或者解析的 ip 地址，可能导致不能查看任务日志
A：修复 bug：
```
   1、confirm hostname
   $hostname
   hadoop1
   2、hostname -i
   127.0.0.1 10.3.57.15
   3、edit /etc/hosts,delete hadoop1 from 127.0.0.1 record
   $cat /etc/hosts
   127.0.0.1 localhost
   10.3.57.15 ds1 hadoop1
   4、hostname -i
   10.3.57.15
```
   hostname 命令返回服务器主机名，hostname -i 返回的是服务器主机名在 /etc/hosts 中所有匹配的ip地址。所以我把 /etc/hosts 中 127.0.0.1 中的主机名删掉，只保留内网 ip 的解析就可以了，没必要把 127.0.0.1 整条注释掉, 只要 hostname 命令返回值在 /etc/hosts 中对应的内网 ip 正确就可以，ds 程序取了第一个值，我理解上 ds 程序不应该用 hostname -i 取值这样有点问题，因为好多公司服务器的主机名都是运维配置的，感觉还是直接取配置文件的域名解析的返回 ip 更准确，或者 znode 中存域名信息而不是 /etc/hosts。

---

## Q：调度系统设置了一个秒级的任务，导致系统挂掉
A：调度系统不支持秒级任务。

---

## Q：编译前后端代码 (dolphinscheduler-ui) 报错不能下载"https://github.com/sass/node-sass/releases/download/v4.13.1/darwin-x64-72_binding.node"
A：1，cd dolphinscheduler-ui 然后删除 node_modules 目录 
```
sudo rm -rf node_modules
```
   ​	2，通过 npm.taobao.org 下载 node-sass
 ```
 sudo npm uninstall node-sass
 sudo npm i node-sass --sass_binary_site=https://npm.taobao.org/mirrors/node-sass/
 ```
   3，如果步骤 2 报错，请重新构建 node-saas [参考链接](https://dolphinscheduler.apache.org/en-us/development/frontend-development.html)
```
 sudo npm rebuild node-sass
```
当问题解决之后，如果你不想每次编译都下载这个 node，你可以设置系统环境变量：SASS_BINARY_PATH= /xxx/xxx/xxx/xxx.node。

---

## Q：当使用 mysql 作为 ds 数据库需要如何配置
A：1，修改项目根目录 maven 配置文件，移除 scope 的 test 属性，这样 mysql 的包就可以在其它阶段被加载
```
<dependency>
	<groupId>mysql</groupId>
	<artifactId>mysql-connector-java</artifactId>
	<version>${mysql.connector.version}</version>
	<scope>test<scope>
</dependency>
```
   ​	2，修改 application-dao.properties 和 quzrtz.properties 来使用 mysql 驱动
   默认驱动是 postgres 主要由于许可证原因。

---

## Q：shell 任务是如何运行的
A：1，被执行的服务器在哪里配置，以及实际执行的服务器是哪台? 要指定在某个 worker 上去执行，可以在 worker 分组中配置，固定 IP，这样就可以把路径写死。如果配置的 worker 分组有多个 worker，实际执行的服务器由调度决定的，具有随机性。

   ​	2，如果是服务器上某个路径的一个 shell 文件，怎么指向这个路径？服务器上某个路径下的 shell 文件，涉及到权限问题，不建议这么做。建议你可以使用资源中心的存储功能，然后在 shell 编辑器里面使用资源引用就可以，系统会帮助你把脚本下载到执行目录下。如果以 hdfs 作为资源中心，在执行的时候，调度器会把依赖的 jar 包，文件等资源拉到 worker 的执行目录上，我这边是 /tmp/escheduler/exec/process，该配置可以在 install.sh 中进行指定。

   3，以哪个用户来执行任务？执行任务的时候，调度器会采用 sudo -u 租户的方式去执行，租户是一个 linux 用户。

---

## Q：生产环境部署方式有推荐的最佳实践吗
A：1，如果没有很多任务要运行，出于稳定性考虑我们建议使用 3 个节点，并且最好把 Master/Worder 服务部署在不同的节点。如果你只有一个节点，当然只能把所有的服务部署在同一个节点！通常来说，需要多少节点取决于你的业务，海豚调度系统本身不需要很多的资源。充分测试之后，你们将找到使用较少节点的合适的部署方式。

---

## Q：DEPENDENT 节点
A：1，DEPENDENT 节点实际是没有执行体的，是专门用来配置数据周期依赖逻辑，然后再把执行节点挂载后面，来实现任务间的周期依赖。

---

## Q：如何改变 Master 服务的启动端口
<p align="center">
   <img src="https://user-images.githubusercontent.com/8263441/62352160-0f3e9100-b53a-11e9-95ba-3ae3dde49c72.png" width="60%" />
 </p>
A：1，修改 application_master.properties 配置文件，例如：server.port=12345。

---

## Q：调度任务不能上线
A：1，我们可以成功创建调度任务，并且表 t_scheduler_schedules 中也成功加入了一条记录，但当我点击上线后，前端页面无反应且会把 t_scheduler_schedules 这张表锁定，我测试过将 t_scheduler_schedules 中的 RELEASE_state 字段手动更新为 1 这样前端会显示为上线状态。DS 版本 1.2+ 表名是 t_ds_schedules，其它版本表名是 t_scheduler_schedules。

---

## Q：请问 swagger ui 的地址是什么
A：1，1.2+ 版本地址是：http://apiServerIp:apiServerPort/dolphinscheduler/doc.html?language=zh_CN&lang=cn，其它版本是 http://apiServerIp:apiServerPort/escheduler/doc.html?language=zh_CN&lang=cn。

---

## Q：前端安装包缺少文件
<p align="center">
   <img src="https://user-images.githubusercontent.com/41460919/61437083-d960b080-a96e-11e9-87f1-297ba3aca5e3.png" width="60%" />
 </p>
 <p align="center">
    <img src="https://user-images.githubusercontent.com/41460919/61437218-1b89f200-a96f-11e9-8e48-3fac47eb2389.png" width="60%" />
  </p>
A： 1，用户修改了 api server 配置文件中的![apiServerContextPath](https://user-images.githubusercontent.com/41460919/61678323-1b09a680-ad35-11e9-9707-3ba68bbc70d6.png)配置项，导致了这个问题，恢复成默认配置之后问题解决。

---

## Q：上传比较大的文件卡住
<p align="center">
   <img src="https://user-images.githubusercontent.com/21357069/58231400-805b0e80-7d69-11e9-8107-7f37b06a95df.png" width="60%" />
 </p>
A：1，编辑 ngnix 配置文件 vi /etc/nginx/nginx.conf，更改上传大小 client_max_body_size 1024m。
     
   ​	2，更新 google chrome 版本到最新版本。

---

## Q：创建 spark 数据源，点击“测试连接”，系统回退回到登入页面
A：1，edit /etc/nginx/conf.d/escheduler.conf
```
     proxy_connect_timeout 300s;
     proxy_read_timeout 300s;
     proxy_send_timeout 300s;
```

---

## Q：工作流依赖
A：1，目前是按照自然天来判断，上月末：判断时间是工作流 A start_time/scheduler_time between '2019-05-31 00:00:00' and '2019-05-31 23:59:59'。上月：是判断上个月从 1 号到月末每天都要有完成的A实例。上周： 上周 7 天都要有完成的 A 实例。前两天： 判断昨天和前天，两天都要有完成的 A 实例。

---

## Q：DS 后端接口文档
A：1，http://106.75.43.194:8888/dolphinscheduler/doc.html?language=zh_CN&lang=zh。


## dolphinscheduler 在运行过程中，ip 地址获取错误的问题

master 服务、worker 服务在 zookeeper 注册时，会以 ip:port 的形式创建相关信息

如果 ip 地址获取错误，请检查网络信息，如 Linux 系统通过 `ifconfig` 命令查看网络信息，以下图为例：

<p align="center">
  <img src="/img/network/network_config.png" width="60%" />
</p>

可以使用 dolphinscheduler 提供的三种策略，获取可用 ip：

* default: 优先获取内网网卡获取 ip 地址，其次获取外网网卡获取 ip 地址，在前两项失效情况下，使用第一块可用网卡的地址
* inner: 使用内网网卡获取 ip地址，如果获取失败抛出异常信息
* outer: 使用外网网卡获取 ip地址，如果获取失败抛出异常信息

配置方式是在 `common.properties` 中修改相关配置：

```shell
# network IP gets priority, default: inner outer
# dolphin.scheduler.network.priority.strategy=default
```

以上配置修改后重启服务生效

如果 ip 地址获取依然错误，请下载 [dolphinscheduler-netutils.jar](/asset/dolphinscheduler-netutils.jar) 到相应机器，执行以下命令以进一步排障，并反馈给社区开发人员：

```shell
java -jar target/dolphinscheduler-netutils.jar
```

## 配置 sudo 免密，用于解决默认配置 sudo 权限过大或不能申请 root 权限的使用问题

配置 dolphinscheduler OS 账号的 sudo 权限为部分普通用户范围内的一个普通用户管理者，限制指定用户在指定主机上运行某些命令，详细配置请看 sudo 权限管理
例如 sudo 权限管理配置 dolphinscheduler OS 账号只能操作用户 userA,userB,userC 的权限（其中用户 userA,userB,userC 用于多租户向大数据集群提交作业）

```shell
echo 'dolphinscheduler  ALL=(userA,userB,userC)  NOPASSWD: NOPASSWD: ALL' >> /etc/sudoers
sed -i 's/Defaults    requirett/#Defaults    requirett/g' /etc/sudoers
```

---

## Q：Yarn多集群支持
A：将Worker节点分别部署至多个Yarn集群，步骤如下（例如AWS EMR）：

   1. 将 Worker 节点部署至 EMR 集群的 Master 节点
   
   2. 将 `conf/common.properties` 中的 `yarn.application.status.address` 修改为当前集群的 Yarn 的信息
   
   3. 通过 `bin/dolphinscheduler-daemon.sh start worker-server` 启动 worker-server

---

## Q：Update process definition error: Duplicate key TaskDefinition

A：在DS 2.0.4之前（2.0.0-alpha之后），可能存在版本切换的重复键问题，导致更新工作流失败；可参考如下SQL进行重复数据的删除，以MySQL为例：（注意：操作前请务必备份原数据，SQL来源于pr [#8408](https://github.com/apache/dolphinscheduler/pull/8408)）

```SQL
DELETE FROM t_ds_process_task_relation_log WHERE id IN
(
 SELECT
     x.id
 FROM
     (
         SELECT
             aa.id
         FROM
             t_ds_process_task_relation_log aa
                 JOIN
             (
                 SELECT
                     a.process_definition_code
                      ,MAX(a.id) as min_id
                      ,a.pre_task_code
                      ,a.pre_task_version
                      ,a.post_task_code
                      ,a.post_task_version
                      ,a.process_definition_version
                      ,COUNT(*) cnt
                 FROM
                     t_ds_process_task_relation_log a
                         JOIN (
                         SELECT
                             code
                         FROM
                             t_ds_process_definition
                         GROUP BY code
                     )b ON b.code = a.process_definition_code
                 WHERE 1=1
                 GROUP BY a.pre_task_code
                        ,a.post_task_code
                        ,a.pre_task_version
                        ,a.post_task_version
                        ,a.process_definition_code
                        ,a.process_definition_version
                 HAVING COUNT(*) > 1
             )bb ON bb.process_definition_code = aa.process_definition_code
                 AND bb.pre_task_code = aa.pre_task_code
                 AND bb.post_task_code = aa.post_task_code
                 AND bb.process_definition_version = aa.process_definition_version
                 AND bb.pre_task_version = aa.pre_task_version
                 AND bb.post_task_version = aa.post_task_version
                 AND bb.min_id != aa.id
     )x
)
;

DELETE FROM t_ds_task_definition_log WHERE id IN
(
   SELECT
       x.id
   FROM
       (
           SELECT
               a.id
           FROM
               t_ds_task_definition_log a
                   JOIN
               (
                   SELECT
                       code
                        ,name
                        ,version
                        ,MAX(id) AS min_id
                   FROM
                       t_ds_task_definition_log
                   GROUP BY code
                          ,name
                          ,version
                   HAVING COUNT(*) > 1
               )b ON b.code = a.code
                   AND b.name = a.name
                   AND b.version = a.version
                   AND b.min_id != a.id
       )x
)
;
```

---

## Q：使用Postgresql数据库从2.0.1升级至2.0.5更新失败

A：在数据库中执行以下SQL即可完成修复:
```SQL
update t_ds_version set version='2.0.1';
```

---

## Q：在二进制分发包中找不到 python-gateway-server 文件夹

A：在 3.0.0-alpha 版本之后，Python gateway server 集成到 api server 中，当您启动 api server 后，Python gateway server 将启动。
如果您不想在 api server 启动的时候启动 Python gateway server，您可以修改 api server 中的配置文件 `api-server/conf/application.yaml`
并更改可选项 `python-gateway.enabled` 中的值设置为 `false`。

--- 

我们会持续收集更多的 FAQ。
