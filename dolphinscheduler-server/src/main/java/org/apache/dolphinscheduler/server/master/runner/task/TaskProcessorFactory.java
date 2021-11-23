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

import org.apache.dolphinscheduler.common.Constants;

import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Strings;

/**
 * the factory to create task processor
 */
public class TaskProcessorFactory {

    public static final Map<String, ITaskProcessFactory> PROCESS_FACTORY_MAP = new ConcurrentHashMap<>();

    private static final String DEFAULT_PROCESSOR = Constants.COMMON_TASK_TYPE;

    static {
        for (ITaskProcessFactory iTaskProcessor : ServiceLoader.load(ITaskProcessFactory.class)) {
            PROCESS_FACTORY_MAP.put(iTaskProcessor.type(), iTaskProcessor);
        }
    }

    public static ITaskProcessor getTaskProcessor(String type) {
        if (Strings.isNullOrEmpty(type)) {
            return PROCESS_FACTORY_MAP.get(DEFAULT_PROCESSOR).create();
        }
        if (!PROCESS_FACTORY_MAP.containsKey(type)) {
            return PROCESS_FACTORY_MAP.get(DEFAULT_PROCESSOR).create();
        }
        return PROCESS_FACTORY_MAP.get(type).create();
    }

}
