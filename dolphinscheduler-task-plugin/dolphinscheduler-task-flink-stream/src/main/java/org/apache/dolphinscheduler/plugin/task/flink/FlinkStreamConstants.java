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

public class FlinkStreamConstants {

    public static final int CHECK_YARN_JOB_STATUS_EXECUTION_STATUS_INTERVAL = 5000;

    public static final String FLINK_HOME = "FLINK_HOME";

    public static final String HADOOP_CONF_DIR = "HADOOP_CONF_DIR";

    public static final String FLINK_CONF_DIR = "/conf";

    public static final String FLINK_LIB_DIR = "/lib";

    private FlinkStreamConstants() {
        throw new IllegalStateException("Utility class");
    }
}
