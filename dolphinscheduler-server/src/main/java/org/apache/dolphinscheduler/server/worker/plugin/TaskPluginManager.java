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

package org.apache.dolphinscheduler.server.worker.plugin;

import static java.lang.String.format;

import org.apache.dolphinscheduler.common.enums.PluginType;
import org.apache.dolphinscheduler.common.enums.TaskType;
import org.apache.dolphinscheduler.dao.PluginDao;
import org.apache.dolphinscheduler.dao.entity.PluginDefine;
import org.apache.dolphinscheduler.spi.params.PluginParamsTransfer;
import org.apache.dolphinscheduler.spi.params.base.PluginParams;
import org.apache.dolphinscheduler.spi.task.TaskChannel;
import org.apache.dolphinscheduler.spi.task.TaskChannelFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class TaskPluginManager {
    private static final Logger logger = LoggerFactory.getLogger(TaskPluginManager.class);

    private final Map<String, TaskChannel> taskChannelMap = new ConcurrentHashMap<>();

    private final PluginDao pluginDao;

    public TaskPluginManager(PluginDao pluginDao) {
        this.pluginDao = pluginDao;
    }

    private void loadTaskChannel(TaskChannelFactory taskChannelFactory) {
        TaskChannel taskChannel = taskChannelFactory.create();
        taskChannelMap.put(taskChannelFactory.getName(), taskChannel);
    }

    public Map<String, TaskChannel> getTaskChannelMap() {
        return Collections.unmodifiableMap(taskChannelMap);
    }

    @EventListener
    public void installPlugin(ApplicationReadyEvent readyEvent) {
        final Set<String> names = new HashSet<>();

        ServiceLoader.load(TaskChannelFactory.class).forEach(factory -> {
            final String name = factory.getName();

            logger.info("Registering task plugin: {}", name);

            if (!names.add(name)) {
                throw new IllegalStateException(format("Duplicate task plugins named '%s'", name));
            }

            loadTaskChannel(factory);

            logger.info("Registered task plugin: {}", name);

            List<PluginParams> params = factory.getParams();
            String paramsJson = PluginParamsTransfer.transferParamsToJson(params);

            PluginDefine pluginDefine = new PluginDefine(name, PluginType.TASK.getDesc(), paramsJson);
            int count = pluginDao.addOrUpdatePluginDefine(pluginDefine);
            if (count <= 0) {
                throw new RuntimeException("Failed to update task plugin: " + name);
            }
        });

        // put WATERDROP task
        taskChannelMap.put(TaskType.WATERDROP.getDesc(), taskChannelMap.get(TaskType.SHELL.getDesc()));

    }
}
