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

package org.apache.dolphinscheduler.server.master.zk;

import static org.apache.dolphinscheduler.common.Constants.REGISTRY_DOLPHINSCHEDULER_NODE;
import static org.apache.dolphinscheduler.common.Constants.SLEEP_TIME_MILLIS;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.NetUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.builder.TaskExecutionContextBuilder;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.master.registry.MasterRegistry;
import org.apache.dolphinscheduler.server.utils.ProcessUtils;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.registry.AbstractRegistryClient;
import org.apache.dolphinscheduler.service.registry.RegistryCenter;

import org.apache.curator.framework.recipes.cache.TreeCacheEvent;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * zookeeper master client
 * <p>
 * single instance
 */
@Component
public class MasterRegistryClient extends AbstractRegistryClient {

    /**
     * logger
     */
    private static final Logger logger = LoggerFactory.getLogger(MasterRegistryClient.class);

    /**
     * process service
     */
    @Autowired
    private ProcessService processService;

    /**
     * master registry
     */
    @Autowired
    private MasterRegistry masterRegistry;

    @Resource
    RegistryCenter registryCenter;

    public void start() {
        String znodeLock = getMasterStartUpLockPath();
        try {
            // create distributed lock with the root node path of the lock space as /dolphinscheduler/lock/failover/startup-masters

            registryCenter.getLock(znodeLock);


            // master registry
            masterRegistry.registry();
            String registryPath = this.masterRegistry.getMasterPath();
            masterRegistry.getRegistryCenter().handleDeadServer(registryPath, NodeType.MASTER, Constants.DELETE_ZK_OP);

            // init system znode
            this.initSystemZNode();

            while (!checkZKNodeExists(NetUtils.getHost(), NodeType.MASTER)) {
                ThreadUtils.sleep(SLEEP_TIME_MILLIS);
            }

            // self tolerant
            if (getActiveMasterNum() == 1) {
                removeNodePath(null, NodeType.MASTER, true);
                removeNodePath(null, NodeType.WORKER, true);
            }
            registryCenter.subscribe(REGISTRY_DOLPHINSCHEDULER_NODE, new MasterRegistryDataListener());
        } catch (Exception e) {
            logger.error("master start up exception", e);
        } finally {

            registryCenter.releaseLock(znodeLock);
        }
    }

    public void setStoppable(IStoppable stoppable) {
        masterRegistry.getRegistryCenter().setStoppable(stoppable);
    }

    public void close() {
        masterRegistry.unRegistry();
        registryCenter.close();
    }


