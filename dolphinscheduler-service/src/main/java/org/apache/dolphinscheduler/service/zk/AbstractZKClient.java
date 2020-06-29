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
package org.apache.dolphinscheduler.service.zk;

import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ZKNodeType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.utils.ResInfo;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;

import static org.apache.dolphinscheduler.common.Constants.*;

/**
 * abstract zookeeper client
 */
@Component
public abstract class AbstractZKClient extends ZookeeperCachedOperator {

	private static final Logger logger = LoggerFactory.getLogger(AbstractZKClient.class);


	/**
	 *  remove dead server by host
	 * @param host host
	 * @param serverType serverType
	 * @throws Exception
	 */
	public void removeDeadServerByHost(String host, String serverType) throws Exception {
		List<String> deadServers = super.getChildrenKeys(getDeadZNodeParentPath());
		for(String serverPath : deadServers){
			if(serverPath.startsWith(serverType+UNDERLINE+host)){
				String server = getDeadZNodeParentPath() + SINGLE_SLASH + serverPath;
				super.remove(server);
				logger.info("{} server {} deleted from zk dead server path success" , serverType , host);
			}
		}
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
		String host = getHostByEventDataPath(zNode);
		String type = (zkNodeType == ZKNodeType.MASTER) ? MASTER_PREFIX : WORKER_PREFIX;

		//check server restart, if restart , dead server path in zk should be delete
		if(opType.equals(DELETE_ZK_OP)){
			removeDeadServerByHost(host, type);

		}else if(opType.equals(ADD_ZK_OP)){
			String deadServerPath = getDeadZNodeParentPath() + SINGLE_SLASH + type + UNDERLINE + host;
			if(!super.isExisted(deadServerPath)){
				//add dead server info to zk dead server path : /dead-servers/

				super.persist(deadServerPath,(type + UNDERLINE + host));

				logger.info("{} server dead , and {} added to zk dead server path success" ,
						zkNodeType.toString(), zNode);
			}
		}

	}

	/**
	 * get active master num
	 * @return active master number
	 */
	public int getActiveMasterNum(){
		List<String> childrenList = new ArrayList<>();
		try {
			// read master node parent path from conf
			if(super.isExisted(getZNodeParentPath(ZKNodeType.MASTER))){
				childrenList = super.getChildrenKeys(getZNodeParentPath(ZKNodeType.MASTER));
			}
		} catch (Exception e) {
			logger.error("getActiveMasterNum error",e);
		}
		return childrenList.size();
	}

	/**
	 *
	 * @return zookeeper quorum
	 */
	public String getZookeeperQuorum(){
		return getZookeeperConfig().getServerList();
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
		for (Map.Entry<String, String> entry : masterMap.entrySet()) {
			Server masterServer = ResInfo.parseHeartbeatForZKInfo(entry.getValue());
			if(masterServer == null){
				continue;
			}
			String key = entry.getKey();
			masterServer.setZkDirectory(parentPath + "/"+ key);
			//set host and port
			String[] hostAndPort=key.split(COLON);
			String[] hosts=hostAndPort[0].split(DIVISION_STRING);
			// fetch the last one
			masterServer.setHost(hosts[hosts.length-1]);
			masterServer.setPort(Integer.parseInt(hostAndPort[1]));
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
			List<String> serverList  = super.getChildrenKeys(path);
			if(zkNodeType == ZKNodeType.WORKER){
			    List<String> workerList = new ArrayList<>();
			    for(String group : serverList){
			    	List<String> groupServers = super.getChildrenKeys(path + Constants.SLASH + group);
			    	for(String groupServer : groupServers){
			    		workerList.add(group + Constants.SLASH + groupServer);
					}
				}
				serverList = workerList;
			}
			for(String server : serverList){
				masterMap.putIfAbsent(server, super.get(path + Constants.SLASH + server));
			}
		} catch (Exception e) {
			logger.error("get server list failed", e);
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
			if(hostKey.contains(host)){
				return true;
			}
		}
		return false;
	}

	/**
	 *
	 * @return get worker node parent path
	 */
	protected String getWorkerZNodeParentPath(){
		return getZookeeperConfig().getDsRoot() + Constants.ZOOKEEPER_DOLPHINSCHEDULER_WORKERS;
	}

	/**
	 *
	 * @return get master node parent path
	 */
	protected String getMasterZNodeParentPath(){
		return getZookeeperConfig().getDsRoot() + Constants.ZOOKEEPER_DOLPHINSCHEDULER_MASTERS;
	}

	/**
	 *
	 * @return get master lock path
	 */
	public String getMasterLockPath(){
		return getZookeeperConfig().getDsRoot() + Constants.ZOOKEEPER_DOLPHINSCHEDULER_LOCK_MASTERS;
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
		return getZookeeperConfig().getDsRoot() + Constants.ZOOKEEPER_DOLPHINSCHEDULER_DEAD_SERVERS;
	}

	/**
	 *
	 * @return get master start up lock path
	 */
	public String getMasterStartUpLockPath(){
		return getZookeeperConfig().getDsRoot() + Constants.ZOOKEEPER_DOLPHINSCHEDULER_LOCK_FAILOVER_STARTUP_MASTERS;
	}

	/**
	 *
	 * @return get master failover lock path
	 */
	public String getMasterFailoverLockPath(){
		return getZookeeperConfig().getDsRoot() + Constants.ZOOKEEPER_DOLPHINSCHEDULER_LOCK_FAILOVER_MASTERS;
	}

	/**
	 *
	 * @return get worker failover lock path
	 */
	public String getWorkerFailoverLockPath(){
		return getZookeeperConfig().getDsRoot() + Constants.ZOOKEEPER_DOLPHINSCHEDULER_LOCK_FAILOVER_WORKERS;
	}

	/**
	 * release mutex
	 * @param mutex mutex
	 */
	public void releaseMutex(InterProcessMutex mutex) {
		if (mutex != null){
			try {
				mutex.release();
			} catch (Exception e) {
				if(e.getMessage().equals("instance must be started before calling this method")){
					logger.warn("lock release");
				}else{
					logger.error("lock release failed",e);
				}

			}
		}
	}

	/**
	 *  init system znode
	 */
	protected void initSystemZNode(){
		try {
			persist(getMasterZNodeParentPath(), "");
			persist(getWorkerZNodeParentPath(), "");
			persist(getDeadZNodeParentPath(), "");

			logger.info("initialize server nodes success.");
		} catch (Exception e) {
			logger.error("init system znode failed",e);
		}
	}

	/**
	 *  get host ip, string format: masterParentPath/ip
	 * @param path path
	 * @return host ip, string format: masterParentPath/ip
	 */
	protected String getHostByEventDataPath(String path) {
		if(StringUtils.isEmpty(path)){
			logger.error("empty path!");
			return "";
		}
		String[] pathArray = path.split(SINGLE_SLASH);
		if(pathArray.length < 1){
			logger.error("parse ip error: {}", path);
			return "";
		}
		return pathArray[pathArray.length - 1];

	}

	@Override
	public String toString() {
		return "AbstractZKClient{" +
				"zkClient=" + zkClient +
				", deadServerZNodeParentPath='" + getZNodeParentPath(ZKNodeType.DEAD_SERVER) + '\'' +
				", masterZNodeParentPath='" + getZNodeParentPath(ZKNodeType.MASTER) + '\'' +
				", workerZNodeParentPath='" + getZNodeParentPath(ZKNodeType.WORKER) + '\'' +
				'}';
	}
}