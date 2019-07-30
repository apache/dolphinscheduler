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
package cn.escheduler.server.zk;

import cn.escheduler.common.Constants;
import cn.escheduler.common.enums.ExecutionStatus;
import cn.escheduler.common.enums.ZKNodeType;
import cn.escheduler.common.utils.CollectionUtils;
import cn.escheduler.common.utils.DateUtils;
import cn.escheduler.common.utils.OSUtils;
import cn.escheduler.common.zk.AbstractZKClient;
import cn.escheduler.dao.AlertDao;
import cn.escheduler.dao.DaoFactory;
import cn.escheduler.dao.ProcessDao;
import cn.escheduler.dao.ServerDao;
import cn.escheduler.dao.model.ProcessInstance;
import cn.escheduler.dao.model.TaskInstance;
import cn.escheduler.dao.model.WorkerServer;
import cn.escheduler.server.ResInfo;
import cn.escheduler.server.utils.ProcessUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.utils.ThreadUtils;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadFactory;


/**
 *  zookeeper master client
 *
 *  single instance
 */
public class ZKMasterClient extends AbstractZKClient {

	private static final Logger logger = LoggerFactory.getLogger(ZKMasterClient.class);

	private static final ThreadFactory defaultThreadFactory = ThreadUtils.newGenericThreadFactory("Master-Main-Thread");

	/**
	 *  master znode
	 */
	private String masterZNode = null;

	/**
	 *  master database access
	 */
	private ServerDao serverDao = null;
	/**
	 *  alert database access
	 */
	private AlertDao alertDao = null;
	/**
	 *  flow database access
	 */
	private ProcessDao processDao;


	private Date createTime = null;

	/**
	 *  zkMasterClient
	 */
	private static ZKMasterClient zkMasterClient = null;


	private ZKMasterClient(ProcessDao processDao){
		this.processDao = processDao;
		init();
	}

	private ZKMasterClient(){}

	/**
	 *  get zkMasterClient
	 * @param processDao
	 * @return
	 */
	public static synchronized ZKMasterClient getZKMasterClient(ProcessDao processDao){
		if(zkMasterClient == null){
			zkMasterClient = new ZKMasterClient(processDao);
		}
		zkMasterClient.processDao = processDao;

		return zkMasterClient;
	}

	/**
	 *  init
	 */
	public void init(){
		// init dao
		this.initDao();

		InterProcessMutex mutex = null;
		try {
			// create distributed lock with the root node path of the lock space as /escheduler/lock/failover/master
			String znodeLock = getMasterStartUpLockPath();

			mutex = new InterProcessMutex(zkClient, znodeLock);
			mutex.acquire();

			// init system znode
			this.initSystemZNode();

			// monitor master
			this.listenerMaster();

			// monitor worker
			this.listenerWorker();

			// register master
			this.registMaster();

			// check if fault tolerance is required，failure and tolerance
			if (getActiveMasterNum() == 1) {
				failoverWorker(null, true);
//				processDao.masterStartupFaultTolerant();
				failoverMaster(null);
			}

		}catch (Exception e){
			logger.error("master start up  exception : " + e.getMessage(),e);
		}finally {
			if (mutex != null){
				try {
					mutex.release();
				} catch (Exception e) {
					if(e.getMessage().equals("instance must be started before calling this method")){
						logger.warn("lock release");
					}else{
						logger.error("lock release failed : " + e.getMessage(),e);
					}

				}
			}
		}
	}


	/**
	 *  init dao
	 */
	public void initDao(){
		this.serverDao = DaoFactory.getDaoInstance(ServerDao.class);
		this.alertDao = DaoFactory.getDaoInstance(AlertDao.class);
		this.processDao = DaoFactory.getDaoInstance(ProcessDao.class);
	}

	/**
	 *  get maste dao
	 * @return
	 */
	public ServerDao getServerDao(){
		return serverDao;
	}

	/**
	 * get alert dao
	 * @return
	 */
	public AlertDao getAlertDao() {
		return alertDao;
	}

	/**
	 *  register master znode
	 */
	public void registMaster(){

		// get current date
		Date now = new Date();
		createTime = now ;
		try {
			String osHost = OSUtils.getHost();

			// zookeeper node exists, cannot start a new one.
			if(checkZKNodeExists(osHost, ZKNodeType.MASTER)){
				logger.error("register failure , master already started on host : {}" , osHost);
				// exit system
				System.exit(-1);
			}

			// specify the format of stored data in ZK nodes
			String heartbeatZKInfo = ResInfo.getHeartBeatInfo(now);
			// create temporary sequence nodes for master znode
			masterZNode = zkClient.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(
					masterZNodeParentPath + "/" + OSUtils.getHost() + "_", heartbeatZKInfo.getBytes());

			logger.info("register master node {} success" , masterZNode);

			// handle dead server
			handleDeadServer(masterZNode, Constants.MASTER_PREFIX, Constants.DELETE_ZK_OP);

			// delete master server from database
			serverDao.deleteMaster(OSUtils.getHost());

			// register master znode
			serverDao.registerMaster(OSUtils.getHost(),
					OSUtils.getProcessID(),
					masterZNode,
					ResInfo.getResInfoJson(),
					createTime,
					createTime);

		} catch (Exception e) {
			logger.error("register master failure : "  + e.getMessage(),e);
		}
	}


