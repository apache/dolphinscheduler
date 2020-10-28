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
package org.apache.dolphinscheduler.server.zk;

import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.ZKNodeType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.thread.ThreadUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.builder.TaskExecutionContextBuilder;
import org.apache.dolphinscheduler.server.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.utils.ProcessUtils;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.zk.AbstractZKClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

import static org.apache.dolphinscheduler.common.Constants.*;


/**
 *  zookeeper master client
 *
 *  single instance
 */
@Component
public class ZKMasterClient extends AbstractZKClient {

	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(ZKMasterClient.class);

	/**
	 *  process service
	 */
	@Autowired
	private ProcessService processService;

	public void start() {

		InterProcessMutex mutex = null;
		try {
			// create distributed lock with the root node path of the lock space as /dolphinscheduler/lock/failover/master
			String znodeLock = getMasterStartUpLockPath();
			mutex = new InterProcessMutex(getZkClient(), znodeLock);
			mutex.acquire();

			// init system znode
			this.initSystemZNode();

			while (!checkZKNodeExists(OSUtils.getHost(), ZKNodeType.MASTER)){
				ThreadUtils.sleep(SLEEP_TIME_MILLIS);
			}
			// startup tolerant
			if (getActiveMasterNum() == 1) {
				removeZKNodePath(null, ZKNodeType.MASTER, true);
				removeZKNodePath(null, ZKNodeType.WORKER, true);
			}
			registerListener();
		}catch (Exception e){
			logger.error("master start up exception",e);
		}finally {
			releaseMutex(mutex);
		}
	}

	@Override
	public void close(){
		super.close();
	}

	/**
	 * handle path events that this class cares about
	 * @param client   zkClient
	 * @param event	   path event
	 * @param path     zk path
	 */
	@Override
	protected void dataChanged(CuratorFramework client, TreeCacheEvent event, String path) {
		//monitor master
		if(path.startsWith(getZNodeParentPath(ZKNodeType.MASTER)+Constants.SINGLE_SLASH)){
			handleMasterEvent(event,path);
		}else if(path.startsWith(getZNodeParentPath(ZKNodeType.WORKER)+Constants.SINGLE_SLASH)){
			//monitor worker
			handleWorkerEvent(event,path);
		}
	}

	/**
	 * remove zookeeper node path
	 *
	 * @param path			zookeeper node path
	 * @param zkNodeType	zookeeper node type
	 * @param failover		is failover
	 */
	private void removeZKNodePath(String path, ZKNodeType zkNodeType, boolean failover) {
		logger.info("{} node deleted : {}", zkNodeType.toString(), path);
		InterProcessMutex mutex = null;
		try {
			String failoverPath = getFailoverLockPath(zkNodeType);
			// create a distributed lock
			mutex = new InterProcessMutex(getZkClient(), failoverPath);
			mutex.acquire();

			String serverHost = null;
			if(StringUtils.isNotEmpty(path)){
				serverHost = getHostByEventDataPath(path);
				if(StringUtils.isEmpty(serverHost)){
					logger.error("server down error: unknown path: {}", path);
					return;
				}
				// handle dead server
				handleDeadServer(path, zkNodeType, Constants.ADD_ZK_OP);
			}
			//failover server
			if(failover){
				failoverServerWhenDown(serverHost, zkNodeType);
			}
		}catch (Exception e){
			logger.error("{} server failover failed.", zkNodeType.toString());
			logger.error("failover exception ",e);
		}
		finally {
			releaseMutex(mutex);
		}
	}

	/**
	 * failover server when server down
	 *
	 * @param serverHost	server host
	 * @param zkNodeType	zookeeper node type
	 * @throws Exception	exception
	 */
	private void failoverServerWhenDown(String serverHost, ZKNodeType zkNodeType) throws Exception {
		switch (zkNodeType) {
			case MASTER:
				failoverMaster(serverHost);
				break;
			case WORKER:
				failoverWorker(serverHost, true);
			default:
				break;
		}
	}

