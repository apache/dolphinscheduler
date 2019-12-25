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
package org.apache.dolphinscheduler.server.monitor;

import org.apache.commons.lang3.StringUtils;
import org.apache.dolphinscheduler.common.utils.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * abstract server monitor and auto restart server
 */
@Component
public abstract class AbstractMonitor implements Monitor {

    private static final Logger logger = LoggerFactory.getLogger(AbstractMonitor.class);

    /**
     * monitor server and restart
     */
    @Override
    public void monitor(String masterPath,String workerPath,Integer port,String installPath) {
        try {
            restartServer(masterPath,port,installPath);
            restartServer(workerPath,port,installPath);
        }catch (Exception e){
            logger.error("server start up error",e);
        }
    }

    private void restartServer(String path,Integer port,String installPath) throws Exception{

        Map<String, String> activeNodeMap = getActiveNodesByPath(path);
        String type = path.split("/")[2];
        Set<String> needRestartServer = getNeedRestartServer(getRunConfigByType(type),
                activeNodeMap.keySet());
        String serverName = null;
        if ("masters".equals(type)){
            serverName = "master-server";
        }else if ("workers".equals(type)){
            serverName = "worker-server";
        }

        for (String node : needRestartServer){
            // os.system('ssh -p ' + ssh_port + ' ' + self.get_ip_by_hostname(master) + ' sh ' + install_path + '/bin/dolphinscheduler-daemon.sh start master-server')
            String runCmd = "ssh -p " + port + " " +  node + " sh "  + installPath + "/bin/dolphinscheduler-daemon.sh start " + serverName;
            Runtime.getRuntime().exec(runCmd);
        }
    }

    /**
     * get need restart server
     * @param deployedNodes  deployedNodes
     * @param activeNodes activeNodes
     * @return need restart server
     */
    private Set<String> getNeedRestartServer(Set<String> deployedNodes,Set<String> activeNodes){
        if (CollectionUtils.isEmpty(activeNodes)){
            return deployedNodes;
        }

        Set<String> result = new HashSet<>();

        result.addAll(deployedNodes);
        result.removeAll(activeNodes);

        return result;
    }

    /**
     * run config masters/workers
     * @return master set/worker set
     */
    private Set<String> getRunConfigByType(String type){
        Set<String> nodeSet = new HashSet();

        String nodes = "";

        if (StringUtils.isEmpty(nodes)){
            return null;
        }

        String[] masterArr = nodes.split(",");

        for (String master : masterArr){
            nodeSet.add(master);
        }

        return nodeSet;
    }

    /**
     * get active nodes by path
     * @param path path
     * @return active nodes
     */
    protected abstract Map<String,String> getActiveNodesByPath(String path);
}
