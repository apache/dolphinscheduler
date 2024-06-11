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

package org.apache.dolphinscheduler.plugin.trigger.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import lombok.extern.slf4j.Slf4j;
import org.apache.dolphinscheduler.common.utils.JSONUtils;
import org.apache.dolphinscheduler.plugin.trigger.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.plugin.trigger.api.parameters.ParametersNode;
import org.apache.dolphinscheduler.spi.plugin.PrioritySPIFactory;

@Slf4j
public class TriggerPluginManager {
    private static final Map<String, TriggerChannelFactory> taskChannelFactoryMap = new HashMap<>();
    private static final Map<String, TriggerChannel> taskChannelMap = new HashMap<>();

    private static final AtomicBoolean loadedFlag = new AtomicBoolean(false);

    /**
     * Load task plugins from classpath.
     */
    public static void loadPlugin() {
        if (!loadedFlag.compareAndSet(false, true)) {
            log.warn("The trigger plugin has already been loaded");
            return;
        }
        PrioritySPIFactory<TriggerChannelFactory> prioritySPIFactory = new PrioritySPIFactory<>(TriggerChannelFactory.class);
        for (Map.Entry<String, TriggerChannelFactory> entry : prioritySPIFactory.getSPIMap().entrySet()) {
            String factoryName = entry.getKey();
            TriggerChannelFactory factory = entry.getValue();

            log.info("Registering trigger plugin: {} - {}", factoryName, factory.getClass().getSimpleName());

            taskChannelFactoryMap.put(factoryName, factory);
            taskChannelMap.put(factoryName, factory.create());

            log.info("Registered trigger plugin: {} - {}", factoryName, factory.getClass().getSimpleName());
        }

    }

    public static Map<String, TriggerChannel> getTaskChannelMap() {
        return Collections.unmodifiableMap(taskChannelMap);
    }

    public static Map<String, TriggerChannelFactory> getTaskChannelFactoryMap() {
        return Collections.unmodifiableMap(taskChannelFactoryMap);
    }

    public static TriggerChannel getTaskChannel(String type) {
        return getTaskChannelMap().get(type);
    }

    public static boolean checkTaskParameters(ParametersNode parametersNode) {
        AbstractParameters abstractParameters = getParameters(parametersNode);
        return abstractParameters != null;
    }

    public static AbstractParameters getParameters(ParametersNode parametersNode) {
        String taskType = parametersNode.getTaskType();
        if (Objects.isNull(taskType)) {
            return null;
        }
        return null;
    }
}
