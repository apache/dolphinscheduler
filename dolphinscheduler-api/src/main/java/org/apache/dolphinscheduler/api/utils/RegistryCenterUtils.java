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

package org.apache.dolphinscheduler.api.utils;

import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.dao.entity.ZookeeperRecord;
import org.apache.dolphinscheduler.service.registry.RegistryClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * monitor zookeeper info todo registry-spi
 * fixme Some of the information obtained in the api belongs to the unique information of zk.
 * I am not sure whether there is a good abstraction method. This is related to whether the specific plug-in is provided.
 */
public class RegistryCenterUtils {

    private static RegistryClient registryClient = RegistryClient.getInstance();

    /**
     * @return zookeeper info list
     */
    public static List<ZookeeperRecord> zookeeperInfoList() {
        return null;
    }

    /**
     * get master servers
     *
     * @return master server information
     */
    public static List<Server> getMasterServers() {
        return registryClient.getServerList(NodeType.MASTER);
    }

    /**
     * master construct is the same with worker, use the master instead
     *
     * @return worker server informations
     */
    public static List<Server> getWorkerServers() {
        return registryClient.getServerList(NodeType.WORKER);
    }

    private static List<ZookeeperRecord> zookeeperInfoList(String zookeeperServers) {
        List<ZookeeperRecord> list = new ArrayList<>(5);
        /*
        if (StringUtils.isNotBlank(zookeeperServers)) {
            String[] zookeeperServersArray = zookeeperServers.split(",");

            for (String zookeeperServer : zookeeperServersArray) {
                ZooKeeperState state = new ZooKeeperState(zookeeperServer);
                boolean ok = state.ruok();
                if (ok) {
                    state.getZookeeperInfo();
                }

                int connections = state.getConnections();
                int watches = state.getWatches();
                long sent = state.getSent();
                long received = state.getReceived();
                String mode =  state.getMode();
                float minLatency =  state.getMinLatency();
                float avgLatency = state.getAvgLatency();
                float maxLatency = state.getMaxLatency();
                int nodeCount = state.getNodeCount();
                int status = ok ? 1 : 0;
                Date date = new Date();

                ZookeeperRecord zookeeperRecord = new ZookeeperRecord(zookeeperServer,connections,watches,sent,received,mode,minLatency,avgLatency,maxLatency,nodeCount,status,date);
                list.add(zookeeperRecord);

            }
        }*/

        return list;
    }

    public static Map<String, String> getServerMaps(NodeType nodeType, boolean hostOnly) {
        return registryClient.getServerMaps(nodeType, hostOnly);
    }

    public static List<String> getServerNodeList(NodeType nodeType, boolean hostOnly) {
        return registryClient.getServerNodeList(nodeType, hostOnly);
    }

    public static boolean isNodeExisted(String key) {
        return registryClient.isExisted(key);
    }

    public static List<String> getChildrenNodes(final String key) {
        return registryClient.getChildrenKeys(key);
    }

    public static String getNodeData(String key) {
        return registryClient.get(key);
    }
}
