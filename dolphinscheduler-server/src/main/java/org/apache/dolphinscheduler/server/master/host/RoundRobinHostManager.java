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

package org.apache.dolphinscheduler.server.master.host;

import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.apache.dolphinscheduler.remote.entity.TaskExecutionContext;
import org.apache.dolphinscheduler.server.master.host.assign.RoundRobinSelector;
import org.apache.dolphinscheduler.server.registry.ZookeeperNodeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


@Service
public class RoundRobinHostManager implements HostManager {

    private final Logger logger = LoggerFactory.getLogger(RoundRobinHostManager.class);

    @Autowired
    private RoundRobinSelector<Host> selector;

    @Autowired
    private ZookeeperNodeManager zookeeperNodeManager;

    @Override
    public Host select(TaskExecutionContext context){
        Host host = new Host();
        Collection<String> nodes = zookeeperNodeManager.getWorkerNodes();
        if(CollectionUtils.isEmpty(nodes)){
            return host;
        }
        List<Host> candidateHosts = new ArrayList<>(nodes.size());
        nodes.stream().forEach(node -> candidateHosts.add(Host.of(node)));
        return selector.select(candidateHosts);
    }

}
