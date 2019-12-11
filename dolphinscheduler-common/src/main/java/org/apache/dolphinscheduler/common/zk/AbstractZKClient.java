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
package org.apache.dolphinscheduler.common.zk;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.common.enums.ZKNodeType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.utils.DateUtils;
import org.apache.dolphinscheduler.common.utils.OSUtils;
import org.apache.dolphinscheduler.common.utils.ResInfo;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.apache.dolphinscheduler.common.Constants.*;


/**
 * abstract zookeeper client
 */
public abstract class AbstractZKClient {

	private static final Logger logger = LoggerFactory.getLogger(AbstractZKClient.class);

	/**
	 *  load configuration file
	 */
	protected static Configuration conf;

	protected  CuratorFramework zkClient = null;

	/**
	 * server stop or not
	 */
	protected IStoppable stoppable = null;


	static {
		try {
			conf = new PropertiesConfiguration(Constants.ZOOKEEPER_PROPERTIES_PATH);
		}catch (ConfigurationException e){
			logger.error("load configuration failed : " + e.getMessage(),e);
			System.exit(1);
		}
	}


	public AbstractZKClient() {

		// retry strategy
		RetryPolicy retryPolicy = new ExponentialBackoffRetry(
				conf.getInt(Constants.ZOOKEEPER_RETRY_SLEEP),
				conf.getInt(Constants.ZOOKEEPER_RETRY_MAXTIME));

		try{
			// crate zookeeper client
			zkClient = CuratorFrameworkFactory.builder()
						.connectString(getZookeeperQuorum())
						.retryPolicy(retryPolicy)
						.sessionTimeoutMs(1000 * conf.getInt(Constants.ZOOKEEPER_SESSION_TIMEOUT))
						.connectionTimeoutMs(1000 * conf.getInt(Constants.ZOOKEEPER_CONNECTION_TIMEOUT))
						.build();

			zkClient.start();
			initStateLister();

		}catch(Exception e){
			logger.error("create zookeeper connect failed : " + e.getMessage(),e);
			System.exit(-1);
		}
    }

	/**
	 *
	 *  register status monitoring events for zookeeper clients
	 */
	public void initStateLister(){
		if(zkClient == null) {
			return;
		}
		// add ConnectionStateListener monitoring zookeeper  connection state
		ConnectionStateListener csLister = new ConnectionStateListener() {

			@Override
			public void stateChanged(CuratorFramework client, ConnectionState newState) {
				logger.info("state changed , current state : " + newState.name());
				/**
				 * probably session expired
				 */
				if(newState == ConnectionState.LOST){
					// if lost , then exit
					logger.info("current zookeepr connection state : connection lost ");
				}
			}
		};

		zkClient.getConnectionStateListenable().addListener(csLister);
	}


    public void start() {
    	zkClient.start();
		logger.info("zookeeper start ...");
    }

    public void close() {
		zkClient.getZookeeperClient().close();
		zkClient.close();
		logger.info("zookeeper close ...");
    }


	/**
	 *  heartbeat for zookeeper
	 * @param znode  zookeeper node
	 * @param serverType server type
	 */
	public void heartBeatForZk(String znode, String serverType){
		try {

			//check dead or not in zookeeper
			if(zkClient.getState() == CuratorFrameworkState.STOPPED || checkIsDeadServer(znode, serverType)){
				stoppable.stop("i was judged to death, release resources and stop myself");
				return;
			}

			byte[] bytes = zkClient.getData().forPath(znode);
			String resInfoStr = new String(bytes);
			String[] splits = resInfoStr.split(Constants.COMMA);
			if (splits.length != Constants.HEARTBEAT_FOR_ZOOKEEPER_INFO_LENGTH){
				return;
			}
			String str = splits[0] + Constants.COMMA
					+ splits[1] + Constants.COMMA
					+ OSUtils.cpuUsage() + Constants.COMMA
					+ OSUtils.memoryUsage() + Constants.COMMA
					+ OSUtils.loadAverage() + Constants.COMMA
					+ splits[5] + Constants.COMMA
					+ DateUtils.dateToString(new Date());
			zkClient.setData().forPath(znode,str.getBytes());

		} catch (Exception e) {
			logger.error("heartbeat for zk failed : " + e.getMessage(), e);
			stoppable.stop("heartbeat for zk exception, release resources and stop myself");
		}
	}

