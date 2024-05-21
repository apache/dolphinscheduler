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

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertChannelFactory;
import org.apache.dolphinscheduler.common.enums.PluginType;
import org.apache.dolphinscheduler.dao.PluginDao;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.spi.params.PluginParamsTransfer;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.plugin.PrioritySPIFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public final class AlertPluginManager {

    private final PluginDao pluginDao;

    public AlertPluginManager(PluginDao pluginDao) {
        this.pluginDao = pluginDao;
    }

    private final Map<Integer, AlertChannel> alertPluginMap = new HashMap<>();

    public void start() {
        log.info("AlertPluginManager start ...");
        checkAlertPluginExist();
        installAlertPlugin();
        log.info("AlertPluginManager started ...");
    }

    public Optional<AlertChannel> getAlertChannel(int id) {
        return Optional.ofNullable(alertPluginMap.get(id));
    }

    public int size() {
        return alertPluginMap.size();
    }

    private void checkAlertPluginExist() {
        if (!pluginDao.checkPluginDefineTableExist()) {
            log.error("Plugin Define Table t_ds_plugin_define Not Exist . Please Create it First !");
            System.exit(1);
        }
    }

    private void installAlertPlugin() {
        PrioritySPIFactory<AlertChannelFactory> prioritySPIFactory =
                new PrioritySPIFactory<>(AlertChannelFactory.class);
        for (Map.Entry<String, AlertChannelFactory> entry : prioritySPIFactory.getSPIMap().entrySet()) {
            String name = entry.getKey();
            AlertChannelFactory factory = entry.getValue();

            log.info("Registering alert plugin: {} - {}", name, factory.getClass().getSimpleName());

            final AlertChannel alertChannel = factory.create();

            log.info("Registered alert plugin: {} - {}", name, factory.getClass().getSimpleName());

            final List<PluginParams> params = new ArrayList<>(factory.params());

            final String paramsJson = PluginParamsTransfer.transferParamsToJson(params);

            final PluginDefine pluginDefine = new PluginDefine(name, PluginType.ALERT.getDesc(), paramsJson);
            final int id = pluginDao.addOrUpdatePluginDefine(pluginDefine);

            alertPluginMap.put(id, alertChannel);
        }
    }

}
