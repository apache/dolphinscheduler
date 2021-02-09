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

package org.apache.dolphinscheduler.alert.plugin;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import static com.google.common.base.Preconditions.checkState;

import org.apache.dolphinscheduler.common.enums.PluginType;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.spi.DolphinSchedulerPlugin;
import org.apache.dolphinscheduler.spi.alert.AlertChannel;
import org.apache.dolphinscheduler.spi.alert.AlertChannelFactory;
import org.apache.dolphinscheduler.spi.classloader.ThreadContextClassLoader;
import org.apache.dolphinscheduler.spi.params.PluginParamsTransfer;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * load the configured alert plugin and manager them
 */
public class AlertPluginManager extends AbstractDolphinPluginManager {
    private static final Logger logger = LoggerFactory.getLogger(AlertPluginManager.class);

    private final Map<String, AlertChannelFactory> alertChannelFactoryMap = new ConcurrentHashMap<>();
    private final Map<String, AlertChannel> alertChannelMap = new ConcurrentHashMap<>();

    /**
     * k->pluginDefineId v->pluginDefineName
     */
    private final Map<Integer, String> pluginDefineMap = new HashMap<>();

    public void addAlertChannelFactory(AlertChannelFactory alertChannelFactory) {
        requireNonNull(alertChannelFactory, "alertChannelFactory is null");

        if (alertChannelFactoryMap.putIfAbsent(alertChannelFactory.getName(), alertChannelFactory) != null) {
            throw new IllegalArgumentException(format("Alert Plugin '{}' is already registered", alertChannelFactory.getName()));
        }

        try {
            loadAlertChannel(alertChannelFactory.getName());
        } catch (Exception e) {
            throw new IllegalArgumentException(format("Alert Plugin '{}' is can not load .", alertChannelFactory.getName()));
        }
    }

    protected void loadAlertChannel(String name) {
        requireNonNull(name, "name is null");

        AlertChannelFactory alertChannelFactory = alertChannelFactoryMap.get(name);
        checkState(alertChannelFactory != null, "Alert Plugin {} is not registered", name);

        try (ThreadContextClassLoader ignored = new ThreadContextClassLoader(alertChannelFactory.getClass().getClassLoader())) {
            AlertChannel alertChannel = alertChannelFactory.create();
            this.alertChannelMap.put(name, alertChannel);
        }

        logger.info("-- Loaded Alert Plugin {} --", name);
    }

    public Map<String, AlertChannelFactory> getAlertChannelFactoryMap() {
        return alertChannelFactoryMap;
    }

    public Map<String, AlertChannel> getAlertChannelMap() {
        return alertChannelMap;
    }

    public String getPluginNameById(int id) {
        return pluginDefineMap.get(id);
    }

    @Override
    public void installPlugin(DolphinSchedulerPlugin dolphinSchedulerPlugin) {
        for (AlertChannelFactory alertChannelFactory : dolphinSchedulerPlugin.getAlertChannelFactorys()) {
            logger.info("Registering Alert Plugin '{}'", alertChannelFactory.getName());
            this.addAlertChannelFactory(alertChannelFactory);
            List<PluginParams> params = alertChannelFactory.getParams();
            String nameEn = alertChannelFactory.getName();
            String paramsJson = PluginParamsTransfer.transferParamsToJson(params);

            PluginDefine pluginDefine = new PluginDefine(nameEn, PluginType.ALERT.getDesc(), paramsJson);
            int id = pluginDao.addOrUpdatePluginDefine(pluginDefine);
            pluginDefineMap.put(id, pluginDefine.getPluginName());
        }
    }
}
