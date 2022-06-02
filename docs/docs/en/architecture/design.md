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

    - **Distributed Quartz** distributed scheduling component, which is mainly responsible for the start and stop operations of schedule tasks. When Quartz starts the task, there will be a thread pool inside the Master responsible for the follow-up operation of the processing task.

    - **MasterSchedulerThread** is a scanning thread that regularly scans the **command** table in the database and runs different business operations according to different **command types**.

    - **MasterExecThread** is mainly responsible for DAG task segmentation, task submission monitoring, and logical processing to different command types.

    - **MasterTaskExecThread** is mainly responsible for the persistence to tasks.

* **WorkerServer** 

     WorkerServer also adopts a distributed and decentralized design concept. WorkerServer is mainly responsible for task execution and providing log services.

     When the WorkerServer service starts, register a temporary node with ZooKeeper and maintain a heartbeat.
     Server provides monitoring services based on netty.
  
     #### The Service Mainly Includes:
  
     - **Fetch TaskThread** is mainly responsible for continuously getting tasks from the **Task Queue**, and calling **TaskScheduleThread** corresponding executor according to different task types.

* **ZooKeeper** 

    ZooKeeper service, MasterServer and WorkerServer nodes in the system all use ZooKeeper for cluster management and fault tolerance. In addition, the system implements event monitoring and distributed locks based on ZooKeeper.

    We have also implemented queues based on Redis, but we hope DolphinScheduler depends on as few components as possible, so we finally removed the Redis implementation.

* **Task Queue** 

    Provide task queue operation, the current queue is also implement base on ZooKeeper. Due to little information stored in the queue, there is no need to worry about excessive data in the queue. In fact, we have tested the millions of data storage in queues, which has no impact on system stability and performance.

* **Alert** 

    Provide alarm related interface, the interface mainly includes **alarm** two types of alarm data storage, query and notification functions. Among them, there are **email notification** and **SNMP (not yet implemented)**.

* **API** 

    The API interface layer is mainly responsible for processing requests from the front-end UI layer. The service uniformly provides RESTful APIs to provide request services to external.
    Interfaces include workflow creation, definition, query, modification, release, logoff, manual start, stop, pause, resume, start execution from specific node, etc.

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

#### Distributed Lock Practice

DolphinScheduler uses ZooKeeper distributed lock to implement only one Master executes Scheduler at the same time, or only one Worker executes the submission of tasks.
1. The following shows the core process algorithm for acquiring distributed locks:
 <p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/distributed_lock.png" alt="Obtain distributed lock process"  width="50%" />
 </p>

2. Flow diagram of implementation of Scheduler thread distributed lock in DolphinScheduler:
 <p align="center">
   <img src="../../../img/distributed_lock_procss.png" alt="Obtain distributed lock process"  width="50%" />
 </p>


#### Insufficient Thread Loop Waiting Problem

-  If there is no sub-process in a DAG, when the number of data in the Command is greater than the threshold set by the thread pool, the process directly waits or fails.
-  If a large DAG nests many sub-processes, there will produce a "dead" state as the following figure:

 <p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/lack_thread.png" alt="Insufficient threads waiting loop problem"  width="50%" />
 </p>
In the above figure, MainFlowThread waits for the end of SubFlowThread1, SubFlowThread1 waits for the end of SubFlowThread2, SubFlowThread2 waits for the end of SubFlowThread3, and SubFlowThread3 waits for a new thread in the thread pool, then the entire DAG process cannot finish, and the threads cannot be released. In this situation, the state of the child-parent process loop waiting is formed. At this moment, unless a new Master is started and add threads to break such a "stalemate", the scheduling cluster will no longer use.

It seems a bit unsatisfactory to start a new Master to break the deadlock, so we proposed the following three solutions to reduce this risk:

1. Calculate the sum of all Master threads, and then calculate the number of threads required for each DAG, that is, pre-calculate before the DAG process executes. Because it is a multi-master thread pool, it is unlikely to obtain the total number of threads in real time. 
2. Judge whether the single-master thread pool is full, let the thread fail directly when fulfilled.
3. Add a Command type with insufficient resources. If the thread pool is insufficient, suspend the main process. In this way, there are new threads in the thread pool, which can make the process suspended by insufficient resources wake up to execute again.

Note: The Master Scheduler thread executes by FIFO when acquiring the Command.

So we choose the third way to solve the problem of insufficient threads.


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

- One is a business node, which corresponds to an actual script or process command, such as shell node, MR node, Spark node, and dependent node.

- Another is a logical node, which does not operate actual script or process command, but only logical processing to the entire process flow, such as sub-process sections.

Each **business node** can configure the number of failed retries. When the task node fails, it will automatically retry until it succeeds or exceeds the retry times. **Logical node** failure retry is not supported, but the tasks in the logical node support.

If there is a task failure in the workflow that reaches the maximum retry times, the workflow will fail and stop, and the failed workflow can be manually re-run or process recovery operations.

#### Task Priority Design

In the early schedule design, if there is no priority design and use the fair scheduling, the task submitted first may complete at the same time with the task submitted later, thus invalid the priority of process or task. So we have re-designed this, and the following is our current design:

-  According to **the priority of different process instances** prior over **priority of the same process instance** prior over **priority of tasks within the same process** prior over **tasks within the same process**, process task submission order from highest to Lowest.
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

-  Since Web (UI) and Worker are not always on the same machine, to view the log cannot be like querying a local file. There are two options:
  -  Put logs on the ES search engine.
  -  Obtain remote log information through netty communication.

-  In consideration of the lightness of DolphinScheduler as much as possible, so choose gRPC to achieve remote access to log information.

 <p align="center">
   <img src="https://analysys.github.io/easyscheduler_docs_cn/images/grpc.png" alt="grpc remote access"  width="50%" />
 </p>

- We use the customized FileAppender and Filter functions from Logback to implement each task instance generates one log file.
- The following is the FileAppender implementation：

```java
 /**
  * task log appender
  */
 public class TaskLogAppender extends FileAppender<ILoggingEvent> {
 
     ...

    @Override
    protected void append(ILoggingEvent event) {

        if (currentlyActiveFile == null){
            currentlyActiveFile = getFile();
        }
        String activeFile = currentlyActiveFile;
        // thread name： taskThreadName-processDefineId_processInstanceId_taskInstanceId
        String threadName = event.getThreadName();
        String[] threadNameArr = threadName.split("-");
        // logId = processDefineId_processInstanceId_taskInstanceId
        String logId = threadNameArr[1];
        ...
        super.subAppend(event);
    }
}
```

Generate logs in the form of /process definition id /process instance id /task instance id.log

- Filter to match the thread name starting with TaskLogInfo:

- The following shows the TaskLogFilter implementation:

 ```java
 /**
 *  task log filter
 */
public class TaskLogFilter extends Filter<ILoggingEvent> {

    @Override
    public FilterReply decide(ILoggingEvent event) {
        if (event.getThreadName().startsWith("TaskLogInfo-")){
            return FilterReply.ACCEPT;
        }
        return FilterReply.DENY;
    }
}
```

## Sum Up

From the perspective of scheduling, this article preliminarily introduces the architecture principles and implementation ideas of the big data distributed workflow scheduling system: DolphinScheduler. To be continued.

