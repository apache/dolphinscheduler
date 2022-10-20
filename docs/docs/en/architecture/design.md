# System Architecture Design

## System Structure

### System Architecture Diagram

<p align="center">
  <img src="../../../img/architecture-1.3.0.jpg" alt="System architecture diagram"  width="70%" />
  <p align="center">
        <em>System architecture diagram</em>
  </p>
</p>

### Start Process Activity Diagram

<p align="center">
  <img src="../../../img/process-start-flow-1.3.0.png" alt="Start process activity diagram"  width="70%" />
  <p align="center">
        <em>Start process activity diagram</em>
  </p>
</p>

### Architecture Description

* **MasterServer**

  MasterServer adopts a distributed and decentralized design concept. MasterServer is mainly responsible for DAG task segmentation, task submission monitoring, and monitoring the health status of other MasterServer and WorkerServer at the same time.
  When the MasterServer service starts, register a temporary node with ZooKeeper, and perform fault tolerance by monitoring changes in the temporary node of ZooKeeper.
  MasterServer provides monitoring services based on netty.

  #### The Service Mainly Includes:

  - **DistributedQuartz** distributed scheduling component, which is mainly responsible for the start and stop operations of scheduled tasks. When quartz start the task, there will be a thread pool inside the Master responsible for the follow-up operation of the processing task;

  - **MasterSchedulerService** is a scanning thread that regularly scans the `t_ds_command` table in the database, runs different business operations according to different **command types**;

  - **WorkflowExecuteRunnable** is mainly responsible for DAG task segmentation, task submission monitoring, and logical processing of different event types;

  - **TaskExecuteRunnable** is mainly responsible for the processing and persistence of tasks, and generates task events and submits them to the event queue of the process instance;

  - **EventExecuteService** is mainly responsible for the polling of the event queue of the process instances;

  - **StateWheelExecuteThread** is mainly responsible for process instance and task timeout, task retry, task-dependent polling, and generates the corresponding process instance or task event and submits it to the event queue of the process instance;

  - **FailoverExecuteThread** is mainly responsible for the logic of Master fault tolerance and Worker fault tolerance;

* **WorkerServer**

  WorkerServer also adopts a distributed and decentralized design concept. WorkerServer is mainly responsible for task execution and providing log services.

  When the WorkerServer service starts, register a temporary node with ZooKeeper and maintain a heartbeat.
  WorkerServer provides monitoring services based on netty.

  #### The Service Mainly Includes:

  - **WorkerManagerThread** is mainly responsible for the submission of the task queue, continuously receives tasks from the task queue, and submits them to the thread pool for processing;

  - **TaskExecuteThread** is mainly responsible for the process of task execution, and the actual processing of tasks according to different task types;

  - **RetryReportTaskStatusThread** is mainly responsible for regularly polling to report the task status to the Master until the Master replies to the status ack to avoid the loss of the task status;

* **ZooKeeper**

  ZooKeeper service, MasterServer and WorkerServer nodes in the system all use ZooKeeper for cluster management and fault tolerance. In addition, the system implements event monitoring and distributed locks based on ZooKeeper.

  We have also implemented queues based on Redis, but we hope DolphinScheduler depends on as few components as possible, so we finally removed the Redis implementation.

* **AlertServer**

  Provides alarm services, and implements rich alarm methods through alarm plugins.

* **API**

  The API interface layer is mainly responsible for processing requests from the front-end UI layer. The service uniformly provides RESTful APIs to provide request services to external.

* **UI**

  The front-end page of the system provides various visual operation interfaces of the system, see more at [Introduction to Functions](../guide/homepage.md) section.

### Architecture Design Ideas

#### Decentralization VS Centralization

##### Centralized Thinking

The centralized design concept is relatively simple. The nodes in the distributed cluster are roughly divided into two roles according to responsibilities:

<p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/master_slave.png" alt="master-slave character"  width="50%" />
 </p>

- The role of the master is mainly responsible for task distribution and monitoring the health status of the slave, and can dynamically balance the task to the slave, so that the slave node won't be in a "busy dead" or "idle dead" state.
- The role of Worker is mainly responsible for task execution and heartbeat maintenance to the Master, so that Master can assign tasks to Slave.

Problems in centralized thought design:

