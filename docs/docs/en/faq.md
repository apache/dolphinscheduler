<!-- markdown-link-check-disable -->
## Q: What's the name of this project?

A: DolphinScheduler

---

## Q: DolphinScheduler service introduction and recommended running memory

A: DolphinScheduler consists of 5 services, MasterServer, WorkerServer, ApiServer, AlertServer, LoggerServer and UI.

| Service                   | Description                                                  |
| ------------------------- | ------------------------------------------------------------ |
| MasterServer              | Mainly responsible for DAG segmentation and task status monitoring |
| WorkerServer/LoggerServer | Mainly responsible for the submission, execution and update of task status. LoggerServer is used for Rest Api to view logs through RPC |
| ApiServer                 | Provides the Rest Api service for the UI to call             |
| AlertServer               | Provide alarm service                                        |
| UI                        | Front page display                                           |

Note：**Due to the large number of services, it is recommended that the single-machine deployment is preferably 4 cores and 16G or more.**

---

## Q: Which mailboxes does the system support?

A: Support most mailboxes, qq, 163, 126, 139, outlook, aliyun, etc. are supported. Support TLS and SSL protocols, optionally configured in alert.properties

---

## Q: What are the common system variable time parameters and how do I use them?

A: Please refer to 'System parameter' in the system-manual 

---

## Q: pip install kazoo This installation gives an error. Is it necessary to install?

A: This is the python connection Zookeeper needs to use, it is used to delete the master/worker temporary node info in the Zookeeper. so you can ignore error if it's your first install. after version 1.3.0, kazoo is not been needed, we use program to replace what kazoo done 

---

## Q: How to specify the machine running task

A: version 1.2 and berfore, Use **the administrator** to create a Worker group, **specify the Worker group** when the **process definition starts**, or **specify the Worker group on the task node**. If not specified, use Default, **Default is to select one of all the workers in the cluster to use for task submission and execution.**
version 1.3, you can set worker group for the worker

---

## Q: Priority of the task

A: We also support **the priority of processes and tasks**. Priority We have five levels of **HIGHEST, HIGH, MEDIUM, LOW and LOWEST**. **You can set the priority between different process instances, or you can set the priority of different task instances in the same process instance.** For details, please refer to the task priority design in the architecture-design.

---

## Q: dolphinscheduler-grpc gives an error

A: Execute in the root directory: mvn -U clean package assembly:assembly -Dmaven.test.skip=true , then refresh the entire project.
version 1.3 not use grpc, we use netty directly

---

## Q: Does DolphinScheduler support running on windows?

A: In theory, **only the Worker needs to run on Linux**. Other services can run normally on Windows. But it is still recommended to deploy on Linux.

---

## Q: UI compiles node-sass prompt in linux: Error: EACCESS: permission denied, mkdir xxxx

A: Install **npm install node-sass --unsafe-perm** separately, then **npm install**

---

## Q: UI cannot log in normally.

A:   1, if it is node startup, check whether the .env API_BASE configuration under dolphinscheduler-ui is the Api Server service address.

​       2, If it is nginx booted and installed via **install-dolphinscheduler-ui.sh**, check if the proxy_pass      			configuration in **/etc/nginx/conf.d/dolphinscheduler.conf** is the Api Server service address

​       3, if the above configuration is correct, then please check if the Api Server service is normal, 

​		   curl http://localhost:12345/dolphinscheduler/users/get-user-info, check the Api Server log,

​          if  Prompt cn.dolphinscheduler.api.interceptor.LoginHandlerInterceptor:[76] - session info is null,   		  which proves that the Api Server service is normal.

​	4, if there is no problem above, you need to check if **server.context-path and server.port configuration** in **application.properties** is correct

---

## Q: After the process definition is manually started or scheduled, no process instance is generated.

A:   1, first **check whether the MasterServer service exists through jps**, or directly check whether there is a master service in zk from the service monitoring.

