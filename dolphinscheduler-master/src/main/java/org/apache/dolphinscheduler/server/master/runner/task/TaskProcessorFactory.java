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

import java.util.Map;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

/**
 * the factory to create task processor
 */
public class TaskProcessorFactory {

    public static final Map<String, ITaskProcessor> PROCESS_MAP = new ConcurrentHashMap<>();

    private static final String DEFAULT_PROCESSOR = COMMON_TASK_TYPE;

    static {
        for (ITaskProcessor iTaskProcessor : ServiceLoader.load(ITaskProcessor.class)) {
            PROCESS_MAP.put(iTaskProcessor.getType(), iTaskProcessor);
        }
    }

    public static ITaskProcessor getTaskProcessor(String type) throws InstantiationException, IllegalAccessException {
        if (StringUtils.isEmpty(type)) {
            type = DEFAULT_PROCESSOR;
        }
        ITaskProcessor iTaskProcessor = PROCESS_MAP.get(type);
        if (Objects.isNull(iTaskProcessor)) {
            iTaskProcessor = PROCESS_MAP.get(DEFAULT_PROCESSOR);
        }

        return iTaskProcessor.getClass().newInstance();
    }

    /**
     * if match master processor, then this task type is processed on the master
     * @param type
     * @return
     */
    public static boolean isMasterTask(String type) {
        return PROCESS_MAP.containsKey(type);
    }
}
