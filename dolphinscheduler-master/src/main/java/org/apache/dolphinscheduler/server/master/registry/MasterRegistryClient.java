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

package org.apache.dolphinscheduler.server.master.registry;

import static org.apache.dolphinscheduler.common.Constants.REGISTRY_DOLPHINSCHEDULER_MASTERS;
import static org.apache.dolphinscheduler.common.Constants.REGISTRY_DOLPHINSCHEDULER_NODE;
import static org.apache.dolphinscheduler.common.Constants.SLEEP_TIME_MILLIS;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.enums.StateEvent;
import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.registry.api.ConnectionState;
import org.apache.dolphinscheduler.remote.utils.NamedThreadFactory;
import org.apache.dolphinscheduler.server.builder.TaskExecutionContextBuilder;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThreadPool;
import org.apache.dolphinscheduler.server.registry.HeartBeatTask;
import org.apache.dolphinscheduler.server.utils.ProcessUtils;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.Sets;

/**
 * zookeeper master client
 * <p>
 * single instance
 */
@Component
public class MasterRegistryClient {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(MasterRegistryClient.class);

    /**
     * process service
     */
    @Autowired
    private ProcessService processService;

    @Autowired
    private RegistryClient registryClient;

    /**
     * master config
     */
    @Autowired
    private MasterConfig masterConfig;

    /**
     * heartbeat executor
     */
    private ScheduledExecutorService heartBeatExecutor;

    @Autowired
    private WorkflowExecuteThreadPool workflowExecuteThreadPool;

    /**
     * master startup time, ms
     */
    private long startupTime;

    private String localNodePath;