​       2,If there is a master service, check **the command status statistics** or whether new records are added in **t_ds_error_command**. If it is added, **please check the message field.**

---

## Q : The task status is always in the successful submission status.

A:   1, **first check whether the WorkerServer service exists through jps**, or directly check whether there is a worker service in zk from the service monitoring.

​       2,If the **WorkerServer** service is normal, you need to **check whether the MasterServer puts the task task in the zk queue. You need to check whether the task is blocked in the MasterServer log and the zk queue.**

​       3, if there is no problem above, you need to locate whether the Worker group is specified, but **the machine grouped by the worker is not online**.

---

## Q: Is there a Docker image and a Dockerfile?

A: Provide Docker image and Dockerfile.

Docker image address: https://hub.docker.com/r/escheduler/escheduler_images

Dockerfile address: https://github.com/qiaozhanwei/escheduler_dockerfile/tree/master/docker_escheduler

---

## Q : Need to pay attention to the problem in install.sh

A:   1, if the replacement variable contains special characters, **use the \ transfer character to transfer**

​       2, installPath="/data1_1T/dolphinscheduler", **this directory can not be the same as the install.sh directory currently installed with one click.**

​       3, deployUser = "dolphinscheduler", **the deployment user must have sudo privileges**, because the worker is executed by sudo -u tenant sh xxx.command

​       4, monitorServerState = "false", whether the service monitoring script is started, the default is not to start the service monitoring script. **If the service monitoring script is started, the master and worker services are monitored every 5 minutes, and if the machine is down, it will automatically restart.**

​       5, hdfsStartupSate="false", whether to enable HDFS resource upload function. The default is not enabled. **If it is not enabled, the resource center cannot be used.** If enabled, you need to configure the configuration of fs.defaultFS and yarn in conf/common/hadoop/hadoop.properties. If you use namenode HA, you need to copy core-site.xml and hdfs-site.xml to the conf root directory.

​    Note: **The 1.0.x version does not automatically create the hdfs root directory, you need to create it yourself, and you need to deploy the user with hdfs operation permission.**

---

## Q : Process definition and process instance offline exception

A : For **versions prior to 1.0.4**, modify the code under the escheduler-api cn.escheduler.api.quartz package.

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

## Q: Can the tenant created before the HDFS startup use the resource center normally?

A: No. Because the tenant created by HDFS is not started, the tenant directory will not be registered in HDFS. So the last resource will report an error.

---

## Q: In the multi-master and multi-worker state, the service is lost, how to be fault-tolerant

A: **Note:** **Master monitors Master and Worker services.**

​    1，If the Master service is lost, other Masters will take over the process of the hanged Master and continue to monitor the Worker task status.

​    2，If the Worker service is lost, the Master will monitor that the Worker service is gone. If there is a Yarn task, the Kill Yarn task will be retried.

Please see the fault-tolerant design in the architecture for details.

---

## Q : Fault tolerance for a machine distributed by Master and Worker

A: The 1.0.3 version only implements the fault tolerance of the Master startup process, and does not take the Worker Fault Tolerance. That is to say, if the Worker hangs, no Master exists. There will be problems with this process. We will add Master and Worker startup fault tolerance in version **1.1.0** to fix this problem. If you want to manually modify this problem, you need to **modify the running task for the running worker task that is running the process across the restart and has been dropped. The running process is set to the failed state across the restart**. Then resume the process from the failed node.

---

## Q : Timing is easy to set to execute every second

A : Note when setting the timing. If the first digit (* * * * * ? *) is set to *, it means execution every second. **We will add a list of recently scheduled times in version 1.1.0.** You can see the last 5 running times online at http://cron.qqe2.com/

---

## Q: Is there a valid time range for timing?

A: Yes, **if the timing start and end time is the same time, then this timing will be invalid timing. If the end time of the start and end time is smaller than the current time, it is very likely that the timing will be automatically deleted.**

---

## Q : There are several implementations of task dependencies

