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

    private static final String INITIALIZE_TASK_CONTEXT_HEADER = new StringBuilder()
            .append("\n")
            .append("************************************************************************************************")
            .append("\n")
            .append("*********************************  Initialize task context  ************************************")
            .append("\n")
            .append("************************************************************************************************")
            .toString();
    private static final String LOAD_TASK_INSTANCE_PLUGIN_HEADER = new StringBuilder()
            .append("\n")
            .append("***********************************************************************************************")
            .append("\n")
            .append("*********************************  Load task instance plugin  *********************************")
            .append("\n")
            .append("***********************************************************************************************")
            .toString();

    public static void printInitializeTaskContextHeader() {
        log.info(INITIALIZE_TASK_CONTEXT_HEADER);
    }

    private static final String EXECUTE_TASK_HEADER = new StringBuilder()
            .append("\n")
            .append("************************************************************************************************")
            .append("\n")
            .append("*********************************  Execute task instance  *************************************")
            .append("\n")
            .append("***********************************************************************************************")
            .toString();

    private static final String FINALIZE_TASK_HEADER = new StringBuilder()
            .append("\n")
            .append("************************************************************************************************")
            .append("\n")
            .append("*********************************  Finalize task instance  ************************************")
            .append("\n")
            .append("***********************************************************************************************")
            .toString();

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
