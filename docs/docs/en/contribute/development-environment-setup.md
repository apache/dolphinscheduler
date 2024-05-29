# DolphinScheduler development

## Software Requirements

Before setting up the DolphinScheduler development environment, please make sure you have installed the software as below:

- [Git](https://git-scm.com/downloads)
- [JDK](https://www.oracle.com/technetwork/java/javase/downloads/index.html): v1.8.x (Currently does not support jdk 11)
- [Maven](http://maven.apache.org/download.cgi): v3.5+
- [Node](https://nodejs.org/en/download): v16.13+ (dolphinScheduler version is lower than 3.0, please install node v12.20+)
- [Pnpm](https://pnpm.io/installation): v6.x

### Clone Git Repository

Download the git repository through your git management tool, here we use git-core as an example

```shell
mkdir dolphinscheduler
cd dolphinscheduler
git clone git@github.com:apache/dolphinscheduler.git
```

### Compile Source Code

Supporting system:

- MacOS
- Liunx

Run `mvn clean install -Prelease -Dmaven.test.skip=true`

### Code Style

DolphinScheduler uses `Spotless` for code style and formatting checks.
You could run the following command and `Spotless` will automatically fix
the code style and formatting errors for you:

```shell
./mvnw spotless:apply
```

We also have provided a `pre-commit` config file for easy configuration. To use it, you need to have python installed
and then install `pre-commit` by running the following command:

```shell
python -m pip install pre-commit
```

After that, you can run the following command to install the `pre-commit` hook:

```shell
pre-commit install
```

Now, every time you commit your code, `pre-commit` will automatically run `Spotless` to check the code style and formatting.

## Docker image build

DolphinScheduler will release new Docker images after it released, you could find them in [Docker Hub](https://hub.docker.com/search?q=DolphinScheduler).

- If you want to modify DolphinScheduler source code, and build Docker images locally, you can run when finished the modification

```shell
cd dolphinscheduler
./mvnw -B clean package \
       -Dmaven.test.skip \
       -Dmaven.javadoc.skip \
       -Dspotless.skip = true \
       -Ddocker.tag=<TAG> \
       -Pdocker,release
```

When the command is finished you could find them by command `docker images`.

- If you want to modify DolphinScheduler source code, build and push Docker images to your registry <HUB_URL>，you can run when finished the modification

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

- If you want to modify DolphinScheduler source code, and also want to add customize dependencies of Docker image, you can modify the definition of Dockerfile after modifying the source code. You can run the following command to find all Dockerfile files.

```shell
cd dolphinscheduler
find . -iname 'Dockerfile'
```

Then run the Docker build command above

- You could create custom Docker images base on those images if you want to change image like add some dependencies or upgrade package.

```Dockerfile
FROM dolphinscheduler-standalone-server
RUN apt update ; \
    apt install -y <YOUR-CUSTOM-DEPENDENCE> ; \
```

> **_Note：_** Docker will build and push linux/amd64,linux/arm64 multi-architecture images by default
>
> Have to use version after Docker 19.03, because after 19.03 docker contains buildx

## Notice

There are two ways to configure the DolphinScheduler development environment, standalone mode and normal mode

- [Standalone mode](#dolphinscheduler-standalone-quick-start): **Recommended**，more convenient to build development environment, it can cover most scenes.
- [Normal mode](#dolphinscheduler-normal-mode): Separate server master, worker, api, which can cover more test environments than standalone, and it is more like production environment in real life.

## DolphinScheduler Standalone Quick Start

> **_Note:_** Use standalone server only for development and debugging, because it uses H2 Database as default database and Zookeeper Testing Server which may not be stable in production.
>
> Standalone is only supported in DolphinScheduler 1.3.9 and later versions.
>
> Standalone server is able to connect to external databases like mysql and postgresql, see [Standalone Deployment](https://dolphinscheduler.apache.org/en-us/docs/3.1.2/guide/installation/standalone) for instructions.

### Git Branch Choose

Use different Git branch to develop different codes

- If you want to develop based on a binary package, switch git branch to specific release branch, for example, if you want to develop base on 1.3.9, you should choose branch `1.3.9-release`.
- If you want to develop the latest code, choose branch branch `dev`.

### Start backend server

Find the class `org.apache.dolphinscheduler.StandaloneServer` in Intellij IDEA and clikc run main function to startup.

### Start frontend server

Install frontend dependencies and run it.

> Note: You can see more detail about the frontend setting in [frontend development](./frontend-development.md).

```shell
cd dolphinscheduler-ui
pnpm install
pnpm run dev
```

The browser access address [http://localhost:5173](http://localhost:5173) can login DolphinScheduler UI. The default username and password are **admin/dolphinscheduler123**

## DolphinScheduler Normal Mode

### Prepare

#### zookeeper

Download [ZooKeeper](https://zookeeper.apache.org/releases.html), and extract it.

- Create directory `zkData` and `zkLog`
- Go to the zookeeper installation directory, copy configure file `zoo_sample.cfg` to `conf/zoo.cfg`, and change value of dataDir in conf/zoo.cfg to dataDir=./tmp/zookeeper

  ```shell
  # We use path /data/zookeeper/data and /data/zookeeper/datalog here as example
  dataDir=/data/zookeeper/data
  dataLogDir=/data/zookeeper/datalog
  ```
- Run `./bin/zkServer.sh` in terminal by command `./bin/zkServer.sh start`.

#### Database

The DolphinScheduler's metadata is stored in relational database. Currently supported MySQL and Postgresql. We use MySQL as an example. Start the database and create a new database named dolphinscheduler as DolphinScheduler metabase

After creating the new database, run the sql file under `dolphinscheduler/dolphinscheduler-dao/src/main/resources/sql/dolphinscheduler_mysql.sql` directly in MySQL to complete the database initialization

#### Start Backend Server

Following steps will guide how to start the DolphinScheduler backend service

##### Backend Start Prepare

- Open project: Use IDE open the project, here we use Intellij IDEA as an example, after opening it will take a while for Intellij IDEA to complete the dependent download

- File change

  - If you use MySQL as your metadata database, you need to modify `dolphinscheduler/pom.xml` and change the `scope` of the `mysql-connector-java` dependency to `compile`. This step is not necessary to use PostgreSQL
  - Modify database configuration, modify the database configuration in the `dolphinscheduler-master/src/main/resources/application.yaml`
  - Modify database configuration, modify the database configuration in the `dolphinscheduler-worker/src/main/resources/application.yaml`
  - Modify database configuration, modify the database configuration in the `dolphinscheduler-api/src/main/resources/application.yaml`

We here use MySQL with database, username, password named dolphinscheduler as an example

```application.yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/dolphinscheduler?useUnicode=true&characterEncoding=UTF-8
    username: dolphinscheduler
    password: dolphinscheduler
```

- Log level: add a line `<appender-ref ref="STDOUT"/>` to the following configuration to enable the log to be displayed on the command line

  `dolphinscheduler-master/src/main/resources/logback-spring.xml`
  `dolphinscheduler-worker/src/main/resources/logback-spring.xml`
  `dolphinscheduler-api/src/main/resources/logback-spring.xml`

  here we add the result after modify as below:

  ```diff
  <root level="INFO">
  +  <appender-ref ref="STDOUT"/>
    <appender-ref ref="APILOGFILE"/>
  </root>
  ```

> **_Note:_** Only DolphinScheduler 2.0 and later versions need to inatall plugin before start server. It not need before version 2.0.

##### Server start

There are three services that need to be started, including MasterServer, WorkerServer, ApiApplicationServer.

- MasterServer：Execute function `main` in the class `org.apache.dolphinscheduler.server.master.MasterServer` by Intellij IDEA, with the configuration _VM Options_ `-Dlogging.config=classpath:logback-spring.xml -Ddruid.mysql.usePingMethod=false -Dspring.profiles.active=mysql`
- WorkerServer：Execute function `main` in the class `org.apache.dolphinscheduler.server.worker.WorkerServer` by Intellij IDEA, with the configuration _VM Options_ `-Dlogging.config=classpath:logback-spring.xml -Ddruid.mysql.usePingMethod=false -Dspring.profiles.active=mysql`
- ApiApplicationServer：Execute function `main` in the class `org.apache.dolphinscheduler.api.ApiApplicationServer` by Intellij IDEA, with the configuration _VM Options_ `-Dlogging.config=classpath:logback-spring.xml -Dspring.profiles.active=api,mysql`. After it started, you could find Open API documentation in http://localhost:12345/dolphinscheduler/swagger-ui/index.html

> The `mysql` in the VM Options `-Dspring.profiles.active=mysql` means specified configuration file

### Start Frontend Server

Install frontend dependencies and run it

```shell
cd dolphinscheduler-ui
pnpm install
pnpm run dev
```

The browser access address [http://localhost:5173](http://localhost:5173) can login DolphinScheduler UI. The default username and password are **admin/dolphinscheduler123**
