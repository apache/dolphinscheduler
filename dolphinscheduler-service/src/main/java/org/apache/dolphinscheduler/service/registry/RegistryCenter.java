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

import static org.apache.dolphinscheduler.common.Constants.ADD_ZK_OP;
import static org.apache.dolphinscheduler.common.Constants.DELETE_ZK_OP;
import static org.apache.dolphinscheduler.common.Constants.MASTER_TYPE;
import static org.apache.dolphinscheduler.common.Constants.REGISTRY_DOLPHINSCHEDULER_DEAD_SERVERS;
import static org.apache.dolphinscheduler.common.Constants.SINGLE_SLASH;
import static org.apache.dolphinscheduler.common.Constants.UNDERLINE;
import static org.apache.dolphinscheduler.common.Constants.WORKER_TYPE;

import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.common.enums.NodeType;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.spi.register.Registry;
import org.apache.dolphinscheduler.spi.register.RegistryException;
import org.apache.dolphinscheduler.spi.register.SubscribeListener;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
public class RegistryCenter implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(RegistryCenter.class);

    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    private static Registry registry;

    private IStoppable stoppable;

    /**
     * nodes namespace
     */
    public String NODES;

    /**
     * master path
     */
    public String MASTER_PATH;

    /**
     * worker path
     */
    public String WORKER_PATH;

    public final String EMPTY = "";

    @Override
    public void afterPropertiesSet() throws Exception {
        NODES = "/nodes";
        MASTER_PATH = NODES + "/master";
        WORKER_PATH = NODES + "/worker";
        init();

    }

    public static final String REGISTRY_PREFIX = "registry";

    /**
     * init node persist
     */
    public void init() {
        if (isStarted.compareAndSet(false, true)) {
            Map<String, String> registryConfig = PropertyUtils.getPropertiesByPrefix(REGISTRY_PREFIX);

            if (registryConfig.isEmpty()) {
                throw new RegistryException("registry config param is null");
            }
            registry.init(registryConfig);
            initNodes();
        }

    }

    /**
     * init nodes
     */
    private void initNodes() {
        registry.persist(MASTER_PATH, EMPTY);
        registry.persist(WORKER_PATH, EMPTY);
    }

    /**
     * close
     */
    public void close() {
        if (isStarted.compareAndSet(true, false) && registry != null) {
            registry.close();
        }
    }

    public void persist(String key, String value) {
        registry.persist(key, value);
    }

    public void remove(String key) {
        registry.remove(key);
    }

    public void update(String key, String value) {
        registry.update(key, value);
    }

    public String get(String key) {
        return registry.get(key);
    }

    public void subscribe(String path, SubscribeListener subscribeListener) {
        registry.subscribe(path, subscribeListener);
    }

    public boolean isExisted(String key) {
        return registry.isExisted(key);
    }

    public boolean getLock(String key) {
        return registry.acquireLock(key);
    }

    public boolean releaseLock(String key) {
        return registry.releaseLock(key);
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

        return !registry.isExisted(zNode) || registry.isExisted(deadServerPath);
    }

    /**
     * @return get dead server node parent path
     */
    public String getDeadZNodeParentPath() {
        return REGISTRY_DOLPHINSCHEDULER_DEAD_SERVERS;
    }

    public void setStoppable(IStoppable stoppable) {
        this.stoppable = stoppable;
    }

    public IStoppable getStoppable() {
        return stoppable;
    }

    /**
     * get master path
     *
     * @return master path
     */
    public String getMasterPath() {
        return MASTER_PATH;
    }

    /**
     * whether master path
     *
     * @param path path
     * @return result
     */
    public boolean isMasterPath(String path) {
        return path != null && path.contains(MASTER_PATH);
    }

    /**
     * get worker path
     *
     * @return worker path
     */
    public String getWorkerPath() {
        return WORKER_PATH;
    }

    /**
     * get worker group path
     *
     * @param workerGroup workerGroup
     * @return worker group path
     */
    public String getWorkerGroupPath(String workerGroup) {
        return WORKER_PATH + "/" + workerGroup;
    }

    /**
     * opType(add): if find dead server , then add to zk deadServerPath
     * opType(delete): delete path from zk
     *
     * @param zNode node path
     * @param nodeType master or worker
     * @param opType delete or add
     * @throws Exception errors
     */
    public void handleDeadServer(String zNode, NodeType nodeType, String opType) throws Exception {
        String host = getHostByEventDataPath(zNode);
        String type = (nodeType == NodeType.MASTER) ? MASTER_TYPE : WORKER_TYPE;

        //check server restart, if restart , dead server path in zk should be delete
        if (opType.equals(DELETE_ZK_OP)) {
            removeDeadServerByHost(host, type);

        } else if (opType.equals(ADD_ZK_OP)) {
            String deadServerPath = getDeadZNodeParentPath() + SINGLE_SLASH + type + UNDERLINE + host;
            if (!registry.isExisted(deadServerPath)) {
                //add dead server info to zk dead server path : /dead-servers/

                registry.persist(deadServerPath, (type + UNDERLINE + host));

                logger.info("{} server dead , and {} added to zk dead server path success",
                        nodeType, zNode);
            }
        }

    }

    /**
     * opType(add): if find dead server , then add to zk deadServerPath
     * opType(delete): delete path from zk
     *
     * @param zNodeSet node path set
     * @param nodeType master or worker
     * @param opType delete or add
     * @throws Exception errors
     */
    public void handleDeadServer(Set<String> zNodeSet, NodeType nodeType, String opType) throws Exception {

        String type = (nodeType == NodeType.MASTER) ? MASTER_TYPE : WORKER_TYPE;
        for (String zNode : zNodeSet) {
            String host = getHostByEventDataPath(zNode);
            //check server restart, if restart , dead server path in zk should be delete
            if (opType.equals(DELETE_ZK_OP)) {
                removeDeadServerByHost(host, type);

            } else if (opType.equals(ADD_ZK_OP)) {
                String deadServerPath = getDeadZNodeParentPath() + SINGLE_SLASH + type + UNDERLINE + host;
                if (!registry.isExisted(deadServerPath)) {
                    //add dead server info to zk dead server path : /dead-servers/
                    registry.persist(deadServerPath, (type + UNDERLINE + host));
                    logger.info("{} server dead , and {} added to zk dead server path success",
                            nodeType, zNode);
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
    public void removeDeadServerByHost(String host, String serverType) throws Exception {
        List<String> deadServers = registry.getChildren(getDeadZNodeParentPath());
        for (String serverPath : deadServers) {
            if (serverPath.startsWith(serverType + UNDERLINE + host)) {
                String server = getDeadZNodeParentPath() + SINGLE_SLASH + serverPath;
                registry.remove(server);
                logger.info("{} server {} deleted from zk dead server path success", serverType, host);
            }
        }
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

    /**
     * whether worker path
     *
     * @param path path
     * @return result
     */
    public boolean isWorkerPath(String path) {
        return path != null && path.contains(WORKER_PATH);
    }

    /**
     * get children nodes
     *
     * @param key key
     * @return children nodes
     */
    public List<String> getChildrenKeys(final String key) {
        return registry.getChildren(key);
    }

}
