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

import static org.apache.dolphinscheduler.common.Constants.REGISTRY_DOLPHINSCHEDULER_DEAD_SERVERS;
import static org.apache.dolphinscheduler.common.Constants.REGISTRY_DOLPHINSCHEDULER_MASTERS;
import static org.apache.dolphinscheduler.common.Constants.REGISTRY_DOLPHINSCHEDULER_WORKERS;

import org.apache.dolphinscheduler.common.IStoppable;
import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.spi.plugin.DolphinPluginLoader;
import org.apache.dolphinscheduler.spi.plugin.DolphinPluginManagerConfig;
import org.apache.dolphinscheduler.spi.register.Registry;
import org.apache.dolphinscheduler.spi.register.RegistryConnectListener;
import org.apache.dolphinscheduler.spi.register.RegistryException;
import org.apache.dolphinscheduler.spi.register.RegistryPluginManager;
import org.apache.dolphinscheduler.spi.register.SubscribeListener;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

/**
 * All business parties use this class to access the registry
 */
public class RegistryCenter {

    private static final Logger logger = LoggerFactory.getLogger(RegistryCenter.class);

    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    private Registry registry;

    private IStoppable stoppable;

    /**
     * nodes namespace
     */
    protected static String NODES;

    private RegistryPluginManager registryPluginManager;

    protected static final String EMPTY = "";

    private static final String REGISTRY_PREFIX = "registry";

    private static final String REGISTRY_PLUGIN_BINDING = "registry.plugin.binding";

    private static final String REGISTRY_PLUGIN_DIR = "registry.plugin.dir";

    private static final String MAVEN_LOCAL_REPOSITORY = "maven.local.repository";

    private static final String REGISTRY_PLUGIN_NAME = "plugin.name";

    /**
     * default registry plugin dir
     */
    private static final String REGISTRY_PLUGIN_PATH = "lib/plugin/registry";

    private static final String REGISTRY_CONFIG_FILE_PATH = "/registry.properties";

    /**
     * init node persist
     */
    public void init() {
        if (isStarted.compareAndSet(false, true)) {
            PropertyUtils.loadPropertyFile(REGISTRY_CONFIG_FILE_PATH);
            Map<String, String> registryConfig = PropertyUtils.getPropertiesByPrefix(REGISTRY_PREFIX);

            if (null == registryConfig || registryConfig.isEmpty()) {
                throw new RegistryException("registry config param is null");
            }
            if (null == registryPluginManager) {
                installRegistryPlugin(registryConfig.get(REGISTRY_PLUGIN_NAME));
                registry = registryPluginManager.getRegistry();
            }

            registry.init(registryConfig);
            initNodes();

        }
    }

    /**
     * init nodes
     */
    private void initNodes() {
        persist(REGISTRY_DOLPHINSCHEDULER_MASTERS, EMPTY);
        persist(REGISTRY_DOLPHINSCHEDULER_WORKERS, EMPTY);
        persist(REGISTRY_DOLPHINSCHEDULER_DEAD_SERVERS, EMPTY);
    }

    /**
     * install registry plugin
     */
    private void installRegistryPlugin(String registryPluginName) {
        DolphinPluginManagerConfig registryPluginManagerConfig = new DolphinPluginManagerConfig();
        registryPluginManagerConfig.setPlugins(PropertyUtils.getString(REGISTRY_PLUGIN_BINDING));
        if (StringUtils.isNotBlank(PropertyUtils.getString(REGISTRY_PLUGIN_DIR))) {
            registryPluginManagerConfig.setInstalledPluginsDir(PropertyUtils.getString(REGISTRY_PLUGIN_DIR, REGISTRY_PLUGIN_PATH).trim());
        }

        if (StringUtils.isNotBlank(PropertyUtils.getString(MAVEN_LOCAL_REPOSITORY))) {
            registryPluginManagerConfig.setMavenLocalRepository(PropertyUtils.getString(MAVEN_LOCAL_REPOSITORY).trim());
        }

        registryPluginManager = new RegistryPluginManager(registryPluginName);

        DolphinPluginLoader registryPluginLoader = new DolphinPluginLoader(registryPluginManagerConfig, ImmutableList.of(registryPluginManager));
        try {
            registryPluginLoader.loadPlugins();
        } catch (Exception e) {
            throw new RuntimeException("Load registry Plugin Failed !", e);
        }
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

    public void persistEphemeral(String key, String value) {
        registry.persistEphemeral(key, value);
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

    public void addConnectionStateListener(RegistryConnectListener registryConnectListener) {
        registry.addConnectionStateListener(registryConnectListener);
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
     * whether master path
     *
     * @param path path
     * @return result
     */
    public boolean isMasterPath(String path) {
        return path != null && path.contains(REGISTRY_DOLPHINSCHEDULER_MASTERS);
    }

    /**
     * get worker group path
     *
     * @param workerGroup workerGroup
     * @return worker group path
     */
    public String getWorkerGroupPath(String workerGroup) {
        return REGISTRY_DOLPHINSCHEDULER_WORKERS + "/" + workerGroup;
    }

    /**
     * whether worker path
     *
     * @param path path
     * @return result
     */
    public boolean isWorkerPath(String path) {
        return path != null && path.contains(REGISTRY_DOLPHINSCHEDULER_WORKERS);
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
