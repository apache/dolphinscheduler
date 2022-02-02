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

import static java.lang.String.format;

import org.apache.dolphinscheduler.alert.api.AlertChannel;
import org.apache.dolphinscheduler.alert.api.AlertChannelFactory;
import org.apache.dolphinscheduler.common.enums.PluginType;
import org.apache.dolphinscheduler.dao.PluginDao;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.spi.params.PluginParamsTransfer;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public final class AlertPluginManager {
    private static final Logger logger = LoggerFactory.getLogger(AlertPluginManager.class);

    private final PluginDao pluginDao;

    private final Map<Integer, AlertChannel> channelKeyedById = new HashMap<>();

    public AlertPluginManager(PluginDao pluginDao) {
        this.pluginDao = pluginDao;
    }

    @EventListener
    public void installPlugin(ApplicationReadyEvent readyEvent) {
        final Set<String> names = new HashSet<>();

        ServiceLoader.load(AlertChannelFactory.class).forEach(factory -> {
            final String name = factory.name();

            logger.info("Registering alert plugin: {}", name);

            if (!names.add(name)) {
                throw new IllegalStateException(format("Duplicate alert plugins named '%s'", name));
            }

            final AlertChannel alertChannel = factory.create();

            logger.info("Registered alert plugin: {}", name);

            final List<PluginParams> params = factory.params();
            final String paramsJson = PluginParamsTransfer.transferParamsToJson(params);

            final PluginDefine pluginDefine = new PluginDefine(name, PluginType.ALERT.getDesc(), paramsJson);
            final int id = pluginDao.addOrUpdatePluginDefine(pluginDefine);

            channelKeyedById.put(id, alertChannel);
        });
    }

    public Optional<AlertChannel> getAlertChannel(int id) {
        return Optional.ofNullable(channelKeyedById.get(id));
    }

    public int size() {
        return channelKeyedById.size();
    }
}