A:	1, the task dependency between **DAG**, is **from the zero degree** of the DAG segmentation

​		2, there are **task dependent nodes**, you can achieve cross-process tasks or process dependencies, please refer to the (DEPENDENT) node design in the system-manual. 

​	Note: **Cross-project processes or task dependencies are not supported**

---

## Q: There are several ways to start the process definition.

A:   1, in **the process definition list**, click the **Start** button.

​       2, **the process definition list adds a timer**, scheduling start process definition.

​       3, process definition **view or edit** the DAG page, any **task node right click** Start process definition.

​       4, you can define DAG editing for the process, set the running flag of some tasks to **prohibit running**, when the process definition is started, the connection of the node will be removed from the DAG.

---

## Q : Python task setting Python version

A:	1，**for the version after 1.0.3** only need to modify PYTHON_HOME in `bin/env/dolphinscheduler_env.sh`

```
export PYTHON_HOME=/bin/python
```

Note: This is **PYTHON_HOME** , which is the absolute path of the python command, not the simple PYTHON_HOME. Also note that when exporting the PATH, you need to directly

```
export PATH=$HADOOP_HOME/bin:$SPARK_HOME1/bin:$SPARK_HOME2/bin:$PYTHON_HOME:$JAVA_HOME/bin:$HIVE_HOME/bin:$PATH
```

​		2，For versions prior to 1.0.3, the Python task only supports the Python version of the system. It does not support specifying the Python version.

---

## Q：Worker Task will generate a child process through sudo -u tenant sh xxx.command, will kill when kill

A：  We will add the kill task in 1.0.4 and kill all the various child processes generated by the task.

---

## Q ： How to use the queue in DolphinScheduler, what does the user queue and tenant queue mean?

A ： The queue in the DolphinScheduler can be configured on the user or the tenant. **The priority of the queue specified by the user is higher than the priority of the tenant queue.** For example, to specify a queue for an MR task, the queue is specified by mapreduce.job.queuename.

Note: When using the above method to specify the queue, the MR uses the following methods:

```
	      Configuration conf = new Configuration();
        GenericOptionsParser optionParser = new GenericOptionsParser(conf, args);
        String[] remainingArgs = optionParser.getRemainingArgs();
```



If it is a Spark task --queue mode specifies the queue

---

## Q : Master or Worker reports the following alarm

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs/images/master_worker_lack_res.png" width="60%" />
 </p>



A ： Change the value of master.properties **master.reserved.memory** under conf to a smaller value, say 0.1 or the value of worker.properties **worker.reserved.memory** is a smaller value, say 0.1

---

## Q: The hive version is 1.1.0+cdh5.15.0, and the SQL hive task connection is reported incorrectly.

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs/images/cdh_hive_error.png" width="60%" />
 </p>


A ： Will hive pom

```
<dependency>
    <groupId>org.apache.hive</groupId>
    <artifactId>hive-jdbc</artifactId>
    <version>2.1.0</version>
</dependency>
```

change into

```
<dependency>
    <groupId>org.apache.hive</groupId>
    <artifactId>hive-jdbc</artifactId>
    <version>1.1.0</version>
</dependency>
```

---