	/**
	 *	check dead server or not , if dead, stop self
	 *
	 * @param zNode   	 node path
	 * @param serverType master or worker prefix
	 * @return  true if not exists
	 * @throws Exception errors
	 */
	protected boolean checkIsDeadServer(String zNode, String serverType) throws Exception{
		//ip_sequenceno
		String[] zNodesPath = zNode.split("\\/");
		String ipSeqNo = zNodesPath[zNodesPath.length - 1];

		String type = serverType.equals(MASTER_PREFIX) ? MASTER_PREFIX : WORKER_PREFIX;
		String deadServerPath = getDeadZNodeParentPath() + SINGLE_SLASH + type + UNDERLINE + ipSeqNo;

		if(zkClient.checkExists().forPath(zNode) == null ||
				zkClient.checkExists().forPath(deadServerPath) != null ){
			return true;
		}


		return false;
	}


	public void removeDeadServerByHost(String host, String serverType) throws Exception {
        List<String> deadServers = zkClient.getChildren().forPath(getDeadZNodeParentPath());
        for(String serverPath : deadServers){
            if(serverPath.startsWith(serverType+UNDERLINE+host)){
				String server = getDeadZNodeParentPath() + SINGLE_SLASH + serverPath;
				if(zkClient.checkExists().forPath(server) != null){
					zkClient.delete().forPath(server);
					logger.info("{} server {} deleted from zk dead server path success" , serverType , host);
				}
            }
        }
	}


	/**
	 * create zookeeper path according the zk node type.
	 * @param zkNodeType zookeeper node type
	 * @return  register zookeeper path
	 * @throws Exception
	 */
	private String createZNodePath(ZKNodeType zkNodeType) throws Exception {
		// specify the format of stored data in ZK nodes
		String heartbeatZKInfo = ResInfo.getHeartBeatInfo(new Date());
		// create temporary sequence nodes for master znode
		String parentPath = getZNodeParentPath(zkNodeType);
		String serverPathPrefix = parentPath + "/" + OSUtils.getHost();
		String registerPath = zkClient.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(
				serverPathPrefix + "_", heartbeatZKInfo.getBytes());
		logger.info("register {} node {} success" , zkNodeType.toString(), registerPath);
		return registerPath;
	}

	/**
	 * register server,  if server already exists, return null.
	 * @param zkNodeType zookeeper node type
	 * @return register server path in zookeeper
	 * @throws Exception errors
	 */
	public String registerServer(ZKNodeType zkNodeType) throws Exception {
		String registerPath = null;
		String host = OSUtils.getHost();
		if(checkZKNodeExists(host, zkNodeType)){
			logger.error("register failure , {} server already started on host : {}" ,
					zkNodeType.toString(), host);
			return registerPath;
		}
		registerPath = createZNodePath(zkNodeType);

        // handle dead server
		handleDeadServer(registerPath, zkNodeType, Constants.DELETE_ZK_OP);

		return registerPath;
	}

	/**
	 * opType(add): if find dead server , then add to zk deadServerPath
	 * opType(delete): delete path from zk
	 *
	 * @param zNode   		  node path
	 * @param zkNodeType	  master or worker
	 * @param opType		  delete or add
	 * @throws Exception errors
	 */
	public void handleDeadServer(String zNode, ZKNodeType zkNodeType, String opType) throws Exception {
		//ip_sequenceno
		String[] zNodesPath = zNode.split("\\/");
		String ipSeqNo = zNodesPath[zNodesPath.length - 1];

		String type = (zkNodeType == ZKNodeType.MASTER) ? MASTER_PREFIX : WORKER_PREFIX;


		//check server restart, if restart , dead server path in zk should be delete
		if(opType.equals(DELETE_ZK_OP)){
			String[] ipAndSeqNo = ipSeqNo.split(UNDERLINE);
			String ip = ipAndSeqNo[0];
			removeDeadServerByHost(ip, type);

		}else if(opType.equals(ADD_ZK_OP)){
			String deadServerPath = getDeadZNodeParentPath() + SINGLE_SLASH + type + UNDERLINE + ipSeqNo;
			if(zkClient.checkExists().forPath(deadServerPath) == null){
				//add dead server info to zk dead server path : /dead-servers/

				zkClient.create().forPath(deadServerPath,(type + UNDERLINE + ipSeqNo).getBytes());

				logger.info("{} server dead , and {} added to zk dead server path success" ,
						zkNodeType.toString(), zNode);
			}
		}

	}



	/**
	 * for stop server
	 * @param serverStoppable server stoppable interface
	 */
	public void setStoppable(IStoppable serverStoppable){
		this.stoppable = serverStoppable;
	}

