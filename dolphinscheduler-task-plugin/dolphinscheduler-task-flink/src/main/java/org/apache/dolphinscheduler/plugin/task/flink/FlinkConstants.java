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
     * flink command
     * usage: flink run [OPTIONS] <jar-file> <arguments>
     */
    public static final String FLINK_COMMAND = "${FLINK_HOME}/bin/flink";
    public static final String FLINK_RUN = "run";

    /**
     * flink sql command
     * usage: sql-client.sh -i <initialization file>, -f <script file>
     */
    public static final String FLINK_SQL_COMMAND = "${FLINK_HOME}/bin/sql-client.sh";

    /**
     * flink run options
     */
    public static final String FLINK_RUN_APPLICATION = "run-application";
    public static final String FLINK_YARN_CLUSTER = "yarn-cluster";
    public static final String FLINK_YARN_APPLICATION = "yarn-application";
    public static final String FLINK_YARN_PER_JOB = "yarn-per-job";
    public static final String FLINK_LOCAL = "local";
    public static final String FLINK_RUN_MODE = "-m";
    public static final String FLINK_EXECUTION_TARGET = "-t";
    public static final String FLINK_YARN_SLOT = "-ys";
    public static final String FLINK_APP_NAME = "-ynm";
    public static final String FLINK_YARN_QUEUE_FOR_MODE = "-yqu";
    public static final String FLINK_YARN_QUEUE_FOR_TARGETS = "-Dyarn.application.queue";
    public static final String FLINK_TASK_MANAGE = "-yn";
    public static final String FLINK_JOB_MANAGE_MEM = "-yjm";
    public static final String FLINK_TASK_MANAGE_MEM = "-ytm";
    public static final String FLINK_MAIN_CLASS = "-c";
    public static final String FLINK_PARALLELISM = "-p";
    public static final String FLINK_SHUTDOWN_ON_ATTACHED_EXIT = "-sae";
    public static final String FLINK_PYTHON = "-py";
    public static final String FLINK_SAVEPOINT = "savepoint";
    public static final String FLINK_METRICS = "metrics";
    public static final String FLINK_OVERVIEW = "overview";
    public static final String FLINK_JOBS = "jobs";
    public static final String FLINK_CANCEL = "cancel";
    // For Flink SQL
    public static final String FLINK_FORMAT_EXECUTION_TARGET = "set execution.target=%s";
    public static final String FLINK_FORMAT_YARN_APPLICATION_NAME = "set yarn.application.name=%s";
    public static final String FLINK_FORMAT_YARN_APPLICATION_QUEUE = "set yarn.application.queue=%s";
    public static final String FLINK_FORMAT_JOBMANAGER_MEMORY_PROCESS_SIZE = "set jobmanager.memory.process.size=%s";
    public static final String FLINK_FORMAT_TASKMANAGER_MEMORY_PROCESS_SIZE = "set taskmanager.memory.process.size=%s";
    public static final String FLINK_FORMAT_TASKMANAGER_NUMBEROFTASKSLOTS = "set taskmanager.numberOfTaskSlots=%d";
    public static final String FLINK_FORMAT_PARALLELISM_DEFAULT = "set parallelism.default=%d";
    public static final String FLINK_SQL_SCRIPT_FILE = "-f";
    public static final String FLINK_SQL_INIT_FILE = "-i";
    public static final String FLINK_SQL_NEWLINE = ";\n";
}
