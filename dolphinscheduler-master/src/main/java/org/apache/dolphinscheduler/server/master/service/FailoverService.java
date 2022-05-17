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

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.enums.StateEvent;
import org.apache.dolphinscheduler.common.enums.StateEventType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.plugin.task.api.TaskExecutionContext;
import org.apache.dolphinscheduler.plugin.task.api.enums.ExecutionStatus;
import org.apache.dolphinscheduler.server.builder.TaskExecutionContextBuilder;
import org.apache.dolphinscheduler.server.master.config.MasterConfig;
import org.apache.dolphinscheduler.server.master.runner.WorkflowExecuteThreadPool;
import org.apache.dolphinscheduler.server.master.runner.task.TaskProcessorFactory;
import org.apache.dolphinscheduler.server.utils.ProcessUtils;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * failover service
 */
@Component
public class FailoverService {
    private static final Logger LOGGER = LoggerFactory.getLogger(FailoverService.class);
    private final RegistryClient registryClient;
    private final MasterConfig masterConfig;
    private final ProcessService processService;
    private final WorkflowExecuteThreadPool workflowExecuteThreadPool;

    public FailoverService(RegistryClient registryClient, MasterConfig masterConfig, ProcessService processService,
                           WorkflowExecuteThreadPool workflowExecuteThreadPool) {
        this.registryClient = registryClient;
        this.masterConfig = masterConfig;
        this.processService = processService;
        this.workflowExecuteThreadPool = workflowExecuteThreadPool;
    }

    /**
     * check master failover
     */
    public void checkMasterFailover() {
        List<String> hosts = getNeedFailoverMasterServers();
        if (CollectionUtils.isEmpty(hosts)) {
            return;
        }
        LOGGER.info("need failover hosts:{}", hosts);

        for (String host : hosts) {
            failoverMasterWithLock(host);
        }
    }

    /**
     * failover server when server down
     *
     * @param serverHost server host
     * @param nodeType   node type
     */
    public void failoverServerWhenDown(String serverHost, NodeType nodeType) {
        switch (nodeType) {
            case MASTER:
                failoverMasterWithLock(serverHost);
                break;
            case WORKER:
                failoverWorker(serverHost);
                break;
            default:
                break;
        }
    }

    private void failoverMasterWithLock(String masterHost) {
        String failoverPath = getFailoverLockPath(NodeType.MASTER, masterHost);
        try {
            registryClient.getLock(failoverPath);
            this.failoverMaster(masterHost);
        } catch (Exception e) {
            LOGGER.error("{} server failover failed, host:{}", NodeType.MASTER, masterHost, e);
        } finally {
            registryClient.releaseLock(failoverPath);
        }
    }

    /**
     * failover master
     * <p>
     * failover process instance and associated task instance
     *
     * @param masterHost master host
     */
    private void failoverMaster(String masterHost) {
        if (StringUtils.isEmpty(masterHost)) {
            return;
        }
        Date serverStartupTime = getServerStartupTime(NodeType.MASTER, masterHost);
        long startTime = System.currentTimeMillis();
        List<ProcessInstance> needFailoverProcessInstanceList = processService.queryNeedFailoverProcessInstances(masterHost);
        LOGGER.info("start master[{}] failover, process list size:{}", masterHost, needFailoverProcessInstanceList.size());

        // servers need to contains master hosts and worker hosts, otherwise the logic task will failover fail.
        List<Server> servers = registryClient.getServerList(NodeType.WORKER);
        servers.addAll(registryClient.getServerList(NodeType.MASTER));

        for (ProcessInstance processInstance : needFailoverProcessInstanceList) {
            if (Constants.NULL.equals(processInstance.getHost())) {
                continue;
            }

            List<TaskInstance> validTaskInstanceList = processService.findValidTaskListByProcessId(processInstance.getId());
            for (TaskInstance taskInstance : validTaskInstanceList) {
                LOGGER.info("failover task instance id: {}, process instance id: {}", taskInstance.getId(), taskInstance.getProcessInstanceId());
                failoverTaskInstance(processInstance, taskInstance, servers);
            }

            if (serverStartupTime != null && processInstance.getRestartTime() != null
                && processInstance.getRestartTime().after(serverStartupTime)) {
                continue;
            }

            LOGGER.info("failover process instance id: {}", processInstance.getId());
            //updateProcessInstance host is null and insert into command
            processInstance.setHost(Constants.NULL);
            processService.processNeedFailoverProcessInstances(processInstance);
        }

        LOGGER.info("master[{}] failover end, useTime:{}ms", masterHost, System.currentTimeMillis() - startTime);
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

        long startTime = System.currentTimeMillis();
        List<TaskInstance> needFailoverTaskInstanceList = processService.queryNeedFailoverTaskInstances(workerHost);
        Map<Integer, ProcessInstance> processInstanceCacheMap = new HashMap<>();
        LOGGER.info("start worker[{}] failover, task list size:{}", workerHost, needFailoverTaskInstanceList.size());
        List<Server> workerServers = registryClient.getServerList(NodeType.WORKER);
        for (TaskInstance taskInstance : needFailoverTaskInstanceList) {
            ProcessInstance processInstance = processInstanceCacheMap.get(taskInstance.getProcessInstanceId());
            if (processInstance == null) {
                processInstance = processService.findProcessInstanceDetailById(taskInstance.getProcessInstanceId());
                if (processInstance == null) {
                    LOGGER.error("failover task instance error, processInstance {} of taskInstance {} is null",
                        taskInstance.getProcessInstanceId(), taskInstance.getId());
                    continue;
                }
                processInstanceCacheMap.put(processInstance.getId(), processInstance);
            }

            // only failover the task owned myself if worker down.
            if (!processInstance.getHost().equalsIgnoreCase(getLocalAddress())) {
                continue;
            }

            LOGGER.info("failover task instance id: {}, process instance id: {}", taskInstance.getId(), taskInstance.getProcessInstanceId());
            failoverTaskInstance(processInstance, taskInstance, workerServers);
        }
        LOGGER.info("end worker[{}] failover, useTime:{}ms", workerHost, System.currentTimeMillis() - startTime);
    }

