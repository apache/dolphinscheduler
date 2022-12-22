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

package org.apache.dolphinscheduler.plugin.task.spark;

public class SparkConstants {

    private SparkConstants() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * --class CLASS_NAME
     */
    public static final String MAIN_CLASS = "--class";

    /**
     * --name NAME
     */
    public static final String SPARK_NAME = "--name";

    /**
     * --queue QUEUE
     */
    public static final String SPARK_QUEUE = "--queue";

    public static final String DEPLOY_MODE = "--deploy-mode";

    public static final String DEPLOY_MODE_LOCAL = "local";

    public static final String DEPLOY_MODE_CLUSTER = "cluster";

    public static final String DEPLOY_MODE_CLIENT = "client";

    /**
     * --driver-cores NUM
     */
    public static final String DRIVER_CORES = "--driver-cores";

    /**
     * --driver-memory MEM
     */
    public static final String DRIVER_MEMORY = "--driver-memory";

    /**
     * master
     */
    public static final String MASTER = "--master";

    public static final String SPARK_ON_YARN = "yarn";

    /**
     * --num-executors NUM
     */
    public static final String NUM_EXECUTORS = "--num-executors";

    /**
     * --executor-cores NUM
     */
    public static final String EXECUTOR_CORES = "--executor-cores";

    /**
     * --executor-memory MEM
     */
    public static final String EXECUTOR_MEMORY = "--executor-memory";

    /**
     * -f <filename> SQL from files
     */
    public static final String SQL_FROM_FILE = "-f";

    /**
     * spark submit command for sql
     */
    public static final String SPARK_SQL_COMMAND = "${SPARK_HOME}/bin/spark-sql";

    /**
     * spark submit command
     */
    public static final String SPARK_SUBMIT_COMMAND = "${SPARK_HOME}/bin/spark-submit";

}