## Q : how to add a worker server
A: 1, Create deployment user and hosts mapping, please refer 1.3 part of [cluster deployment](https://dolphinscheduler.apache.org/en-us/docs/laster/user_doc/installation/cluster.html)

​		2, Configure hosts mapping and ssh access and modify directory permissions. please refer 1.4 part of [cluster deployment](https://dolphinscheduler.apache.org/en-us/docs/laster/user_doc/installation/cluster.html)

​		3, Copy the deployment directory from worker server that has already deployed

​		4, Go to bin dir, then start worker server

        ```
        ./dolphinscheduler-daemon.sh start worker-server
        ```

---

## Q : When DolphinScheduler release a new version, and the change between current version and latest, and how to upgrade, and version number specification
A: 1, The release process of Apache Project happens in the mailing list. You can subscribe DolphinScheduler's mailing list and then when the release is in process, you'll receive release emails. Please follow this [introduction](https://github.com/apache/dolphinscheduler#get-help) to subscribe DolphinScheduler's mailing list.
    
2, When new version published, there would be release note which describe the change log, and there also have upgrade document for the previous version to new's.

3, Version number is x.y.z, when x is increased, it represents the version of the new architecture. When y is increased, it means that it is incompatible with the y version before it needs to be upgraded by script or other manual processing. When the z increase represents a bug fix, the upgrade is fully compatible. No additional processing is required. Remaining problem, the 1.0.2 upgrade is not compatible with 1.0.1 and requires an upgrade script. 

---

## Q : Subsequent tasks can execute even front task failed
A: When start the workflow, you can set the task failure strategy: continue or failure.
![set task failure strategy](https://user-images.githubusercontent.com/15833811/80368215-ee378080-88be-11ea-9074-01a33d012b23.png)

---

## Q : Workflow template DAG, workflow instance, work task and what is the relationship among them? A DAG supports a maximum concurrency of 100, does it mean that 100 workflow instances are generated and run concurrently? A task node in a DAG also has a concurrent number configuration. Does it mean that tasks can run concurrently with multiple threads? Is the maximum number 100?
A: 

1.2.1 version
```
master.properties
Control the max parallel number of master node workflows
master.exec.threads=100

Control the max number of parallel tasks in each workflow
master.exec.task.number=20

worker.properties
Control the max parallel number of worker node tasks
worker.exec.threads=100
```

---

## Q : Worker group manage page no buttons displayed
<p align="center">
   <img src="https://user-images.githubusercontent.com/39816903/81903776-d8cb9180-95f4-11ea-98cb-94ca1e6a1db5.png" width="60%" />
</p>
A: For version 1.3.0, we want to support k8s, while the ip always will be changed, so can't config on the UI, worker can config group name in the worker.properties.

---

## Q : Why not add mysql jdbc connector to docker image
A: The license of mysql jdbc connector is not compatible with apache v2 license, so it can't be included by docker image.

---

## Q : Allways fail when a task instance submit multiple yarn application
<p align="center">
   <img src="https://user-images.githubusercontent.com/16174111/81312485-476e9380-90b9-11ea-9aad-ed009db899b1.png" width="60%" />
 </p>
A： This bug have fix in dev and in Requirement/TODO list.

---

## Q : Master server and worker server stop abnormally after run for a few days
<p align="center">
   <img src="https://user-images.githubusercontent.com/18378986/81293969-c3101680-90a0-11ea-87e5-ac9f0dd53f5e.png" width="60%" />
 </p>
A: Session timeout is too short, only 0.3 seconds. Change the config item in zookeeper.properties:

```
   zookeeper.session.timeout=60000
   zookeeper.connection.timeout=30000
```

---

## Q : Started using the docker-compose default configuration and display zookeeper errors
<p align="center">
   <img src="https://user-images.githubusercontent.com/42579056/80374318-13c98780-88c9-11ea-8d5f-53448b957f02.png" width="60%" />
 </p>
A: This problem is solved in dev-1.3.0. This [pr](https://github.com/apache/dolphinscheduler/pull/2595) has solved this bug, brief change log:

```
    1. add zookeeper environment variable ZOO_4LW_COMMANDS_WHITELIST in docker-compose.yml file.
    2. change the data type of minLatency, avgLatency and maxLatency from int to float.
```

---

## Q : Interface show some task would be running all the time when db delayed and log show task instance is null
<p align="center">
   <img src="https://user-images.githubusercontent.com/51871547/80302626-b1478d00-87dd-11ea-97d4-08aa2244a6d0.jpg" width="60%" />
 </p>
<p align="center">
   <img src="https://user-images.githubusercontent.com/51871547/80302626-b1478d00-87dd-11ea-97d4-08aa2244a6d0.jpg" width="60%" />
 </p>

A: This [bug](https://github.com/apache/dolphinscheduler/issues/1477) describe the problem detail and it has been been solved in version 1.2.1.

For version under 1.2.1, some tips for this situation:

```
1. clear the task queue in zk for path: /dolphinscheduler/task_queue
2. change the state of the task to failed( integer value: 6).
3. run the work flow by recover from failed
```

---

## Q : Zookeeper masters znode list ip address is 127.0.0.1, instead of wanted ip eth0 or eth1, and may can't see task log
A: bug fix:
   ```
      1, confirm hostname
      $hostname
      hadoop1
      2, hostname -i
      127.0.0.1 10.3.57.15
      3, edit /etc/hosts,delete hadoop1 from 127.0.0.1 record
      $cat /etc/hosts
      127.0.0.1 localhost
      10.3.57.15 ds1 hadoop1
      4, hostname -i
      10.3.57.15
   ```

   Hostname cmd return server hostname, hostname -i return all matched ips configured in /etc/hosts. So after I delete the hostname matched with 127.0.0.1, and only remain internal ip resolution, instead of remove all the 127.0.0.1 resolution record. As long as hostname cmd return the correct internal ip configured in /etc/hosts can fix this bug. DolphinScheduler use the first record returned by hostname -i command. In my opion, DS should not use hostname -i to get the ip , as in many companies the devops configured the server name, we suggest use ip configured in configuration file or znode instead of /etc/hosts.

---

## Q : The scheduling system set a second frequency task, causing the system to crash
A: The scheduling system not support second frequency task.

---

## Q : Compile front-end code(dolphinscheduler-ui) show error cannot download "https://github.com/sass/node-sass/releases/download/v4.13.1/darwin-x64-72_binding.node"
A: 1, cd dolphinscheduler-ui and delete node_modules directory 
```
sudo rm -rf node_modules
```
   ​	2, install node-sass through npm.taobao.org
 ```
 sudo npm uninstall node-sass
 sudo npm i node-sass --sass_binary_site=https://npm.taobao.org/mirrors/node-sass/
 ```
   3, if the 2nd step failure, please, [referer url](https://dolphinscheduler.apache.org/en-us/development/frontend-development.html)
```
 sudo npm rebuild node-sass
```
When solved this problem, if you don't want to download this node every time, you can set system environment variable: SASS_BINARY_PATH= /xxx/xxx/xxx/xxx.node.

---

## Q : How to config when use mysql as database instead of postgres
A: 1, Edit project root dir maven config file, remove scope test property so that mysql driver can be loaded.
```
<dependency>
	<groupId>mysql</groupId>
	<artifactId>mysql-connector-java</artifactId>
	<version>${mysql.connector.version}</version>
	<scope>test<scope>
</dependency>
```
   ​	2, Edit application-dao.properties and quzrtz.properties config file to use mysql driver.
   Default is postgresSql driver because of license problem.

---

## Q : How does a shell task run
A: 1, Where is the executed server? Specify one worker to run the task, you can create worker group in Security Center, then the task can be send to the particular worker. If a worker group have multiple servers, which server actually execute is determined by scheduling and has randomness.

   ​	2, If it is a shell file of a path on the server, how to point to the path? The server shell file, involving permissions issues, it is not recommended to do so. It is recommended that you use the storage function of the resource center, and then use the resource reference in the shell editor. The system will help you download the script to the execution directory. If the task dependent on resource center files, worker use "hdfs dfs -get" to get the resource files in HDFS, then run the task in /tmp/escheduler/exec/process, this path can be customized when installtion dolphinscheduler.

   3, Which user execute the task? Task is run by the tenant through "sudo -u ${tenant}", tenant is a linux user.

---

## Q : What’s the best deploy mode you suggest in production env
A: 1, I suggest you use 3 nodes for stability if you don't have too many tasks to run. And deploy Master/Worker server on different nodes is better. If you only have one node, you of course only can deploy them together! By the way, how many machines you need is determined by your business. The DolphinScheduler system itself does not use too many resources. Test more, and you'll find the right way to use a few machines. 

---

## Q : DEPENDENT Task Node
A: 1, DEPENDENT task node actually does not have script, it used for config data cycle dependent logic, and then add task node after that to realize task cycle dependent.

---

## Q : How to change the boot port of the master
<p align="center">
   <img src="https://user-images.githubusercontent.com/8263441/62352160-0f3e9100-b53a-11e9-95ba-3ae3dde49c72.png" width="60%" />
 </p>
A: 1, modify application_master.properties, for example: server.port=12345.

---

## Q : Scheduled tasks cannot be online
A: 1, We can successly create scheduled task and add one record into t_scheduler_schedules table, but when I click online, front page no reaction and will lock table t_scheduler_schedules, and tested set field release_state value to 1 in table t_scheduler_schedules, and task display online state. For DS version above 1.2 table name is t_ds_schedules, other version table name is t_scheduler_schedules.

---

## Q : What is the address of swagger ui
A: 1, For version 1.2+ is http://apiServerIp:apiServerPort/dolphinscheduler/doc.html others is http://apiServerIp:apiServerPort/escheduler/doc.html.

---

## Q : Front-end installation package is missing files
<p align="center">
   <img src="https://user-images.githubusercontent.com/41460919/61437083-d960b080-a96e-11e9-87f1-297ba3aca5e3.png" width="60%" />
 </p>
 <p align="center">
    <img src="https://user-images.githubusercontent.com/41460919/61437218-1b89f200-a96f-11e9-8e48-3fac47eb2389.png" width="60%" />
  </p>

A: 1, User changed the config api server config file and item
 ![apiServerContextPath](https://user-images.githubusercontent.com/41460919/61678323-1b09a680-ad35-11e9-9707-3ba68bbc70d6.png), thus lead to the problem. After resume to the default value and problem solved.

---

## Q : Upload a relatively large file blocked
<p align="center">
   <img src="https://user-images.githubusercontent.com/21357069/58231400-805b0e80-7d69-11e9-8107-7f37b06a95df.png" width="60%" />
 </p>
A: 1, Edit ngnix config file, edit upload max size client_max_body_size 1024m.
     
   ​	2, the version of Google Chrome is old, and the latest version of the browser has been updated.

---

## Q : Create a spark data source, click "Test Connection", the system will fall back to the login page
A: 1, edit nginx config file /etc/nginx/conf.d/escheduler.conf
```
     proxy_connect_timeout 300s;
     proxy_read_timeout 300s;
     proxy_send_timeout 300s;
```

---

## Q : Workflow Dependency
A: 1, It is currently judged according to natural days, at the end of last month: the judgment time is the workflow A start_time/scheduler_time between '2019-05-31 00:00:00' and '2019-05-31 23:59:59'. Last month: It is judged that there is an A instance completed every day from the 1st to the end of the month. Last week: There are completed A instances 7 days last week. The first two days: Judging yesterday and the day before yesterday, there must be a completed A instance for two days.

---

## Q : DS Backend Inteface Document
A: 1, http://106.75.43.194:8888/dolphinscheduler/doc.html?language=en.

## During the operation of dolphinscheduler, the ip address is obtained incorrectly

When the master service and worker service are registered with zookeeper, relevant information will be created in the form of ip:port

If the ip address is obtained incorrectly, please check the network information. For example, in the Linux system, use the `ifconfig` command to view the network information. The following figure is an example:

<p align="center">
  <img src="/img/network/network_config.png" width="60%" />
</p>

You can use the three strategies provided by dolphinscheduler to get the available ip:

* default: First using internal network card to obtain the IP address, and then using external network card. If all above fail, use the address of the first available network card
* inner: Use the internal network card to obtain the ip address, if fails thrown an exception.
* outer: Use the external network card to obtain the ip address, if fails thrown an exception.

Modify the configuration in `common.properties`:

```shell
# network IP gets priority, default: inner outer
# dolphin.scheduler.network.priority.strategy=default
```

After configuration is modified, restart the service to activation

If the ip address is still wrong, please download [dolphinscheduler-netutils.jar](/asset/dolphinscheduler-netutils.jar) to the machine, execute the following commands and feedback the output to the community developers:

```shell
java -jar target/dolphinscheduler-netutils.jar
```

## Configure sudo to be secret free, which is used to solve the problem of using the default configuration sudo authority to be too large or unable to apply for root authority

Configure the sudo permission of the dolphinscheduler account to be an ordinary user manager within the scope of some ordinary users, and restrict specified users to run certain commands on the specified host. For detailed configuration, please see sudo rights management
For example, sudo permission management configuration dolphinscheduler OS account can only operate the permissions of users userA, userB, userC (users userA, userB, and userC are used for multi-tenant submitting jobs to the big data cluster)

```shell
echo 'dolphinscheduler  ALL=(userA,userB,userC)  NOPASSWD: NOPASSWD: ALL' >> /etc/sudoers
sed -i 's/Defaults    requirett/#Defaults    requirett/g' /etc/sudoers
```

---

## Q：Deploy for multiple YARN clusters
A：By deploying different worker in different yarn clusters，the steps are as follows(eg: AWS EMR):

   1. Deploying the worker server on the master node of the EMR cluster
   
   2. Changing `yarn.application.status.address` to current emr's yarn url in the `conf/common.properties`
   
   3. Execute command `bin/dolphinscheduler-daemon.sh start worker-server` to start worker-server

---

## Q：Update process definition error: Duplicate key TaskDefinition

A：Before DS 2.0.4 (after 2.0.0-alpha), there may be a problem of duplicate keys TaskDefinition due to version switching, which may cause the update workflow to fail; you can refer to the following SQL to delete duplicate data, taking MySQL as an example: (Note: Before operating, be sure to back up the original data, the SQL from pr[#8408](https://github.com/apache/dolphinscheduler/pull/8408))

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

## Q：Upgrade from 2.0.1 to 2.0.5 using PostgreSQL database failed

A：The repair can be completed by executing the following SQL in the database:
```SQL
update t_ds_version set version='2.0.1';
```

## Can not find python-gateway-server in distribute package

After version 3.0.0-alpha, Python gateway server integrate into API server, and Python gateway service will start when you
start API server. If you want disabled when Python gateway service you could change API server configuration in path
`api-server/conf/application.yaml` and change attribute `python-gateway.enabled : false`.

## How to Build Custom Docker Image

DolphinScheduler will release new Docker images after it released, you could find them in DockerHub. You could create
custom Docker images base on those images if you want to change image like add some dependencies or upgrade package.

```Dockerfile
FROM apache/dolphinscheduler-standalone-server
RUN apt update ; \
    apt install -y <YOUR-CUSTOM-DEPENDENCE> ; \
```

If you want to modify DolphinScheduler source code, then build and distribute your own images, you can run below command
to build Docker images and install them locally, which you could find them by command `docker imaegs`.

```shell
./mvnw -B clean install \
  -Dmaven.test.skip \
  -Dmaven.javadoc.skip \
  -Dmaven.checkstyle.skip \
  -Dmaven.deploy.skip \
  -Ddocker.tag=latest \
  -Pdocker,release
```

If you want to modify DolphinScheduler source code, but also want to add customize dependencies of Docker image, you can
modify the definition of Dockerfile after modifying the source code. You can run the following command in root source code
directory to find all Dockerfile files.

```shell
find . -iname 'Dockerfile'
```

Then run the command above start with `./mvnw -B clean install`. You can see all docker images you just created with
command `docker images` after finish commnand `mvnw`.

---

## We will collect more FAQ later