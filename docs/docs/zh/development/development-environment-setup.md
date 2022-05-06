# DolphinScheduler 开发手册

## 前置条件

在搭建 DolphinScheduler 开发环境之前请确保你已经安装一下软件

* [Git](https://git-scm.com/downloads): 版本控制系统
* [JDK](https://www.oracle.com/technetwork/java/javase/downloads/index.html): 后端开发
* [Maven](http://maven.apache.org/download.cgi): Java包管理系统
* [Node](https://nodejs.org/en/download): 前端开发

### 克隆代码库

通过你 git 管理工具下载 git 代码，下面以 git-core 为例

```shell
mkdir dolphinscheduler
cd dolphinscheduler
git clone git@github.com:apache/dolphinscheduler.git
```
### 编译源码 
* 如果使用MySQL数据库，请注意修改pom.xml， 添加 ` mysql-connector-java ` 依赖。
* 运行 `mvn clean install -Prelease -Dmaven.test.skip=true`



## 开发者须知

DolphinScheduler 开发环境配置有两个方式，分别是standalone模式，以及普通模式

* [standalone模式](#dolphinscheduler-standalone快速开发模式)：**推荐使用，但仅支持 1.3.9 及以后的版本**，方便快速的开发环境搭建，能解决大部分场景的开发
* [普通模式](#dolphinscheduler-普通开发模式)：master、worker、api等单独启动，能更好的的模拟真实生产环境，可以覆盖的测试环境更多

## DolphinScheduler Standalone快速开发模式

> **_注意：_** 仅供单机开发调试使用，默认使用 H2 Database,Zookeeper Testing Server
> Standalone 仅在 DolphinScheduler 1.3.9 及以后的版本支持

### 分支选择

开发不同的代码需要基于不同的分支

* 如果想基于二进制包开发，切换到对应版本的代码，如 1.3.9 则是 `1.3.9-release`
* 如果想要开发最新代码，切换到 `dev` 分支

### 启动后端

在 Intellij IDEA 找到并启动类 `org.apache.dolphinscheduler.server.StandaloneServer` 即可完成后端启动

### 启动前端

安装前端依赖并运行前端组件

```shell
cd dolphinscheduler-ui
pnpm install
pnpm run dev
```

截止目前，前后端已成功运行起来，浏览器访问[http://localhost:3000](http://localhost:3000)，并使用默认账户密码 **admin/dolphinscheduler123** 即可完成登录

## DolphinScheduler 普通开发模式

### 必要软件安装

#### zookeeper

下载 [ZooKeeper](https://www.apache.org/dyn/closer.lua/zookeeper/zookeeper-3.6.3)，解压

* 在 ZooKeeper 的目录下新建 zkData、zkLog文件夹
* 将 conf 目录下的 `zoo_sample.cfg` 文件，复制一份，重命名为 `zoo.cfg`，修改其中数据和日志的配置，如：

    ```shell
    dataDir=/data/zookeeper/data ## 此处使用绝对路径
    dataLogDir=/data/zookeeper/datalog
    ```

* 运行 `./bin/zkServer.sh`

#### 数据库

DolphinScheduler 的元数据存储在关系型数据库中，目前支持的关系型数据库包括 MySQL 以及 PostgreSQL。下面以MySQL为例，启动数据库并创建新 database 作为 DolphinScheduler 元数据库，这里以数据库名 dolphinscheduler 为例

创建完新数据库后，将 `dolphinscheduler/dolphinscheduler-dao/src/main/resources/sql/dolphinscheduler_mysql.sql` 下的 sql 文件直接在 MySQL 中运行，完成数据库初始化

#### 启动后端

下面步骤将引导如何启动 DolphinScheduler 后端服务

##### 必要的准备工作

* 打开项目：使用开发工具打开项目，这里以 Intellij IDEA 为例，打开后需要一段时间，让 Intellij IDEA 完成以依赖的下载
  
* 插件的配置（**仅 2.0 及以后的版本需要**）：

  * 注册中心插件配置, 以Zookeeper 为例 (registry.properties)
  dolphinscheduler-service/src/main/resources/registry.properties
  ```registry.properties
   registry.plugin.name=zookeeper
   registry.servers=127.0.0.1:2181
  ```
* 必要的修改
  * 如果使用 MySQL 作为元数据库，需要先修改 `dolphinscheduler/pom.xml`，将 `mysql-connector-java` 依赖的 `scope` 改为 `compile`，使用 PostgreSQL 则不需要
  * 修改数据库配置，修改 `dolphinscheduler-dao/src/main/resources/application-mysql.yaml` 文件中的数据库配置


  本样例以 MySQL 为例，其中数据库名为 dolphinscheduler，账户名密码均为 dolphinscheduler
  ```application-mysql.yaml
   spring:
     datasource:
       driver-class-name: com.mysql.jdbc.Driver
       url: jdbc:mysql://127.0.0.1:3306/dolphinscheduler?useUnicode=true&characterEncoding=UTF-8
       username: ds_user
       password: dolphinscheduler
  ```

* 修改日志级别：为以下配置增加一行内容 `<appender-ref ref="STDOUT"/>` 使日志能在命令行中显示
  
  `dolphinscheduler-server/src/main/resources/logback-worker.xml`
  
  `dolphinscheduler-server/src/main/resources/logback-master.xml`
  
  `dolphinscheduler-api/src/main/resources/logback-api.xml` 
  
  修改后的结果如下：

  ```diff
  <root level="INFO">
  +  <appender-ref ref="STDOUT"/>
    <appender-ref ref="APILOGFILE"/>
    <appender-ref ref="SKYWALKING-LOG"/>
  </root>
  ```

##### 启动服务

我们需要启动三个服务，包括 MasterServer，WorkerServer，ApiApplicationServer

* MasterServer：在 Intellij IDEA 中执行 `org.apache.dolphinscheduler.server.master.MasterServer` 中的 `main` 方法，并配置 *VM Options* `-Dlogging.config=classpath:logback-master.xml -Ddruid.mysql.usePingMethod=false -Dspring.profiles.active=mysql`
* WorkerServer：在 Intellij IDEA 中执行 `org.apache.dolphinscheduler.server.worker.WorkerServer` 中的 `main` 方法，并配置 *VM Options* `-Dlogging.config=classpath:logback-worker.xml -Ddruid.mysql.usePingMethod=false -Dspring.profiles.active=mysql`
* ApiApplicationServer：在 Intellij IDEA 中执行 `org.apache.dolphinscheduler.api.ApiApplicationServer` 中的 `main` 方法，并配置 *VM Options* `-Dlogging.config=classpath:logback-api.xml -Dspring.profiles.active=api,mysql`。启动完成可以浏览 Open API 文档，地址为 http://localhost:12345/dolphinscheduler/doc.html

> VM Options `-Dspring.profiles.active=mysql` 中 `mysql` 表示指定的配置文件

### 启动前端

安装前端依赖并运行前端组件

```shell
cd dolphinscheduler-ui
pnpm install
pnpm run dev
```

截止目前，前后端已成功运行起来，浏览器访问[http://localhost:3000](http://localhost:3000)，并使用默认账户密码 **admin/dolphinscheduler123** 即可完成登录
