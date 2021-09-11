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

package org.apache.dolphinscheduler.service.registry;

import static org.apache.dolphinscheduler.common.Constants.ADD_OP;
import static org.apache.dolphinscheduler.common.Constants.COLON;
import static org.apache.dolphinscheduler.common.Constants.DELETE_OP;
import static org.apache.dolphinscheduler.common.Constants.DIVISION_STRING;
import static org.apache.dolphinscheduler.common.Constants.MASTER_TYPE;
import static org.apache.dolphinscheduler.common.Constants.REGISTRY_DOLPHINSCHEDULER_MASTERS;
import static org.apache.dolphinscheduler.common.Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS;
import static org.apache.dolphinscheduler.common.Constants.SINGLE_SLASH;
import static org.apache.dolphinscheduler.common.Constants.UNDERLINE;
import static org.apache.dolphinscheduler.common.Constants.WORKER_TYPE;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.utils.ResInfo;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * registry client singleton
 */
public class RegistryClient extends RegistryCenter {

    private static final Logger logger = LoggerFactory.getLogger(RegistryClient.class);

    private static RegistryClient registryClient = new RegistryClient();

    private RegistryClient() {
        super.init();
    }

    public static RegistryClient getInstance() {
        return registryClient;
    }

    /**
     * get active master num
     *
     * @return active master number
     */
    public int getActiveMasterNum() {
        List<String> childrenList = new ArrayList<>();
        try {
            // read master node parent path from conf
            if (isExisted(getNodeParentPath(NodeType.MASTER))) {
                childrenList = getChildrenKeys(getNodeParentPath(NodeType.MASTER));
            }
        } catch (Exception e) {
            logger.error("getActiveMasterNum error", e);
        }
        return childrenList.size();
    }

    /**
     * get server list.
     *
     * @param nodeType zookeeper node type
     * @return server list
     */
    public List<Server> getServerList(NodeType nodeType) {
        Map<String, String> serverMaps = getServerMaps(nodeType);
        String parentPath = getNodeParentPath(nodeType);

        List<Server> serverList = new ArrayList<>();
        for (Map.Entry<String, String> entry : serverMaps.entrySet()) {
            Server server = ResInfo.parseHeartbeatForRegistryInfo(entry.getValue());
            if (server == null) {
                continue;
            }
            String key = entry.getKey();
            server.setZkDirectory(parentPath + "/" + key);
            // set host and port
            String[] hostAndPort = key.split(COLON);
            String[] hosts = hostAndPort[0].split(DIVISION_STRING);
            // fetch the last one
            server.setHost(hosts[hosts.length - 1]);
            server.setPort(Integer.parseInt(hostAndPort[1]));
            serverList.add(server);
        }
        return serverList;
    }

    /**
     * get server nodes.
     *
     * @param nodeType registry node type
     * @return result : list<node>
     */
    public List<String> getServerNodes(NodeType nodeType) {
        String path = getNodeParentPath(nodeType);
        List<String> serverList = getChildrenKeys(path);
        if (nodeType == NodeType.WORKER) {
            List<String> workerList = new ArrayList<>();
            for (String group : serverList) {
                List<String> groupServers = getChildrenKeys(path + Constants.SLASH + group);
                for (String groupServer : groupServers) {
                    workerList.add(group + Constants.SLASH + groupServer);
                }
            }
            serverList = workerList;
        }
        return serverList;
    }

    /**
     * get server list map.
     *
     * @param nodeType zookeeper node type
     * @param hostOnly host only
     * @return result : {host : resource info}
     */
    public Map<String, String> getServerMaps(NodeType nodeType, boolean hostOnly) {
        Map<String, String> serverMap = new HashMap<>();
        try {
            String path = getNodeParentPath(nodeType);
            List<String> serverList = getServerNodes(nodeType);
            for (String server : serverList) {
                String host = server;
                if (nodeType == NodeType.WORKER && hostOnly) {
                    host = server.split(Constants.SLASH)[1];
                }
                serverMap.putIfAbsent(host, get(path + Constants.SLASH + server));
            }
        } catch (Exception e) {
            logger.error("get server list failed", e);
        }

        return serverMap;
    }

    /**
     * get server list map.
     *
     * @param nodeType zookeeper node type
     * @return result : {host : resource info}
     */
    public Map<String, String> getServerMaps(NodeType nodeType) {
        return getServerMaps(nodeType, false);
    }