	/**
	 * get active master num
	 * @return active master number
	 */
	public int getActiveMasterNum(){
		List<String> childrenList = new ArrayList<>();
		try {
			// read master node parent path from conf
			if(zkClient.checkExists().forPath(getZNodeParentPath(ZKNodeType.MASTER)) != null){
				childrenList = zkClient.getChildren().forPath(getZNodeParentPath(ZKNodeType.MASTER));
			}
		} catch (Exception e) {
			if(e.getMessage().contains("java.lang.IllegalStateException: instance must be started")){
				logger.error("zookeeper service not started",e);
			}else{
				logger.error(e.getMessage(),e);
			}

		}finally {
			return childrenList.size();
		}
	}

	/**
	 *
	 * @return zookeeper quorum
	 */
	public static String getZookeeperQuorum(){
		StringBuilder sb = new StringBuilder();
		String[] zookeeperParamslist = conf.getStringArray(Constants.ZOOKEEPER_QUORUM);
		for (String param : zookeeperParamslist) {
			sb.append(param).append(Constants.COMMA);
		}

		if(sb.length() > 0){
			sb.deleteCharAt(sb.length() - 1);
		}

		return sb.toString();
	}

	/**
	 * get server list.
	 * @param zkNodeType zookeeper node type
	 * @return server list
	 */
	public List<Server> getServersList(ZKNodeType zkNodeType){
		Map<String, String> masterMap = getServerMaps(zkNodeType);
		String parentPath = getZNodeParentPath(zkNodeType);

		List<Server> masterServers = new ArrayList<>();
		int i = 0;
		for (Map.Entry<String, String> entry : masterMap.entrySet()) {
			Server masterServer = ResInfo.parseHeartbeatForZKInfo(entry.getValue());
			masterServer.setZkDirectory( parentPath + "/"+ entry.getKey());
			masterServer.setId(i);
			i ++;
			masterServers.add(masterServer);
		}
		return masterServers;
	}

	/**
	 * get master server list map.
	 * @param zkNodeType zookeeper node type
	 * @return result : {host : resource info}
	 */
	public Map<String, String> getServerMaps(ZKNodeType zkNodeType){

		Map<String, String> masterMap = new HashMap<>();
		try {
			String path =  getZNodeParentPath(zkNodeType);
			List<String> serverList  = getZkClient().getChildren().forPath(path);
			for(String server : serverList){
				byte[] bytes  = getZkClient().getData().forPath(path + "/" + server);
				masterMap.putIfAbsent(server, new String(bytes));
			}
		} catch (Exception e) {
			logger.error("get server list failed : " + e.getMessage(), e);
		}

		return masterMap;
	}

	/**
	 * check the zookeeper node already exists
	 * @param host host
	 * @param zkNodeType zookeeper node type
	 * @return true if exists
	 */
	public boolean checkZKNodeExists(String host, ZKNodeType zkNodeType) {
		String path = getZNodeParentPath(zkNodeType);
		if(StringUtils.isEmpty(path)){
			logger.error("check zk node exists error, host:{}, zk node type:{}",
					host, zkNodeType.toString());
			return false;
		}
		Map<String, String> serverMaps = getServerMaps(zkNodeType);
		for(String hostKey : serverMaps.keySet()){
			if(hostKey.startsWith(host)){
				return true;
			}
		}
		return false;
	}

	/**
	 *  get zkclient
	 * @return zookeeper client
	 */
	public  CuratorFramework getZkClient() {
		return zkClient;
	}

	/**
	 *
	 * @return get worker node parent path
	 */
	protected String getWorkerZNodeParentPath(){
		return conf.getString(Constants.ZOOKEEPER_DOLPHINSCHEDULER_WORKERS);
	}

	/**
	 *
	 * @return get master node parent path
	 */
	protected String getMasterZNodeParentPath(){
		return conf.getString(Constants.ZOOKEEPER_DOLPHINSCHEDULER_MASTERS);
	}

	/**
	 *
	 * @return get master lock path
	 */
	public String getMasterLockPath(){
		return conf.getString(Constants.ZOOKEEPER_DOLPHINSCHEDULER_LOCK_MASTERS);
	}

	/**
	 *
	 * @return get alert lock path
	 */
	public String getAlertLockPath(){
		return conf.getString(ZOOKEEPER_DOLPHINSCHEDULER_LOCK_ALERT);
	}

