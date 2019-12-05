# 后端部署文档

后端有2种部署方式，分别为自动化部署和编译源码部署

## 1、准备工作

请下载最新版本的安装包，下载地址： [码云下载](https://gitee.com/easyscheduler/EasyScheduler/attach_files/)或者[github下载](https://github.com/analysys/EasyScheduler/releases) ，下载escheduler-backend-x.x.x.tar.gz(后端简称escheduler-backend),escheduler-ui-x.x.x.tar.gz(前端简称escheduler-ui)

#### 准备一: 基础软件安装(必装项请自行安装)

 * [Mysql](http://geek.analysys.cn/topic/124) (5.5+) :  必装
 * [JDK](https://www.oracle.com/technetwork/java/javase/downloads/index.html) (1.8+) :  必装
 * [ZooKeeper](https://www.jianshu.com/p/de90172ea680)(3.4.6+) ：必装 
 * [Hadoop](https://blog.csdn.net/Evankaka/article/details/51612437)(2.6+) ：选装， 如果需要使用到资源上传功能，MapReduce任务提交则需要配置Hadoop(上传的资源文件目前保存在Hdfs上)
 * [Hive](https://staroon.pro/2017/12/09/HiveInstall/)(1.2.1) :  选装，hive任务提交需要安装
 * Spark(1.x,2.x) : 选装，Spark任务提交需要安装
 * PostgreSQL(8.2.15+) : 选装，PostgreSQL PostgreSQL存储过程需要安装
 
```
 注意：EasyScheduler本身不依赖Hadoop、Hive、Spark、PostgreSQL,仅是会调用他们的Client，用于对应任务的运行。
```

#### 准备二: 创建部署用户

- 在所有需要部署调度的机器上创建部署用户，因为worker服务是以 sudo -u {linux-user} 方式来执行作业，所以部署用户需要有 sudo 权限，而且是免密的。

```部署账号
vi /etc/sudoers

# 例如部署用户是escheduler账号
escheduler  ALL=(ALL)       NOPASSWD: NOPASSWD: ALL

# 并且需要注释掉 Default requiretty 一行
#Default requiretty
```

#### 准备三: ssh免密配置
 在部署机器和其他安装机器上配置ssh免密登录，如果要在部署机上安装调度，需要配置本机免密登录自己
 
- [将 **主机器** 和各个其它机器SSH打通](http://geek.analysys.cn/topic/113)


#### 准备四：数据库初始化

* 创建database和账号
    
    执行以下命令创建database和账号
    
    ```sql 
    CREATE DATABASE escheduler DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
    GRANT ALL PRIVILEGES ON escheduler.* TO '{user}'@'%' IDENTIFIED BY '{password}';
    GRANT ALL PRIVILEGES ON escheduler.* TO '{user}'@'localhost' IDENTIFIED BY '{password}';
    flush privileges;
    ```

* 创建表和导入基础数据
    修改./conf/dao/data_source.properties中的下列属性

    ```
        spring.datasource.url
        spring.datasource.username
        spring.datasource.password
    ```
    执行创建表和导入基础数据脚本
    ```
    sh ./script/create-escheduler.sh
    ```

#### 准备五: 修改部署目录权限及运行参数

    escheduler-backend目录介绍

```
bin : 基础服务启动脚本
conf : 项目配置文件
lib : 项目依赖jar包，包括各个模块jar和第三方jar
script : 集群启动、停止和服务监控启停脚本
sql : 项目依赖sql文件
install.sh : 一键部署脚本
```

- 修改权限(请将'deployUser'字段修改为对应部署用户)，使得部署用户对escheduler-backend目录有操作权限
    
    `sudo chown -R deployUser:deployUser escheduler-backend`

- 修改conf/env/目录下的 `.escheduler_env.sh` 环境变量

- 修改部署参数(根据自己服务器及业务情况):

 - 修改 **install.sh**中的各参数，替换成自身业务所需的值
   - monitorServerState 开关变量,在1.0.3版本中增加，控制是否启动自启动脚本(监控master,worker状态,如果掉线会自动启动)
   默认值为"false"表示不启动自启动脚本,如果需要启动改为"true"

   - hdfsStartupSate 开关变量，控制是否启动hdfs
      默认值为"false"表示不启动hdfs
      如果需要启动改为"true",启动hdfs需要自行创建hdfs根路径，也就是install.sh中的 hdfsPath

 - 如果使用hdfs相关功能，需要拷贝**hdfs-site.xml**和**core-site.xml**到conf目录下


## 2、部署
推荐自动化部署，有经验的小伙伴也可以使用源码部署

### 2.1 自动部署

- 安装zookeeper工具 

   `pip install kazoo`

- 切换到部署用户，一键部署

    `sh install.sh` 

- 使用`jps`命令查看服务是否启动(`jps`为`java JDK`自带)

```aidl
    MasterServer         ----- master服务
    WorkerServer         ----- worker服务
    LoggerServer         ----- logger服务
    ApiApplicationServer ----- api服务
    AlertServer          ----- alert服务
```
如果以上服务都正常启动，说明自动部署成功


部署成功后，可以进行日志查看，日志统一存放于指定文件夹内

```日志路径
 logs/
    ├── escheduler-alert-server.log
    ├── escheduler-master-server.log
    |—— escheduler-worker-server.log
    |—— escheduler-api-server.log
    |—— escheduler-logger-server.log
```

### 2.2 编译源码来部署

将源码包release版本下载后，解压进入根目录

* 执行编译命令：

```
 mvn -U clean package assembly:assembly -Dmaven.test.skip=true
```

* 查看目录

正常编译完后，会在当前目录生成 `./target/escheduler-{version}/`

```查看目录
 ../
    ├── bin
    ├── conf
    |—— install.sh
    |—— lib
    |—— logs
    |—— script
    |—— sql
```


### 2.3 系统常用启停服务(服务用途请具体参见《系统架构设计》小节)

* 一键停止集群所有服务
   
   ` sh ./bin/stop-all.sh`
   
* 一键开启集群所有服务
   
   ` sh ./bin/start-all.sh`

* 启停Master

```启动master
sh ./bin/escheduler-daemon.sh start master-server
sh ./bin/escheduler-daemon.sh stop master-server
```

* 启停Worker

```
sh ./bin/escheduler-daemon.sh start worker-server
sh ./bin/escheduler-daemon.sh stop worker-server
```

* 启停Api

```
sh ./bin/escheduler-daemon.sh start api-server
sh ./bin/escheduler-daemon.sh stop api-server
```
* 启停Logger

```
sh ./bin/escheduler-daemon.sh start logger-server
sh ./bin/escheduler-daemon.sh stop logger-server
```
* 启停Alert

```
sh ./bin/escheduler-daemon.sh start alert-server
sh ./bin/escheduler-daemon.sh stop alert-server
```

## 3、数据库升级
数据库升级是在1.0.2版本增加的功能,执行以下命令即可自动升级数据库
```
sh ./script/upgrade-escheduler.sh
```
