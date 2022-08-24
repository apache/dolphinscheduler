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

package org.apache.dolphinscheduler.alert;

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertChannelFactory;
import org.apache.dolphinscheduler.alert.api.AlertConstants;
import org.apache.dolphinscheduler.common.enums.PluginType;
import org.apache.dolphinscheduler.common.enums.WarningType;
import org.apache.dolphinscheduler.dao.PluginDao;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.spi.params.PluginParamsTransfer;
import org.apache.dolphinscheduler.spi.params.base.ParamsOptions;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.params.base.Validate;
import org.apache.dolphinscheduler.spi.params.radio.RadioParam;
import org.apache.dolphinscheduler.spi.plugin.PrioritySPIFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.String.format;

@Component
public final class AlertPluginManager {
    private static final Logger logger = LoggerFactory.getLogger(AlertPluginManager.class);

    private final PluginDao pluginDao;

    public AlertPluginManager(PluginDao pluginDao) {
        this.pluginDao = pluginDao;
    }

    private final Map<Integer, AlertChannel> channelKeyedById = new HashMap<>();

    private final PluginParams warningTypeParams = getWarningTypeParams();

    public PluginParams getWarningTypeParams() {
        return
                RadioParam.newBuilder(AlertConstants.NAME_WARNING_TYPE, AlertConstants.WARNING_TYPE)
                        .addParamsOptions(new ParamsOptions(WarningType.SUCCESS.getDescp(), WarningType.SUCCESS.getDescp(), false))
                        .addParamsOptions(new ParamsOptions(WarningType.FAILURE.getDescp(), WarningType.FAILURE.getDescp(), false))
                        .addParamsOptions(new ParamsOptions(WarningType.ALL.getDescp(), WarningType.ALL.getDescp(), false))
                        .setValue(WarningType.ALL.getDescp())
                        .addValidate(Validate.newBuilder().setRequired(true).build())
                        .build();
    }

    @EventListener
    public void installPlugin(ApplicationReadyEvent readyEvent) {

        PrioritySPIFactory<AlertChannelFactory> prioritySPIFactory = new PrioritySPIFactory<>(AlertChannelFactory.class);
        for (Map.Entry<String, AlertChannelFactory> entry : prioritySPIFactory.getSPIMap().entrySet()) {
            String name = entry.getKey();
            AlertChannelFactory factory = entry.getValue();

            logger.info("Registering alert plugin: {} - {}", name, factory.getClass());

            final AlertChannel alertChannel = factory.create();

            logger.info("Registered alert plugin: {} - {}", name, factory.getClass());

            final List<PluginParams> params = new ArrayList<>(factory.params());
            params.add(0, warningTypeParams);

            final String paramsJson = PluginParamsTransfer.transferParamsToJson(params);

            final PluginDefine pluginDefine = new PluginDefine(name, PluginType.ALERT.getDesc(), paramsJson);
            final int id = pluginDao.addOrUpdatePluginDefine(pluginDefine);

            channelKeyedById.put(id, alertChannel);
        }
    }

    public Optional<AlertChannel> getAlertChannel(int id) {
        return Optional.ofNullable(channelKeyedById.get(id));
    }

    public int size() {
        return channelKeyedById.size();
    }
}
