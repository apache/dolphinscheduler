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

package org.apache.dolphinscheduler.spi.resource;

import org.apache.dolphinscheduler.spi.DolphinSchedulerPlugin;
import org.apache.dolphinscheduler.spi.classloader.ThreadContextClassLoader;
import org.apache.dolphinscheduler.spi.plugin.AbstractDolphinPluginManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceStoragePluginManager extends AbstractDolphinPluginManager {

    private static final Logger logger = LoggerFactory.getLogger(ResourceStorageFactory.class);

    private String pluginName;

    private ResourceStorageFactory resourceStorageFactory;

    private ResourceStorage resourceStorage;

    public ResourceStoragePluginManager(String pluginName) {
        this.pluginName = pluginName;
    }

    @Override
    public void installPlugin(DolphinSchedulerPlugin dolphinSchedulerPlugin) {
        for (ResourceStorageFactory storageFactory : dolphinSchedulerPlugin.getResourceStorageFactorys()) {
            logger.info("Registering Resource Restore Plugin '{}'", storageFactory.getName());
            if (pluginName.equals(storageFactory.getName())) {
                this.resourceStorageFactory = storageFactory;
                try (ThreadContextClassLoader ignored = new ThreadContextClassLoader(storageFactory.getClass().getClassLoader())) {
                    resourceStorage = storageFactory.create();
                }
                return;
            }
        }
        if (null == resourceStorageFactory) {
            throw new ResourceStorageException(String.format("install resource storage plugin error, not found %s plugin", pluginName));
        }
    }

    /**
     * get resource storage
     *
     * @return resource storage
     */
    public ResourceStorage getResourceStorage() {
        if (null == resourceStorage) {
            throw new ResourceStorageException("not install resource storage plugin");
        }
        return resourceStorage;
    }

}
