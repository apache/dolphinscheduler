# Backend Deployment Document

There are two deployment modes for the backend: 

- automatic deployment  
- source code compile and then deployment

## Preparations

Download the latest version of the installation package, download address： [gitee download](https://gitee.com/easyscheduler/EasyScheduler/attach_files/) or [github download](https://github.com/analysys/EasyScheduler/releases), download escheduler-backend-x.x.x.tar.gz(back-end referred to as escheduler-backend),escheduler-ui-x.x.x.tar.gz(front-end referred to as escheduler-ui)



#### Preparations 1: Installation of basic software (self-installation of required items)

 * [Mysql](http://geek.analysys.cn/topic/124) (5.5+) :  Mandatory
 * [JDK](https://www.oracle.com/technetwork/java/javase/downloads/index.html) (1.8+) :  Mandatory
 * [ZooKeeper](https://www.jianshu.com/p/de90172ea680)(3.4.6+) ：Mandatory
 * [Hadoop](https://blog.csdn.net/Evankaka/article/details/51612437)(2.6+) ：Optionally, if you need to use the resource upload function, MapReduce task submission needs to configure Hadoop (uploaded resource files are currently stored on Hdfs)
 * [Hive](https://staroon.pro/2017/12/09/HiveInstall/)(1.2.1) :   Optional, hive task submission needs to be installed
 * Spark(1.x,2.x) :  Optional, Spark task submission needs to be installed
 * PostgreSQL(8.2.15+) : Optional, PostgreSQL PostgreSQL stored procedures need to be installed

```
 Note: Easy Scheduler itself does not rely on Hadoop, Hive, Spark, PostgreSQL, but only calls their Client to run the corresponding tasks.
```

#### Preparations 2: Create deployment users

- Deployment users are created on all machines that require deployment scheduling, because the worker service executes jobs in `sudo-u {linux-user}`, so deployment users need sudo privileges and are confidential.

```
vi /etc/sudoers

# For example, the deployment user is an escheduler account
escheduler  ALL=(ALL)       NOPASSWD: NOPASSWD: ALL

# And you need to comment out the Default requiretty line
#Default requiretty
```

#### Preparations 3: SSH Secret-Free Configuration
Configure SSH secret-free login on deployment machines and other installation machines. If you want to install easyscheduler on deployment machines, you need to configure native password-free login itself.

- [Connect the host and other machines SSH](http://geek.analysys.cn/topic/113)

#### Preparations 4: database initialization

* Create databases and accounts

    Execute the following command to create database and account
    
    ```
    CREATE DATABASE escheduler DEFAULT CHARACTER SET utf8 DEFAULT COLLATE utf8_general_ci;
    GRANT ALL PRIVILEGES ON escheduler.* TO '{user}'@'%' IDENTIFIED BY '{password}';
    GRANT ALL PRIVILEGES ON escheduler.* TO '{user}'@'localhost' IDENTIFIED BY '{password}';
    flush privileges;
    ```

* creates tables and imports basic data
    Modify the following attributes in ./conf/dao/data_source.properties

    ```
        spring.datasource.url
        spring.datasource.username
        spring.datasource.password
    ```
    
    Execute scripts for creating tables and importing basic data
    
    ```
    sh ./script/create-escheduler.sh
    ```

#### Preparations 5: Modify the deployment directory permissions and operation parameters

     instruction of escheduler-backend directory 

```directory
bin : Basic service startup script
conf : Project Profile
lib : The project relies on jar packages, including individual module jars and third-party jars
script :  Cluster Start, Stop and Service Monitor Start and Stop scripts
sql : The project relies on SQL files
install.sh :  One-click deployment script
```

- Modify permissions (please modify the 'deployUser' to the corresponding deployment user) so that the deployment user has operational privileges on the escheduler-backend directory

    `sudo chown -R deployUser:deployUser escheduler-backend`

- Modify the `.escheduler_env.sh` environment variable in the conf/env/directory

- Modify deployment parameters (depending on your server and business situation):

 - Modify the parameters in **install.sh** to replace the values required by your business
   - MonitorServerState switch variable, added in version 1.0.3, controls whether to start the self-start script (monitor master, worker status, if off-line will start automatically). The default value of "false" means that the self-start script is not started, and if it needs to start, it is changed to "true".
   - 'hdfsStartupSate' switch variable controls whether to start hdfs
      The default value of "false" means not to start hdfs
      Change the variable to 'true' if you want to use hdfs, you also need to create the hdfs root path by yourself, that 'hdfsPath' in install.sh.

 - If you use hdfs-related functions, you need to copy**hdfs-site.xml** and **core-site.xml** to the conf directory


## Deployment
Automated deployment is recommended, and experienced partners can use source deployment as well.

### Automated Deployment

- Install zookeeper tools

   `pip install kazoo`

- Switch to deployment user, one-click deployment

    `sh install.sh` 

- Use the `jps` command to check if the services are started (`jps` comes from `Java JDK`)

```aidl
    MasterServer         ----- Master Service
    WorkerServer         ----- Worker Service
    LoggerServer         ----- Logger Service
    ApiApplicationServer ----- API Service
    AlertServer          ----- Alert Service
```

If all services are normal, the automatic deployment is successful


After successful deployment, the log can be viewed and stored in a specified folder.

```logPath
 logs/
    ├── escheduler-alert-server.log
    ├── escheduler-master-server.log
    |—— escheduler-worker-server.log
    |—— escheduler-api-server.log
    |—— escheduler-logger-server.log
```

### Compile source code to deploy

After downloading the release version of the source package, unzip it into the root directory

* Execute the compilation command：

```
 mvn -U clean package assembly:assembly -Dmaven.test.skip=true
```

* View directory

After normal compilation, ./target/escheduler-{version}/ is generated in the current directory


### Start-and-stop services commonly used in systems (for service purposes, please refer to System Architecture Design for details)

* stop all services in the cluster
  
   ` sh ./bin/stop-all.sh`
   
* start all services in the cluster
  
   ` sh ./bin/start-all.sh`

* start and stop one master server

```master
sh ./bin/escheduler-daemon.sh start master-server
sh ./bin/escheduler-daemon.sh stop master-server
```

* start and stop one worker server

```worker
sh ./bin/escheduler-daemon.sh start worker-server
sh ./bin/escheduler-daemon.sh stop worker-server
```

* start and stop api server

```Api
sh ./bin/escheduler-daemon.sh start api-server
sh ./bin/escheduler-daemon.sh stop api-server
```
* start and stop logger server

```Logger
sh ./bin/escheduler-daemon.sh start logger-server
sh ./bin/escheduler-daemon.sh stop logger-server
```
* start and stop alert server

```Alert
sh ./bin/escheduler-daemon.sh start alert-server
sh ./bin/escheduler-daemon.sh stop alert-server
```

## Database Upgrade
Database upgrade is a function added in version 1.0.2. The database can be upgraded automatically by executing the following command:

```upgrade
sh ./script/upgrade-escheduler.sh
```