    /**
     * remove zookeeper node path
     *
     * @param path zookeeper node path
     * @param nodeType zookeeper node type
     * @param failover is failover
     */
    public void removeNodePath(String path, NodeType nodeType, boolean failover) {
        logger.info("{} node deleted : {}", nodeType, path);
        String failoverPath = getFailoverLockPath(nodeType);
        try {
            registryCenter.getLock(failoverPath);

            String serverHost = null;
            if (StringUtils.isNotEmpty(path)) {
                serverHost = registryCenter.getHostByEventDataPath(path);
                if (StringUtils.isEmpty(serverHost)) {
                    logger.error("server down error: unknown path: {}", path);
                    return;
                }
                // handle dead server
                registryCenter.handleDeadServer(path, nodeType, Constants.ADD_ZK_OP);
            }
            //failover server
            if (failover) {
                failoverServerWhenDown(serverHost, nodeType);
            }
        } catch (Exception e) {
            logger.error("{} server failover failed.", nodeType);
            logger.error("failover exception ", e);
        } finally {
            registryCenter.releaseLock(failoverPath);
        }
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
                failoverWorker(serverHost, true);
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
    private String getFailoverLockPath(NodeType nodeType) {
        switch (nodeType) {
            case MASTER:
                return getMasterFailoverLockPath();
            case WORKER:
                return getWorkerFailoverLockPath();
            default:
                return "";
        }
    }

    /**
     * monitor master
     *
     * @param event event
     * @param path path
     */
    public void handleMasterEvent(TreeCacheEvent event, String path) {
        switch (event.getType()) {
            case NODE_ADDED:
                logger.info("master node added : {}", path);
                break;
            case NODE_REMOVED:
                removeNodePath(path, NodeType.MASTER, true);
                break;
            default:
                break;
        }
    }

    /**
     * monitor worker
     *
     * @param event event
     * @param path path
     */
    public void handleWorkerEvent(TreeCacheEvent event, String path) {
        switch (event.getType()) {
            case NODE_ADDED:
                logger.info("worker node added : {}", path);
                break;
            case NODE_REMOVED:
                logger.info("worker node deleted : {}", path);
                removeNodePath(path, NodeType.WORKER, true);
                break;
            default:
                break;
        }
    }

    /**
     * task needs failover if task start before worker starts
     *
     * @param taskInstance task instance
     * @return true if task instance need fail over
     */
    private boolean checkTaskInstanceNeedFailover(TaskInstance taskInstance) {

        boolean taskNeedFailover = true;

        //now no host will execute this task instance,so no need to failover the task
        if (taskInstance.getHost() == null) {
            return false;
        }

        // if the worker node exists in zookeeper, we must check the task starts after the worker
        if (checkZKNodeExists(taskInstance.getHost(), NodeType.WORKER)) {
            //if task start after worker starts, there is no need to failover the task.
            if (checkTaskAfterWorkerStart(taskInstance)) {
                taskNeedFailover = false;
            }
        }
        return taskNeedFailover;
    }

    /**
     * check task start after the worker server starts.
     *
     * @param taskInstance task instance
     * @return true if task instance start time after worker server start date
     */
    private boolean checkTaskAfterWorkerStart(TaskInstance taskInstance) {
        if (StringUtils.isEmpty(taskInstance.getHost())) {
            return false;
        }
        Date workerServerStartDate = null;
        List<Server> workerServers = getServerList(NodeType.WORKER);
        for (Server workerServer : workerServers) {
            if (taskInstance.getHost().equals(workerServer.getHost() + Constants.COLON + workerServer.getPort())) {
                workerServerStartDate = workerServer.getCreateTime();
                break;
            }
        }
        if (workerServerStartDate != null) {
            return taskInstance.getStartTime().after(workerServerStartDate);
        }
        return false;
    }

    /**
     * failover worker tasks
     * <p>
     * 1. kill yarn job if there are yarn jobs in tasks.
     * 2. change task state from running to need failover.
     * 3. failover all tasks when workerHost is null
     *
     * @param workerHost worker host
     * @param needCheckWorkerAlive need check worker alive
     */
    private void failoverWorker(String workerHost, boolean needCheckWorkerAlive) {
        logger.info("start worker[{}] failover ...", workerHost);
        List<TaskInstance> needFailoverTaskInstanceList = processService.queryNeedFailoverTaskInstances(workerHost);
        for (TaskInstance taskInstance : needFailoverTaskInstanceList) {
            if (needCheckWorkerAlive) {
                if (!checkTaskInstanceNeedFailover(taskInstance)) {
                    continue;
                }
            }

            ProcessInstance processInstance = processService.findProcessInstanceDetailById(taskInstance.getProcessInstanceId());
            if (processInstance != null) {
                taskInstance.setProcessInstance(processInstance);
            }

            TaskExecutionContext taskExecutionContext = TaskExecutionContextBuilder.get()
                    .buildTaskInstanceRelatedInfo(taskInstance)
                    .buildProcessInstanceRelatedInfo(processInstance)
                    .create();
            // only kill yarn job if exists , the local thread has exited
            ProcessUtils.killYarnJob(taskExecutionContext);

            taskInstance.setState(ExecutionStatus.NEED_FAULT_TOLERANCE);
            processService.saveTaskInstance(taskInstance);
        }
        logger.info("end worker[{}] failover ...", workerHost);
    }

    /**
     * failover master tasks
     *
     * @param masterHost master host
     */
    private void failoverMaster(String masterHost) {
        logger.info("start master failover ...");

        List<ProcessInstance> needFailoverProcessInstanceList = processService.queryNeedFailoverProcessInstances(masterHost);

        logger.info("failover process list size:{} ", needFailoverProcessInstanceList.size());
        //updateProcessInstance host is null and insert into command
        for (ProcessInstance processInstance : needFailoverProcessInstanceList) {
            logger.info("failover process instance id: {} host:{}", processInstance.getId(), processInstance.getHost());
            if (Constants.NULL.equals(processInstance.getHost())) {
                continue;
            }
            processService.processNeedFailoverProcessInstances(processInstance);
        }

        logger.info("master failover end");
    }

    public void blockAcquireMutex() {
        registryCenter.getLock(getMasterLockPath());
    }

    public void releaseLock() {
        registryCenter.releaseLock(getMasterLockPath());
    }
}