    /**
     * failover task instance
     * <p>
     * 1. kill yarn job if run on worker and there are yarn jobs in tasks.
     * 2. change task state from running to need failover.
     * 3. try to notify local master
     * @param processInstance
     * @param taskInstance
     * @param servers if failover master, servers container master servers and worker servers; if failover worker, servers contain worker servers.
     */
    private void failoverTaskInstance(ProcessInstance processInstance, TaskInstance taskInstance, List<Server> servers) {
        if (processInstance == null) {
            LOGGER.error("failover task instance error, processInstance {} of taskInstance {} is null",
                taskInstance.getProcessInstanceId(), taskInstance.getId());
            return;
        }
        if (!checkTaskInstanceNeedFailover(servers, taskInstance)) {
            return;
        }

        boolean isMasterTask = TaskProcessorFactory.isMasterTask(taskInstance.getTaskType());

        taskInstance.setProcessInstance(processInstance);

        if (!isMasterTask) {
            TaskExecutionContext taskExecutionContext = TaskExecutionContextBuilder.get()
                .buildTaskInstanceRelatedInfo(taskInstance)
                .buildProcessInstanceRelatedInfo(processInstance)
                .create();

            if (masterConfig.isKillYarnJobWhenTaskFailover()) {
                // only kill yarn job if exists , the local thread has exited
                ProcessUtils.killYarnJob(taskExecutionContext);
            }
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
     * get need failover master servers
     *
     * @return need failover master servers
     */
    private List<String> getNeedFailoverMasterServers() {
        // failover myself && failover dead masters
        List<String> hosts = processService.queryNeedFailoverProcessInstanceHost();

        Iterator<String> iterator = hosts.iterator();
        while (iterator.hasNext()) {
            String host = iterator.next();
            if (registryClient.checkNodeExists(host, NodeType.MASTER)) {
                if (!host.equals(getLocalAddress())) {
                    iterator.remove();
                }
            }
        }
        return hosts;
    }

    /**
     * task needs failover if task start before server starts
     *
     * @param servers servers, can container master servers or worker servers
     * @param taskInstance  task instance
     * @return true if task instance need fail over
     */
    private boolean checkTaskInstanceNeedFailover(List<Server> servers, TaskInstance taskInstance) {

        boolean taskNeedFailover = true;

        if (taskInstance == null) {
            LOGGER.error("failover task instance error, taskInstance is null");
            return false;
        }

        if (Constants.NULL.equals(taskInstance.getHost())) {
            return false;
        }

        if (taskInstance.getState() != null && taskInstance.getState().typeIsFinished()) {
            return false;
        }

        //now no host will execute this task instance,so no need to failover the task
        if (taskInstance.getHost() == null) {
            return false;
        }

        //if task start after server starts, there is no need to failover the task.
        if (checkTaskAfterServerStart(servers, taskInstance)) {
            taskNeedFailover = false;
        }

        return taskNeedFailover;
    }

    /**
     * check task start after the worker server starts.
     *
     * @param servers servers, can contain master servers or worker servers
     * @param taskInstance task instance
     * @return true if task instance start time after server start date
     */
    private boolean checkTaskAfterServerStart(List<Server> servers, TaskInstance taskInstance) {
        if (StringUtils.isEmpty(taskInstance.getHost())) {
            return false;
        }
        Date serverStartDate = getServerStartupTime(servers, taskInstance.getHost());
        if (serverStartDate != null) {
            if (taskInstance.getStartTime() == null) {
                return taskInstance.getSubmitTime().after(serverStartDate);
            } else {
                return taskInstance.getStartTime().after(serverStartDate);
            }
        }
        return false;
    }

    /**
     * get failover lock path
     *
     * @param nodeType zookeeper node type
     * @return fail over lock path
     */
    private String getFailoverLockPath(NodeType nodeType, String host) {
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
     * get local address
     */
    String getLocalAddress() {
        return NetUtils.getAddr(masterConfig.getListenPort());
    }

}
