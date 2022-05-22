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
import static org.apache.dolphinscheduler.common.Constants.REGISTRY_DOLPHINSCHEDULER_DEAD_SERVERS;
import static org.apache.dolphinscheduler.common.Constants.REGISTRY_DOLPHINSCHEDULER_MASTERS;
import static org.apache.dolphinscheduler.common.Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS;
import static org.apache.dolphinscheduler.common.Constants.SINGLE_SLASH;
import static org.apache.dolphinscheduler.common.Constants.UNDERLINE;
import static org.apache.dolphinscheduler.common.Constants.WORKER_TYPE;

import static com.google.common.base.Preconditions.checkArgument;

import org.apache.dolphinscheduler.common.Constants;
import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.common.utils.HeartBeat;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.registry.api.ConnectionListener;
import org.apache.dolphinscheduler.registry.api.Registry;
import org.apache.dolphinscheduler.registry.api.RegistryException;
import org.apache.dolphinscheduler.registry.api.SubscribeListener;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component
public class RegistryClient {
    private static final Logger logger = LoggerFactory.getLogger(RegistryClient.class);

    private static final String EMPTY = "";
    private IStoppable stoppable;

    private final Registry registry;

    public RegistryClient(Registry registry) {
        this.registry = registry;
    }

    @PostConstruct
    public void afterConstruct() {
        initNodes();
    }

    public int getActiveMasterNum() {
        Collection<String> childrenList = new ArrayList<>();
        try {
            // read master node parent path from conf
            if (exists(rootNodePath(NodeType.MASTER))) {
                childrenList = getChildrenKeys(rootNodePath(NodeType.MASTER));
            }
        } catch (Exception e) {
            logger.error("getActiveMasterNum error", e);
        }
        return childrenList.size();
    }

