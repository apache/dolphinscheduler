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

package org.apache.dolphinscheduler.plugin.task.api.log;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import com.google.common.collect.Lists;

@Slf4j
public class TaskInstanceLogHeader {

    private static final List<String> INITIALIZE_TASK_CONTEXT_HEADER = Lists.newArrayList(
            "***********************************************************************************************",
            "*********************************  Initialize task context  ***********************************",
            "***********************************************************************************************");
    private static final List<String> LOAD_TASK_INSTANCE_PLUGIN_HEADER = Lists.newArrayList(
            "***********************************************************************************************",
            "*********************************  Load task instance plugin  *********************************",
            "***********************************************************************************************");

    public static void printInitializeTaskContextHeader() {
        INITIALIZE_TASK_CONTEXT_HEADER.forEach(log::info);
    }

    private static final List<String> EXECUTE_TASK_HEADER = Lists.newArrayList(
            "***********************************************************************************************",
            "*********************************  Execute task instance  *************************************",
            "***********************************************************************************************");

    private static final List<String> FINALIZE_TASK_HEADER = Lists.newArrayList(
            "***********************************************************************************************",
            "*********************************  Finalize task instance  ************************************",
            "***********************************************************************************************");

    public static void printLoadTaskInstancePluginHeader() {
        LOAD_TASK_INSTANCE_PLUGIN_HEADER.forEach(log::info);
    }

    public static void printExecuteTaskHeader() {
        EXECUTE_TASK_HEADER.forEach(log::info);
    }

    public static void printFinalizeTaskHeader() {
        FINALIZE_TASK_HEADER.forEach(log::info);
    }
}
