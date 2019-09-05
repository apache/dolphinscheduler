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
import cn.escheduler.common.enums.ZKNodeType;
import cn.escheduler.common.zk.AbstractZKClient;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.utils.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
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
	 *  register worker
	 */
	private void registWorker(){
		try {
			String serverPath = registerServer(ZKNodeType.WORKER);
			if(StringUtils.isEmpty(serverPath)){
				System.exit(-1);
			}
			workerZNode = serverPath;
		} catch (Exception e) {
			logger.error("register worker failure : "  + e.getMessage(),e);
			System.exit(-1);
		}
	}
	
	/**
	 *  monitor worker
	 */
	private void listenerWorker(){
		PathChildrenCache workerPc = new PathChildrenCache(zkClient, getZNodeParentPath(ZKNodeType.WORKER), true, defaultThreadFactory);
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
							//find myself dead
							String serverHost = getHostByEventDataPath(path);
							if(checkServerSelfDead(serverHost, ZKNodeType.WORKER)){
								return;
							}
							break;
						case CHILD_UPDATED:
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