    public List<Server> getServerList(NodeType nodeType) {
        Map<String, String> serverMaps = getServerMaps(nodeType, false);
        String parentPath = rootNodePath(nodeType);

        List<Server> serverList = new ArrayList<>();
        for (Map.Entry<String, String> entry : serverMaps.entrySet()) {
            HeartBeat heartBeat = HeartBeat.decodeHeartBeat(entry.getValue());
            if (heartBeat == null) {
                continue;
            }

            Server server = new Server();
            server.setResInfo(JSONUtils.toJsonString(heartBeat));
            server.setCreateTime(new Date(heartBeat.getStartupTime()));
            server.setLastHeartbeatTime(new Date(heartBeat.getReportTime()));
            server.setId(heartBeat.getProcessId());

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

    public Map<String, String> getServerMaps(NodeType nodeType, boolean hostOnly) {
        Map<String, String> serverMap = new HashMap<>();
        try {
            String path = rootNodePath(nodeType);
            Collection<String> serverList = getServerNodes(nodeType);
            for (String server : serverList) {
                String host = server;
                if (nodeType == NodeType.WORKER && hostOnly) {
                    host = server.split(SINGLE_SLASH)[1];
                }
                serverMap.putIfAbsent(host, get(path + SINGLE_SLASH + server));
            }
        } catch (Exception e) {
            logger.error("get server list failed", e);
        }

        return serverMap;
    }

    public boolean checkNodeExists(String host, NodeType nodeType) {
        return getServerMaps(nodeType, true).keySet()
                                            .stream()
                                            .anyMatch(it -> it.contains(host));
    }

    public void handleDeadServer(Collection<String> nodes, NodeType nodeType, String opType) {
        nodes.forEach(node -> {
            final String host = getHostByEventDataPath(node);
            final String type = nodeType == NodeType.MASTER ? MASTER_TYPE : WORKER_TYPE;

            if (opType.equals(DELETE_OP)) {
                removeDeadServerByHost(host, type);
            } else if (opType.equals(ADD_OP)) {
                String deadServerPath = REGISTRY_DOLPHINSCHEDULER_DEAD_SERVERS + SINGLE_SLASH + type + UNDERLINE + host;
                // Add dead server info to zk dead server path : /dead-servers/
                registry.put(deadServerPath, type + UNDERLINE + host, false);
                logger.info("{} server dead , and {} added to zk dead server path success", nodeType, node);
            }
        });
    }

    public boolean checkIsDeadServer(String node, String serverType) {
        // ip_sequence_no
        String[] zNodesPath = node.split("/");
        String ipSeqNo = zNodesPath[zNodesPath.length - 1];
        String deadServerPath = REGISTRY_DOLPHINSCHEDULER_DEAD_SERVERS + SINGLE_SLASH + serverType + UNDERLINE + ipSeqNo;

        return !exists(node) || exists(deadServerPath);
    }

    public Collection<String> getMasterNodesDirectly() {
        return getChildrenKeys(REGISTRY_DOLPHINSCHEDULER_MASTERS);
    }

    public Collection<String> getWorkerGroupDirectly() {
        return getChildrenKeys(REGISTRY_DOLPHINSCHEDULER_WORKERS);
    }

    public Collection<String> getWorkerGroupNodesDirectly(String workerGroup) {
        return getChildrenKeys(REGISTRY_DOLPHINSCHEDULER_WORKERS + "/" + workerGroup);
    }

    /**
     * get host ip:port, path format: parentPath/ip:port
     *
     * @param path path
     * @return host ip:port, string format: parentPath/ip:port
     */
    public String getHostByEventDataPath(String path) {
        checkArgument(!Strings.isNullOrEmpty(path), "path cannot be null or empty");

        final String[] pathArray = path.split(SINGLE_SLASH);

        checkArgument(pathArray.length >= 1, "cannot parse path: %s", path);

        return pathArray[pathArray.length - 1];
    }

    public void close() throws IOException {
        registry.close();
    }

    public void persistEphemeral(String key, String value) {
        registry.put(key, value, true);
    }

    public void remove(String key) {
        registry.delete(key);
    }

    public String get(String key) {
        return registry.get(key);
    }

    public void subscribe(String path, SubscribeListener listener) {
        registry.subscribe(path, listener);
    }

    public void addConnectionStateListener(ConnectionListener listener) {
        registry.addConnectionStateListener(listener);
    }

    public boolean exists(String key) {
        return registry.exists(key);
    }

    public boolean getLock(String key) {
        return registry.acquireLock(key);
    }

    public boolean releaseLock(String key) {
        return registry.releaseLock(key);
    }

    public void setStoppable(IStoppable stoppable) {
        this.stoppable = stoppable;
    }

    public IStoppable getStoppable() {
        return stoppable;
    }

    public boolean isMasterPath(String path) {
        return path != null && path.startsWith(REGISTRY_DOLPHINSCHEDULER_MASTERS);
    }

    public boolean isWorkerPath(String path) {
        return path != null && path.startsWith(REGISTRY_DOLPHINSCHEDULER_WORKERS);
    }

    public Collection<String> getChildrenKeys(final String key) {
        return registry.children(key);
    }

    public Set<String> getServerNodeSet(NodeType nodeType, boolean hostOnly) {
        try {
            return getServerNodes(nodeType).stream().map(server -> {
                if (nodeType == NodeType.WORKER && hostOnly) {
                    return server.split(SINGLE_SLASH)[1];
                }
                return server;
            }).collect(Collectors.toSet());
        } catch (Exception e) {
            throw new RegistryException("Failed to get server node: " + nodeType, e);
        }
    }

    private void initNodes() {
        registry.put(REGISTRY_DOLPHINSCHEDULER_MASTERS, EMPTY, false);
        registry.put(REGISTRY_DOLPHINSCHEDULER_WORKERS, EMPTY, false);
        registry.put(REGISTRY_DOLPHINSCHEDULER_DEAD_SERVERS, EMPTY, false);
    }

    private String rootNodePath(NodeType type) {
        switch (type) {
            case MASTER:
                return Constants.REGISTRY_DOLPHINSCHEDULER_MASTERS;
            case WORKER:
                return Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS;
            case DEAD_SERVER:
                return Constants.REGISTRY_DOLPHINSCHEDULER_DEAD_SERVERS;
            default:
                throw new IllegalStateException("Should not reach here");
        }
    }

    private Collection<String> getServerNodes(NodeType nodeType) {
        final String path = rootNodePath(nodeType);
        final Collection<String> serverList = getChildrenKeys(path);
        if (nodeType != NodeType.WORKER) {
            return serverList;
        }
        return serverList.stream().flatMap(group ->
            getChildrenKeys(path + SINGLE_SLASH + group)
                .stream()
                .map(it -> group + SINGLE_SLASH + it)
        ).collect(Collectors.toList());
    }

    private void removeDeadServerByHost(String host, String serverType) {
        Collection<String> deadServers = getChildrenKeys(REGISTRY_DOLPHINSCHEDULER_DEAD_SERVERS);
        for (String serverPath : deadServers) {
            if (serverPath.startsWith(serverType + UNDERLINE + host)) {
                String server = REGISTRY_DOLPHINSCHEDULER_DEAD_SERVERS + SINGLE_SLASH + serverPath;
                remove(server);
                logger.info("{} server {} deleted from zk dead server path success", serverType, host);
            }
        }
    }

    public Duration getSessionTimeout() {
        return registry.getSessionTimeout();
    }
}