	/**
	 * check the zookeeper node already exists
	 * @param host
	 * @param zkNodeType
	 * @return
	 * @throws Exception
	 */
	private boolean checkZKNodeExists(String host, ZKNodeType zkNodeType) throws Exception {

		String path = null;
		switch (zkNodeType){
			case MASTER:
				path = masterZNodeParentPath;
				break;
			case WORKER:
				path = workerZNodeParentPath;
				break;
			case DEAD_SERVER:
				path = deadServerZNodeParentPath;
				break;
			default:
				break;
		}
		if(StringUtils.isEmpty(path)){
			logger.error("check zk node exists error, host:{}, zk node type:{}", host, zkNodeType.toString());
			return false;
		}

		List<String> serverList = null;
        serverList = zkClient.getChildren().forPath(path);
		if (CollectionUtils.isNotEmpty(serverList)){
            for (String masterZNode : serverList){
                if (masterZNode.startsWith(host)){
                    return true;
                }
                }
            }
        return false;
	}

	/**
	 *  monitor master
	 */
	public void listenerMaster(){
		PathChildrenCache masterPc = new PathChildrenCache(zkClient, masterZNodeParentPath, true ,defaultThreadFactory);

		try {
			Date now = new Date();
			createTime = now ;
			masterPc.start();
			masterPc.getListenable().addListener(new PathChildrenCacheListener() {
				@Override
				public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
					switch (event.getType()) {
						case CHILD_ADDED:
							logger.info("master node added : {}",event.getData().getPath());
							break;
						case CHILD_REMOVED:
							String path = event.getData().getPath();
							logger.info("master node deleted : {}",event.getData().getPath());

							InterProcessMutex mutexLock = null;
							try {
								if(masterZNode.equals(path)){
									logger.error("master server({}) of myself dead , stopping...", path);
									stoppable.stop(String.format("master server(%s) of myself dead , stopping...", path));
									break;
								}

								// create a distributed lock, and the root node path of the lock space is /escheduler/lock/failover/master
								String znodeLock = zkMasterClient.getMasterFailoverLockPath();
								mutexLock = new InterProcessMutex(zkMasterClient.getZkClient(), znodeLock);
								mutexLock.acquire();

								// handle dead server, add to zk dead server pth
								handleDeadServer(path, Constants.MASTER_PREFIX, Constants.ADD_ZK_OP);

								String masterHost = getHostByEventDataPath(path);
								for (int i = 0; i < Constants.ESCHEDULER_WARN_TIMES_FAILOVER;i++) {
									alertDao.sendServerStopedAlert(1, masterHost, "Master-Server");
								}
								if(StringUtils.isNotEmpty(masterHost)){
									failoverMaster(masterHost);
								}
							}catch (Exception e){
								logger.error("master failover failed : " + e.getMessage(),e);
							}finally {
								if (mutexLock != null){
									try {
										mutexLock.release();
									} catch (Exception e) {
										logger.error("lock relase failed : " + e.getMessage(),e);
									}
								}
							}
							break;
						case CHILD_UPDATED:
							if (event.getData().getPath().contains(OSUtils.getHost())){
								byte[] bytes = zkClient.getData().forPath(event.getData().getPath());
								String resInfoStr = new String(bytes);
								String[] splits = resInfoStr.split(Constants.COMMA);
								if (splits.length != Constants.HEARTBEAT_FOR_ZOOKEEPER_INFO_LENGTH) {
									return;
								}
								// updateProcessInstance Master information in database according to host
								serverDao.updateMaster(OSUtils.getHost(),
										OSUtils.getProcessID(),
										ResInfo.getResInfoJson(Double.parseDouble(splits[2]),
												Double.parseDouble(splits[3])),
										DateUtils.stringToDate(splits[5]));

								logger.debug("master zk node updated : {}",event.getData().getPath());
							}
							break;
						default:
							break;
					}
				}
			});
		}catch (Exception e){
			logger.error("monitor master failed : " + e.getMessage(),e);
		}

	}



	/**
	 *  monitor worker
	 */
	public void listenerWorker(){

		PathChildrenCache workerPc = new PathChildrenCache(zkClient,workerZNodeParentPath,true ,defaultThreadFactory);
		try {
			Date now = new Date();
			createTime = now ;
			workerPc.start();
			workerPc.getListenable().addListener(new PathChildrenCacheListener() {
				@Override
				public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) {
					switch (event.getType()) {
						case CHILD_ADDED:
							logger.info("node added : {}" ,event.getData().getPath());
							break;
						case CHILD_REMOVED:
							String path = event.getData().getPath();

							logger.info("node deleted : {}",event.getData().getPath());

							InterProcessMutex mutex = null;
							try {
								// create a distributed lock, and the root node path of the lock space is /escheduler/lock/failover/worker
								String znodeLock = zkMasterClient.getWorkerFailoverLockPath();
								mutex = new InterProcessMutex(zkMasterClient.getZkClient(), znodeLock);
								mutex.acquire();

								// handle dead server
								handleDeadServer(path, Constants.WORKER_PREFIX, Constants.ADD_ZK_OP);

								String workerHost = getHostByEventDataPath(path);
								for (int i = 0; i < Constants.ESCHEDULER_WARN_TIMES_FAILOVER;i++) {
									alertDao.sendServerStopedAlert(1, workerHost, "Worker-Server");
								}

								if(StringUtils.isNotEmpty(workerHost)){
									failoverWorker(workerHost, true);
                                }
							}catch (Exception e){
								logger.error("worker failover failed : " + e.getMessage(),e);
							}
							finally {
								if (mutex != null){
									try {
										mutex.release();
									} catch (Exception e) {
										logger.error("lock relase failed : " + e.getMessage(),e);
									}
								}
							}
							break;
						default:
							break;
					}
				}
			});
		}catch (Exception e){
			logger.error("listener worker failed : " + e.getMessage(),e);
		}

	}

	/**
	 *  get master znode
	 * @return
	 */
	public String getMasterZNode() {
		return masterZNode;
	}




	/**
	 * task needs failover if task start before worker starts
     *
	 * @param taskInstance
	 * @return
	 */
	private boolean checkTaskInstanceNeedFailover(TaskInstance taskInstance) throws Exception {

		boolean taskNeedFailover = true;

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
	 * @param taskInstance
	 * @return
	 */
	private boolean checkTaskAfterWorkerStart(TaskInstance taskInstance) {
	    Date workerServerStartDate = null;
		List<WorkerServer> workerServers = processDao.queryWorkerServerByHost(taskInstance.getHost());
		if(workerServers.size() > 0){
		    workerServerStartDate = workerServers.get(0).getCreateTime();
		}

		if(workerServerStartDate != null){
			return taskInstance.getStartTime().after(workerServerStartDate);

		}else{
			return false;
		}
	}

	/**
	 * failover worker tasks
	 * 1. kill yarn job if there are yarn jobs in tasks.
	 * 2. change task state from running to need failover.
	 * @param workerHost
	 */
	private void failoverWorker(String workerHost, boolean needCheckWorkerAlive) throws Exception {
		logger.info("start worker[{}] failover ...", workerHost);

		List<TaskInstance> needFailoverTaskInstanceList = processDao.queryNeedFailoverTaskInstances(workerHost);
		for(TaskInstance taskInstance : needFailoverTaskInstanceList){
			if(needCheckWorkerAlive){
				if(!checkTaskInstanceNeedFailover(taskInstance)){
					continue;
                }
			}

			ProcessInstance instance = processDao.findProcessInstanceDetailById(taskInstance.getProcessInstanceId());
			if(instance!=null){
				taskInstance.setProcessInstance(instance);
			}
			// only kill yarn job if exists , the local thread has exited
			ProcessUtils.killYarnJob(taskInstance);

			taskInstance.setState(ExecutionStatus.NEED_FAULT_TOLERANCE);
			processDao.saveTaskInstance(taskInstance);
		}

		//update task Instance state value is NEED_FAULT_TOLERANCE
		// processDao.updateNeedFailoverTaskInstances(workerHost);
		logger.info("end worker[{}] failover ...", workerHost);
	}

	/**
	 * failover master tasks
	 * @param masterHost
	 */
	private void failoverMaster(String masterHost) {
		logger.info("start master failover ...");

		List<ProcessInstance> needFailoverProcessInstanceList = processDao.queryNeedFailoverProcessInstances(masterHost);

		//updateProcessInstance host is null and insert into command
		for(ProcessInstance processInstance : needFailoverProcessInstanceList){
			processDao.processNeedFailoverProcessInstances(processInstance);
		}

		logger.info("master failover end");
	}

	/**
	 *  get host ip, string format: masterParentPath/ip_000001/value
	 * @param path
	 * @return
	 */
	private String getHostByEventDataPath(String path) {
		int  startIndex = path.lastIndexOf("/")+1;
		int endIndex = 	path.lastIndexOf("_");

		if(startIndex >= endIndex){
			logger.error("parse ip error");
			return "";
		}
		return path.substring(startIndex, endIndex);
	}





}
