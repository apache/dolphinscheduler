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

package org.apache.dolphinscheduler.server.master.engine.executor.plugin;

import org.apache.dolphinscheduler.plugin.task.api.parameters.AbstractParameters;
import org.apache.dolphinscheduler.server.master.exception.LogicTaskFactoryNotFoundException;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LogicTaskPluginFactoryBuilder {

    private final Map<String, ILogicTaskPluginFactory<? extends ILogicTask<? extends AbstractParameters>>> logicTaskPluginFactoryMap =
            new ConcurrentHashMap<>();

    public LogicTaskPluginFactoryBuilder(List<ILogicTaskPluginFactory<? extends ILogicTask<? extends AbstractParameters>>> logicTaskPluginFactories) {
        logicTaskPluginFactories.forEach(
                logicTaskPluginFactory -> logicTaskPluginFactoryMap.put(logicTaskPluginFactory.getTaskType(),
                        logicTaskPluginFactory));
    }

    public ILogicTaskPluginFactory<? extends ILogicTask<? extends AbstractParameters>> createILogicTaskPluginFactory(String taskType) throws LogicTaskFactoryNotFoundException {
        ILogicTaskPluginFactory<? extends ILogicTask<? extends AbstractParameters>> logicTaskPluginFactory =
                logicTaskPluginFactoryMap.get(taskType);
        if (logicTaskPluginFactory == null) {
            throw new LogicTaskFactoryNotFoundException("Cannot find the logic task factory: " + taskType);
        }
        return logicTaskPluginFactory;
    }

}
