# DolphinScheduler扩容/缩容 文档

## 1. DolphinScheduler扩容文档

本文扩容是针对现有的DolphinScheduler集群添加新的master或者worker节点的操作说明.

```
注意： 一台物理机上不能存在多个master服务进程或者worker服务进程.
      如果扩容master或者worker节点所在的物理机已经安装了调度的服务,请直接跳到 [1.4.修改配置]. 编辑 ** 所有 ** 节点上的配置文件 `conf/config/install_config.conf`. 新增masters或者workers参数,重启调度集群即可.
```

### 1.1. 基础软件安装(必装项请自行安装)

* [必装] [JDK](https://www.oracle.com/technetwork/java/javase/downloads/index.html) (1.8+) :  必装，请安装好后在/etc/profile下配置 JAVA_HOME 及 PATH 变量
* [可选] 如果扩容的是worker类型的节点,需要考虑是否要安装外部客户端,比如Hadoop、Hive、Spark 的Client.

```markdown
注意：DolphinScheduler本身不依赖Hadoop、Hive、Spark,仅是会调用他们的Client，用于对应任务的提交。
```

### 1.2. 获取安装包

- 确认现有环境使用的DolphinScheduler是哪个版本,获取对应版本的安装包,如果版本不同,可能存在兼容性的问题.
- 确认其他节点的统一安装目录,本文假设DolphinScheduler统一安装在 /opt/ 目录中,安装全路径为/opt/dolphinscheduler.
- 请下载对应版本的安装包至服务器安装目录,解压并重名为dolphinscheduler存放在/opt目录中.
- 添加数据库依赖包,本文使用Mysql数据库,添加mysql-connector-java驱动包到/opt/dolphinscheduler/lib目录中

```shell
# 创建安装目录,安装目录请不要创建在/root、/home等高权限目录 
mkdir -p /opt
cd /opt
# 解压缩
tar -zxvf apache-dolphinscheduler-<version>-bin.tar.gz -C /opt 
cd /opt
mv apache-dolphinscheduler-<version>-bin  dolphinscheduler
```

```markdown
注意：安装包可以从现有的环境直接复制到扩容的物理机上使用.
```

### 1.3. 创建部署用户

- 在**所有**扩容的机器上创建部署用户，并且一定要配置sudo免密。假如我们计划在ds1,ds2,ds3,ds4这四台扩容机器上部署调度，首先需要在每台机器上都创建部署用户

```shell
# 创建用户需使用root登录，设置部署用户名，请自行修改，后面以dolphinscheduler为例
useradd dolphinscheduler;

# 设置用户密码，请自行修改，后面以dolphinscheduler123为例
echo "dolphinscheduler123" | passwd --stdin dolphinscheduler

# 配置sudo免密
echo 'dolphinscheduler  ALL=(ALL)  NOPASSWD: NOPASSWD: ALL' >> /etc/sudoers
sed -i 's/Defaults    requirett/#Defaults    requirett/g' /etc/sudoers

```

```markdown
注意：
- 因为是以 sudo -u {linux-user} 切换不同linux用户的方式来实现多租户运行作业，所以部署用户需要有 sudo 权限，而且是免密的。
- 如果发现/etc/sudoers文件中有"Default requiretty"这行，也请注释掉
- 如果用到资源上传的话，还需要在`HDFS或者MinIO`上给该部署用户分配读写的权限
```

### 1.4. 修改配置

- 从现有的节点比如Master/Worker节点,直接拷贝conf目录替换掉新增节点中的conf目录.拷贝之后检查一下配置项是否正确.

  ```markdown
  重点检查:
  datasource.properties 中的数据库连接信息. 
  zookeeper.properties 中的连接zk的信息.
  common.properties 中关于资源存储的配置信息(如果设置了hadoop,请检查是否存在core-site.xml和hdfs-site.xml配置文件).
  dolphinscheduler_env.sh 中的环境变量
  ```
- 根据机器配置,修改 conf/env 目录下的 `dolphinscheduler_env.sh` 环境变量(以相关用到的软件都安装在/opt/soft下为例)

  ```shell
      export HADOOP_HOME=/opt/soft/hadoop
      export HADOOP_CONF_DIR=/opt/soft/hadoop/etc/hadoop
      export SPARK_HOME=/opt/soft/spark
      export PYTHON_HOME=/opt/soft/python
      export JAVA_HOME=/opt/soft/java
      export HIVE_HOME=/opt/soft/hive
      export FLINK_HOME=/opt/soft/flink
      export DATAX_HOME=/opt/soft/datax/bin/datax.py
      export PATH=$HADOOP_HOME/bin:$SPARK_HOME/bin:$PYTHON_HOME:$JAVA_HOME/bin:$HIVE_HOME/bin:$PATH:$FLINK_HOME/bin:$DATAX_HOME:$PATH

      ```

   `注: 这一步非常重要,例如 JAVA_HOME 和 PATH 是必须要配置的，没有用到的可以忽略或者注释掉`


  ```
- 将jdk软链到/usr/bin/java下(仍以 JAVA_HOME=/opt/soft/java 为例)

  ```shell
  sudo ln -s /opt/soft/java/bin/java /usr/bin/java
  ```
- 修改 **所有** 节点上的配置文件 `conf/config/install_config.conf`, 同步修改以下配置.
  * 新增的master节点, 需要修改 ips 和 masters 参数.
  * 新增的worker节点, 需要修改 ips 和  workers 参数.

```shell
#在哪些机器上新增部署DS服务,多个物理机之间用逗号隔开.
ips="ds1,ds2,ds3,ds4"

#ssh端口,默认22
sshPort="22"

#master服务部署在哪台机器上
masters="现有master01,现有master02,ds1,ds2"

#worker服务部署在哪台机器上,并指定此worker属于哪一个worker组,下面示例的default即为组名
workers="现有worker01:default,现有worker02:default,ds3:default,ds4:default"

```

- 如果扩容的是worker节点,需要设置worker分组.请参考安全中心[创建worker分组](./security.md)

- 在所有的新增节点上，修改目录权限，使得部署用户对dolphinscheduler目录有操作权限

```shell
sudo chown -R dolphinscheduler:dolphinscheduler dolphinscheduler
```

### 1.4. 重启集群&验证

- 重启集群

```shell
停止命令:
bin/stop-all.sh 停止所有服务

bash bin/dolphinscheduler-daemon.sh stop master-server  停止 master 服务
bash bin/dolphinscheduler-daemon.sh stop worker-server  停止 worker 服务
bash bin/dolphinscheduler-daemon.sh stop api-server     停止 api    服务
bash bin/dolphinscheduler-daemon.sh stop alert-server   停止 alert  服务


启动命令:
bin/start-all.sh 启动所有服务

bash bin/dolphinscheduler-daemon.sh start master-server  启动 master 服务
bash bin/dolphinscheduler-daemon.sh start worker-server  启动 worker 服务
bash bin/dolphinscheduler-daemon.sh start api-server     启动 api    服务
bash bin/dolphinscheduler-daemon.sh start alert-server   启动 alert  服务

```

```
注意： 使用stop-all.sh或者stop-all.sh的时候,如果执行该命令的物理机没有配置到所有机器的ssh免登陆的话,会提示输入密码
```

- 脚本完成后，使用`jps`命令查看各个节点服务是否启动(`jps`为`java JDK`自带)

```
MasterServer         ----- master服务
WorkerServer         ----- worker服务
ApiApplicationServer ----- api服务
AlertServer          ----- alert服务
```

启动成功后，可以进行日志查看，日志统一存放于logs文件夹内

```日志路径
logs/
   ├── dolphinscheduler-alert-server.log
   ├── dolphinscheduler-master-server.log
   ├── dolphinscheduler-worker-server.log
   ├── dolphinscheduler-api-server.log
```

如果以上服务都正常启动且调度系统页面正常,在web系统的[监控中心]查看是否有扩容的Master或者Worker服务.如果存在,则扩容完成

-----------------------------------------------------------------------------

## 2. 缩容

缩容是针对现有的DolphinScheduler集群减少master或者worker服务,
缩容一共分两个步骤,执行完以下两步,即可完成缩容操作.

### 2.1 停止缩容节点上的服务

* 如果缩容master节点,要确定要缩容master服务所在的物理机,并在物理机上停止该master服务.
* 如果缩容worker节点,要确定要缩容worker服务所在的物理机,并在物理机上停止worker服务.

```shell
停止命令:
bin/stop-all.sh 停止所有服务

bash bin/dolphinscheduler-daemon.sh stop master-server  停止 master 服务
bash bin/dolphinscheduler-daemon.sh stop worker-server  停止 worker 服务
bash bin/dolphinscheduler-daemon.sh stop api-server     停止 api    服务
bash bin/dolphinscheduler-daemon.sh stop alert-server   停止 alert  服务


启动命令:
bin/start-all.sh 启动所有服务

bash bin/dolphinscheduler-daemon.sh start master-server  启动 master 服务
bash bin/dolphinscheduler-daemon.sh start worker-server  启动 worker 服务
bash bin/dolphinscheduler-daemon.sh start api-server     启动 api    服务
bash bin/dolphinscheduler-daemon.sh start alert-server   启动 alert  服务

```

```
注意： 使用stop-all.sh或者stop-all.sh的时候,如果没有执行该命令的机器没有配置到所有机器的ssh免登陆的话,会提示输入密码
```

- 脚本完成后，使用`jps`命令查看各个节点服务是否成功关闭(`jps`为`java JDK`自带)

```
MasterServer         ----- master服务
WorkerServer         ----- worker服务
ApiApplicationServer ----- api服务
AlertServer          ----- alert服务
```

如果对应的master服务或者worker服务不存在,则代表master/worker服务成功关闭.

### 2.2 修改配置文件

- 修改 **所有** 节点上的配置文件 `conf/config/install_config.conf`, 同步修改以下配置.
  * 缩容master节点, 需要修改 ips 和 masters 参数.
  * 缩容worker节点, 需要修改 ips 和  workers 参数.

```shell
#在哪些机器上部署DS服务，本机选localhost
ips="ds1,ds2,ds3,ds4"

#ssh端口,默认22
sshPort="22"

#master服务部署在哪台机器上
masters="现有master01,现有master02,ds1,ds2"

#worker服务部署在哪台机器上,并指定此worker属于哪一个worker组,下面示例的default即为组名
workers="现有worker01:default,现有worker02:default,ds3:default,ds4:default"

```

