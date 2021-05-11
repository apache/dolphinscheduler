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

import static org.apache.dolphinscheduler.common.Constants.COLON;
import static org.apache.dolphinscheduler.common.Constants.DIVISION_STRING;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.enums.ZKNodeType;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * abstract zookeeper client
 */
@Component
public abstract class AbstractZKClient extends RegisterOperator {

    private static final Logger logger = LoggerFactory.getLogger(AbstractZKClient.class);

    /**
     * get active master num
     *
     * @return active master number
     */
    public int getActiveMasterNum() {
        List<String> childrenList = new ArrayList<>();
        try {
            // read master node parent path from conf
            if (super.isExisted(getZNodeParentPath(ZKNodeType.MASTER))) {
                childrenList = super.getChildrenKeys(getZNodeParentPath(ZKNodeType.MASTER));
            }
        } catch (Exception e) {
            logger.error("getActiveMasterNum error", e);
        }
        return childrenList.size();
    }

    /**
     * @return zookeeper quorum
     */
    public String getZookeeperQuorum() {
        return getZookeeperConfig().getServerList();
    }

    /**
     * get server list.
     *
     * @param zkNodeType zookeeper node type
     * @return server list
     */
    public List<Server> getServerList(ZKNodeType zkNodeType) {
        Map<String, String> serverMaps = getServerMaps(zkNodeType);
        String parentPath = getZNodeParentPath(zkNodeType);

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
     * get server zk nodes.
     *
     * @param zkNodeType zookeeper node type
     * @return result : list<zknode>
     */
    public List<String> getServerZkNodes(ZKNodeType zkNodeType) {
        String path = getZNodeParentPath(zkNodeType);
        List<String> serverList = super.getChildrenKeys(path);
        if (zkNodeType == ZKNodeType.WORKER) {
            List<String> workerList = new ArrayList<>();
            for (String group : serverList) {
                List<String> groupServers = super.getChildrenKeys(path + Constants.SLASH + group);
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
     * @param zkNodeType zookeeper node type
     * @param hostOnly host only
     * @return result : {host : resource info}
     */
    public Map<String, String> getServerMaps(ZKNodeType zkNodeType, boolean hostOnly) {
        Map<String, String> serverMap = new HashMap<>();
        try {
            String path = getZNodeParentPath(zkNodeType);
            List<String> serverList = getServerZkNodes(zkNodeType);
            for (String server : serverList) {
                String host = server;
                if (zkNodeType == ZKNodeType.WORKER && hostOnly) {
                    host = server.split(Constants.SLASH)[1];
                }
                serverMap.putIfAbsent(host, super.get(path + Constants.SLASH + server));
            }
        } catch (Exception e) {
            logger.error("get server list failed", e);
        }

        return serverMap;
    }

    /**
     * get server list map.
     *
     * @param zkNodeType zookeeper node type
     * @return result : {host : resource info}
     */
    public Map<String, String> getServerMaps(ZKNodeType zkNodeType) {
        return getServerMaps(zkNodeType, false);
    }

    /**
     * get server node set.
     *
     * @param zkNodeType zookeeper node type
     * @param hostOnly host only
     * @return result : set<host>
     */
    public Set<String> getServerNodeSet(ZKNodeType zkNodeType, boolean hostOnly) {
        Set<String> serverSet = new HashSet<>();
        try {
            List<String> serverList = getServerZkNodes(zkNodeType);
            for (String server : serverList) {
                String host = server;
                if (zkNodeType == ZKNodeType.WORKER && hostOnly) {
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
     * @param zkNodeType zookeeper node type
     * @param hostOnly host only
     * @return result : list<host>
     */
    public List<String> getServerNodeList(ZKNodeType zkNodeType, boolean hostOnly) {
        Set<String> serverSet = getServerNodeSet(zkNodeType, hostOnly);
        List<String> serverList = new ArrayList<>(serverSet);
        Collections.sort(serverList);
        return serverList;
    }

    /**
     * check the zookeeper node already exists
     *
     * @param host host
     * @param zkNodeType zookeeper node type
     * @return true if exists
     */
    public boolean checkZKNodeExists(String host, ZKNodeType zkNodeType) {
        String path = getZNodeParentPath(zkNodeType);
        if (StringUtils.isEmpty(path)) {
            logger.error("check zk node exists error, host:{}, zk node type:{}",
                    host, zkNodeType);
            return false;
        }
        Map<String, String> serverMaps = getServerMaps(zkNodeType, true);
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
    protected String getWorkerZNodeParentPath() {
        return getZookeeperConfig().getDsRoot() + Constants.ZOOKEEPER_DOLPHINSCHEDULER_WORKERS;
    }

    /**
     * @return get master node parent path
     */
    protected String getMasterZNodeParentPath() {
        return getZookeeperConfig().getDsRoot() + Constants.ZOOKEEPER_DOLPHINSCHEDULER_MASTERS;
    }

    /**
     * @return get master lock path
     */
    public String getMasterLockPath() {
        return getZookeeperConfig().getDsRoot() + Constants.ZOOKEEPER_DOLPHINSCHEDULER_LOCK_MASTERS;
    }

    /**
     * @param zkNodeType zookeeper node type
     * @return get zookeeper node parent path
     */
    public String getZNodeParentPath(ZKNodeType zkNodeType) {
        String path = "";
        switch (zkNodeType) {
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
     * @return get master start up lock path
     */
    public String getMasterStartUpLockPath() {
        return getZookeeperConfig().getDsRoot() + Constants.ZOOKEEPER_DOLPHINSCHEDULER_LOCK_FAILOVER_STARTUP_MASTERS;
    }

    /**
     * @return get master failover lock path
     */
    public String getMasterFailoverLockPath() {
        return getZookeeperConfig().getDsRoot() + Constants.ZOOKEEPER_DOLPHINSCHEDULER_LOCK_FAILOVER_MASTERS;
    }

    /**
     * @return get worker failover lock path
     */
    public String getWorkerFailoverLockPath() {
        return getZookeeperConfig().getDsRoot() + Constants.ZOOKEEPER_DOLPHINSCHEDULER_LOCK_FAILOVER_WORKERS;
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
    protected void initSystemZNode() {
        try {
            persist(getMasterZNodeParentPath(), "");
            persist(getWorkerZNodeParentPath(), "");
            persist(getDeadZNodeParentPath(), "");

            logger.info("initialize server nodes success.");
        } catch (Exception e) {
            logger.error("init system znode failed", e);
        }
    }

    @Override
    public String toString() {
        return "AbstractZKClient{"
                + "zkClient=" + getZkClient()
                + ", deadServerZNodeParentPath='" + getZNodeParentPath(ZKNodeType.DEAD_SERVER) + '\''
                + ", masterZNodeParentPath='" + getZNodeParentPath(ZKNodeType.MASTER) + '\''
                + ", workerZNodeParentPath='" + getZNodeParentPath(ZKNodeType.WORKER) + '\''
                + '}';
    }
}
