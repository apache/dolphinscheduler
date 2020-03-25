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

package org.apache.dolphinscheduler.server.master.dispatch.host;

import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.remote.utils.Host;
import org.apache.dolphinscheduler.server.master.dispatch.context.ExecutionContext;
import org.apache.dolphinscheduler.server.master.dispatch.enums.ExecutorType;
import org.apache.dolphinscheduler.server.registry.ZookeeperNodeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 *  round robin host manager
 */
public abstract class CommonHostManager implements HostManager {

    private final Logger logger = LoggerFactory.getLogger(CommonHostManager.class);

    /**
     * zookeeperNodeManager
     */
    @Autowired
    protected ZookeeperNodeManager zookeeperNodeManager;

    /**
     * select host
     * @param context context
     * @return host
     */
    @Override
    public Host select(ExecutionContext context){
        Host host = new Host();
        Collection<String> nodes = null;
        /**
         * executor type
         */
        ExecutorType executorType = context.getExecutorType();
        switch (executorType){
            case WORKER:
                nodes = zookeeperNodeManager.getWorkerGroupNodes(context.getWorkerGroup());
                break;
            case CLIENT:
                break;
            default:
                throw new IllegalArgumentException("invalid executorType : " + executorType);

        }
        if(CollectionUtils.isEmpty(nodes)){
            return host;
        }
        List<Host> candidateHosts = new ArrayList<>(nodes.size());
        nodes.stream().forEach(node -> candidateHosts.add(Host.of(node)));

        return select(candidateHosts);
    }

    protected abstract Host select(Collection<Host> nodes);

    public void setZookeeperNodeManager(ZookeeperNodeManager zookeeperNodeManager) {
        this.zookeeperNodeManager = zookeeperNodeManager;
    }

    public ZookeeperNodeManager getZookeeperNodeManager() {
        return zookeeperNodeManager;
    }
}