    public void init() {
        this.startupTime = System.currentTimeMillis();
        this.heartBeatExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("HeartBeatExecutor"));
    }

    public void start() {
        String nodeLock = Constants.REGISTRY_DOLPHINSCHEDULER_LOCK_FAILOVER_STARTUP_MASTERS;
        try {
            // create distributed lock with the root node path of the lock space as /dolphinscheduler/lock/failover/startup-masters
            registryClient.getLock(nodeLock);
            // master registry
            registry();

            registryClient.subscribe(REGISTRY_DOLPHINSCHEDULER_NODE, new MasterRegistryDataListener());
        } catch (Exception e) {
            logger.error("master start up exception", e);
            throw new RuntimeException("master start up error", e);
        } finally {
            registryClient.releaseLock(nodeLock);
        }
    }

    public void setRegistryStoppable(IStoppable stoppable) {
        registryClient.setStoppable(stoppable);
    }

    public void closeRegistry() {
        // TODO unsubscribe MasterRegistryDataListener
        deregister();
    }

    /**
     * remove master node path
     *
     * @param path node path
     * @param nodeType node type
     * @param failover is failover
     */
    public void removeMasterNodePath(String path, NodeType nodeType, boolean failover) {
        logger.info("{} node deleted : {}", nodeType, path);

        if (StringUtils.isEmpty(path)) {
            logger.error("server down error: empty path: {}, nodeType:{}", path, nodeType);
            return;
        }

        String serverHost = registryClient.getHostByEventDataPath(path);
        if (StringUtils.isEmpty(serverHost)) {
            logger.error("server down error: unknown path: {}, nodeType:{}", path, nodeType);
            return;
        }

        String failoverPath = getFailoverLockPath(nodeType, serverHost);
        try {
            registryClient.getLock(failoverPath);

            if (!registryClient.exists(path)) {
                logger.info("path: {} not exists", path);
                // handle dead server
                registryClient.handleDeadServer(Collections.singleton(path), nodeType, Constants.ADD_OP);
            }

            //failover server
            if (failover) {
                failoverServerWhenDown(serverHost, nodeType);
            }
        } catch (Exception e) {
            logger.error("{} server failover failed, host:{}", nodeType, serverHost, e);
        } finally {
            registryClient.releaseLock(failoverPath);
        }
    }

    /**
     * remove worker node path
     *
     * @param path     node path
     * @param nodeType node type
     * @param failover is failover
     */
    public void removeWorkerNodePath(String path, NodeType nodeType, boolean failover) {
        logger.info("{} node deleted : {}", nodeType, path);
        try {
            String serverHost = null;
            if (!StringUtils.isEmpty(path)) {
                serverHost = registryClient.getHostByEventDataPath(path);
                if (StringUtils.isEmpty(serverHost)) {
                    logger.error("server down error: unknown path: {}", path);
                    return;
                }
                if (!registryClient.exists(path)) {
                    logger.info("path: {} not exists", path);
                    // handle dead server
                    registryClient.handleDeadServer(Collections.singleton(path), nodeType, Constants.ADD_OP);
                }
            }
            //failover server
            if (failover) {
                failoverServerWhenDown(serverHost, nodeType);
            }
        } catch (Exception e) {
            logger.error("{} server failover failed", nodeType, e);
        }
    }

    private boolean isNeedToHandleDeadServer(String host, NodeType nodeType, Duration sessionTimeout) {
        long sessionTimeoutMillis = Math.max(Constants.REGISTRY_SESSION_TIMEOUT, sessionTimeout.toMillis());
        List<Server> serverList = registryClient.getServerList(nodeType);
        if (CollectionUtils.isEmpty(serverList)) {
            return true;
        }
        Date startupTime = getServerStartupTime(serverList, host);
        if (startupTime == null) {
            return true;
        }
        if (System.currentTimeMillis() - startupTime.getTime() > sessionTimeoutMillis) {
            return true;
        }
        return false;
    }

    /**
     * failover server when server down
     *
     * @param serverHost server host
     * @param nodeType zookeeper node type
     */
    private void failoverServerWhenDown(String serverHost, NodeType nodeType) {
        switch (nodeType) {
            case MASTER:
                failoverMaster(serverHost);
                break;
            case WORKER:
                failoverWorker(serverHost);
                break;
            default:
                break;
        }
    }

    /**
     * get failover lock path
     *
     * @param nodeType zookeeper node type
     * @return fail over lock path
     */
    public String getFailoverLockPath(NodeType nodeType, String host) {
        switch (nodeType) {
            case MASTER:
                return Constants.REGISTRY_DOLPHINSCHEDULER_LOCK_FAILOVER_MASTERS + "/" + host;
            case WORKER:
                return Constants.REGISTRY_DOLPHINSCHEDULER_LOCK_FAILOVER_WORKERS + "/" + host;
            default:
                return "";
        }
    }

    /**
     * task needs failover if task start before worker starts
     *
     * @param workerServers worker servers
     * @param taskInstance task instance
     * @return true if task instance need fail over
     */
    private boolean checkTaskInstanceNeedFailover(List<Server> workerServers, TaskInstance taskInstance) {

        boolean taskNeedFailover = true;

        //now no host will execute this task instance,so no need to failover the task
        if (taskInstance.getHost() == null) {
            return false;
        }

        //if task start after worker starts, there is no need to failover the task.
        if (checkTaskAfterWorkerStart(workerServers, taskInstance)) {
            taskNeedFailover = false;
        }

        return taskNeedFailover;
    }

    /**
     * check task start after the worker server starts.
     *
     * @param taskInstance task instance
     * @return true if task instance start time after worker server start date
     */
    private boolean checkTaskAfterWorkerStart(List<Server> workerServers, TaskInstance taskInstance) {
        if (StringUtils.isEmpty(taskInstance.getHost())) {
            return false;
        }
        Date workerServerStartDate = getServerStartupTime(workerServers, taskInstance.getHost());
        if (workerServerStartDate != null) {
            if (taskInstance.getStartTime() == null) {
                return taskInstance.getSubmitTime().after(workerServerStartDate);
            } else {
                return taskInstance.getStartTime().after(workerServerStartDate);
            }
        }
        return false;
    }

    /**
     * get server startup time
     */
    private Date getServerStartupTime(List<Server> servers, String host) {
        if (CollectionUtils.isEmpty(servers)) {
            return null;
        }
        Date serverStartupTime = null;
        for (Server server : servers) {
            if (host.equals(server.getHost() + Constants.COLON + server.getPort())) {
                serverStartupTime = server.getCreateTime();
                break;
            }
        }
        return serverStartupTime;
    }

    /**
     * get server startup time
     */
    private Date getServerStartupTime(NodeType nodeType, String host) {
        if (StringUtils.isEmpty(host)) {
            return null;
        }
        List<Server> servers = registryClient.getServerList(nodeType);
        return getServerStartupTime(servers, host);
    }

    /**
     * failover worker tasks
     * <p>
     * 1. kill yarn job if there are yarn jobs in tasks.
     * 2. change task state from running to need failover.
     * 3. failover all tasks when workerHost is null
     *
     * @param workerHost worker host
     */
    private void failoverWorker(String workerHost) {

        if (StringUtils.isEmpty(workerHost)) {
            return;
        }

        List<Server> workerServers = registryClient.getServerList(NodeType.WORKER);

        long startTime = System.currentTimeMillis();
        List<TaskInstance> needFailoverTaskInstanceList = processService.queryNeedFailoverTaskInstances(workerHost);
        Map<Integer, ProcessInstance> processInstanceCacheMap = new HashMap<>();
        logger.info("start worker[{}] failover, task list size:{}", workerHost, needFailoverTaskInstanceList.size());

        for (TaskInstance taskInstance : needFailoverTaskInstanceList) {
            ProcessInstance processInstance = processInstanceCacheMap.get(taskInstance.getProcessInstanceId());
            if (processInstance == null) {
                processInstance = processService.findProcessInstanceDetailById(taskInstance.getProcessInstanceId());
                if (processInstance == null) {
                    logger.error("failover task instance error, processInstance {} of taskInstance {} is null",
                            taskInstance.getProcessInstanceId(), taskInstance.getId());
                    continue;
                }
                processInstanceCacheMap.put(processInstance.getId(), processInstance);
            }

            if (!checkTaskInstanceNeedFailover(workerServers, taskInstance)) {
                continue;
            }

            // only failover the task owned myself if worker down.
            if (!processInstance.getHost().equalsIgnoreCase(getLocalAddress())) {
                continue;
            }

            logger.info("failover task instance id: {}, process instance id: {}", taskInstance.getId(), taskInstance.getProcessInstanceId());
            failoverTaskInstance(processInstance, taskInstance);
        }
        logger.info("end worker[{}] failover, useTime:{}ms", workerHost, System.currentTimeMillis() - startTime);
    }

    /**
     * failover master
     * <p>
     * failover process instance and associated task instance
     *
     * @param masterHost master host
     */
    public void failoverMaster(String masterHost) {

        if (StringUtils.isEmpty(masterHost)) {
            return;
        }

        Date serverStartupTime = getServerStartupTime(NodeType.MASTER, masterHost);
        List<Server> workerServers = registryClient.getServerList(NodeType.WORKER);

        long startTime = System.currentTimeMillis();
        List<ProcessInstance> needFailoverProcessInstanceList = processService.queryNeedFailoverProcessInstances(masterHost);
        logger.info("start master[{}] failover, process list size:{}", masterHost, needFailoverProcessInstanceList.size());

        for (ProcessInstance processInstance : needFailoverProcessInstanceList) {
            if (Constants.NULL.equals(processInstance.getHost())) {
                continue;
            }

            List<TaskInstance> validTaskInstanceList = processService.findValidTaskListByProcessId(processInstance.getId());
            for (TaskInstance taskInstance : validTaskInstanceList) {
                if (Constants.NULL.equals(taskInstance.getHost())) {
                    continue;
                }
                if (taskInstance.getState().typeIsFinished()) {
                    continue;
                }
                if (!checkTaskInstanceNeedFailover(workerServers, taskInstance)) {
                    continue;
                }
                logger.info("failover task instance id: {}, process instance id: {}", taskInstance.getId(), taskInstance.getProcessInstanceId());
                failoverTaskInstance(processInstance, taskInstance);
            }

            if (serverStartupTime != null && processInstance.getRestartTime() != null
                    && processInstance.getRestartTime().after(serverStartupTime)) {
                continue;
            }

            logger.info("failover process instance id: {}", processInstance.getId());
            //updateProcessInstance host is null and insert into command
            processService.processNeedFailoverProcessInstances(processInstance);
        }

        logger.info("master[{}] failover end, useTime:{}ms", masterHost, System.currentTimeMillis() - startTime);
    }

    /**
     * failover task instance
     * <p>
     * 1. kill yarn job if there are yarn jobs in tasks.
     * 2. change task state from running to need failover.
     * 3. try to notify local master
     */
    private void failoverTaskInstance(ProcessInstance processInstance, TaskInstance taskInstance) {
        if (taskInstance == null) {
            logger.error("failover task instance error, taskInstance is null");
            return;
        }

        if (processInstance == null) {
            logger.error("failover task instance error, processInstance {} of taskInstance {} is null",
                    taskInstance.getProcessInstanceId(), taskInstance.getId());
            return;
        }

        taskInstance.setProcessInstance(processInstance);
        TaskExecutionContext taskExecutionContext = TaskExecutionContextBuilder.get()
                .buildTaskInstanceRelatedInfo(taskInstance)
                .buildProcessInstanceRelatedInfo(processInstance)
                .create();

        if (masterConfig.isKillYarnJobWhenTaskFailover()) {
            // only kill yarn job if exists , the local thread has exited
            ProcessUtils.killYarnJob(taskExecutionContext);
        }

        taskInstance.setState(ExecutionStatus.NEED_FAULT_TOLERANCE);
        processService.saveTaskInstance(taskInstance);

        StateEvent stateEvent = new StateEvent();
        stateEvent.setTaskInstanceId(taskInstance.getId());
        stateEvent.setType(StateEventType.TASK_STATE_CHANGE);
        stateEvent.setProcessInstanceId(processInstance.getId());
        stateEvent.setExecutionStatus(taskInstance.getState());
        workflowExecuteThreadPool.submitStateEvent(stateEvent);
    }

    /**
     * registry
     */
    public void registry() {
        String address = NetUtils.getAddr(masterConfig.getListenPort());
        localNodePath = getMasterPath();
        int masterHeartbeatInterval = masterConfig.getHeartbeatInterval();
        HeartBeatTask heartBeatTask = new HeartBeatTask(startupTime,
                masterConfig.getMaxCpuLoadAvg(),
                masterConfig.getReservedMemory(),
                Sets.newHashSet(getMasterPath()),
                Constants.MASTER_TYPE,
                registryClient);

        // remove before persist
        registryClient.remove(localNodePath);
        registryClient.persistEphemeral(localNodePath, heartBeatTask.getHeartBeatInfo());

        while (!registryClient.checkNodeExists(NetUtils.getHost(), NodeType.MASTER)) {
            ThreadUtils.sleep(SLEEP_TIME_MILLIS);
        }

        // sleep 1s, waiting master failover remove
        ThreadUtils.sleep(SLEEP_TIME_MILLIS);

        // delete dead server
        registryClient.handleDeadServer(Collections.singleton(localNodePath), NodeType.MASTER, Constants.DELETE_OP);

        registryClient.addConnectionStateListener(this::handleConnectionState);
        this.heartBeatExecutor.scheduleAtFixedRate(heartBeatTask, masterHeartbeatInterval, masterHeartbeatInterval, TimeUnit.SECONDS);
        logger.info("master node : {} registry to ZK successfully with heartBeatInterval : {}s", address, masterHeartbeatInterval);

    }

    public void handleConnectionState(ConnectionState state) {
        switch (state) {
            case CONNECTED:
                logger.debug("registry connection state is {}", state);
                break;
            case SUSPENDED:
                logger.warn("registry connection state is {}, ready to retry connection", state);
                break;
            case RECONNECTED:
                logger.debug("registry connection state is {}, clean the node info", state);
                registryClient.persistEphemeral(localNodePath, "");
                break;
            case DISCONNECTED:
                logger.warn("registry connection state is {}, ready to stop myself", state);
                registryClient.getStoppable().stop("registry connection state is DISCONNECTED, stop myself");
                break;
            default:
        }
    }

    public void deregister() {
        try {
            String address = getLocalAddress();
            String localNodePath = getMasterPath();
            registryClient.remove(localNodePath);
            logger.info("master node : {} unRegistry to register center.", address);
            heartBeatExecutor.shutdown();
            logger.info("heartbeat executor shutdown");
            registryClient.close();
        } catch (Exception e) {
            logger.error("remove registry path exception ", e);
        }
    }

    /**
     * get master path
     */
    public String getMasterPath() {
        String address = getLocalAddress();
        return REGISTRY_DOLPHINSCHEDULER_MASTERS + "/" + address;
    }

    /**
     * get local address
     */
    public String getLocalAddress() {
        return NetUtils.getAddr(masterConfig.getListenPort());
    }

}