	/**
	 *
	 * @param zkNodeType zookeeper node type
	 * @return get zookeeper node parent path
	 */
	public String getZNodeParentPath(ZKNodeType zkNodeType) {
		String path = "";
		switch (zkNodeType){
			case MASTER:
				return getMasterZNodeParentPath();
			case WORKER:
				return getWorkerZNodeParentPath();
			case DEAD_SERVER:
				return getDeadZNodeParentPath();
			default:
				break;
		}
		return path;
	}

	/**
	 *
	 * @return get dead server node parent path
	 */
	protected String getDeadZNodeParentPath(){
		return conf.getString(ZOOKEEPER_DOLPHINSCHEDULER_DEAD_SERVERS);
	}

	/**
	 *
	 * @return get master start up lock path
	 */
	public String getMasterStartUpLockPath(){
		return conf.getString(Constants.ZOOKEEPER_DOLPHINSCHEDULER_LOCK_FAILOVER_STARTUP_MASTERS);
	}

	/**
	 *
	 * @return get master failover lock path
	 */
	public String getMasterFailoverLockPath(){
		return conf.getString(Constants.ZOOKEEPER_DOLPHINSCHEDULER_LOCK_FAILOVER_MASTERS);
	}

	/**
	 *
	 * @return get worker failover lock path
	 */
	public String getWorkerFailoverLockPath(){
		return conf.getString(Constants.ZOOKEEPER_DOLPHINSCHEDULER_LOCK_FAILOVER_WORKERS);
	}

	/**
	 * release mutex
	 * @param mutex mutex
	 */
	public static void releaseMutex(InterProcessMutex mutex) {
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

	/**
	 *  init system znode
	 */
	protected void initSystemZNode(){
		try {
			createNodePath(getMasterZNodeParentPath());
			createNodePath(getWorkerZNodeParentPath());
			createNodePath(getDeadZNodeParentPath());

		} catch (Exception e) {
			logger.error("init system znode failed : " + e.getMessage(),e);
		}
	}

	/**
	 * create zookeeper node path if not exists
	 * @param zNodeParentPath zookeeper parent path
	 * @throws Exception errors
	 */
	private void createNodePath(String zNodeParentPath) throws Exception {
	    if(null == zkClient.checkExists().forPath(zNodeParentPath)){
	        zkClient.create().creatingParentContainersIfNeeded()
					.withMode(CreateMode.PERSISTENT).forPath(zNodeParentPath);
		}
	}

	/**
	 * server self dead, stop all threads
	 * @param serverHost server host
	 * @param zkNodeType zookeeper node type
	 * @return true if server dead and stop all threads
	 */
	protected boolean checkServerSelfDead(String serverHost, ZKNodeType zkNodeType) {
		if (serverHost.equals(OSUtils.getHost())) {
			logger.error("{} server({}) of myself dead , stopping...",
					zkNodeType.toString(), serverHost);
			stoppable.stop(String.format(" {} server {} of myself dead , stopping...",
					zkNodeType.toString(), serverHost));
			return true;
		}
		return false;
	}

	/**
	 *  get host ip, string format: masterParentPath/ip_000001/value
	 * @param path path
	 * @return host ip, string format: masterParentPath/ip_000001/value
	 */
	protected String getHostByEventDataPath(String path) {
		int  startIndex = path.lastIndexOf("/")+1;
		int endIndex = 	path.lastIndexOf("_");

		if(startIndex >= endIndex){
			logger.error("parse ip error");
			return "";
		}
		return path.substring(startIndex, endIndex);
	}
	/**
	 * acquire zk lock
	 * @param zkClient zk client
	 * @param zNodeLockPath zk lock path
	 * @return zk lock
	 * @throws Exception errors
	 */
	public InterProcessMutex acquireZkLock(CuratorFramework zkClient,String zNodeLockPath)throws Exception{
		InterProcessMutex mutex = new InterProcessMutex(zkClient, zNodeLockPath);
		mutex.acquire();
		return mutex;
	}

	@Override
	public String toString() {
		return "AbstractZKClient{" +
				"zkClient=" + zkClient +
				", deadServerZNodeParentPath='" + getZNodeParentPath(ZKNodeType.DEAD_SERVER) + '\'' +
				", masterZNodeParentPath='" + getZNodeParentPath(ZKNodeType.MASTER) + '\'' +
				", workerZNodeParentPath='" + getZNodeParentPath(ZKNodeType.WORKER) + '\'' +
				", stoppable=" + stoppable +
				'}';
	}
}
