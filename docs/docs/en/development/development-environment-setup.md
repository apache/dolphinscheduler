# DolphinScheduler development

## Software Requests

Before setting up the DolphinScheduler development environment, please make sure you have installed the software as below:

* [Git](https://git-scm.com/downloads): DolphinScheduler version control system
* [JDK](https://www.oracle.com/technetwork/java/javase/downloads/index.html): DolphinScheduler backend language
* [Maven](http://maven.apache.org/download.cgi): Java Package Management System
* [Node](https://nodejs.org/en/download): DolphinScheduler frontend
 language

### Clone Git Repository

Download the git repository through your git management tool, here we use git-core as an example

```shell
mkdir dolphinscheduler
cd dolphinscheduler
git clone git@github.com:apache/dolphinscheduler.git
```
### compile source code

i. If you use MySQL database, pay attention to modify pom.xml in the root project, and change the scope of the mysql-connector-java dependency to compile.

ii. Run `mvn clean install -Prelease -Dmaven.test.skip=true`


## Notice

There are two ways to configure the DolphinScheduler development environment, standalone mode and normal mode

* [Standalone mode](#dolphinscheduler-standalone-quick-start): **Recommended**，more convenient to build development environment, it can cover most scenes.
* [Normal mode](#dolphinscheduler-normal-mode): Separate server master, worker, api, which can cover more test environments than standalone, and it is more like production environment in real life.

## DolphinScheduler Standalone Quick Start

> **_Note:_** Use standalone server only for development and debugging, because it uses H2 Database as default database and Zookeeper Testing Server which may not be stable in production.
> Standalone is only supported in DolphinScheduler 1.3.9 and later versions.
> Standalone server is able to connect to external databases like mysql and postgresql, see [Standalone Deployment](https://dolphinscheduler.apache.org/en-us/docs/dev/user_doc/guide/installation/standalone.html) for instructions.

### Git Branch Choose

Use different Git branch to develop different codes

* If you want to develop based on a binary package, switch git branch to specific release branch, for example, if you want to develop base on 1.3.9, you should choose branch `1.3.9-release`.
* If you want to develop the latest code, choose branch branch `dev`.

### Start backend server

Find the class `org.apache.dolphinscheduler.server.StandaloneServer` in Intellij IDEA and clikc run main function to startup.

### Start frontend server

Install frontend dependencies and run it

```shell
cd dolphinscheduler-ui
pnpm install
pnpm run dev
```

The browser access address [http://localhost:3000](http://localhost:3000) can login DolphinScheduler UI. The default username and password are **admin/dolphinscheduler123**

## DolphinScheduler Normal Mode

### Prepare

#### zookeeper

Download [ZooKeeper](https://www.apache.org/dyn/closer.lua/zookeeper/zookeeper-3.6.3), and extract it.

* Create directory `zkData` and `zkLog`
* Go to the zookeeper installation directory, copy configure file `zoo_sample.cfg` to `conf/zoo.cfg`, and change value of dataDir in conf/zoo.cfg to dataDir=./tmp/zookeeper

    ```shell
    # We use path /data/zookeeper/data and /data/zookeeper/datalog here as example
    dataDir=/data/zookeeper/data
    dataLogDir=/data/zookeeper/datalog
    ```

* Run `./bin/zkServer.sh` in terminal by command `./bin/zkServer.sh start`.

#### Database

The DolphinScheduler's metadata is stored in relational database. Currently supported MySQL and Postgresql. We use MySQL as an example. Start the database and create a new database named dolphinscheduler as DolphinScheduler metabase

After creating the new database, run the sql file under `dolphinscheduler/dolphinscheduler-dao/src/main/resources/sql/dolphinscheduler_mysql.sql` directly in MySQL to complete the database initialization

#### Start Backend Server

Following steps will guide how to start the DolphinScheduler backend service

##### Backend Start Prepare

* Open project: Use IDE open the project, here we use Intellij IDEA as an example, after opening it will take a while for Intellij IDEA to complete the dependent download
* Plugin installation(**Only required for 2.0 or later**)

 * Registry plug-in configuration, take Zookeeper as an example (registry.properties)
  dolphinscheduler-service/src/main/resources/registry.properties
  ```registry.properties
   registry.plugin.name=zookeeper
   registry.servers=127.0.0.1:2181
  ```
* File change
  * If you use MySQL as your metadata database, you need to modify `dolphinscheduler/pom.xml` and change the `scope` of the `mysql-connector-java` dependency to `compile`. This step is not necessary to use PostgreSQL
  * Modify database configuration, modify the database configuration in the `dolphinscheduler-dao/src/main/resources/application-mysql.yaml`


  We here use MySQL with database, username, password named dolphinscheduler as an example
  ```application-mysql.yaml
   spring:
     datasource:
       driver-class-name: com.mysql.jdbc.Driver
       url: jdbc:mysql://127.0.0.1:3306/dolphinscheduler?useUnicode=true&characterEncoding=UTF-8
       username: ds_user
       password: dolphinscheduler
  ```

* Log level: add a line `<appender-ref ref="STDOUT"/>` to the following configuration to enable the log to be displayed on the command line

  `dolphinscheduler-server/src/main/resources/logback-worker.xml`
  
  `dolphinscheduler-server/src/main/resources/logback-master.xml` 
  
  `dolphinscheduler-api/src/main/resources/logback-api.xml` 

  here we add the result after modify as below:

  ```diff
  <root level="INFO">
  +  <appender-ref ref="STDOUT"/>
    <appender-ref ref="APILOGFILE"/>
    <appender-ref ref="SKYWALKING-LOG"/>
  </root>
  ```

> **_Note:_** Only DolphinScheduler 2.0 and later versions need to inatall plugin before start server. It not need before version 2.0.

##### Server start

There are three services that need to be started, including MasterServer, WorkerServer, ApiApplicationServer.

* MasterServer：Execute function `main` in the class `org.apache.dolphinscheduler.server.master.MasterServer` by Intellij IDEA, with the configuration *VM Options* `-Dlogging.config=classpath:logback-master.xml -Ddruid.mysql.usePingMethod=false -Dspring.profiles.active=mysql`
* WorkerServer：Execute function `main` in the class `org.apache.dolphinscheduler.server.worker.WorkerServer` by Intellij IDEA, with the configuration *VM Options* `-Dlogging.config=classpath:logback-worker.xml -Ddruid.mysql.usePingMethod=false -Dspring.profiles.active=mysql`
* ApiApplicationServer：Execute function `main` in the class `org.apache.dolphinscheduler.api.ApiApplicationServer` by Intellij IDEA, with the configuration *VM Options* `-Dlogging.config=classpath:logback-api.xml -Dspring.profiles.active=api,mysql`. After it started, you could find Open API documentation in http://localhost:12345/dolphinscheduler/doc.html

> The `mysql` in the VM Options `-Dspring.profiles.active=mysql` means specified configuration file

### Start Frontend Server

Install frontend dependencies and run it

```shell
cd dolphinscheduler-ui
pnpm install
pnpm run dev
```

The browser access address [http://localhost:3000](http://localhost:3000) can login DolphinScheduler UI. The default username and password are **admin/dolphinscheduler123**
