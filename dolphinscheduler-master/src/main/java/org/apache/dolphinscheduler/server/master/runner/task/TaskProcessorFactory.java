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

import static org.apache.dolphinscheduler.common.Constants.COMMON_TASK_TYPE;

import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * the factory to create task processor
 */
public final class TaskProcessorFactory {

    private static final Logger logger = LoggerFactory.getLogger(TaskProcessorFactory.class);

    public static final Map<String, Constructor<ITaskProcessor>> PROCESS_MAP = new ConcurrentHashMap<>();

    private static final String DEFAULT_PROCESSOR = COMMON_TASK_TYPE;

    static {
        for (ITaskProcessor iTaskProcessor : ServiceLoader.load(ITaskProcessor.class)) {
            try {
                PROCESS_MAP.put(iTaskProcessor.getType(), (Constructor<ITaskProcessor>) iTaskProcessor.getClass().getConstructor());
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("The task processor should has a no args constructor");
            }
        }
    }

    public static ITaskProcessor getTaskProcessor(String type) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (StringUtils.isEmpty(type)) {
            type = DEFAULT_PROCESSOR;
        }
        Constructor<ITaskProcessor> iTaskProcessorConstructor = PROCESS_MAP.get(type);
        if (iTaskProcessorConstructor == null) {
            logger.warn("ITaskProcessor could not found for taskType: {}", type);
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

    private TaskProcessorFactory() {
        throw new UnsupportedOperationException("TaskProcessorFactory cannot be instantiated");
    }
}
