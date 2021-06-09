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

package org.apache.dolphinscheduler.spi.register;

import org.apache.dolphinscheduler.spi.DolphinSchedulerPlugin;
import org.apache.dolphinscheduler.spi.classloader.ThreadContextClassLoader;
import org.apache.dolphinscheduler.spi.plugin.AbstractDolphinPluginManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The plug-in address of the registry needs to be configured.
 * Multi-registries are not supported.
 * When the plug-in directory contains multiple plug-ins, only the configured plug-in will be used.
 * todo It’s not good to put it here, consider creating a separate API module for each plugin
 */
public class RegistryPluginManager extends AbstractDolphinPluginManager {

    private static final Logger logger = LoggerFactory.getLogger(RegistryPluginManager.class);

    private RegistryFactory registryFactory;

    public static Registry registry;

    private String registerPluginName;

    public RegistryPluginManager(String registerPluginName) {
        this.registerPluginName = registerPluginName;
    }

    @Override
    public void installPlugin(DolphinSchedulerPlugin dolphinSchedulerPlugin) {
        for (RegistryFactory registryFactory : dolphinSchedulerPlugin.getRegisterFactorys()) {
            logger.info("Registering Registry Plugin '{}'", registryFactory.getName());
            if (registerPluginName.equals(registryFactory.getName())) {
                this.registryFactory = registryFactory;
                loadRegistry();
                return;
            }
        }
        if (null == registry) {
            throw new RegistryException(String.format("not found %s registry plugin ", registerPluginName));
        }
    }

    /**
     * load registry
     */
    private void loadRegistry() {
        try (ThreadContextClassLoader ignored = new ThreadContextClassLoader(registryFactory.getClass().getClassLoader())) {
            registry = registryFactory.create();
        }
    }

    /**
     * get registry
     * @return registry
     */
    public  Registry getRegistry() {
        if (null == registry) {
            throw new RegistryException("not install registry");
        }
        return registry;
    }

}
