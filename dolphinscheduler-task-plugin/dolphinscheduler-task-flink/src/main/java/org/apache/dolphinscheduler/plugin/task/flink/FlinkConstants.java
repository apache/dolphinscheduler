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

package org.apache.dolphinscheduler.plugin.task.flink;

public class FlinkConstants {

    private FlinkConstants() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * flink
     */
    public static final String FLINK_YARN_CLUSTER = "yarn-cluster";
    public static final String FLINK_RUN_MODE = "-m";
    public static final String FLINK_YARN_SLOT = "-ys";
    public static final String FLINK_APP_NAME = "-ynm";
    public static final String FLINK_QUEUE = "-yqu";
    public static final String FLINK_TASK_MANAGE = "-yn";

    public static final String FLINK_JOB_MANAGE_MEM = "-yjm";
    public static final String FLINK_TASK_MANAGE_MEM = "-ytm";
    public static final String FLINK_MAIN_CLASS = "-c";
    public static final String FLINK_PARALLELISM = "-p";
    public static final String FLINK_SHUTDOWN_ON_ATTACHED_EXIT = "-sae";

    /**
     * flink sql
     */
    public static final String FLINK_SQL_EXECUTION_RESULT_MODE = "set execution.result-mode=";
    public static final String FLINK_SQL_EXECUTION_TARGET = "set execution.target=yarn-per-job";
    public static final String FLINK_SQL_YARN_APPLICATION_NAME = "set yarn.application.name=";
    public static final String FLINK_SQL_YARN_APPLICATION_QUEUE = "set yarn.application.queue=";
    public static final String FLINK_SQL_JOBMANAGER_MEMORY_PROCESS_SIZE = "set jobmanager.memory.process.size=";
    public static final String FLINK_SQL_TASKMANAGER_MEMORY_PROCESS_SIZE = "set taskmanager.memory.process.size=";
    public static final String FLINK_SQL_TASKMANAGER_NUMBEROFTASKSLOTS = "set taskmanager.numberOfTaskSlots=";
    public static final String FLINK_SQL_PARALLELISM_DEFAULT = "set parallelism.default=";
    public static final String FLINK_SQL_EXECUTION_RUNTIME_MODE = "set execution.runtime-mode=";
    public static final String FLINK_SQL_FILE = "-f";
    public static final String FLINK_SQL_NEWLINE = ";";

}