- Once there is a problem with the Master, the team grow aimless without commander and the entire cluster collapse. In order to solve this problem, most of the Master and Slave architecture models adopt the design scheme of active and standby Master, which can be hot standby or cold standby, or automatic switching or manual switching. More and more new systems are beginning to have ability to automatically elect and switch Master to improve the availability of the system.
- Another problem is that if the Scheduler is on the Master, although it can support different tasks in a DAG running on different machines, it will cause the Master to be overloaded. If the Scheduler is on the slave, all tasks in a DAG can only submit jobs on a certain machine. When there are more parallel tasks, the pressure on the slave may be greater.

##### Decentralized

 <p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/decentralization.png" alt="Decentralization"  width="50%" />
 </p>

- In the decentralized design, there is usually no concept of Master or Slave. All roles are the same, the status is equal, the global Internet is a typical decentralized distributed system. Any node connected to the network goes down, will only affect a small range of functions.
- The core design of decentralized design is that there is no distinct "manager" different from other nodes in the entire distributed system, so there is no single point failure. However, because there is no "manager" node, each node needs to communicate with other nodes to obtain the necessary machine information, and the unreliability of distributed system communication greatly increases the difficulty to implement the above functions.
- In fact, truly decentralized distributed systems are rare. Instead, dynamic centralized distributed systems are constantly pouring out. Under this architecture, the managers in the cluster are dynamically selected, rather than preset, and when the cluster fails, the nodes of the cluster will automatically hold "meetings" to elect new "managers" To preside over the work. The most typical case is Etcd implemented by ZooKeeper and Go language.
- The decentralization of DolphinScheduler is that the Master and Worker register in ZooKeeper, for implement the centerless feature to Master cluster and Worker cluster. Use the ZooKeeper distributed lock to elect one of the Master or Worker as the "manager" to perform the task.

#### Fault-Tolerant Design

Fault tolerance divides into service downtime fault tolerance and task retry, and service downtime fault tolerance divides into master fault tolerance and worker fault tolerance.

##### Downtime Fault Tolerance

The service fault-tolerance design relies on ZooKeeper's Watcher mechanism, and the implementation principle shows in the figure:

 <p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/fault-tolerant.png" alt="DolphinScheduler fault-tolerant design"  width="40%" />
 </p>
Among them, the Master monitors the directories of other Masters and Workers. If the remove event is triggered, perform fault tolerance of the process instance or task instance according to the specific business logic.

- Master fault tolerance：

<p align="center">
   <img src="../../../img/failover-master.jpg" alt="failover-master"  width="50%" />
 </p>

Fault tolerance range: From the perspective of host, the fault tolerance range of Master includes: own host and node host that does not exist in the registry, and the entire process of fault tolerance will be locked;

Fault-tolerant content: Master's fault-tolerant content includes: fault-tolerant process instances and task instances. Before fault-tolerant, compares the start time of the instance with the server start-up time, and skips fault-tolerance if after the server start time;

Fault-tolerant post-processing: After the fault tolerance of ZooKeeper Master completed, then re-schedule by the Scheduler thread in DolphinScheduler, traverses the DAG to find the "running" and "submit successful" tasks. Monitor the status of its task instances for the "running" tasks, and for the "commits successful" tasks, it is necessary to find out whether the task queue already exists. If exists, monitor the status of the task instance. Otherwise, resubmit the task instance.

- Worker fault tolerance：

<p align="center">
   <img src="../../../img/failover-worker.jpg" alt="failover-worker"  width="50%" />
 </p>

Fault tolerance range: From the perspective of process instance, each Master is only responsible for fault tolerance of its own process instance; it will lock only when `handleDeadServer`;

Fault-tolerant content: When sending the remove event of the Worker node, the Master only fault-tolerant task instances. Before fault-tolerant, compares the start time of the instance with the server start-up time, and skips fault-tolerance if after the server start time;

Fault-tolerant post-processing: Once the Master Scheduler thread finds that the task instance is in the "fault-tolerant" state, it takes over the task and resubmits it.

Note: Due to "network jitter", the node may lose heartbeat with ZooKeeper in a short period of time, and the node's remove event may occur. For this situation, we use the simplest way, that is, once the node and ZooKeeper timeout connection occurs, then directly stop the Master or Worker service.

