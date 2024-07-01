# DolphinScheduler 开发手册

## 软件要求

在搭建 DolphinScheduler 开发环境之前请确保你已经安装以下软件:

* [Git](https://git-scm.com/downloads)
* [JDK](https://www.oracle.com/technetwork/java/javase/downloads/index.html): v1.8.x (当前暂不支持 jdk 11)
* [Maven](http://maven.apache.org/download.cgi): v3.5+
* [Node](https://nodejs.org/en/download): v16.13+ (dolphinScheduler 版本低于 3.0, 请安装 node v12.20+)
* [Pnpm](https://pnpm.io/installation): v6.x

### 克隆代码库

通过你 git 管理工具下载 git 代码，下面以 git-core 为例

```shell
mkdir dolphinscheduler
cd dolphinscheduler
git clone git@github.com:apache/dolphinscheduler.git
```

### 编译源码

支持的系统:
* MacOS
* Linux

运行 `mvn clean install -Prelease -Dmaven.test.skip=true`

### 代码风格

DolphinScheduler使用`Spotless`检查并修复代码风格和格式问题。
您可以执行如下的命令，`Spotless`将会为您自动检查并修复代码风格和格式问题。

```shell
./mvnw spotless:apply
```

我们也提供了一个`pre-commit`配置文件，方便您配置。要使用它，您需要先安装python，然后通过运行以下命令安装`pre-commit`：

```shell
python -m pip install pre-commit
```

之后，您可以运行以下命令安装`pre-commit`钩子：

```shell
pre-commit install
```

现在，每次您提交代码时，`pre-commit`都会自动运行`Spotless`来检查代码风格和格式。

## Docker镜像构建

DolphinScheduler 每次发版都会同时发布 Docker 镜像，你可以在 [Docker Hub](https://hub.docker.com/search?q=DolphinScheduler) 中找到这些镜像

* 如果你想基于源码进行改造，然后在本地构建Docker镜像，可以在代码改造完成后运行

```shell
cd dolphinscheduler
./mvnw -B clean package \
       -Dmaven.test.skip \
       -Dmaven.javadoc.skip \
       -Dspotless.skip=true \
       -Ddocker.tag=<TAG> \
       -Pdocker,release
```

当命令运行完了后你可以通过 `docker images` 命令查看刚刚创建的镜像

* 如果你想基于源码进行改造，然后构建Docker镜像并推送到 <HUB_URL>，可以在代码改造完成后运行

```shell
cd dolphinscheduler
./mvnw -B clean deploy \
       -Dmaven.test.skip \
       -Dmaven.javadoc.skip \
       -Dspotless.skip = true \
       -Dmaven.deploy.skip \
       -Ddocker.tag=<TAG> \
       -Ddocker.hub=<HUB_URL> \
       -Pdocker,release
```

* 如果你不仅需要改造源码，还想要自定义 Docker 镜像打包的依赖，可以在修改源码的同时修改 Dockerfile 的定义。你可以运行以下命令找到所有的 Dockerfile 文件

```shell
cd dolphinscheduler
find . -iname 'Dockerfile'
```

之后再运行上面的构建镜像命令

* 如果你因为个性化需求想要自己打包 Docker 镜像，最佳实践是基于 DolphinScheduler 对应镜像编写 Dockerfile 文件

```Dockerfile
FROM dolphinscheduler-standalone-server
RUN apt update ; \
    apt install -y <YOUR-CUSTOM-DEPENDENCE> ; \
```

> **_注意：_** Docker默认会构建并推送 linux/amd64,linux/arm64 多架构镜像
>
> 必须使用Docker 19.03及以后的版本，因为19.03及以后的版本包含 buildx

## 开发者须知

DolphinScheduler 开发环境配置有两个方式，分别是standalone模式，以及普通模式

* [standalone模式](#dolphinscheduler-standalone快速开发模式)：**推荐使用，但仅支持 1.3.9 及以后的版本**，方便快速的开发环境搭建，能解决大部分场景的开发
* [普通模式](#dolphinscheduler-普通开发模式)：master、worker、api等单独启动，能更好的的模拟真实生产环境，可以覆盖的测试环境更多

## DolphinScheduler Standalone快速开发模式

> **_注意：_** 仅供单机开发调试使用，默认使用 H2 Database,Zookeeper Testing Server
>
> Standalone 仅在 DolphinScheduler 1.3.9 及以后的版本支持

### 分支选择

开发不同的代码需要基于不同的分支

* 如果想基于二进制包开发，切换到对应版本的代码，如 1.3.9 则是 `1.3.9-release`
* 如果想要开发最新代码，切换到 `dev` 分支

### 启动后端

在 Intellij IDEA 找到并启动类 `org.apache.dolphinscheduler.StandaloneServer` 即可完成后端启动

### 启动前端

安装前端依赖并运行前端组件

> 注意：你可以在[frontend development](./frontend-development.md)里查看更多前端的相关配置

```shell
cd dolphinscheduler-ui
pnpm install
pnpm run dev
```

截止目前，前后端已成功运行起来，浏览器访问[http://localhost:5173](http://localhost:5173)，并使用默认账户密码 **admin/dolphinscheduler123** 即可完成登录

## DolphinScheduler 普通开发模式

### 必要软件安装

#### zookeeper

下载 [ZooKeeper](https://zookeeper.apache.org/releases.html)，解压

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

* 必要的修改

  * 如果使用 MySQL 作为元数据库，需要先修改 `dolphinscheduler/pom.xml`，将 `mysql-connector-java` 依赖的 `scope` 改为 `compile`，使用 PostgreSQL 则不需要
  * 修改 Master 数据库配置，修改 `dolphinscheduler-master/src/main/resources/application.yaml` 文件中的数据库配置
  * 修改 Worker 数据库配置，修改 `dolphinscheduler-worker/src/main/resources/application.yaml` 文件中的数据库配置
  * 修改 Api 数据库配置，修改 `dolphinscheduler-api/src/main/resources/application.yaml` 文件中的数据库配置

  本样例以 MySQL 为例，其中数据库名为 dolphinscheduler，账户名密码均为 dolphinscheduler

  ```application.yaml
  spring:
    datasource:
      driver-class-name: com.mysql.cj.jdbc.Driver
      url: jdbc:mysql://127.0.0.1:3306/dolphinscheduler?useUnicode=true&characterEncoding=UTF-8
      username: dolphinscheduler
      password: dolphinscheduler
  ```
* 修改日志级别：为以下配置增加一行内容 `<appender-ref ref="STDOUT"/>` 使日志能在命令行中显示

  `dolphinscheduler-master/src/main/resources/logback-spring.xml`
  `dolphinscheduler-worker/src/main/resources/logback-spring.xml`
  `dolphinscheduler-api/src/main/resources/logback-spring.xml`

  修改后的结果如下：

  ```diff
  <root level="INFO">
  +  <appender-ref ref="STDOUT"/>
    <appender-ref ref="APILOGFILE"/>
  </root>
  ```

##### 启动服务

我们需要启动三个服务，包括 MasterServer，WorkerServer，ApiApplicationServer

* MasterServer：在 Intellij IDEA 中执行 `org.apache.dolphinscheduler.server.master.MasterServer` 中的 `main` 方法，并配置 *VM Options* `-Dlogging.config=classpath:logback-spring.xml -Ddruid.mysql.usePingMethod=false -Dspring.profiles.active=mysql`
* WorkerServer：在 Intellij IDEA 中执行 `org.apache.dolphinscheduler.server.worker.WorkerServer` 中的 `main` 方法，并配置 *VM Options* `-Dlogging.config=classpath:logback-spring.xml -Ddruid.mysql.usePingMethod=false -Dspring.profiles.active=mysql`
* ApiApplicationServer：在 Intellij IDEA 中执行 `org.apache.dolphinscheduler.api.ApiApplicationServer` 中的 `main` 方法，并配置 *VM Options* `-Dlogging.config=classpath:logback-spring.xml -Dspring.profiles.active=api,mysql`。启动完成可以浏览 Open API 文档，地址为 http://localhost:12345/dolphinscheduler/swagger-ui/index.html

> VM Options `-Dspring.profiles.active=mysql` 中 `mysql` 表示指定的配置文件

### 启动前端

安装前端依赖并运行前端组件

```shell
cd dolphinscheduler-ui
pnpm install
pnpm run dev
```

截止目前，前后端已成功运行起来，浏览器访问[http://localhost:5173](http://localhost:5173)，并使用默认账户密码 **admin/dolphinscheduler123** 即可完成登录
