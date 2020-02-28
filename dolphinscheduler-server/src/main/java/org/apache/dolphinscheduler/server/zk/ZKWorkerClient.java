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
import org.apache.dolphinscheduler.common.enums.ZKNodeType;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.dolphinscheduler.service.zk.AbstractZKClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 *  zookeeper worker client
 *  single instance
 */
@Component
public class ZKWorkerClient extends AbstractZKClient {

	/**
	 * logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(ZKWorkerClient.class);


	/**
	 * worker znode
	 */
	private String workerZNode = null;


	/**
	 * init
	 */
	public void init(){

		logger.info("initialize worker client...");
		// init system znode
		this.initSystemZNode();

	}

	/**
	 * handle path events that this class cares about
	 * @param client   zkClient
	 * @param event	   path event
	 * @param path     zk path
	 */
	@Override
	protected void dataChanged(CuratorFramework client, TreeCacheEvent event, String path) {
		if(path.startsWith(getZNodeParentPath(ZKNodeType.WORKER)+Constants.SINGLE_SLASH)){
			handleWorkerEvent(event,path);
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
				//find myself dead
				String serverHost = getHostByEventDataPath(path);
				if(checkServerSelfDead(serverHost, ZKNodeType.WORKER)){
					return;
				}
				break;
			default:
				break;
		}
	}

	/**
	 * get worker znode
	 * @return worker zookeeper node
	 */
	public String getWorkerZNode() {
		return workerZNode;
	}

}