    /**
     * get server node set.
     *
     * @param nodeType zookeeper node type
     * @param hostOnly host only
     * @return result : set<host>
     */
    public Set<String> getServerNodeSet(NodeType nodeType, boolean hostOnly) {
        Set<String> serverSet = new HashSet<>();
        try {
            List<String> serverList = getServerNodes(nodeType);
            for (String server : serverList) {
                String host = server;
                if (nodeType == NodeType.WORKER && hostOnly) {
                    host = server.split(Constants.SLASH)[1];
                }
                serverSet.add(host);
            }
        } catch (Exception e) {
            logger.error("get server node set failed", e);
        }
        return serverSet;
    }

    /**
     * get server node list.
     *
     * @param nodeType zookeeper node type
     * @param hostOnly host only
     * @return result : list<host>
     */
    public List<String> getServerNodeList(NodeType nodeType, boolean hostOnly) {
        Set<String> serverSet = getServerNodeSet(nodeType, hostOnly);
        List<String> serverList = new ArrayList<>(serverSet);
        Collections.sort(serverList);
        return serverList;
    }

    /**
     * check the zookeeper node already exists
     *
     * @param host host
     * @param nodeType zookeeper node type
     * @return true if exists
     */
    public boolean checkNodeExists(String host, NodeType nodeType) {
        String path = getNodeParentPath(nodeType);
        if (StringUtils.isEmpty(path)) {
            logger.error("check zk node exists error, host:{}, zk node type:{}",
                    host, nodeType);
            return false;
        }
        Map<String, String> serverMaps = getServerMaps(nodeType, true);
        for (String hostKey : serverMaps.keySet()) {
            if (hostKey.contains(host)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return get worker node parent path
     */
    protected String getWorkerNodeParentPath() {
        return Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS;
    }

    /**
     * @return get master node parent path
     */
    protected String getMasterNodeParentPath() {
        return Constants.REGISTRY_DOLPHINSCHEDULER_MASTERS;
    }

    /**
     * @return get dead server node parent path
     */
    protected String getDeadNodeParentPath() {
        return Constants.REGISTRY_DOLPHINSCHEDULER_DEAD_SERVERS;
    }

    /**
     * @return get master lock path
     */
    public String getMasterLockPath() {
        return Constants.REGISTRY_DOLPHINSCHEDULER_LOCK_MASTERS;
    }

    /**
     * @param nodeType zookeeper node type
     * @return get zookeeper node parent path
     */
    public String getNodeParentPath(NodeType nodeType) {
        String path = "";
        switch (nodeType) {
            case MASTER:
                return getMasterNodeParentPath();
            case WORKER:
                return getWorkerNodeParentPath();
            case DEAD_SERVER:
                return getDeadNodeParentPath();
            default:
                break;
        }
        return path;
    }

    /**
     * @return get master start up lock path
     */
    public String getMasterStartUpLockPath() {
        return Constants.REGISTRY_DOLPHINSCHEDULER_LOCK_FAILOVER_STARTUP_MASTERS;
    }

    /**
     * @return get master failover lock path
     */
    public String getMasterFailoverLockPath() {
        return Constants.REGISTRY_DOLPHINSCHEDULER_LOCK_FAILOVER_MASTERS;
    }

    /**
     * @return get worker failover lock path
     */
    public String getWorkerFailoverLockPath() {
        return Constants.REGISTRY_DOLPHINSCHEDULER_LOCK_FAILOVER_WORKERS;
    }

    /**
     * opType(add): if find dead server , then add to zk deadServerPath
     * opType(delete): delete path from zk
     *
     * @param node node path
     * @param nodeType master or worker
     * @param opType delete or add
     * @throws Exception errors
     */
    public void handleDeadServer(String node, NodeType nodeType, String opType) throws Exception {
        String host = getHostByEventDataPath(node);
        String type = (nodeType == NodeType.MASTER) ? MASTER_TYPE : WORKER_TYPE;

        //check server restart, if restart , dead server path in zk should be delete
        if (opType.equals(DELETE_OP)) {
            removeDeadServerByHost(host, type);

        } else if (opType.equals(ADD_OP)) {
            String deadServerPath = getDeadZNodeParentPath() + SINGLE_SLASH + type + UNDERLINE + host;
            if (!isExisted(deadServerPath)) {
                //add dead server info to zk dead server path : /dead-servers/

                persist(deadServerPath, (type + UNDERLINE + host));

                logger.info("{} server dead , and {} added to zk dead server path success",
                        nodeType, node);
            }
        }

    }

    /**
     * check dead server or not , if dead, stop self
     *
     * @param node node path
     * @param serverType master or worker prefix
     * @return true if not exists
     * @throws Exception errors
     */
    public boolean checkIsDeadServer(String node, String serverType) throws Exception {
        // ip_sequence_no
        String[] zNodesPath = node.split("\\/");
        String ipSeqNo = zNodesPath[zNodesPath.length - 1];
        String deadServerPath = getDeadZNodeParentPath() + SINGLE_SLASH + serverType + UNDERLINE + ipSeqNo;

        return !isExisted(node) || isExisted(deadServerPath);
    }

    /**
     * get master nodes directly
     *
     * @return master nodes
     */
    public Set<String> getMasterNodesDirectly() {
        List<String> masters = getChildrenKeys(REGISTRY_DOLPHINSCHEDULER_MASTERS);
        return new HashSet<>(masters);
    }

    /**
     * get worker nodes directly
     *
     * @return master nodes
     */
    public Set<String> getWorkerNodesDirectly() {
        List<String> workers = getChildrenKeys(REGISTRY_DOLPHINSCHEDULER_WORKERS);
        return new HashSet<>(workers);
    }

    /**
     * get worker group directly
     *
     * @return worker group nodes
     */
    public Set<String> getWorkerGroupDirectly() {
        List<String> workers = getChildrenKeys(REGISTRY_DOLPHINSCHEDULER_WORKERS);
        return new HashSet<>(workers);
    }

    /**
     * get worker group nodes
     */
    public Set<String> getWorkerGroupNodesDirectly(String workerGroup) {
        List<String> workers = getChildrenKeys(getWorkerGroupPath(workerGroup));
        return new HashSet<>(workers);
    }

    /**
     * opType(add): if find dead server , then add to zk deadServerPath
     * opType(delete): delete path from zk
     *
     * @param nodeSet node path set
     * @param nodeType master or worker
     * @param opType delete or add
     * @throws Exception errors
     */
    public void handleDeadServer(Set<String> nodeSet, NodeType nodeType, String opType) throws Exception {

        String type = (nodeType == NodeType.MASTER) ? MASTER_TYPE : WORKER_TYPE;
        for (String node : nodeSet) {
            String host = getHostByEventDataPath(node);
            //check server restart, if restart , dead server path in zk should be delete
            if (opType.equals(DELETE_OP)) {
                removeDeadServerByHost(host, type);

            } else if (opType.equals(ADD_OP)) {
                String deadServerPath = getDeadZNodeParentPath() + SINGLE_SLASH + type + UNDERLINE + host;
                if (!isExisted(deadServerPath)) {
                    //add dead server info to zk dead server path : /dead-servers/
                    persist(deadServerPath, (type + UNDERLINE + host));
                    logger.info("{} server dead , and {} added to registry dead server path success",
                            nodeType, node);
                }
            }

        }

    }

    /**
     * get host ip:port, string format: parentPath/ip:port
     *
     * @param path path
     * @return host ip:port, string format: parentPath/ip:port
     */
    public String getHostByEventDataPath(String path) {
        if (StringUtils.isEmpty(path)) {
            logger.error("empty path!");
            return "";
        }
        String[] pathArray = path.split(SINGLE_SLASH);
        if (pathArray.length < 1) {
            logger.error("parse ip error: {}", path);
            return "";
        }
        return pathArray[pathArray.length - 1];

    }

    /**
     * remove dead server by host
     *
     * @param host host
     * @param serverType serverType
     */
    public void removeDeadServerByHost(String host, String serverType) {
        List<String> deadServers = getChildrenKeys(getDeadZNodeParentPath());
        for (String serverPath : deadServers) {
            if (serverPath.startsWith(serverType + UNDERLINE + host)) {
                String server = getDeadZNodeParentPath() + SINGLE_SLASH + serverPath;
                remove(server);
                logger.info("{} server {} deleted from zk dead server path success", serverType, host);
            }
        }
    }

}
