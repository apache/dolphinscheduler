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
import cn.escheduler.common.utils.CollectionUtils;
import cn.escheduler.common.utils.DateUtils;
import cn.escheduler.common.utils.OSUtils;
import cn.escheduler.common.zk.AbstractZKClient;
import cn.escheduler.dao.DaoFactory;
import cn.escheduler.dao.ServerDao;
import cn.escheduler.server.ResInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.utils.ThreadUtils;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ThreadFactory;


/**
 *  zookeeper worker client
 *  single instance
 */
public class ZKWorkerClient extends AbstractZKClient {

	private static final Logger logger = LoggerFactory.getLogger(ZKWorkerClient.class);


	private static final ThreadFactory defaultThreadFactory = ThreadUtils.newGenericThreadFactory("Worker-Main-Thread");


	/**
	 *  worker znode
	 */
	private String workerZNode = null;

	/**
	 *  worker database access
	 */
	private ServerDao serverDao = null;

	/**
	 *  create time
	 */
	private Date createTime = null;

	/**
	 *  zkWorkerClient
	 */
	private static ZKWorkerClient zkWorkerClient = null;

	private ZKWorkerClient(){
		init();
	}

	/**
	 *  init
	 */
	private void init(){
		// init worker dao
		serverDao = DaoFactory.getDaoInstance(ServerDao.class);

		// init system znode
		this.initSystemZNode();

		// monitor worker
		this.listenerWorker();

		// register worker
		this.registWorker();
	}


	/**
	 * get zkWorkerClient
	 *
	 * @return
	 */
	public static synchronized ZKWorkerClient  getZKWorkerClient(){
		if(zkWorkerClient == null){
			zkWorkerClient = new ZKWorkerClient();
		}

		return zkWorkerClient;
	}

	/**
	 *  get worker dao
	 * @return
	 */
	public ServerDao getServerDao(){
		return serverDao;
	}


	public String initWorkZNode() throws Exception {

		String heartbeatZKInfo = ResInfo.getHeartBeatInfo(new Date());

		workerZNode = workerZNodeParentPath + "/" + OSUtils.getHost() + "_";

		workerZNode = zkClient.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(workerZNode,
				heartbeatZKInfo.getBytes());
		logger.info("register worker node {} success", workerZNode);
		return workerZNode;
	}

	/**
	 *  register worker
	 */
	private void registWorker(){

		// get current date
		Date now = new Date();
		createTime = now ;
		try {

			// encapsulation worker znnode
			workerZNode = workerZNodeParentPath + "/" + OSUtils.getHost() + "_";
			List<String> workerZNodeList = zkClient.getChildren().forPath(workerZNodeParentPath);

			if (CollectionUtils.isNotEmpty(workerZNodeList)){
				boolean flag = false;
				for (String workerZNode : workerZNodeList){
					if (workerZNode.startsWith(OSUtils.getHost())){
						flag = true;
						break;
					}
				}

				if (flag){
					logger.info("register failure , worker already started on : {}, please wait for a moment and try again" , OSUtils.getHost());
					// exit system
					System.exit(-1);
				}
			}

//			String heartbeatZKInfo = getOsInfo(now);
//			workerZNode = zkClient.create().withMode(CreateMode.EPHEMERAL_SEQUENTIAL).forPath(workerZNode,
//					heartbeatZKInfo.getBytes());

            initWorkZNode();
			// handle dead server
			handleDeadServer(workerZNode, Constants.WORKER_PREFIX, Constants.DELETE_ZK_OP);

			// delete worker server from database
			serverDao.deleteWorker(OSUtils.getHost());

			// register worker znode
			serverDao.registerWorker(OSUtils.getHost(),
					OSUtils.getProcessID(),
					workerZNode,
					ResInfo.getResInfoJson(),
					createTime,
					createTime);
		} catch (Exception e) {
			logger.error("register worker failure : "  + e.getMessage(),e);
		}
	}
	
	/**
	 *  monitor worker
	 */
	private void listenerWorker(){
		PathChildrenCache workerPc = new PathChildrenCache(zkClient, workerZNodeParentPath, true, defaultThreadFactory);
		try {

			Date now = new Date();
			createTime = now ;
			workerPc.start();
			workerPc.getListenable().addListener(new PathChildrenCacheListener() {
				@Override
				public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
					switch (event.getType()) {
						case CHILD_ADDED:
							logger.info("node added : {}" ,event.getData().getPath());
							break;
						case CHILD_REMOVED:
                            String path = event.getData().getPath();

							// handle dead server, add to zk dead server path
							handleDeadServer(path, Constants.WORKER_PREFIX, Constants.ADD_ZK_OP);

							//find myself dead
                            if(workerZNode.equals(path)){

                                logger.warn(" worker server({}) of myself dead , stopping...", path);
                                stoppable.stop(String.format("worker server(%s) of myself dead , stopping",path));
                            }
							logger.info("node deleted : {}", event.getData().getPath());
							break;
						case CHILD_UPDATED:
							if (event.getData().getPath().contains(OSUtils.getHost())){
								byte[] bytes = zkClient.getData().forPath(event.getData().getPath());
								String resInfoStr = new String(bytes);
								String[] splits = resInfoStr.split(Constants.COMMA);
								if (splits.length != Constants.HEARTBEAT_FOR_ZOOKEEPER_INFO_LENGTH) {
									return;
								}

								// updateProcessInstance master info in database according to host
								serverDao.updateWorker(OSUtils.getHost(),
										OSUtils.getProcessID(),
										ResInfo.getResInfoJson(Double.parseDouble(splits[2])
												,Double.parseDouble(splits[3])),
										DateUtils.stringToDate(splits[5]));
								logger.debug("node updated : {}",event.getData().getPath());
							}
							break;
						default:
							break;
					}
				}
			});
		}catch (Exception e){
			logger.error("monitor worker failed : " + e.getMessage(),e);
		}

	}

	/**
	 * get worker znode
	 * @return
	 */
	public String getWorkerZNode() {
		return workerZNode;
	}

	/**
	 *  get worker lock path
	 * @return
	 */
	public String getWorkerLockPath(){
		return conf.getString(Constants.ZOOKEEPER_ESCHEDULER_LOCK_WORKERS);
	}


}
