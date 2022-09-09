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

package org.apache.dolphinscheduler.server.master.runner.task;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.dolphinscheduler.spi.plugin.PrioritySPIFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.dolphinscheduler.common.Constants.COMMON_TASK_TYPE;

/**
 * the factory to create task processor
 */
@UtilityClass
public final class TaskProcessorFactory {

    private static final Logger logger = LoggerFactory.getLogger(TaskProcessorFactory.class);

    private static final Map<String, Constructor<ITaskProcessor>> PROCESS_MAP = new ConcurrentHashMap<>();

    private static final String DEFAULT_PROCESSOR = COMMON_TASK_TYPE;

    static {
        PrioritySPIFactory<ITaskProcessor> prioritySPIFactory = new PrioritySPIFactory<>(ITaskProcessor.class);
        for (Map.Entry<String, ITaskProcessor> entry : prioritySPIFactory.getSPIMap().entrySet()) {
            try {
                logger.info("Registering task processor: {} - {}", entry.getKey(), entry.getValue().getClass());
                PROCESS_MAP.put(entry.getKey(), (Constructor<ITaskProcessor>) entry.getValue().getClass().getConstructor());
                logger.info("Registered task processor: {} - {}", entry.getKey(), entry.getValue().getClass());
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(String.format("The task processor: %s should has a no args constructor", entry.getKey()));
            }
        }
    }

    public static ITaskProcessor getTaskProcessor(String type) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (StringUtils.isEmpty(type)) {
            type = DEFAULT_PROCESSOR;
        }
        Constructor<ITaskProcessor> iTaskProcessorConstructor = PROCESS_MAP.get(type);
        if (iTaskProcessorConstructor == null) {
            iTaskProcessorConstructor = PROCESS_MAP.get(DEFAULT_PROCESSOR);
        }

        return iTaskProcessorConstructor.newInstance();
    }

    /**
     * if match master processor, then this task type is processed on the master
     *
     * @param type
     * @return
     */
    public static boolean isMasterTask(String type) {
        return PROCESS_MAP.containsKey(type);
    }

}
