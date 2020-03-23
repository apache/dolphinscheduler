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

import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ExecutionStatus;
import org.apache.dolphinscheduler.common.enums.ZKNodeType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.dao.AlertDao;
import org.apache.dolphinscheduler.dao.DaoFactory;
import org.apache.dolphinscheduler.dao.entity.ProcessInstance;
import org.apache.dolphinscheduler.dao.entity.TaskInstance;
import org.apache.dolphinscheduler.server.utils.ProcessUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.utils.ThreadUtils;
import org.apache.dolphinscheduler.service.process.ProcessService;
import org.apache.dolphinscheduler.service.zk.AbstractZKClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadFactory;


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
	 * thread factory
	 */
	private static final ThreadFactory defaultThreadFactory = ThreadUtils.newGenericThreadFactory("Master-Main-Thread");

	/**
	 *  master znode
	 */
	private String masterZNode = null;

	/**
	 *  alert database access
	 */
	private AlertDao alertDao = null;
	/**
	 *  process service
	 */
	@Autowired
	private ProcessService processService;

	/**
	 * default constructor
	 */
	private ZKMasterClient(){}

	/**
	 * init
	 */
	public void init(){

		logger.info("initialize master client...");

		// init dao
		this.initDao();

		InterProcessMutex mutex = null;
		try {
			// create distributed lock with the root node path of the lock space as /dolphinscheduler/lock/failover/master
			String znodeLock = getMasterStartUpLockPath();
			mutex = new InterProcessMutex(zkClient, znodeLock);
			mutex.acquire();

			// init system znode
			this.initSystemZNode();

			// register master
			this.registerMaster();

			// check if fault tolerance is required，failure and tolerance
			if (getActiveMasterNum() == 1) {
				failoverWorker(null, true);
				failoverMaster(null);
			}

		}catch (Exception e){
			logger.error("master start up  exception",e);
		}finally {
			releaseMutex(mutex);
		}
	}


	/**
	 *  init dao
	 */
	public void initDao(){
		this.alertDao = DaoFactory.getDaoInstance(AlertDao.class);
	}
	/**
	 * get alert dao
	 *
	 * @return AlertDao
	 */
	public AlertDao getAlertDao() {
		return alertDao;
	}




	/**
	 *  register master znode
	 */
	public void registerMaster(){
		try {
		    String serverPath = registerServer(ZKNodeType.MASTER);
		    if(StringUtils.isEmpty(serverPath)){
		    	System.exit(-1);
			}
			masterZNode = serverPath;
		} catch (Exception e) {
			logger.error("register master failure ",e);
			System.exit(-1);
		}
	}

	/**
	 * handle path events that this class cares about
	 * @param client   zkClient
	 * @param event	   path event
	 * @param path     zk path
	 */
	@Override
	protected void dataChanged(CuratorFramework client, TreeCacheEvent event, String path) {
		if(path.startsWith(getZNodeParentPath(ZKNodeType.MASTER)+Constants.SINGLE_SLASH)){  //monitor master
			handleMasterEvent(event,path);

		}else if(path.startsWith(getZNodeParentPath(ZKNodeType.WORKER)+Constants.SINGLE_SLASH)){  //monitor worker
			handleWorkerEvent(event,path);
		}
		//other path event, ignore
	}

	/**
	 * remove zookeeper node path
	 *
	 * @param path			zookeeper node path
	 * @param zkNodeType	zookeeper node type
	 * @param failover		is failover
	 */
	private void removeZKNodePath(String path, ZKNodeType zkNodeType, boolean failover) {
		logger.info("{} node deleted : {}", zkNodeType, path);
		InterProcessMutex mutex = null;
		try {
			String failoverPath = getFailoverLockPath(zkNodeType);
			// create a distributed lock
			mutex = new InterProcessMutex(getZkClient(), failoverPath);
			mutex.acquire();

			String serverHost = getHostByEventDataPath(path);
			// handle dead server
			handleDeadServer(path, zkNodeType, Constants.ADD_ZK_OP);
			//alert server down.
			alertServerDown(serverHost, zkNodeType);
			//failover server
			if(failover){
				failoverServerWhenDown(serverHost, zkNodeType);
			}
		}catch (Exception e){
			logger.error("{} server failover failed.", zkNodeType);
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
	    if(StringUtils.isEmpty(serverHost)){
	    	return ;
		}
		switch (zkNodeType){
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
	 * send alert when server down
	 *
	 * @param serverHost	server host
	 * @param zkNodeType	zookeeper node type
	 */
	private void alertServerDown(String serverHost, ZKNodeType zkNodeType) {

		String serverType = zkNodeType.toString();
		alertDao.sendServerStopedAlert(1, serverHost, serverType);
	}

	/**
	 * monitor master
	 */
	public void handleMasterEvent(TreeCacheEvent event, String path){
		switch (event.getType()) {
			case NODE_ADDED:
				logger.info("master node added : {}", path);
				break;
			case NODE_REMOVED:
				String serverHost = getHostByEventDataPath(path);
				if (checkServerSelfDead(serverHost, ZKNodeType.MASTER)) {
					return;
				}
				removeZKNodePath(path, ZKNodeType.MASTER, true);
				break;
			default:
				break;
		}
	}

	/**
	 * monitor worker
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
	 * get master znode
	 *
	 * @return master zookeeper node
	 */
	public String getMasterZNode() {
		return masterZNode;
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
	    	if(workerServer.getHost().equals(taskInstance.getHost())){
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
			if(needCheckWorkerAlive && !checkTaskInstanceNeedFailover(taskInstance)){
				continue;
			}

			ProcessInstance instance = processService.findProcessInstanceDetailById(taskInstance.getProcessInstanceId());
			if(instance!=null){
				taskInstance.setProcessInstance(instance);
			}
			// only kill yarn job if exists , the local thread has exited
			ProcessUtils.killYarnJob(taskInstance);

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

		//updateProcessInstance host is null and insert into command
		for(ProcessInstance processInstance : needFailoverProcessInstanceList){
			processService.processNeedFailoverProcessInstances(processInstance);
		}

		logger.info("master failover end");
	}

}
