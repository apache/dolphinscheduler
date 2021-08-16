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

package org.apache.dolphinscheduler.api.plugin.resource;

import org.apache.dolphinscheduler.common.utils.PropertyUtils;
import org.apache.dolphinscheduler.common.utils.StringUtils;
import org.apache.dolphinscheduler.spi.plugin.DolphinPluginLoader;
import org.apache.dolphinscheduler.spi.plugin.DolphinPluginManagerConfig;
import org.apache.dolphinscheduler.spi.resource.ResourceStorage;
import org.apache.dolphinscheduler.spi.resource.ResourceStoragePluginManager;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableList;

public class ResourceStorageCenter {

    private static final Logger logger = LoggerFactory.getLogger(ResourceStorageCenter.class);

    private ResourceStoragePluginManager resourceStoragePluginManager;

    private static final String RESOURCE_PREFIX = "resource";

    private static final String RESOURCE_PLUGIN_BINDING = "registry.plugin.binding";

    private static final String RESOURCE_PLUGIN_DIR = "registry.plugin.dir";

    private static final String MAVEN_LOCAL_REPOSITORY = "maven.local.repository";

    private static final String RESOURCE_PLUGIN_NAME = "plugin.name";

    /**
     * default resource plugin dir
     */
    private static final String RESOURCE_PLUGIN_PATH = "lib/plugin/resource";

    private static final String RESOURCE_CONFIG_FILE_PATH = "/registry.properties";

    private static ResourceStorage resourceStorage;

    private static class ResourceStorageCenterHelper {
        private static final ResourceStorageCenter INSTANCE = new ResourceStorageCenter();
    }

    public static ResourceStorageCenter getInstance() {
        return ResourceStorageCenterHelper.INSTANCE;
    }

    public boolean resourceStoragePluginStart() {
        return null != resourceStorage;
    }

    public synchronized void init() {
        PropertyUtils.loadPropertyFile(RESOURCE_CONFIG_FILE_PATH);
        Map<String, String> resourceConfig = PropertyUtils.getPropertiesByPrefix(RESOURCE_PREFIX);

        if (null == resourceConfig || resourceConfig.isEmpty()) {
            logger.warn("not config resource storage!");
            return;
        }
        if (null == resourceStoragePluginManager) {
            installResourceStoragePlugin(resourceConfig.get(RESOURCE_PLUGIN_NAME));
            resourceStorage = resourceStoragePluginManager.getResourceStorage();
        }
    }

    private void installResourceStoragePlugin(String pluginName) {
        DolphinPluginManagerConfig resourcePluginManagerConfig = new DolphinPluginManagerConfig();
        resourcePluginManagerConfig.setPlugins(PropertyUtils.getString(RESOURCE_PLUGIN_BINDING));
        if (StringUtils.isNotBlank(PropertyUtils.getString(RESOURCE_PLUGIN_DIR))) {
            resourcePluginManagerConfig.setInstalledPluginsDir(PropertyUtils.getString(RESOURCE_PLUGIN_DIR, RESOURCE_PLUGIN_PATH).trim());
        }

        if (StringUtils.isNotBlank(PropertyUtils.getString(MAVEN_LOCAL_REPOSITORY))) {
            resourcePluginManagerConfig.setMavenLocalRepository(PropertyUtils.getString(MAVEN_LOCAL_REPOSITORY).trim());
        }

        resourceStoragePluginManager = new ResourceStoragePluginManager(pluginName);

        DolphinPluginLoader registryPluginLoader = new DolphinPluginLoader(resourcePluginManagerConfig, ImmutableList.of(resourceStoragePluginManager));
        try {
            registryPluginLoader.loadPlugins();
        } catch (Exception e) {
            throw new RuntimeException("Load Resource Storage Plugin Failed !", e);
        }
    }

    public byte[] catFile(String filePath) {
        return resourceStorage.catFile(filePath);
    }

    public List<String> catFile(String filePath, int skipLineNums, int limit) {
        return resourceStorage.catFile(filePath, skipLineNums, limit);
    }

    public void deleteFile(String filePath, Boolean recursive) {
        resourceStorage.deleteFile(filePath, recursive);
    }

    public boolean exists(String filePath) {
        return resourceStorage.exists(filePath);
    }

    public void uploadLocalFile(String localFileName, String resourceStorageName, boolean overwrite) {
        resourceStorage.uploadLocalFile(localFileName, resourceStorageName, overwrite);
    }

    public void copyFile(String filePath, String targetFilePath, boolean overwrite, boolean deleteSouurce) {
        resourceStorage.copyFile(filePath, targetFilePath, overwrite, deleteSouurce);
    }

    public void downloadFileToLocal(String resourceFilePath, String localFilePath) {
        resourceStorage.downloadFileToLocal(resourceFilePath, localFilePath);
    }

}
