/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.server.master.service;

import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.common.enums.Flag;
import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.dao.repository.TaskInstanceDao;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.TaskExecutionStatus;
import org.apache.dolphinscheduler.plugin.task.api.utils.LogUtils;
import org.apache.dolphinscheduler.registry.api.RegistryClient;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;
import org.apache.dolphinscheduler.server.master.builder.TaskExecutionContextBuilder;
import org.apache.dolphinscheduler.server.master.cache.ProcessInstanceExecCacheManager;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.event.TaskStateEvent;
import org.apache.dolphinscheduler.server.master.metrics.TaskMetrics;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteRunnable;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThreadPool;
import org.apache.dolphinscheduler.server.master.utils.TaskUtils;
import org.apache.dolphinscheduler.service.log.LogClient;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.utils.ProcessUtils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.client.api.YarnClient;
import org.apache.hadoop.yarn.api.records.ApplicationId;
import org.apache.hadoop.yarn.exceptions.YarnException;

import javax.annotation.Nullable;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WorkerFailoverService {

    private final RegistryClient registryClient;
    private final MasterConfig masterConfig;
    private final ProcessService processService;
    private final WorkflowExecuteThreadPool workflowExecuteThreadPool;
    private final ProcessInstanceExecCacheManager cacheManager;
    private final LogClient logClient;
    private final String localAddress;

    private final TaskInstanceDao taskInstanceDao;

    public WorkerFailoverService(@NonNull RegistryClient registryClient,
                                 @NonNull MasterConfig masterConfig,
                                 @NonNull ProcessService processService,
                                 @NonNull WorkflowExecuteThreadPool workflowExecuteThreadPool,
                                 @NonNull ProcessInstanceExecCacheManager cacheManager,
                                 @NonNull LogClient logClient,
                                 @NonNull TaskInstanceDao taskInstanceDao) {
        this.registryClient = registryClient;
        this.masterConfig = masterConfig;
        this.processService = processService;
        this.workflowExecuteThreadPool = workflowExecuteThreadPool;
        this.cacheManager = cacheManager;
        this.logClient = logClient;
        this.localAddress = masterConfig.getMasterAddress();
        this.taskInstanceDao = taskInstanceDao;
    }

    /**
     * Do the worker failover. Will find the SUBMITTED_SUCCESS/DISPATCH/RUNNING_EXECUTION/DELAY_EXECUTION/READY_PAUSE/READY_STOP tasks belong the given worker,
     * and failover these tasks.
     * <p>
     * Note: When we do worker failover, the master will only failover the processInstance belongs to the current master.
     *函数名failoverWorker，接受一个参数，workerHost，它是一个代表Worker主机的字符串。
     * 总的来说，该函数的目标是找到在指定worker上运行、并且需要失败重试的任务。对于这些任务，该函数将检查它们是否真的需要失败重试，如果需要，就进行失败重试操作。
     * @param workerHost worker host
     */
    public void failoverWorker(@NonNull String workerHost) {
        //日志开始记录当前开始失败重试的worker。
        log.info("Worker[{}] failover starting", workerHost);
        //创建一个StopWatch实例failoverTimeCost用以计算失败重试的时间。
        final StopWatch failoverTimeCost = StopWatch.createStarted();

        // we query the task instance from cache, so that we can directly update the cache
        //获取要进行故障转worker的启动时间。
        final Optional<Date> needFailoverWorkerStartTime =
                getServerStartupTime(registryClient.getServerList(RegistryNodeType.WORKER), workerHost);
        //获取需要执行失败重试的任务实例列表。
        final List<TaskInstance> needFailoverTaskInstanceList = getNeedFailoverTaskInstance(workerHost);
        //如果列表为空，即没有需要进行失败重试的任务实例，那么就结束失败重试，并记录日志。
        if (CollectionUtils.isEmpty(needFailoverTaskInstanceList)) {
            log.info("Worker[{}] failover finished there are no taskInstance need to failover", workerHost);
            return;
        }
        //如果有需要执行失败重试的任务实例，会对其执行深度检查。
        log.info(
                "Worker[{}] failover there are {} taskInstance may need to failover, will do a deep check, taskInstanceIds: {}",
                workerHost,
                needFailoverTaskInstanceList.size(),
                needFailoverTaskInstanceList.stream().map(TaskInstance::getId).collect(Collectors.toList()));
        //创建一个缓存映射，用于存储进程实例。
        final Map<Integer, ProcessInstance> processInstanceCacheMap = new HashMap<>();
        //对需要失败重试的任务实例进行遍历，检查每个任务实例是否需要进行失败重试。
        for (TaskInstance taskInstance : needFailoverTaskInstanceList) {
            try (
                    //在遍历过程中，为了方便记录日志，设置了LogUtils的MDC上下文，将工作流和任务实例ID设置到MDC中。
                    //根据任务实例的流程实例ID从缓存映射
                    final LogUtils.MDCAutoClosableContext mdcAutoClosableContext =
                            LogUtils.setWorkflowAndTaskInstanceIDMDC(taskInstance.getProcessInstanceId(),
                                    taskInstance.getId())) {
                try {
                    //根据任务实例的流程实例ID从缓存映射processInstanceCacheMap中获取对应的进程实例processInstance。
                //使用computeIfAbsent方法来从缓存映射processInstanceCacheMap中获取或计算processInstance对象。
                    //通过taskInstance.getProcessInstanceId()获取任务实例的流程实例ID。
                    //computeIfAbsent方法接收两个参数，第一个参数是要查找的键，第二个参数是一个lambda表达式，用于计算要存储的值。
                    //lambda表达式中，首先通过cacheManager.getByProcessInstanceId()方法根据流程实例ID获取到WorkflowExecuteRunnable对象。
                    //判断获取到的WorkflowExecuteRunnable对象是否为null，如果为null，说明无法找到对应的WorkflowExecuteRunnable对象，返回null。
                    //如果获取到的WorkflowExecuteRunnable对象不为null，通过调用workflowExecuteRunnable.getProcessInstance()方法获取对应的进程实例。
                    //如果processInstance对象为null，computeIfAbsent方法会将计算得到的processInstance对象存储到processInstanceCacheMap中，并返回这个对象作为computeIfAbsent方法的返回值。
                    //如果processInstance对象不为null，computeIfAbsent方法会直接返回这个对象，不会进行任何计算和存储。
//总的来说，这段代码的目标是从缓存映射processInstanceCacheMap中获取或计算一个进程实例对象，并将其存储到缓存中。如果进程实例对象已存在缓存中，则直接返回，否则，根据流程实例ID获取到对应的WorkflowExecuteRunnable对象，并从中获取进程实例对象进行存储和返回。
                    ProcessInstance processInstance = processInstanceCacheMap.computeIfAbsent(
                            taskInstance.getProcessInstanceId(), k -> {
                                WorkflowExecuteRunnable workflowExecuteRunnable = cacheManager.getByProcessInstanceId(
                                        taskInstance.getProcessInstanceId());
                                if (workflowExecuteRunnable == null) {
                                    return null;
                                }
                                return workflowExecuteRunnable.getProcessInstance();
                            });
                    //如果任务实例需要失败重试，那么就进行失败重试，并记录日志。
                    if (!checkTaskInstanceNeedFailover(needFailoverWorkerStartTime, processInstance, taskInstance)) {
                        log.info("Worker[{}] the current taskInstance doesn't need to failover", workerHost);
                        continue;
                    }
                    log.info(
                            "Worker[{}] failover: begin to failover taskInstance, will set the status to NEED_FAULT_TOLERANCE",
                            workerHost);
                    //开始任务失败重试
                    failoverTaskInstance(processInstance, taskInstance);
                    log.info("Worker[{}] failover: Finish failover taskInstance", workerHost);
                } catch (Exception ex) {
                    //如果任务实例执行失败重试时出现异常，也会被捕获并记录到日志中。
                    log.info("Worker[{}] failover taskInstance occur exception", workerHost, ex);
                }
            }
        }
        failoverTimeCost.stop();
        //在所有需要失败重试的任务实例都处理完毕后，停止计时并记录失败重试所需的总时间。
        log.info("Worker[{}] failover finished, useTime:{}ms",
                workerHost,
                failoverTimeCost.getTime(TimeUnit.MILLISECONDS));
    }

    /**
     * failover task instance
     * <p>
     * 1. kill yarn/k8s job if run on worker and there are yarn/k8s jobs in tasks.
     * 2. change task state from running to need failover.
     * 3. try to notify local master
     *
     * @param processInstance
     * @param taskInstance
     *
     * 这段代码是处理任务失移（Failover）的逻辑。
     * 首先，通过TaskMetrics类的incTaskInstanceByState方法增加任务实例的状态计数，将该任务实例的状态设置为"failover"。
     *
     * 设置任务实例的流程实例为传入的processInstance。
     *
     * 如果任务不是主任务（master task），执行以下逻辑：
     *
     * 创建TaskExecutionContext对象，用于任务执行的上下文信息，包括主节点地址、任务实例相关信息、流程实例相关信息和流程定义相关信息。
     * 如果masterConfig.isKillApplicationWhenTaskFailover()返回true，执行以下逻辑：
     * 执行ProcessUtils的killApplication方法，用在任务执行失败时终止相关的yarn或k8s作业。
     * 更新任务实例的状态为NEED_FAULT_TOLERANCE（需要容错）。
     * 调用taskInstanceDao的upsertTaskInstance方法，将更新后的任务实例对象保存到数据库中。 -TaskState对象，用于标识任务状态的变化，设置其相关属性，并提交到workflowExecuteThreadPool线程池中进行状态事件的处理。
     * 4.任务是主任务（master task），则打印"no need to failover"的提示信息。
     *
     * 总的来说，这段代码用于处理任务失败重试的逻辑。在该逻辑中，会根据任务的类型和配置决定是否终止相关的yarn或k8s作业，更新任务实例的状态为NEED_FAULT_TOLERANCE，并提交任务状态事件到线程池进行处理。
     */
    private void failoverTaskInstance(@NonNull ProcessInstance processInstance, @NonNull TaskInstance taskInstance) {
        TaskMetrics.incTaskInstanceByState("failover");

        taskInstance.setProcessInstance(processInstance);

        if (!TaskUtils.isMasterTask(taskInstance.getTaskType())) {
            log.info("The failover taskInstance is not master task");
            TaskExecutionContext taskExecutionContext = TaskExecutionContextBuilder.get()
                    .buildWorkflowInstanceHost(masterConfig.getMasterAddress())
                    .buildTaskInstanceRelatedInfo(taskInstance)
                    .buildProcessInstanceRelatedInfo(processInstance)
                    .buildProcessDefinitionRelatedInfo(processInstance.getProcessDefinition())
                    .create();

            if (masterConfig.isKillApplicationWhenTaskFailover()) {
                // only kill yarn/k8s job if exists , the local thread has exited
                log.info("TaskInstance failover begin kill the task related yarn or k8s job");
                ProcessUtils.killApplication(logClient, taskExecutionContext);
            }
        } else {
            log.info("The failover taskInstance is a master task, no need to failover in worker failover");
        }

        taskInstance.setState(TaskExecutionStatus.NEED_FAULT_TOLERANCE);
        taskInstance.setFlag(Flag.NO);
        taskInstanceDao.upsertTaskInstance(taskInstance);

        TaskStateEvent stateEvent = TaskStateEvent.builder()
                .processInstanceId(processInstance.getId())
                .taskInstanceId(taskInstance.getId())
                .status(TaskExecutionStatus.NEED_FAULT_TOLERANCE)
                .type(StateEventType.TASK_STATE_CHANGE)
                .build();
        workflowExecuteThreadPool.submitStateEvent(stateEvent);
    }

    /**
     * task needs failover if task start before server starts
     * 如果任务在服务器启动前启动，则任务需要失败重试
     * @return true if task instance need fail over
     */
    //这段代码是检查任务实例是否需要进行失败重试（Failover）的逻辑。
    //用于检查任务实例是否需要进行失败重试。首先检查相关的参数是否为空，然后判断任务实例所属的流程实例的host和当前主节点的地址是否相同，接着判断任务实例的状态和提交时间，最后根据条件判断是否需要进行失败重试。
    private boolean checkTaskInstanceNeedFailover(Optional<Date> needFailoverWorkerStartTime,
                                                  @Nullable ProcessInstance processInstance,
                                                  TaskInstance taskInstance) throws IOException {
        // 首先，检查processInstance和taskInstance是否为空。如果为空，打印错误日志，并返回false。
        if (processInstance == null) {
            // This case should be happened.
            log.error(
                    "Failover task instance error, cannot find the related processInstance form memory, this case shouldn't happened");
            return false;
        }
        if (taskInstance == null) {
            // This case should be happened.
            log.error("Master failover task instance error, taskInstance is null, this case shouldn't happened");
            return false;
        }
        // only failover the task owned myself if worker down.
        //检查任务实例所属的流程实例的host是否和当前主节点的地址相同。如果不相同，打印错误日志，并返回false。这是为了确保只有当前主节点才能进行任务失败重试。
        if (!StringUtils.equalsIgnoreCase(processInstance.getHost(), localAddress)) {
            log.error(
                    "Master failover task instance error, the taskInstance's processInstance's host: {} is not the current master: {}",
                    processInstance.getHost(),
                    localAddress);
            return false;
        }
        //检查任务实例的状态是否已完成。如果已完成，打印信息，并返回false。因为已完成的任务不需要进行失败重试。
        if (taskInstance.getState() != null && taskInstance.getState().isFinished()) {
            // The taskInstance is already finished, doesn't need to failover
            log.info("The task is already finished, doesn't need to failover");
            return false;
        }
        if (taskInstance.getTaskType() != null){
            switch (taskInstance.getTaskType()) {
                case "SHELL":{
                    String processPath = "path";

                    // 构建shell命令   命令使用ps -ef来列出所有的进程，并使用grep过滤器来匹配给定的进程执行路径。
                    String[] command = { "/bin/sh", "-c", "ps -ef | grep " + processPath };
                    //使用 ProcessBuilder 类来创建一个新的进程，并设置其命令行参数为上面构造的命令
                    ProcessBuilder processBuilder = new ProcessBuilder(command);

                    // 执行shell命令  ProcessBuilder类的start()方法执行shell命令，并返回一个Process对象，它代表了正在执行的进程。
                    // 启动进程，并等待其执行完毕
                    Process process = processBuilder.start();

                    // 读取命令的输出  获取进程的标准输入流
                    //使用BufferedReader从Process对象的输入流中命令的输出。
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    String line;
                    StringBuilder output = new StringBuilder();
                    //循环读取进程的标准输入流中的每一行内容
                    while ((line = reader.readLine()) != null) {
                        ////将每一行内容追加到 StringBuilder 对象中
                        output.append(line);
                    }
                    try {
                        //命令执行完成后，可以使用waitFor()方法获取命令的退出码。如果为0（表示成功），并且输出结果中包含要查找的进程名，则说明进程存在。
                        int exitCode = process.waitFor();
                        if (exitCode == 0 && output.toString().contains(processPath)) {
                            // 进程存在  不需要进行失败重试
                            return false;
                        } else {
                            // 进程不存在
                            return true;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                case "SPARK":{

                    String applicationIdStr = "application_id";
                    //创建了一个 YarnClient 对象并初始化它
                    Configuration conf = new Configuration();
                    YarnClient yarnClient = YarnClient.createYarnClient();
                    yarnClient.init(conf);
                    yarnClient.start();
                    //使用 ApplicationId 类创建应用程序 ID 对象，并将其转换为 YarnApplicationState 枚举值
                    ApplicationId applicationId = ApplicationId.fromString(applicationIdStr);
                    YarnApplicationState appState = null;
                    try {
                        //使用 YarnClient 类的方法来获取应用程序的状态
                        appState = yarnClient.getApplicationReport(applicationId).getYarnApplicationState();
                    } catch (YarnException e) {
                        throw new RuntimeException(e);
                    }

                    if(appState == YarnApplicationState.RUNNING){
                        log.info("The application with id " + applicationIdStr + " is running.");
                        return false;
                    } else {
                        log.info("The application with id " + applicationIdStr + " is not running.");
                    }

                    yarnClient.stop();
                    return true;

                }
                case "FLINK":{

                }
            }
        }

        //如果needFailoverWorkerStartTime为空，表示worker还未启动或者宕机。在这种情况下，需要进行任务失败重试，返回true。
        if (!needFailoverWorkerStartTime.isPresent()) {
            // The worker is still down
            return true;
        }
        // The worker is active, may already send some new task to it
        //如果needFailoverWorkerStartTime不为空，表示worker节点已启动，并且存在新的任务已经分配给worker节点。
        // 在这种情况下，需要判断任务实例是否是新提交的任务。如果任务实例的提交时间在needFailoverWorkerStartTime之后，表示任务实例是新提交的，不需要进行失败重试。打印信息，并返回false。
        if (taskInstance.getSubmitTime() != null && taskInstance.getSubmitTime()
                .after(needFailoverWorkerStartTime.get())) {
            log.info(
                    "The taskInstance's submitTime: {} is after the need failover worker's start time: {}, the taskInstance is newly submit, it doesn't need to failover",
                    taskInstance.getSubmitTime(),
                    needFailoverWorkerStartTime.get());
            return false;
        }
        //如果以上所有条件都不符合，方法返回 true，表示此工作流程实例需要进行失败重试。
        return true;
    }

    private List<TaskInstance> getNeedFailoverTaskInstance(@NonNull String failoverWorkerHost) {
        // we query the task instance from cache, so that we can directly update the cache
        return cacheManager.getAll()
                .stream()
                .flatMap(workflowExecuteRunnable -> workflowExecuteRunnable.getAllTaskInstances().stream())
                // If the worker is in dispatching and the host is not set
                .filter(taskInstance -> failoverWorkerHost.equals(taskInstance.getHost())
                        && taskInstance.getState().shouldFailover())
                .collect(Collectors.toList());
    }

    private Optional<Date> getServerStartupTime(List<Server> servers, String host) {
        if (CollectionUtils.isEmpty(servers)) {
            return Optional.empty();
        }
        Date serverStartupTime = null;
        for (Server server : servers) {
            if (host.equals(server.getHost() + Constants.COLON + server.getPort())) {
                serverStartupTime = server.getCreateTime();
                break;
            }
        }
        return Optional.ofNullable(serverStartupTime);
    }
}