	/**
	 * get failover lock path
	 *
	 * @param zkNodeType zookeeper node type
	 * @return fail over lock path
	 */
	private String getFailoverLockPath(ZKNodeType zkNodeType){

		switch (zkNodeType){
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
	 * @param event event
	 * @param path path
	 */
	public void handleMasterEvent(TreeCacheEvent event, String path){
		switch (event.getType()) {
			case NODE_ADDED:
				logger.info("master node added : {}", path);
				break;
			case NODE_REMOVED:
				removeZKNodePath(path, ZKNodeType.MASTER, true);
				break;
			default:
				break;
		}
	}

	/**
	 * monitor worker
	 * @param event event
	 * @param path path
	 */
	public void handleWorkerEvent(TreeCacheEvent event, String path){
		switch (event.getType()) {
			case NODE_ADDED:
				logger.info("worker node added : {}", path);
				break;
			case NODE_REMOVED:
				logger.info("worker node deleted : {}", path);
				removeZKNodePath(path, ZKNodeType.WORKER, true);
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
	private boolean checkTaskInstanceNeedFailover(TaskInstance taskInstance) throws Exception {

		boolean taskNeedFailover = true;

		//now no host will execute this task instance,so no need to failover the task
		if(taskInstance.getHost() == null){
			return false;
		}

		// if the worker node exists in zookeeper, we must check the task starts after the worker
		if(checkZKNodeExists(taskInstance.getHost(), ZKNodeType.WORKER)){
			//if task start after worker starts, there is no need to failover the task.
			if(checkTaskAfterWorkerStart(taskInstance)){
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
		if(StringUtils.isEmpty(taskInstance.getHost())){
			return false;
		}
		Date workerServerStartDate = null;
		List<Server> workerServers = getServersList(ZKNodeType.WORKER);
		for(Server workerServer : workerServers){
			if(taskInstance.getHost().equals(workerServer.getHost() + Constants.COLON + workerServer.getPort())){
				workerServerStartDate = workerServer.getCreateTime();
				break;
			}
		}

		if(workerServerStartDate != null){
			return taskInstance.getStartTime().after(workerServerStartDate);
		}else{
			return false;
		}
	}

	/**
	 * failover worker tasks
	 *
	 * 1. kill yarn job if there are yarn jobs in tasks.
	 * 2. change task state from running to need failover.
	 * 3. failover all tasks when workerHost is null
	 * @param workerHost worker host
	 */

	/**
	 * failover worker tasks
	 *
	 * 1. kill yarn job if there are yarn jobs in tasks.
	 * 2. change task state from running to need failover.
	 * 3. failover all tasks when workerHost is null
	 * @param workerHost			worker host
	 * @param needCheckWorkerAlive	need check worker alive
	 * @throws Exception			exception
	 */
	private void failoverWorker(String workerHost, boolean needCheckWorkerAlive) throws Exception {
		logger.info("start worker[{}] failover ...", workerHost);

		List<TaskInstance> needFailoverTaskInstanceList = processService.queryNeedFailoverTaskInstances(workerHost);
		for(TaskInstance taskInstance : needFailoverTaskInstanceList){
			if(needCheckWorkerAlive){
				if(!checkTaskInstanceNeedFailover(taskInstance)){
					continue;
				}
			}

			ProcessInstance processInstance = processService.findProcessInstanceDetailById(taskInstance.getProcessInstanceId());
			if(processInstance != null){
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
		for(ProcessInstance processInstance : needFailoverProcessInstanceList){
			logger.info("failover process instance id: {} host:{}",
					processInstance.getId(), processInstance.getHost());
			if(Constants.NULL.equals(processInstance.getHost()) ){
				continue;
			}
			processService.processNeedFailoverProcessInstances(processInstance);
		}

		logger.info("master failover end");
	}

	public InterProcessMutex blockAcquireMutex() throws Exception {
		InterProcessMutex mutex = new InterProcessMutex(getZkClient(), getMasterLockPath());
		mutex.acquire();
		return mutex;
	}

}