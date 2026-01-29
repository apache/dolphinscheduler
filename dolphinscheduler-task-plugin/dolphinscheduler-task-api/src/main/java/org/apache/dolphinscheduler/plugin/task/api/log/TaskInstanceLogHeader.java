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

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TaskInstanceLogHeader {

    public static final String DOLPHIN_EMOJI = "\uD83D\uDC2C";

    private static final String INITIALIZE_TASK_CONTEXT_HEADER = DOLPHIN_EMOJI + " Initialize Task Context";
    private static final String LOAD_TASK_INSTANCE_PLUGIN_HEADER = DOLPHIN_EMOJI + " Load Task Instance Plugin";
    private static final String EXECUTE_TASK_HEADER = DOLPHIN_EMOJI + " Execute Task Instance";
    private static final String FINALIZE_TASK_HEADER = DOLPHIN_EMOJI + " Finalize Task Instance";

    public static void printInitializeTaskContextHeader() {
        log.info(INITIALIZE_TASK_CONTEXT_HEADER);
    }

    public static void printLoadTaskInstancePluginHeader() {
        log.info(LOAD_TASK_INSTANCE_PLUGIN_HEADER);
    }

    public static void printExecuteTaskHeader() {
        log.info(EXECUTE_TASK_HEADER);
    }

    public static void printFinalizeTaskHeader() {
        log.info(FINALIZE_TASK_HEADER);
    }
}
