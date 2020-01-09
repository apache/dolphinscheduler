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
package org.apache.dolphinscheduler.api.utils.exportprocess;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * task node param factory
 */
public class TaskNodeParamFactory {

    private static Map<String, exportProcessAddTaskParam> taskServices = new ConcurrentHashMap<>();

    public static exportProcessAddTaskParam getByTaskType(String taskType){
        return taskServices.get(taskType);
    }

    static void register(String taskType, exportProcessAddTaskParam addSpecialTaskParam){
        if (null != taskType) {
            taskServices.put(taskType, addSpecialTaskParam);
        }
    }
}