##### Task Failed and Try Again

Here we must first distinguish the concepts of task failure retry, process failure recovery, and process failure re-run:

- Task failure retry is at the task level and is automatically performed by the schedule system. For example, if a Shell task sets to retry for 3 times, it will try to run it again up to 3 times after the Shell task fails.
- Process failure recovery is at the process level and is performed manually. Recovery can only perform **from the failed node** or **from the current node**.
- Process failure re-run is also at the process level and is performed manually, re-run perform from the beginning node.

Next to the main point, we divide the task nodes in the workflow into two types.

- One is a business task, which corresponds to an actual script or process command, such as Shell task, SQL task, and Spark task.

- Another is a logical task, which does not operate actual script or process command, but only logical processing to the entire process flow, such as sub-process task, dependent task.

**Business node** can configure the number of failed retries. When the task node fails, it will automatically retry until it succeeds or exceeds the retry times. **Logical node** failure retry is not supported.

If there is a task failure in the workflow that reaches the maximum retry times, the workflow will fail and stop, and the failed workflow can be manually re-run or process recovery operations.

#### Task Priority Design

In the early schedule design, if there is no priority design and use the fair scheduling, the task submitted first may complete at the same time with the task submitted later, thus invalid the priority of process or task. So we have re-designed this, and the following is our current design:

- According to **the priority of different process instances** prior over **priority of the same process instance** prior over **priority of tasks within the same process** prior over **tasks within the same process**, process task submission order from highest to Lowest.
  - The specific implementation is to parse the priority according to the JSON of the task instance, and then save the **process instance priority_process instance id_task priority_task id** information to the ZooKeeper task queue. When obtain from the task queue, we can get the highest priority task by comparing string.
    - The priority of the process definition is to consider that some processes need to process before other processes. Configure the priority when the process starts or schedules. There are 5 levels in total, which are HIGHEST, HIGH, MEDIUM, LOW, and LOWEST. As shown below

      <p align="center">
         <img src="https://user-images.githubusercontent.com/10797147/146744784-eb351b14-c94a-4ed6-8ba4-5132c2a3d116.png" alt="Process priority configuration"  width="40%" />
       </p>

    - The priority of the task is also divides into 5 levels, ordered by HIGHEST, HIGH, MEDIUM, LOW, LOWEST. As shown below:

        <p align="center">
           <img src="https://user-images.githubusercontent.com/10797147/146744830-5eac611f-5933-4f53-a0c6-31613c283708.png" alt="Task priority configuration"  width="35%" />
         </p>

#### Logback and Netty Implement Log Access

- Since Web (UI) and Worker are not always on the same machine, to view the log cannot be like querying a local file. There are two options:
- Put logs on the ES search engine.
- Obtain remote log information through netty communication.
- In consideration of the lightness of DolphinScheduler as much as possible, so choose gRPC to achieve remote access to log information.

 <p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/grpc.png" alt="grpc remote access"  width="50%" />
 </p>

- For details, please refer to the logback configuration of Master and Worker, as shown in the following example:

```xml
<conversionRule conversionWord="messsage" converterClass="org.apache.dolphinscheduler.service.log.SensitiveDataConverter"/>
<appender name="TASKLOGFILE" class="ch.qos.logback.classic.sift.SiftingAppender">
    <filter class="org.apache.dolphinscheduler.service.log.TaskLogFilter"/>
    <Discriminator class="org.apache.dolphinscheduler.service.log.TaskLogDiscriminator">
        <key>taskAppId</key>
        <logBase>${log.base}</logBase>
    </Discriminator>
    <sift>
        <appender name="FILE-${taskAppId}" class="ch.qos.logback.core.FileAppender">
            <file>${log.base}/${taskAppId}.log</file>
            <encoder>
                <pattern>
                            [%level] %date{yyyy-MM-dd HH:mm:ss.SSS Z} [%thread] %logger{96}:[%line] - %messsage%n
                </pattern>
                <charset>UTF-8</charset>
            </encoder>
            <append>true</append>
        </appender>
    </sift>
</appender>
```

## Sum Up

From the perspective of scheduling, this article preliminarily introduces the architecture principles and implementation ideas of the big data distributed workflow scheduling system: DolphinScheduler. To be continued.

