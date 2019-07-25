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
package cn.escheduler.common.zk;

import cn.escheduler.common.Constants;
import cn.escheduler.common.IStoppable;
import cn.escheduler.common.enums.ZKNodeType;
import cn.escheduler.common.model.MasterServer;
import cn.escheduler.common.utils.CollectionUtils;
import cn.escheduler.common.utils.DateUtils;
import cn.escheduler.common.utils.OSUtils;
import cn.escheduler.common.utils.ResInfo;
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

import static cn.escheduler.common.Constants.*;


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
	 *  server node parent path
	 */
	protected String deadServerZNodeParentPath = null;

	/**
	 *  master node parent path
	 */
	protected String masterZNodeParentPath = null;

	/**
	 *  worker node parent path
	 */
	protected String workerZNodeParentPath = null;

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
				Integer.parseInt(conf.getString(Constants.ZOOKEEPER_RETRY_SLEEP)),
				Integer.parseInt(conf.getString(Constants.ZOOKEEPER_RETRY_MAXTIME)));

		try{
			// crate zookeeper client
			zkClient = CuratorFrameworkFactory.builder()
						.connectString(getZookeeperQuorum())
						.retryPolicy(retryPolicy)
						.sessionTimeoutMs(1000 * Integer.parseInt(conf.getString(Constants.ZOOKEEPER_SESSION_TIMEOUT)))
						.connectionTimeoutMs(1000 * Integer.parseInt(conf.getString(Constants.ZOOKEEPER_CONNECTION_TIMEOUT)))
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
	 * @param znode
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
			String str = splits[0] + Constants.COMMA +splits[1] + Constants.COMMA
					+ OSUtils.cpuUsage() + Constants.COMMA
					+ OSUtils.memoryUsage() + Constants.COMMA
					+ splits[4] + Constants.COMMA
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
	 * @param zNode   		  node path
	 * @param serverType	  master or worker prefix
	 * @throws Exception
	 */
	protected boolean checkIsDeadServer(String zNode, String serverType) throws Exception {
		//ip_sequenceno
		String[] zNodesPath = zNode.split("\\/");
		String ipSeqNo = zNodesPath[zNodesPath.length - 1];

		String type = serverType.equals(MASTER_PREFIX) ? MASTER_PREFIX : WORKER_PREFIX;
		String deadServerPath = deadServerZNodeParentPath + SINGLE_SLASH + type + UNDERLINE + ipSeqNo;

		if(zkClient.checkExists().forPath(zNode) == null ||
				zkClient.checkExists().forPath(deadServerPath) != null ){
			return true;
		}


		return false;
	}


	public void removeDeadServerByHost(String host, String serverType) throws Exception {
        List<String> deadServers = zkClient.getChildren().forPath(deadServerZNodeParentPath);
        for(String serverPath : deadServers){
            if(serverPath.startsWith(serverType+UNDERLINE+host)){
				String server = deadServerZNodeParentPath + SINGLE_SLASH + serverPath;
				zkClient.delete().forPath(server);
                logger.info("{} server {} deleted from zk dead server path success" , serverType , host);
            }
        }
	}

	/**
	 * opType(add): if find dead server , then add to zk deadServerPath
	 * opType(delete): delete path from zk
	 *
	 * @param zNode   		  node path
	 * @param serverType	  master or worker prefix
	 * @param opType		  delete or add
	 * @throws Exception
	 */
	public void handleDeadServer(String zNode, String serverType, String opType) throws Exception {
		//ip_sequenceno
		String[] zNodesPath = zNode.split("\\/");
		String ipSeqNo = zNodesPath[zNodesPath.length - 1];

		String type = serverType.equals(MASTER_PREFIX) ? MASTER_PREFIX : WORKER_PREFIX;


		//check server restart, if restart , dead server path in zk should be delete
		if(opType.equals(DELETE_ZK_OP)){
			String[] ipAndSeqNo = ipSeqNo.split(UNDERLINE);
			String ip = ipAndSeqNo[0];
			removeDeadServerByHost(ip, serverType);

		}else if(opType.equals(ADD_ZK_OP)){
			String deadServerPath = deadServerZNodeParentPath + SINGLE_SLASH + type + UNDERLINE + ipSeqNo;
			if(zkClient.checkExists().forPath(deadServerPath) == null){
				//add dead server info to zk dead server path : /dead-servers/

				zkClient.create().forPath(deadServerPath,(type + UNDERLINE + ipSeqNo).getBytes());

				logger.info("{} server dead , and {} added to zk dead server path success" , serverType, zNode);
			}
		}

	}



	/**
	 * for stop server
	 * @param serverStoppable
	 */
	public void setStoppable(IStoppable serverStoppable){
		this.stoppable = serverStoppable;
	}

	/**
	 * get active master num
	 * @return
	 */
	public int getActiveMasterNum(){
		List<String> childrenList = new ArrayList<>();
		try {
			// read master node parent path from conf
			masterZNodeParentPath = conf.getString(Constants.ZOOKEEPER_ESCHEDULER_MASTERS);
			if(zkClient.checkExists().forPath(masterZNodeParentPath) != null){
				childrenList = zkClient.getChildren().forPath(masterZNodeParentPath);
			}
		} catch (Exception e) {
			if(!e.getMessage().contains("java.lang.IllegalStateException: instance must be started")){
				logger.warn(e.getMessage(),e);
			}

			return childrenList.size();
		}
		return childrenList.size();
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
	 * @param zkNodeType
	 * @return
	 */
	public List<MasterServer> getServers(ZKNodeType zkNodeType){
		Map<String, String> masterMap = getServerList(zkNodeType);
		String parentPath = getZNodeParentPath(zkNodeType);

		List<MasterServer> masterServers = new ArrayList<>();
		for(String path : masterMap.keySet()){
			MasterServer masterServer = ResInfo.parseHeartbeatForZKInfo(masterMap.get(path));
			masterServer.setZkDirectory( parentPath + "/"+ path);
			masterServers.add(masterServer);
		}
		return masterServers;
	}

	/**
	 * get master server list map.
	 * result : {host : resource info}
	 * @return
	 */
	public Map<String, String> getServerList(ZKNodeType zkNodeType){

		Map<String, String> masterMap = new HashMap<>();
		try {
			String path =  getZNodeParentPath(zkNodeType);
			List<String> serverList  = getZkClient().getChildren().forPath(path);
			for(String server : serverList){
				byte[] bytes  = getZkClient().getData().forPath(path + "/" + server);
				masterMap.putIfAbsent(server, new String(bytes));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return masterMap;
	}

	/**
	 * check the zookeeper node already exists
	 * @param host
	 * @param zkNodeType
	 * @return
	 * @throws Exception
	 */
	public boolean checkZKNodeExists(String host, ZKNodeType zkNodeType) {
		String path = getZNodeParentPath(zkNodeType);
		if(StringUtils.isEmpty(path)){
			logger.error("check zk node exists error, host:{}, zk node type:{}",
					host, zkNodeType.toString());
			return false;
		}
		Map<String, String> serverMaps = getServerList(zkNodeType);
		for(String hostKey : serverMaps.keySet()){
			if(hostKey.startsWith(host)){
				return true;
			}
		}
		return false;
	}

	/**
	 *  get zkclient
	 * @return
	 */
	public  CuratorFramework getZkClient() {
		return zkClient;
	}

	/**
	 * get worker node parent path
	 * @return
	 */
	protected String getWorkerZNodeParentPath(){return conf.getString(Constants.ZOOKEEPER_ESCHEDULER_WORKERS);};

	/**
	 * get master node parent path
	 * @return
	 */
	protected String getMasterZNodeParentPath(){return conf.getString(Constants.ZOOKEEPER_ESCHEDULER_MASTERS);}

	/**
	 *  get master lock path
	 * @return
	 */
	public String getMasterLockPath(){
		return conf.getString(Constants.ZOOKEEPER_ESCHEDULER_LOCK_MASTERS);
	}

	/**
	 * get zookeeper node parent path
	 * @param zkNodeType
	 * @return
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
	 * get dead server node parent path
	 * @return
	 */
	protected String getDeadZNodeParentPath(){
		return conf.getString(ZOOKEEPER_ESCHEDULER_DEAD_SERVERS);
	}

	/**
	 *  get master start up lock path
	 * @return
	 */
	public String getMasterStartUpLockPath(){
		return conf.getString(Constants.ZOOKEEPER_ESCHEDULER_LOCK_FAILOVER_STARTUP_MASTERS);
	}

	/**
	 *  get master failover lock path
	 * @return
	 */
	public String getMasterFailoverLockPath(){
		return conf.getString(Constants.ZOOKEEPER_ESCHEDULER_LOCK_FAILOVER_MASTERS);
	}

	/**
	 * get worker failover lock path
	 * @return
	 */
	public String getWorkerFailoverLockPath(){
		return conf.getString(Constants.ZOOKEEPER_ESCHEDULER_LOCK_FAILOVER_WORKERS);
	}

	/**
	 * release mutex
	 * @param mutex
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
	 * @param zNodeParentPath
	 * @throws Exception
	 */
	private void createNodePath(String zNodeParentPath) throws Exception {
	    if(null == zkClient.checkExists().forPath(zNodeParentPath)){
	        zkClient.create().creatingParentContainersIfNeeded()
					.withMode(CreateMode.PERSISTENT).forPath(zNodeParentPath);
		}
	}

	@Override
	public String toString() {
		return "AbstractZKClient{" +
				"zkClient=" + zkClient +
				", deadServerZNodeParentPath='" + deadServerZNodeParentPath + '\'' +
				", masterZNodeParentPath='" + masterZNodeParentPath + '\'' +
				", workerZNodeParentPath='" + workerZNodeParentPath + '\'' +
				", stoppable=" + stoppable +
				'}';
	}
}
