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

import static java.util.Objects.requireNonNull;

import org.apache.dolphinscheduler.spi.plugin.AbstractDolphinPluginManager;
import org.apache.dolphinscheduler.spi.DolphinSchedulerPlugin;
import org.apache.dolphinscheduler.spi.classloader.ThreadContextClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The plug-in address of the registry needs to be configured.
 * Multi-registries are not supported.
 * When the plug-in directory contains multiple plug-ins, only the configured plug-in will be used.
 */
public class RegisterPluginManager extends AbstractDolphinPluginManager {

    private static final Logger logger = LoggerFactory.getLogger(RegisterPluginManager.class);

    private RegisterFactory registerFactory;

    public static Register register;

    private String registerPluginName;

    public RegisterPluginManager(String registerPluginName) {
        this.registerPluginName = registerPluginName;
    }

    @Override
    public void installPlugin(DolphinSchedulerPlugin dolphinSchedulerPlugin) {
        for (RegisterFactory registerFactory : dolphinSchedulerPlugin.getRegisterFactorys()) {
            logger.info("Registering Register Plugin '{}'", registerFactory.getName());
            if (registerPluginName.equals(registerFactory.getName())) {
                this.registerFactory = registerFactory;
                loadRegister();
                return;
            }
        }
        if (null == register) {
            throw new RegisterException(String.format("not found %s register plugin ", registerPluginName));
        }
    }

    private void loadRegister() {
        try (ThreadContextClassLoader ignored = new ThreadContextClassLoader(registerFactory.getClass().getClassLoader())) {
            register = registerFactory.create();
        }
    }

    public static Register getRegister() {
        return register;
    }

}
