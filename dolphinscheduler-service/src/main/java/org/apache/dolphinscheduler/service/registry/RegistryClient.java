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
import static org.apache.dolphinscheduler.common.Constants.SINGLE_SLASH;
import static org.apache.dolphinscheduler.common.Constants.UNDERLINE;
import static org.apache.dolphinscheduler.common.Constants.WORKER_TYPE;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.utils.ResInfo;
import org.apache.dolphinscheduler.common.utils.StringUtils;

import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * abstract registry client
 */
@Service
public class RegistryClient extends RegistryCenter {

    private static final Logger logger = LoggerFactory.getLogger(RegistryClient.class);

    @Resource
    RegistryCenter registryCenter;

    /**
     * get active master num
     *
     * @return active master number
     */
    public int getActiveMasterNum() {
        List<String> childrenList = new ArrayList<>();
        try {
            // read master node parent path from conf
            if (registryCenter.isExisted(getNodeParentPath(NodeType.MASTER))) {
                childrenList = registryCenter.getChildrenKeys(getNodeParentPath(NodeType.MASTER));
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
            Server server = ResInfo.parseHeartbeatForZKInfo(entry.getValue());
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
    public List<String> getServerZkNodes(NodeType nodeType) {
        String path = getNodeParentPath(nodeType);
        List<String> serverList = registryCenter.getChildrenKeys(path);
        if (nodeType == NodeType.WORKER) {
            List<String> workerList = new ArrayList<>();
            for (String group : serverList) {
                List<String> groupServers = registryCenter.getChildrenKeys(path + Constants.SLASH + group);
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
            List<String> serverList = getServerZkNodes(nodeType);
            for (String server : serverList) {
                String host = server;
                if (nodeType == NodeType.WORKER && hostOnly) {
                    host = server.split(Constants.SLASH)[1];
                }
                serverMap.putIfAbsent(host, registryCenter.get(path + Constants.SLASH + server));
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
            List<String> serverList = getServerZkNodes(nodeType);
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
     * release mutex
     *
     * @param mutex mutex
     */
    public void releaseMutex(InterProcessMutex mutex) {
        if (mutex != null) {
            try {
                mutex.release();
            } catch (Exception e) {
                if ("instance must be started before calling this method".equals(e.getMessage())) {
                    logger.warn("lock release");
                } else {
                    logger.error("lock release failed", e);
                }

            }
        }
    }

    /**
     * init system znode
     */
    public void initSystemNode() {
        try {
            registryCenter.persist(getMasterNodeParentPath(), "");
            registryCenter.persist(getWorkerNodeParentPath(), "");
            registryCenter.persist(getDeadNodeParentPath(), "");

            logger.info("initialize server nodes success.");
        } catch (Exception e) {
            logger.error("init system node failed", e);
        }
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
     * @param zNode node path
     * @param serverType master or worker prefix
     * @return true if not exists
     * @throws Exception errors
     */
    public boolean checkIsDeadServer(String zNode, String serverType) throws Exception {
        // ip_sequence_no
        String[] zNodesPath = zNode.split("\\/");
        String ipSeqNo = zNodesPath[zNodesPath.length - 1];
        String deadServerPath = getDeadZNodeParentPath() + SINGLE_SLASH + serverType + UNDERLINE + ipSeqNo;

        return !isExisted(zNode) || isExisted(deadServerPath);
    }

    /**
     * get master nodes directly
     *
     * @return master nodes
     */
    public Set<String> getMasterNodesDirectly() {
        List<String> masters = getChildrenKeys(MASTER_PATH);
        return new HashSet<>(masters);
    }

    /**
     * get worker nodes directly
     *
     * @return master nodes
     */
    public Set<String> getWorkerNodesDirectly() {
        List<String> workers = getChildrenKeys(WORKER_PATH);
        return new HashSet<>(workers);
    }

    /**
     * get worker group directly
     *
     * @return worker group nodes
     */
    public Set<String> getWorkerGroupDirectly() {
        List<String> workers = getChildrenKeys(getWorkerPath());
        return new HashSet<>(workers);
    }

    /**
     * get worker group nodes
     */
    public Set<String> getWorkerGroupNodesDirectly(String workerGroup) {
        List<String> workers = getChildrenKeys(getWorkerGroupPath(workerGroup));
        return new HashSet<>(workers);
    }
}
