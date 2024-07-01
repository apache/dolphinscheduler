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
    public static final String SPARK_YARN_QUEUE = "--queue";

    public static final String DEPLOY_MODE = "--deploy-mode";

    public static final String DEPLOY_MODE_LOCAL = "local";

    /**
     * --conf spark.driver.cores NUM
     */
    public static final String DRIVER_CORES = "--conf spark.driver.cores=%d";

    /**
     * --conf spark.driver.memory MEM
     */
    public static final String DRIVER_MEMORY = "--conf spark.driver.memory=%s";

    /**
     * master
     */
    public static final String MASTER = "--master";

    public static final String SPARK_ON_YARN = "yarn";

    public static final String SPARK_ON_K8S_MASTER_PREFIX = "k8s://";

    /**
     * add label for driver pod
     */
    public static final String DRIVER_LABEL_CONF = "--conf spark.kubernetes.driver.label.%s=%s";

    /**
     * spark kubernetes namespace
     */
    public static final String SPARK_KUBERNETES_NAMESPACE = "--conf spark.kubernetes.namespace=%s";

    /**
     * --conf spark.executor.instances NUM
     */
    public static final String NUM_EXECUTORS = "--conf spark.executor.instances=%d";

    /**
     * --conf spark.executor.cores NUM
     */
    public static final String EXECUTOR_CORES = "--conf spark.executor.cores=%d";

    /**
     * --conf spark.executor.memory MEM
     */
    public static final String EXECUTOR_MEMORY = "--conf spark.executor.memory=%s";

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

    public static final String TYPE_SCRIPT = "SCRIPT";

    public static final String TYPE_FILE = "FILE";

}
